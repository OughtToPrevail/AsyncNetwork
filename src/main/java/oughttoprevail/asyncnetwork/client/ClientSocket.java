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
package oughttoprevail.asyncnetwork.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import oughttoprevail.asyncnetwork.ClientSocketManager;
import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.ExceptionThrower;
import oughttoprevail.asyncnetwork.util.ThreadCreator;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.reader.Reader;
import oughttoprevail.asyncnetwork.util.writer.Writer;
import oughttoprevail.asyncnetwork.util.writer.client.ClientWriter;

public class ClientSocket extends Socket
{
	/***
	 * The {@link SocketChannel} used by this {@link ClientSocket}, this will be a blocking {@link SocketChannel}.
	 */
	private final SocketChannel socketChannel;
	/**
	 * The manager of this {@link ClientSocket}, it will give access to more sensitive data.
	 */
	private ClientSocketManager manager;
	
	/**
	 * Constructs a new {@link ClientSocket} and uses {@link Socket#DEFAULT_BUFFER_SIZE} as the bufferSize.
	 */
	public ClientSocket()
	{
		this(DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Constructs a new {@link ClientSocket} and uses the specified bufferSize to create a new {@link ByteBuffer}.
	 *
	 * @param bufferSize used by this client for allocating buffers and initializing default options
	 */
	public ClientSocket(int bufferSize)
	{
		this(bufferSize, new Reader(), new ClientWriter());
	}
	
	public ClientSocket(int bufferSize, Reader reader, Writer writer)
	{
		super(bufferSize, reader, writer);
		try
		{
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			manager = createClientManager();
			manager.init();
		} catch(IOException e)
		{
			ExceptionThrower.throwException(e);
			throw new RuntimeException();
		}
	}
	
	/**
	 * Whether this client is currently connected
	 */
	private boolean connected;
	private int connectionTimeout = -1;
	
	/**
	 * The name used when creating the client {@link Thread}.
	 */
	private static final String CLIENT_THREAD_NAME = "Client-Thread";
	
	/**
	 * Connects to the specified address.
	 *
	 * @param address the address that the socket will connect to
	 * @throws java.nio.channels.AlreadyConnectedException if the socket is already connected
	 */
	public void connect(SocketAddress address)
	{
		try
		{
			if(connectionTimeout == -1)
			{
				socketChannel.connect(address);
			} else
			{
				socketChannel.socket().connect(address, connectionTimeout);
			}
		} catch(IOException e)
		{
			for(Consumer<IOException> consumer : onConnectionFailure)
			{
				consumer.accept(e);
			}
			manager().exception(e);
			return;
		}
		ThreadCreator.newThread(CLIENT_THREAD_NAME, () ->
		{
			while(!isClosed())
			{
				manager().callRead();
			}
		});
		connected = true;
		for(Runnable connectRunnable : onConnect)
		{
			connectRunnable.run();
		}
		//clear since no more than one connection can occur per client.
		onConnect.clear();
	}
	
	/**
	 * Connects to the specified host and port.
	 *
	 * @param host the host that the socket will connect to
	 * @param port the port that the socket will connect to
	 * @throws java.nio.channels.AlreadyConnectedException if the socket is already connected
	 */
	public void connect(String host, int port)
	{
		Validator.requireNonNull(host, "Host");
		Validator.validatePort(port);
		connect(new InetSocketAddress(host, port));
	}
	
	/**
	 * Connects to the specified port with {@link Socket#LOCAL_ADDRESS} as the host.
	 *
	 * @param port the port that the socket will connect to
	 * @throws java.nio.channels.AlreadyConnectedException if the socket is already connected
	 */
	public void connectLocalHost(int port)
	{
		Validator.validatePort(port);
		connect(LOCAL_ADDRESS, port);
	}
	
	/**
	 * A {@link Runnable} which will be called once a connection has established.
	 */
	private final List<Runnable> onConnect = new ArrayList<>();
	
	/**
	 * Invokes the specified runnable when the socket's connect process has successfully finished.
	 *
	 * @param onConnect the runnable that will be called the socket's connect has successfully
	 * finished
	 */
	public void onConnect(Runnable onConnect)
	{
		if(isConnected())
		{
			Validator.runRunnable(onConnect);
		} else
		{
			this.onConnect.add(onConnect);
		}
	}
	
	/**
	 * List of consumers to be invoked when a connection failure has occurred
	 */
	private final List<Consumer<IOException>> onConnectionFailure = new ArrayList<>();
	
	/**
	 * @param onConnectionFailure consumer to be invoked if connection establishment has failed
	 */
	public void onConnectionFailure(Consumer<IOException> onConnectionFailure)
	{
		this.onConnectionFailure.add(onConnectionFailure);
	}
	
	/**
	 * Sets the timeout in milliseconds for a connection to the specified timeout
	 *
	 * @param timeout to set in milliseconds, must be higher or equal to zero
	 */
	public void setConnectionTimeout(int timeout)
	{
		if(timeout < 0)
		{
			throw new IllegalArgumentException("Timeout must be higher or equal to zero!");
		}
		this.connectionTimeout = timeout;
	}
	
	/**
	 * Returns the {@link SocketChannel} this socket uses.
	 *
	 * @return the {@link SocketChannel} this socket uses
	 */
	@Override
	public SocketChannel getSocketChannel()
	{
		return socketChannel;
	}
	
	/**
	 * Returns whether the socket is connected.
	 *
	 * @return whether the socket is connected
	 */
	@Override
	public boolean isConnected()
	{
		return connected && !isClosed();
	}
	
	/**
	 * Returns the client's manager.
	 *
	 * @return the client's manager
	 */
	@Override
	public ClientSocketManager manager()
	{
		return manager;
	}
}