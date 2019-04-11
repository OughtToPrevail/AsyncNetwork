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
package oughttoprevail.asyncnetwork.impl.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.Client;
import oughttoprevail.asyncnetwork.ClientManager;
import oughttoprevail.asyncnetwork.impl.ChannelImpl;
import oughttoprevail.asyncnetwork.impl.util.ExceptionThrower;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.impl.util.writer.Writer;
import oughttoprevail.asyncnetwork.impl.util.writer.client.ClientWriter;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.ThreadCreator;

;

public class ClientImpl extends ChannelImpl<Client> implements Client
{
	/**
	 * Returns the extending class.
	 *
	 * @return the extending class
	 */
	@Override
	protected ClientImpl getThis()
	{
		return this;
	}
	
	/***
	 * The {@link SocketChannel} used by this {@link ClientImpl}, this will be a blocking {@link SocketChannel}.
	 */
	private final SocketChannel channel;
	/**
	 * The manager of this {@link Client}, it will handle sensitive calls.
	 */
	private ClientManager manager;
	
	/**
	 * Constructs a new {@link ClientImpl} and uses {@link Channel#DEFAULT_BUFFER_SIZE} as the bufferSize.
	 */
	public ClientImpl()
	{
		this(DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * Constructs a new {@link ClientImpl} and uses the specified bufferSize to create a new {@link ByteBuffer}
	 * and {@link Channel#initializeDefaultOptions(SocketChannel, int)}.
	 *
	 * @param bufferSize used by this client for allocating buffers and initializing default options
	 */
	public ClientImpl(int bufferSize)
	{
		super(ByteBuffer.allocateDirect(bufferSize));
		try
		{
			channel = SocketChannel.open();
			channel.configureBlocking(true);
			Channel.initializeDefaultOptions(channel, bufferSize);
			manager = createClientManager();
		} catch(IOException e)
		{
			ExceptionThrower.throwException(e);
			throw new RuntimeException();
		}
	}
	
	/**
	 * Creates a new {@link Writer}.
	 *
	 * @return the new writer
	 */
	@Override
	protected Writer<Client> createWriter()
	{
		return new ClientWriter();
	}
	
	/**
	 * Whether this client is currently connected
	 */
	private boolean connected;
	
	/**
	 * The name used when creating the client {@link Thread}.
	 */
	private static final String CLIENT_THREAD_NAME = "Client-Thread";
	
	/**
	 * Connects to the specified address.
	 *
	 * @param address the address that the channel will connect to
	 * @return this
	 * @throws java.nio.channels.AlreadyConnectedException if the channel is already connected
	 */
	@Override
	public Client connect(SocketAddress address)
	{
		try
		{
			channel.connect(address);
			connected = true;
			if(onConnect != null)
			{
				onConnect.accept(this);
				//set to null since no more than one connection can occur per client.
				onConnect = null;
			}
			ThreadCreator.newThread(CLIENT_THREAD_NAME, () ->
			{
				while(!isClosed())
				{
					manager().callRead();
				}
			});
		} catch(IOException e)
		{
			Validator.exceptionClose(this, e);
		}
		return this;
	}
	
	/**
	 * Connects to the specified host and port.
	 *
	 * @param host the host that the channel will connect to
	 * @param port the port that the channel will connect to
	 * @return this
	 * @throws java.nio.channels.AlreadyConnectedException if the channel is already connected
	 */
	@Override
	public Client connect(String host, int port)
	{
		Validator.requireNonNull(host, "Host");
		Validator.validatePort(port);
		return connect(new InetSocketAddress(host, port));
	}
	
	/**
	 * Connects to the specified port with {@link Channel#LOCAL_ADDRESS} as the host.
	 *
	 * @param port the port that the channel will connect to
	 * @return this
	 * @throws java.nio.channels.AlreadyConnectedException if the channel is already connected
	 */
	@Override
	public Client connectLocalHost(int port)
	{
		Validator.validatePort(port);
		return connect(LOCAL_ADDRESS, port);
	}
	
	/**
	 * A {@link Consumer} which will be called once a connection has established.
	 */
	private Consumer<Client> onConnect;
	
	/**
	 * Calls the specified runnable when the channel's connect process has successfully finished.
	 *
	 * @param onConnect the runnable that will be called the channel's connect has successfully
	 * finished
	 * @return this
	 */
	@Override
	public Client onConnect(Runnable onConnect)
	{
		return onConnect(client -> onConnect.run());
	}
	
	/**
	 * Calls the specified consumer when the channel's connect process has successfully finished.
	 *
	 * @param onConnect the consumer that will be called the channel's connect has successfully
	 * finished
	 * @return this
	 */
	@Override
	public Client onConnect(Consumer<Client> onConnect)
	{
		if(connected)
		{
			if(onConnect != null)
			{
				onConnect.accept(this);
			}
		} else
		{
			this.onConnect = onConnect;
		}
		return this;
	}
	
	/**
	 * Returns the {@link SocketChannel} this channel uses.
	 *
	 * @return the {@link SocketChannel} this channel uses
	 */
	@Override
	public SocketChannel getSocketChannel()
	{
		return channel;
	}
	
	/**
	 * Returns whether the channel is connected.
	 *
	 * @return whether the channel is connected
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
	public ClientManager manager()
	{
		return manager;
	}
}