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
package oughttoprevail.asyncnetwork.impl.util.selector.flags;

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

import oughttoprevail.asyncnetwork.DisconnectionType;
import oughttoprevail.asyncnetwork.IServer;
import oughttoprevail.asyncnetwork.IServerClient;
import oughttoprevail.asyncnetwork.impl.Util;
import oughttoprevail.asyncnetwork.impl.packet.ByteBufferElement;
import oughttoprevail.asyncnetwork.impl.packet.ByteBufferPool;
import oughttoprevail.asyncnetwork.impl.util.UnsafeGetter;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.impl.util.writer.server.PendingWrite;
import oughttoprevail.asyncnetwork.util.IndexedList;
import oughttoprevail.asyncnetwork.util.WindowsSelector;

public abstract class WindowsSelectorFlags<S extends IServerClient>
{
	private static final Unsafe UNSAFE = UnsafeGetter.getUnsafe();
	
	private static final int INET4_BYTES = Util.LONG_BYTES;
	private static final int INET6_BYTES = Util.BYTE_BYTES * 16;
	
	private final IServer<?, S> server;
	private final IndexedList<S> clients;
	private final WindowsSelector selector;
	
	private final int serverSocket;
	
	private final Constructor<?> newSocketChannel;
	private final long localAddress;
	private final long remoteAddress;
	private final long state;
	
	private final ByteBufferElement socketAddressesElement;
	private final ByteBuffer socketAddresses;
	private final long socketAddressesAddress;
	
	private final AtomicReference<SocketData> socketData = new AtomicReference<>();
	
	protected WindowsSelectorFlags(IServer<?, S> server,
			IndexedList<S> clients,
			WindowsSelector selector,
			int serverSocket) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException
	{
		this.server = server;
		this.clients = clients;
		this.selector = selector;
		this.serverSocket = serverSocket;
		Class<?> socketChannelImpl = Class.forName("sun.nio.ch.SocketChannelImpl");
		newSocketChannel = socketChannelImpl.getDeclaredConstructor(SelectorProvider.class,
				FileDescriptor.class,
				boolean.class);
		newSocketChannel.setAccessible(true);
		localAddress = findOffset(socketChannelImpl, "localAddress");
		remoteAddress = findOffset(socketChannelImpl, "remoteAddress");
		state = findOffset(socketChannelImpl, "state");
		socketAddressesElement = ByteBufferPool.getInstance()
				.take(Byte.BYTES * 2 + Short.BYTES * 2 + Math.max(INET4_BYTES, INET6_BYTES) * 2);
		socketAddresses = socketAddressesElement.getByteBuffer();
		socketAddresses.order(ByteOrder.nativeOrder());
		socketAddressesAddress = Util.address(socketAddresses);
	}
	
	private long findOffset(Class<?> socketChannelImpl, String fieldName) throws NoSuchFieldException
	{
		Field field = socketChannelImpl.getDeclaredField(fieldName);
		return UNSAFE.objectFieldOffset(field);
	}
	
	public void AcceptEx()
	{
		synchronized(socketData)
		{
			int index = clients.index();
			int socket = selector.AcceptEx(serverSocket, index, server.getThreadsCount());
			SocketChannel channel = allocateSocketChannel(socket);
			socketData.set(new SocketData(channel, socket, index));
		}
	}
	
	private static final int READ_OR_WRITE = 1;
	private static final int ACCEPT = 2;
	
	public void run(ByteBuffer result, Object pendingWrite)
	{
		byte opcode = result.get();
		if(opcode == READ_OR_WRITE)
		{
			int index = result.getInt();
			S client = clients.get(index);
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
					ByteBufferElement readBufferElement = client.manager().getReadBuffer();
					ByteBuffer readBuffer = readBufferElement.getByteBuffer();
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
							selector.WSARecv(client.manager().getFD(),
									readBufferElement.address(),
									readBuffer.capacity() - position);
						} catch(IOException e)
						{
							Validator.exceptionClose(client, e);
						}
					}
				} else
				{
					if(pendingWrite != null)
					{
						if(pendingWrite instanceof PendingWrite)
						{
							((PendingWrite) pendingWrite).finish(client);
						}
					}
				}
			}
		} else if(opcode == ACCEPT)
		{
			synchronized(socketData)
			{
				SocketData data = socketData.get();
				int socket = data.socket;
				try
				{
					selector.getAddress(socketAddressesAddress);
					
					InetSocketAddress localAddress = getSocketAddress();
					if(localAddress != null)
					{
						InetSocketAddress remoteAddress = getSocketAddress();
						if(remoteAddress != null)
						{
							SocketChannel socketChannel = data.channel;
							if(fixChannel(socketChannel, localAddress, remoteAddress))
							{
								selector.registerClient(serverSocket, socket);
								int clientsIndex = data.index;
								S client = initializeClient(socketChannel, clientsIndex);
								if(client != null)
								{
									clients.add(clientsIndex, client);
									socketAddresses.clear();
									AcceptEx();
									client.manager().setFD(socket);
									ByteBufferElement readBuffer = client.manager().getReadBuffer();
									selector.WSARecv(socket,
											readBuffer.address(),
											readBuffer.getByteBuffer().capacity());
									connected(client);
									return;
								}
							}
						}
						socketAddresses.clear();
					}
					AcceptEx();
				} catch(IOException e)
				{
					server.manager().exception(e);
				}
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
				ByteBufferPool.getInstance().give(socketAddressesElement);
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
			server.manager()
					.exception(new IllegalArgumentException("Cannot determine whether address is ipv6 or ipv4!"));
			return null;
		}
		byte[] data = new byte[length];
		socketAddresses.get(data);
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
	
	private boolean fixChannel(SocketChannel channel, InetSocketAddress local, InetSocketAddress remote)
	{
		UNSAFE.putObject(channel, localAddress, local);
		UNSAFE.putObject(channel, remoteAddress, remote);
		UNSAFE.putInt(channel, state, CONNECTED_STATE);
		return true;
	}
	
	protected abstract void connected(S client);
	
	protected abstract S initializeClient(SocketChannel channel, int clientsIndex);
	
	private static class SocketData
	{
		private final SocketChannel channel;
		private final int socket;
		private final int index;
		
		private SocketData(SocketChannel channel, int socket, int index)
		{
			this.channel = channel;
			this.socket = socket;
			this.index = index;
		}
	}
}