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
package oughttoprevail.asyncnetwork.impl.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.DisconnectionType;
import oughttoprevail.asyncnetwork.IServer;
import oughttoprevail.asyncnetwork.IServerClient;
import oughttoprevail.asyncnetwork.SelectorImplementation;
import oughttoprevail.asyncnetwork.ServerManager;
import oughttoprevail.asyncnetwork.exceptions.SelectorFailedCloseException;
import oughttoprevail.asyncnetwork.impl.util.ExceptionThrower;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.IndexedList;
import oughttoprevail.asyncnetwork.util.LinuxMacSelector;
import oughttoprevail.asyncnetwork.util.OS;
import oughttoprevail.asyncnetwork.util.WindowsSelector;

;

public abstract class AbstractServer<T extends IServer, S extends IServerClient> implements IServer<T, S>
{
	/**
	 * Returns the extending class.
	 *
	 * @return the extending class
	 */
	protected abstract T getThis();
	
	/**
	 * The {@link ServerSocketChannel} used by this {@link AbstractServer}.
	 */
	private final ServerSocketChannel channel;
	
	/**
	 * The bufferSize used when creating a new {@link IServerClient}.
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
	 * The manager of this {@link AbstractServer}, it will handle sensitive calls.
	 */
	private final ServerManager manager;
	
	/**
	 * Constructs a new {@link AbstractServer} and uses default values.
	 */
	protected AbstractServer()
	{
		this(Channel.DEFAULT_BUFFER_SIZE, DEFAULT_SELECT_TIMEOUT, DEFAULT_SELECT_ARRAY_SIZE, DEFAULT_THREADS_COUNT);
	}
	
	/**
	 * Constructs a new {@link AbstractServer} and uses the specified bufferSize when creating a {@link AbstractServer},
	 * uses the specified selectTimeout when using a {@code select} function,
	 * uses the specified selectArraySize when creating an {@link oughttoprevail.asyncnetwork.util.IndexesBuffer}
	 * and the specified threadsCount to know how many threads this should {@link AbstractServer} should use.
	 *
	 * @param bufferSize used when creating a new {@link IServerClient}
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
		this.threadsCount = threadsCount == 0 ? IServer.getSystemThreadsCount() : threadsCount;
		manager = new ServerManager()
		{
			@Override
			public void close(DisconnectionType disconnectionType)
			{
				if(closed)
					return;
				try
				{
					channel.close();
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
					if(onClose != null)
					{
						onClose.accept(disconnectionType);
					}
					for(S client : getClients())
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
				if(onException != null)
				{
					onException.accept(throwable);
				}
			}
			
			@Override
			public Object getSelector()
			{
				return selector;
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
			channel = ServerSocketChannel.open();
			if(OS.ANDROID)
			{
				channel.socket().setReceiveBufferSize(bufferSize);
			} else
			{
				channel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
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
	 * Calls {@link ServerSocketChannel#bind(SocketAddress)} with the specified address.
	 *
	 * @param address the address that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress)}
	 * @return this
	 */
	@Override
	public T bind(SocketAddress address)
	{
		return bind(address, 0);
	}
	
	/**
	 * Calls {@link ServerSocketChannel#bind(SocketAddress, int)} with the specified address and
	 * specified backlog.
	 *
	 * @param address the address that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress, int)}
	 * @param backlog maximum rate the server can accept new TCP connections on a socket
	 * if 0 Java will use it's default
	 * @return this
	 */
	@Override
	public T bind(SocketAddress address, int backlog)
	{
		Validator.requireNonNull(address, "Address");
		try
		{
			if(onConnection == null)
				throw new IllegalArgumentException("On connection has not been setValue!");
			if(OS.ANDROID)
			{
				channel.socket().bind(address, backlog);
			} else
			{
				channel.bind(address, backlog);
			}
			doBind();
		} catch(IOException e)
		{
			manager.exception(e);
		}
		return getThis();
	}
	
	/**
	 * Bind to the specified port.
	 *
	 * @param port the port that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress)} with {@link
	 * InetSocketAddress#InetSocketAddress(int)}
	 * @return this
	 */
	@Override
	public T bind(int port)
	{
		Validator.validatePort(port);
		return bind(new InetSocketAddress(port));
	}
	
	/**
	 * Binds to the specified address and the specified port.
	 *
	 * @param address the address that will be used when creating {@link
	 * InetSocketAddress#InetSocketAddress(String, int)}
	 * @param port the port that will be used when creating {@link
	 * InetSocketAddress#InetSocketAddress(String, int)}
	 * @return this
	 */
	@Override
	public T bind(String address, int port)
	{
		Validator.requireNonNull(address, "Host");
		Validator.validatePort(port);
		return bind(new InetSocketAddress(address, port));
	}
	
	/**
	 * Binds to the specified port with {@link Channel#LOCAL_ADDRESS} as the host.
	 *
	 * @param port the port that will be used when creating {@link
	 * InetSocketAddress#InetSocketAddress(int)}
	 * @return this
	 */
	@Override
	public T bindLocalHost(int port)
	{
		Validator.validatePort(port);
		return bind(Channel.LOCAL_ADDRESS, port);
	}
	
	/**
	 * A {@link Consumer} which will be called once a bind has successfully finished.
	 */
	private Consumer<T> onBind;
	
	/**
	 * Calls the specified runnable when the T has successfully binded.
	 *
	 * @param onBind the runnable that will be called when the T has successfully binded
	 * @return this
	 */
	@Override
	public T onBind(Runnable onBind)
	{
		return onBind(T -> onBind.run());
	}
	
