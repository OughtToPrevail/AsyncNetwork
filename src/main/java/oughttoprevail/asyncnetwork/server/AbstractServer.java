/*
Copyright 2019 https://github.com/OughtToPrevail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUvoid WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package oughttoprevail.asyncnetwork.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import oughttoprevail.asyncnetwork.CloseType;
import oughttoprevail.asyncnetwork.ServerManager;
import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.exceptions.SelectorFailedCloseException;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.ExceptionThrower;
import oughttoprevail.asyncnetwork.util.IndexesBuffer;
import oughttoprevail.asyncnetwork.util.OS;
import oughttoprevail.asyncnetwork.util.SelectorImplementation;
import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.selector.LinuxMacSelector;
import oughttoprevail.asyncnetwork.util.selector.WindowsSelector;

public abstract class AbstractServer
{
	/**
	 * Infinite select timeout infinity is -1.
	 */
	private static final int INFINITE_SELECT_TIMEOUT = -1;
	/**
	 * Default select timeout.
	 */
	public static final int DEFAULT_SELECT_TIMEOUT = INFINITE_SELECT_TIMEOUT;
	/**
	 * Default select array size.
	 */
	public static final int DEFAULT_SELECT_ARRAY_SIZE = 256;
	/**
	 * Default threads count.
	 */
	public static final int DEFAULT_THREADS_COUNT = 0;
	
	/**
	 * The {@link ServerSocketChannel} used by this {@link AbstractServer}.
	 */
	private final ServerSocketChannel serverSocketChannel;
	
	/**
	 * The bufferSize used when creating a new {@link ServerClientSocket}.
	 */
	private final int bufferSize;
	/**
	 * The selectTime used when using a {@code select} function.
	 */
	private final int selectTimeout;
	/**
	 * This is <b>only</b> used for {@link LinuxMacSelector}.
	 * This will determine how many events {@link LinuxMacSelector} can pick up
	 * per {@code select} function call.
	 */
	private final int selectArraySize;
	/**
	 * How many threads will be used with this {@link AbstractServer}
	 */
	private final int threadsCount;
	
	/**
	 * The manager of this {@link AbstractServer}, it will give access to more sensitive data.
	 */
	private final ServerManager manager;
	
	/**
	 * Constructs a new {@link AbstractServer} and uses default values.
	 */
	protected AbstractServer()
	{
		this(Socket.DEFAULT_BUFFER_SIZE, DEFAULT_SELECT_TIMEOUT, DEFAULT_SELECT_ARRAY_SIZE, DEFAULT_THREADS_COUNT);
	}
	
	/**
	 * Constructs a new {@link AbstractServer} and uses the specified bufferSize when creating a {@link AbstractServer},
	 * uses the specified selectTimeout when using a {@code select} function,
	 * uses the specified selectArraySize when creating an {@link IndexesBuffer}
	 * and the specified threadsCount to know how many threads this should {@link AbstractServer} should use.
	 *
	 * @param bufferSize used when creating a new {@link ServerClientSocket}
	 * @param selectTimeout used when using a {@code select} function
	 * @param selectArraySize is <b>only</b> used for {@link LinuxMacSelector}.
	 * This will determine how many events {@link LinuxMacSelector} can pick up
	 * per {@code select} function call
	 * @param threadsCount how many threads will be used with this {@link AbstractServer}
	 */
	protected AbstractServer(int bufferSize, int selectTimeout, int selectArraySize, int threadsCount)
	{
		this.bufferSize = bufferSize;
		this.selectTimeout = selectTimeout;
		this.selectArraySize = selectArraySize;
		this.threadsCount = threadsCount == 0 ? Runtime.getRuntime().availableProcessors() * 2 : threadsCount;
		manager = new ServerManager()
		{
			@Override
			public void close(CloseType closeType)
			{
				if(closed)
					return;
				try
				{
					serverSocketChannel.close();
					if(selector != null)
					{
						try
						{
							selector.close();
						} catch(IOException e)
						{
							manager.exception(new SelectorFailedCloseException(e));
						}
					}
					closed = true;
					for(Consumer<CloseType> closeConsumer : onClose)
					{
						closeConsumer.accept(closeType);
					}
					for(ServerClientSocket client : getClients())
					{
						client.close();
					}
				} catch(IOException e)
				{
					exception(e);
				}
			}
			
			@Override
			public void exception(Throwable throwable)
			{
				Util.exception(onException, throwable);
			}
			
			@Override
			public Object getSelector()
			{
				return selector;
			}
			
			private final ExecutorService executorService = OS.ANDROID ? Executors.newFixedThreadPool(getThreadsCount()) : ForkJoinPool.commonPool();
			
			@Override
			public ExecutorService getExecutorService()
			{
				return executorService;
			}
			
			@Override
			public boolean isWindowsImplementation()
			{
				return selector instanceof WindowsSelector;
			}
			
			@Override
			public void clientDisconnected(int clientsIndex)
			{
				clients.remove(clientsIndex);
			}
		};
		try
		{
			serverSocketChannel = ServerSocketChannel.open();
			if(OS.ANDROID)
			{
				serverSocketChannel.socket().setReceiveBufferSize(bufferSize);
			} else
			{
				serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
			}
		} catch(IOException e)
		{
			ExceptionThrower.throwException(e);
			throw new RuntimeException();
		}
	}
	
	/**
	 * Whether this {@link AbstractServer} is binded.
	 */
	private boolean binded;
	
	/**
	 * Invokes {@link ServerSocketChannel#bind(SocketAddress)} with the specified address.
	 *
	 * @param address the address that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress)}
	 */
	public void bind(SocketAddress address)
	{
		bind(address, 0);
	}
	
	/**
	 * Invokes {@link ServerSocketChannel#bind(SocketAddress, int)} with the specified address and
	 * specified backlog.
	 *
	 * @param address the address that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress, int)}
	 * @param backlog maximum rate the server can accept new TCP connections on a socket
	 * if 0 Java will use it's default
	 */
	public void bind(SocketAddress address, int backlog)
	{
		Validator.requireNonNull(address, "Address");
		Validator.requireNonNull(onConnection, "onConnection");
		try
		{
			if(OS.ANDROID)
			{
				serverSocketChannel.socket().bind(address, backlog);
			} else
			{
				serverSocketChannel.bind(address, backlog);
			}
			doBind();
		} catch(IOException e)
		{
			manager.exception(e);
		}
	}
	
	/**
	 * Bind to the specified port.
	 *
	 * @param port the port that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress)} with {@link
	 * InetSocketAddress#InetSocketAddress(int)}
	 */
	public void bind(int port)
	{
		Validator.validatePort(port);
		bind(new InetSocketAddress(port));
	}
	
	/**
	 * Binds to the specified address and the specified port.
	 *
	 * @param address the address that will be used when creating {@link
	 * InetSocketAddress#InetSocketAddress(String, int)}
	 * @param port the port that will be used when creating {@link
	 * InetSocketAddress#InetSocketAddress(String, int)}
	 */
	public void bind(String address, int port)
	{
		Validator.requireNonNull(address, "Host");
		Validator.validatePort(port);
		bind(new InetSocketAddress(address, port));
	}
	
	/**
	 * Binds to the specified port with {@link Socket#LOCAL_ADDRESS} as the host.
	 *
	 * @param port the port that will be used when creating {@link
	 * InetSocketAddress#InetSocketAddress(int)}
	 */
	public void bindLocalHost(int port)
	{
		Validator.validatePort(port);
		bind(Socket.LOCAL_ADDRESS, port);
	}
	
	/**
	 * A {@link Runnable} which will be called once a bind has successfully finished.
	 */
	private final List<Runnable> onBind = new ArrayList<>();
	
	/**
	 * Invokes the specified runnable when the void has successfully binded.
	 *
	 * @param onBind the runnable that will be called when the void has successfully binded
	 */
	public void onBind(Runnable onBind)
	{
		if(binded)
		{
			Validator.runRunnable(onBind);
		} else
		{
			this.onBind.add(onBind);
		}
	}
	
	/**
	 * The list of clients contained by this {@link AbstractServer}.
	 */
	private final IndexedList<ServerClientSocket> clients = new IndexedList<>();
	
	/**
	 * This {@link AbstractServer} {@link Closeable} selector.
	 */
	private Closeable selector;
	
	/**
	 * Operates the after bind operations such as creating a
	 * selector and calling the {@link #onBind} consumer.
	 */
	private void doBind()
	{
		if(getSelectorImplementation() != SelectorImplementation.THREAD_PER_CLIENT)
		{
			try
			{
				serverSocketChannel.configureBlocking(false);
			} catch(IOException e)
			{
				Validator.exceptionClose(this, e);
				return;
			}
		}
		binded = true;
		for(Runnable bindConsumer : onBind)
		{
			bindConsumer.run();
		}
		this.selector = createSelector();
	}
	
	/**
	 * Creates a new selector which will be closed when the server is closed.
	 * The selector must be either {@link LinuxMacSelector} or {@link WindowsSelector} or {@link java.nio.channels.Selector} or a exception will be thrown.
	 * <p>
	 * if {@link OS#LINUX} and {@link OS#MAC} aren't {@code true} it will be set to null
	 *
	 * @return the new closeable selector
	 */
	protected abstract Closeable createSelector();
	
	/**
	 * Returns a new initialized client.
	 *
	 * @param socketChannel the socket which will be transformed into S
	 * @param clientsIndex the index that will be used when removing the client when a client closes
	 * @return the new initialized {@link ServerClientSocket}
	 */
	public ServerClientSocket initializeClient(SocketChannel socketChannel, int clientsIndex) throws IOException
	{
		if(getSelectorImplementation() != SelectorImplementation.THREAD_PER_CLIENT)
		{
			socketChannel.configureBlocking(false);
		}
		ServerClientSocket serverClientSocket = createServerClientSocket(socketChannel, clientsIndex);
		serverClientSocket.manager().init();
		return serverClientSocket;
	}
	
	/**
	 * Returns a new {@link ServerClientSocket} dependent on the specified parameters.
	 *
	 * @param socketChannel is the {@link SocketChannel} of the {@link ServerClientSocket}
	 * @param clientsIndex the index that will be used when removing the client when a client closes
	 * @return a new {@link ServerClientSocket}
	 */
	protected abstract ServerClientSocket createServerClientSocket(SocketChannel socketChannel, int clientsIndex);
	
	/**
	 * Returns whether this server has binded.
	 *
	 * @return whether this server has binded
	 */
	public boolean isBinded()
	{
		return binded;
	}
	
	/**
	 * A {@link Consumer} which will be called once a connection has established.
	 */
	private final List<Consumer<ServerClientSocket>> onConnection = new ArrayList<>();
	
	/**
	 * Invokes the specified consumer when a client has connected.
	 *
	 * @param onConnection the consumer that will be called when a client connects
	 */
	public void onConnection(Consumer<ServerClientSocket> onConnection)
	{
		if(onConnection == null && binded)
			throw new IllegalArgumentException("Cannot set onConnection to null after bind!");
		this.onConnection.add(onConnection);
	}
	
	/**
	 * Invokes the {@link #onConnection} {@link Consumer} with the specified client.
	 *
	 * @param client to call the {@link #onConnection} with
	 */
	public void connected(ServerClientSocket client)
	{
		for(Consumer<ServerClientSocket> connectionConsumer : onConnection)
		{
			connectionConsumer.accept(client);
		}
	}
	
	/**
	 * A {@link Consumer} which will be called once an {@link Throwable} has occurred.
	 */
	private final List<Consumer<Throwable>> onException = new ArrayList<>();
	
	/**
	 * Invokes the specified consumer with a throwable when a caught exception occurs.
	 *
	 * @param onException the consumer that will be called with the caught exceptions
	 */
	public void onException(Consumer<Throwable> onException)
	{
		this.onException.add(onException);
	}
	
	/**
	 * Returns the {@link ServerSocketChannel} the void uses.
	 *
	 * @return the {@link ServerSocketChannel} the void uses
	 */
	public ServerSocketChannel getServerChannel()
	{
		return serverSocketChannel;
	}
	
	/**
	 * Returns an unmodifiable list that contains the clients that have connected.
	 *
	 * @return an unmodifiable list that contains the clients that have connected
	 */
	public List<ServerClientSocket> getClients()
	{
		return clients.list();
	}
	
	/**
	 * Returns the {@link IndexedList} used by the server. Implemented for extending classes.
	 *
	 * @return the {@link IndexedList} used by the server
	 */
	protected IndexedList<ServerClientSocket> getClientList()
	{
		return clients;
	}
	
	/**
	 * Returns the bufferSize.
	 *
	 * @return the bufferSize
	 */
	public int getBufferSize()
	{
		return bufferSize;
	}
	
	/**
	 * Returns the selectTimeout.
	 *
	 * @return the selectTimeout
	 */
	public int getSelectTimeout()
	{
		return selectTimeout;
	}
	
	/**
	 * Returns the selectArraySize.
	 *
	 * @return the selectArraySize
	 */
	public int getSelectArraySize()
	{
		return selectArraySize;
	}
	
	/**
	 * Returns the server's thread count.
	 *
	 * @return the server's thread count
	 */
	public int getThreadsCount()
	{
		return threadsCount;
	}
	
	/**
	 * Whether this {@link AbstractServer} is closed.
	 */
	private boolean closed;
	
	/**
	 * A {@link Consumer} which will be called once this {@link AbstractServer} has closed.
	 */
	private final List<Consumer<CloseType>> onClose = new ArrayList<>();
	
	/**
	 * Invokes the specified consumer with a disconnectionType when the server is closed.
	 *
	 * @param onClose the consumer that will be called with the disconnectionType when the server is
	 * closed
	 */
	public void onClose(Consumer<CloseType> onClose)
	{
		this.onClose.add(onClose);
	}
	
	/**
	 * If the void has yet to be closed the method closes the {@link ServerSocketChannel} and invokes the
	 * onDisconnect on all clients that is specified by {@link Socket#onDisconnect(Consumer)}.
	 */
	public void close()
	{
		manager().close(CloseType.USER_CLOSE);
	}
	
	/**
	 * Returns whether this server has been closed.
	 *
	 * @return whether this server has been closed
	 */
	public boolean isClosed()
	{
		return closed;
	}
	
	/**
	 * Returns the {@link ServerManager}.
	 *
	 * @return the {@link ServerManager}
	 */
	public ServerManager manager()
	{
		return manager;
	}
	
	/**
	 * Returns the server's {@link SelectorImplementation}.
	 *
	 * @return the server's {@link SelectorImplementation}.
	 */
	public abstract SelectorImplementation getSelectorImplementation();
}