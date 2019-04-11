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

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Implementation at {@link oughttoprevail.asyncnetwork.impl.server.ServerClientManagerImpl}.
 *
 * @param <S> the server owning the server client
 */
public interface ServerClientManager<S extends IServer> extends ChannelManager
{
	/**
	 * Returns the owning server.
	 *
	 * @return the owning server
	 */
	S getServer();
	
	/**
	 * Sets the file descriptor of the serverClient socket.
	 *
	 * @param fd the file descriptor of the serverClient socket
	 */
	void setFD(int fd);
	
	/**
	 * Returns the file descriptor of the serverClient socket.
	 *
	 * @return the file descriptor of the serverClient socket
	 */
	int getFD();
	
	/**
	 * Calls pending read requests with the remaining in the channel's read {@link
	 * ByteBuffer}.
	 */
	void callRequests();
	
	/**
	 * Reads from the socket into the channel's read {@link ByteBuffer} then calls the
	 * pending read requests.
	 */
	void callRead();
	
	/**
	 * Writes the channel's {@link ByteBuffer} to the socket.
	 *
	 * @return true if it needs to write more, false if an exception occurred or it has written the
	 * whole {@link ByteBuffer}
	 */
	boolean callWrite();
	
	// Java selector methods
	
	/**
	 * Sets the {@link SelectionKey} of {@code selectionKey} to the specified {@code selectionKey}.
	 *
	 * @param selectionKey the value that {@code selectionKey} will be setValue to
	 */
	void setSelectionKey(SelectionKey selectionKey);
	
	/**
	 * Returns the channel's selection key or null if it was never setValue.
	 *
	 * @return the channel's selection key or null if it was never setValue
	 */
	SelectionKey getSelectionKey();
}