	/**
	 * Calls the specified consumer with this T when the T has successfully binded.
	 *
	 * @param onBind the consumer that will be called when the T has successfully binded
	 * @return this
	 */
	@Override
	public T onBind(Consumer<T> onBind)
	{
		if(binded)
		{
			onBind.accept(getThis());
		} else
		{
			this.onBind = onBind;
		}
		return getThis();
	}
	
	/**
	 * The list of clients contained by this {@link AbstractServer}.
	 */
	private final IndexedList<S> clients = new IndexedListImpl<>();
	
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
				channel.configureBlocking(false);
			} catch(IOException e)
			{
				Validator.exceptionClose(this, e);
				return;
			}
		}
		binded = true;
		if(onBind != null)
		{
			onBind.accept(getThis());
		}
		this.selector = createSelector();
	}
	
	/**
	 * Creates a new selector which will be closed when the server is closed.
	 * The selector must be either {@link LinuxMacSelector} or {@link WindowsSelector} or {@link java.nio.channels.Selector} or a exception will be thrown.
	 * <p>
	 * if {@link OS#LINUX} and {@link OS#MAC} aren't {@code true} it will be setValue to null
	 *
	 * @return the new closeable selector
	 */
	protected abstract Closeable createSelector();
	
	/**
	 * Returns a new initialized client.
	 *
	 * @param channel the channel which will be transformed into S
	 * @param clientsIndex the index that will be used when removing the client when a client closes
	 * @return the new initialized {@link S}
	 */
	protected abstract S initializeClient(SocketChannel channel, int clientsIndex);
	
	/**
	 * Returns whether this server has binded.
	 *
	 * @return whether this server has binded
	 */
	@Override
	public boolean isBinded()
	{
		return binded;
	}
	
	/**
	 * A {@link Consumer} which will be called once a connection has established.
	 */
	private Consumer<S> onConnection;
	
	/**
	 * Calls the specified consumer when a client has connected.
	 *
	 * @param onConnection the consumer that will be called when a client connects
	 * @return this
	 */
	@Override
	public T onConnection(Consumer<S> onConnection)
	{
		if(onConnection == null && binded)
			throw new IllegalArgumentException("Cannot setValue onConnection to null after bind!");
		this.onConnection = onConnection;
		return getThis();
	}
	
	/**
	 * Calls the {@link #onConnection} {@link Consumer} with the specified client.
	 *
	 * @param client to call the {@link #onConnection} with
	 */
	protected void connected(S client)
	{
		onConnection.accept(client);
	}
	
	/**
	 * A {@link Consumer} which will be called once an {@link Throwable} has occurred.
	 */
	private Consumer<Throwable> onException;
	
	/**
	 * Calls the specified consumer with a throwable when a caught exception occurs.
	 *
	 * @param onException the consumer that will be called with the caught exceptions
	 * @return this
	 */
	@Override
	public T onException(Consumer<Throwable> onException)
	{
		this.onException = onException;
		return getThis();
	}
	
	/**
	 * Returns the {@link ServerSocketChannel} the T uses.
	 *
	 * @return the {@link ServerSocketChannel} the T uses
	 */
	@Override
	public ServerSocketChannel getServerChannel()
	{
		return channel;
	}
	
	/**
	 * Returns an unmodifiable list that contains the clients that have connected.
	 *
	 * @return an unmodifiable list that contains the clients that have connected
	 */
	@Override
	public List<S> getClients()
	{
		return clients.list();
	}
	
	/**
	 * Returns the {@link IndexedList} used by the server. Implemented for extending classes.
	 *
	 * @return the {@link IndexedList} used by the server
	 */
	protected IndexedList<S> getClientList()
	{
		return clients;
	}
	
	/**
	 * Returns the bufferSize.
	 *
	 * @return the bufferSize
	 */
	@Override
	public int getBufferSize()
	{
		return bufferSize;
	}
	
	/**
	 * Returns the selectTimeout.
	 *
	 * @return the selectTimeout
	 */
	@Override
	public int getSelectTimeout()
	{
		return selectTimeout;
	}
	
	/**
	 * Returns the selectArraySize.
	 *
	 * @return the selectArraySize
	 */
	@Override
	public int getSelectArraySize()
	{
		return selectArraySize;
	}
	
	/**
	 * Returns the server's thread count.
	 *
	 * @return the server's thread count
	 */
	@Override
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
	private Consumer<DisconnectionType> onClose;
	
	/**
	 * Calls the specified consumer with a disconnectionType when the server is closed.
	 *
	 * @param onClose the consumer that will be called with the disconnectionType when the server is
	 * closed
	 * @return this
	 */
	@Override
	public T onClose(Consumer<DisconnectionType> onClose)
	{
		this.onClose = onClose;
		return getThis();
	}
	
	/**
	 * If the T has yet to be closed the method closes the {@link ServerSocketChannel} and calls the
	 * onDisconnect on all clients that is specified by {@link Channel#onDisconnect(Consumer)}.
	 *
	 * @return this
	 */
	@Override
	public T close()
	{
		manager().close(DisconnectionType.USER_CLOSE);
		return getThis();
	}
	
	/**
	 * Returns whether this server has been closed.
	 *
	 * @return whether this server has been closed
	 */
	@Override
	public boolean isClosed()
	{
		return closed;
	}
	
	/**
	 * Returns the {@link ServerManager}.
	 *
	 * @return the {@link ServerManager}
	 */
	@Override
	public ServerManager manager()
	{
		return manager;
	}
}