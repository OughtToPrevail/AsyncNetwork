/*
Copyright 2019 https://github.com/OughtToPrevail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package oughttoprevail.asyncnetwork.util.selector.flags;

import sun.misc.Unsafe;
import sun.nio.ch.IOUtil;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.server.AbstractServer;
import oughttoprevail.asyncnetwork.server.IndexedList;
import oughttoprevail.asyncnetwork.server.ServerClientSocket;
import oughttoprevail.asyncnetwork.util.UnsafeGetter;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.selector.WindowsSelector;
import oughttoprevail.asyncnetwork.util.writer.server.PendingWrite;
import oughttoprevail.asyncnetwork.util.DisconnectionType;

public class WindowsSelectorFlags
{
	private static final Unsafe UNSAFE = UnsafeGetter.getUnsafe();
	
	private static final int INET4_BYTES = Util.LONG_BYTES;
	private static final int INET6_BYTES = Util.BYTE_BYTES * 16;
	
	private final AbstractServer server;
	private final IndexedList<ServerClientSocket> clients;
	private final WindowsSelector selector;
	
	private final int serverSocket;
	
	private final Constructor newSocketChannel;
	private final long localAddress;
	private final long remoteAddress;
	private final long stateAddress;
	
	private final PooledByteBuffer pooledSocketAddresses;
	private final ByteBuffer socketAddresses;
	private final long socketAddressesAddress;
	
	private final AtomicReference<SocketData> socketData = new AtomicReference<>();
	
	public WindowsSelectorFlags(AbstractServer server, IndexedList<ServerClientSocket> clients, WindowsSelector selector, int serverSocket)
			throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException
	{
		this.server = server;
		this.clients = clients;
		this.selector = selector;
		this.serverSocket = serverSocket;
		Class socketChannelImpl = Class.forName("sun.nio.ch.SocketChannelImpl");
		newSocketChannel = socketChannelImpl.getDeclaredConstructor(SelectorProvider.class, FileDescriptor.class, boolean.class);
		newSocketChannel.setAccessible(true);
		localAddress = findOffset(socketChannelImpl, "localAddress");
		remoteAddress = findOffset(socketChannelImpl, "remoteAddress");
		stateAddress = findOffset(socketChannelImpl, "state");
		pooledSocketAddresses = new PooledByteBuffer(Util.BYTE_BYTES * 2 + Util.SHORT_BYTES * 2 + Math.max(INET4_BYTES, INET6_BYTES) * 2);
		socketAddresses = pooledSocketAddresses.getByteBuffer();
		socketAddresses.order(ByteOrder.nativeOrder());
		socketAddressesAddress = Util.address(socketAddresses);
	}
	
	private long findOffset(Class socketChannelImpl, String fieldName) throws NoSuchFieldException
	{
		Field field = socketChannelImpl.getDeclaredField(fieldName);
		return UNSAFE.objectFieldOffset(field);
	}
	
	public void AcceptEx()
	{
		int index = clients.index();
		synchronized(socketData)
		{
			int socket = selector.AcceptEx(serverSocket, index, server.getThreadsCount());
			SocketChannel socketChannel = allocateSocketChannel(socket);
			socketData.set(new SocketData(socketChannel, socket, index));
		}
	}
	
	private static final int READ_OR_WRITE = 1;
	private static final int ACCEPT = 2;
	
	public void select(ByteBuffer result, Object pendingWrite)
	{
		byte opcode = result.get();
		if(opcode == READ_OR_WRITE)
		{
			int index = result.getInt();
			ServerClientSocket client = clients.get(index);
			if(client != null)
			{
				byte isRead = result.get();
				int totalBytes = result.getInt();
				if(totalBytes == 0 || totalBytes == -1)
				{
					client.manager().close(DisconnectionType.REMOTE_CLOSE);
					return;
				}
				if(isRead == 1)
				{
					PooledByteBuffer pooledReadBuffer = client.manager().getReadByteBuffer();
					ByteBuffer readBuffer = pooledReadBuffer.getByteBuffer();
					readBuffer.position(readBuffer.position() + totalBytes);
					client.manager().callRequests();
					if(!client.isClosed())
					{
						int position = readBuffer.position();
						if(position == client.getBufferSize())
						{
							client.manager().bufferOverflow(readBuffer);
							position = readBuffer.position();
						}
						try
						{
							selector.WSARecv(client.manager().getFD(), pooledReadBuffer.address() + position, readBuffer.capacity() - position);
						} catch(IOException e)
						{
							Validator.handleRemoteHostCloseException(client, e);
						}
					}
				} else
				{
					if(pendingWrite instanceof PendingWrite)
					{
						((PendingWrite) pendingWrite).finish(client);
					}
				}
			}
		} else if(opcode == ACCEPT)
		{
			SocketData data;
			synchronized(socketData)
			{
				data = socketData.get();
			}
			int socket = data.socket;
			InetSocketAddress localAddress;
			InetSocketAddress remoteAddress = null;
			synchronized(socketAddresses)
			{
				selector.getAddress(socketAddressesAddress);
				
				try
				{
					localAddress = getSocketAddress();
					if(localAddress != null)
					{
						remoteAddress = getSocketAddress();
					}
				} finally
				{
					socketAddresses.clear();
				}
			}
			AcceptEx();
			//if the localAddress is null then remoteAddress will be null as well so by checking remoteAddress we know they are both not null
			int clientsIndex = data.index;
			Throwable pendingException = null;
			if(remoteAddress != null)
			{
				SocketChannel socketChannel = data.socketChannel;
				fixChannel(socketChannel, localAddress, remoteAddress);
				try
				{
					ServerClientSocket client = server.initializeClient(socketChannel, clientsIndex);
					try
					{
						if(client != null)
						{
							clients.add(clientsIndex, client);
							selector.registerClient(serverSocket, socket);
							client.manager().setFD(socket);
							PooledByteBuffer readBuffer = client.manager().getReadByteBuffer();
							try
							{
								selector.WSARecv(socket, readBuffer.address(), readBuffer.getByteBuffer().capacity());
							} catch(IOException e)
							{
								client.close();
								server.manager().exception(e);
							}
							server.connected(client);
							return;
						}
					} catch(IOException e)
					{
						Validator.exceptionClose(client, e);
						server.manager().exception(e);
					}
				} catch(IOException e)
				{
					pendingException = e;
				}
			}
			clients.fail(clientsIndex);
			if(pendingException != null)
			{
				server.manager().exception(pendingException);
			}
		}
	}
	
	private final AtomicBoolean closed = new AtomicBoolean();
	
	public void close()
	{
		synchronized(closed)
		{
			if(closed.compareAndSet(false, true))
			{
				pooledSocketAddresses.close();
			}
		}
	}
	
	private InetSocketAddress getSocketAddress()
	{
		byte ipv6 = socketAddresses.get();
		int port = Util.toUnsignedInt(socketAddresses.getShort());
		int length;
		if(ipv6 == 1)
		{
			length = INET6_BYTES;
		} else if(ipv6 == 0)
		{
			length = INET4_BYTES;
		} else
		{
			server.manager().exception(new IllegalArgumentException("Cannot determine whether address is ipv6 or ipv4!"));
			return null;
		}
		byte[] data = Util.getBytes(socketAddresses, length);
		try
		{
			return new InetSocketAddress(InetAddress.getByAddress(data), port);
		} catch(UnknownHostException e)
		{
			server.manager().exception(e);
			return null;
		}
	}
	
	private SocketChannel allocateSocketChannel(int socket)
	{
		try
		{
			FileDescriptor fd = IOUtil.newFD(socket);
			return (SocketChannel) newSocketChannel.newInstance(SelectorProvider.provider(), fd, false);
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			server.manager().exception(e);
			return null;
		}
	}
	
	private static final int CONNECTED_STATE = 2;
	
	private void fixChannel(SocketChannel socketChannel, InetSocketAddress local, InetSocketAddress remote)
	{
		UNSAFE.putObject(socketChannel, localAddress, local);
		UNSAFE.putObject(socketChannel, remoteAddress, remote);
		UNSAFE.putInt(socketChannel, stateAddress, CONNECTED_STATE);
	}
	
	private static class SocketData
	{
		private final SocketChannel socketChannel;
		private final int socket;
		private final int index;
		
		private SocketData(SocketChannel socketChannel, int socket, int index)
		{
			this.socketChannel = socketChannel;
			this.socket = socket;
			this.index = index;
		}
	}
}