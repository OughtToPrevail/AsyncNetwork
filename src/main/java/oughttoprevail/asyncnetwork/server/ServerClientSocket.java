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
package oughttoprevail.asyncnetwork.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.ServerClientManager;
import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.ConditionWaiter;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.DisconnectionType;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.reader.Reader;
import oughttoprevail.asyncnetwork.util.writer.Writer;
import oughttoprevail.asyncnetwork.util.writer.server.ServerWriter;
import oughttoprevail.asyncnetwork.util.writer.server.WindowsWriter;

public abstract class ServerClientSocket extends Socket
{
	/***
	 * The {@link SocketChannel} used by this {@link ServerClientSocket}, this will be a non-blocking {@link SocketChannel}.
	 */
	private final SocketChannel socketChannel;
	/**
	 * The manager of this {@link oughttoprevail.asyncnetwork.server.ServerClientSocket}, it will give access to more sensitive data.
	 */
	private final ServerClientManager manager;
	/**
	 * The index of this client in the {@link ServerSocket#getClients()}.
	 */
	private final int clientsIndex;
	
	public ServerClientSocket(ServerSocket server, SocketChannel socketChannel, int clientsIndex)
	{
		this(server, socketChannel, clientsIndex, new Reader(), server.manager().isWindowsImplementation() ? new WindowsWriter() : new ServerWriter());
	}
	
	public ServerClientSocket(ServerSocket server, SocketChannel socketChannel, int clientsIndex, Reader reader, Writer writer)
	{
		super(server.getBufferSize(), reader, writer);
		this.socketChannel = socketChannel;
		this.clientsIndex = clientsIndex;
		manager = createServerClientManager(server);
	}
	
	/**
	 * Invoked before the closed has occurred, this is useful for extending classes to make final changes.
	 *
	 * @param disconnectionType is the reason why the disconnection should occur
	 * @return whether the close should continue, if {@code true} the close will continue if
	 * {@code false} the close will stop
	 */
	@Override
	protected boolean preClose(DisconnectionType disconnectionType)
	{
		manager.getServer().manager().clientDisconnected(clientsIndex);
		return super.preClose(disconnectionType);
	}
	
	/**
	 * Invokes {@link Socket#write(ByteBuffer, Consumer)} and blocks until the write has successfully complete.
	 *
	 * @param waiter the {@link ConditionWaiter} that will block until the write has successfully complete
	 */
	public void writeBlocking(ByteBuffer writeBuffer, ConditionWaiter waiter)
	{
		Validator.requireNonNull(waiter, "ConditionWaiter");
		write(writeBuffer, ignored -> waiter.finish());
		waiter.await();
		waiter.reset();
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
		return true;
	}
	
	/**
	 * Returns the serverClient's manager.
	 *
	 * @return the serverClient's manager
	 */
	@Override
	public ServerClientManager manager()
	{
		return manager;
	}
}