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

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.ConditionWaiter;
import oughttoprevail.asyncnetwork.Server;
import oughttoprevail.asyncnetwork.ServerClient;
import oughttoprevail.asyncnetwork.ServerClientManager;
import oughttoprevail.asyncnetwork.impl.ChannelImpl;
import oughttoprevail.asyncnetwork.impl.packet.ByteBufferPool;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.impl.util.writer.Writer;
import oughttoprevail.asyncnetwork.impl.util.writer.server.ServerWriter;
import oughttoprevail.asyncnetwork.impl.util.writer.server.WindowsWriter;
import oughttoprevail.asyncnetwork.util.Consumer;

;

public abstract class ServerClientImpl extends ChannelImpl<ServerClient> implements ServerClient
{
	/**
	 * Returns the extending class.
	 *
	 * @return the extending class
	 */
	@Override
	protected ServerClient getThis()
	{
		return this;
	}
	
	/***
	 * The {@link SocketChannel} used by this {@link ServerClientImpl}, this will be a non-blocking {@link SocketChannel}.
	 */
	private final SocketChannel channel;
	/**
	 * The manager of this {@link ServerClient}, it will handle sensitive calls.
	 */
	private final ServerClientManager<Server> manager;
	
	public ServerClientImpl(Server server, SocketChannel channel, int clientsIndex)
	{
		super(ByteBufferPool.INSTANCE.takeExactly(server.getBufferSize()));
		this.channel = channel;
		manager = createServerClientManager(server, clientsIndex);
	}
	
	/**
	 * Creates a new {@link Writer}.
	 *
	 * @return the new writer
	 */
	@Override
	protected Writer<ServerClient> createWriter()
	{
		return isWindowsImplementedServer() ? new WindowsWriter() : new ServerWriter();
	}
	
	/**
	 * Calls {@link Channel#write(ByteBuffer, Consumer)} and blocks until the write has successfully complete.
	 *
	 * @param waiter the {@link ConditionWaiter} that will block until the write has successfully complete
	 * @return this
	 */
	@Override
	public ServerClient writeBlocking(ByteBuffer writeBuffer, ConditionWaiter waiter)
	{
		Validator.requireNonNull(waiter, "ConditionWaiter");
		write(writeBuffer, ignored -> waiter.finish());
		waiter.await();
		waiter.reset();
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
		return true;
	}
	
	/**
	 * Returns the serverClient's manager.
	 *
	 * @return the serverClient's manager
	 */
	@Override
	public ServerClientManager<Server> manager()
	{
		return manager;
	}
	
	/**
	 * Returns whether the owning server is implemented with windows
	 * {@link oughttoprevail.asyncnetwork.SelectorImplementation} this is needed so
	 * {@link ServerClientImpl#createWriter()} can know whether to copy a
	 * {@link WindowsWriter} or {@link ServerWriter}
	 *
	 * @return hether the owning server is implemented with windows
	 */
	protected abstract boolean isWindowsImplementedServer();
}
