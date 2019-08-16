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
package oughttoprevail.asyncnetwork;

import java.nio.channels.SelectionKey;

import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.server.ServerClientSocket;
import oughttoprevail.asyncnetwork.server.ServerSocket;
import oughttoprevail.asyncnetwork.util.SelectorImplementation;

public abstract class ServerClientManager extends SocketManager
{
	public ServerClientManager(Socket socket, PooledByteBuffer readByteBuffer)
	{
		super(socket, readByteBuffer);
	}
	
	public abstract ServerSocket getServer();
	
	public abstract boolean callWrite();
	
	public abstract void callRequests();
	
	/**
	 * File descriptor of the owning {@link ServerClientSocket}.
	 */
	private int fd;
	
	/**
	 * Sets the file descriptor of the serverClient socket.
	 *
	 * @param fd the file descriptor of the serverClient socket
	 */
	public void setFD(int fd)
	{
		this.fd = fd;
	}
	
	/**
	 * Returns the file descriptor of the serverClient socket.
	 *
	 * @return the file descriptor of the serverClient socket
	 */
	public int getFD()
	{
		return fd;
	}
	
	/**
	 * {@link SelectionKey} of the owning {@link ServerClientSocket}.
	 * This will only be used if the {@link SelectorImplementation}
	 * is {@link SelectorImplementation#JAVA}.
	 * This is used for the {@link ServerClientSocket} to know
	 * how to ask the selector to notify itself when writing: It is done by using
	 * {@link SelectionKey#interestOps(int)}.
	 */
	private SelectionKey selectionKey;
	
	/**
	 * Sets the {@link SelectionKey} of {@code selectionKey} to the specified {@code selectionKey}.
	 *
	 * @param selectionKey the value that {@code selectionKey} will be set to
	 */
	public void setSelectionKey(SelectionKey selectionKey)
	{
		this.selectionKey = selectionKey;
	}
	
	/**
	 * Returns the socket's selection key or null if it was never set.
	 *
	 * @return the socket's selection key or null if it was never set
	 */
	public SelectionKey getSelectionKey()
	{
		return selectionKey;
	}
}