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

import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.IndexedList;

;

/**
 * Implementation at {@link oughttoprevail.asyncnetwork.impl.server.ServerClientManagerImpl}.
 */
public interface ServerManager
{
	/**
	 * Closes the channel and calls {@link Channel#onDisconnect(Consumer)} specified consumer with the
	 * specified disconnectionType.
	 *
	 * @param disconnectionType the disconnectionType that will be used when calling the {@link
	 * Channel#onDisconnect(Consumer)} specified consumer
	 */
	void close(DisconnectionType disconnectionType);
	
	/**
	 * Calls the channel's {@link Channel#onException(Consumer)} consumer with the specified
	 * exception.
	 *
	 * @param throwable the exception that the channel's {@link Channel#onException(Consumer)}
	 * consumer will be called with
	 */
	void exception(Throwable throwable);
	
	/**
	 * Returns the server selector.
	 *
	 * @return the server selector
	 */
	Object getSelector();
	
	/**
	 * Returns whether this is a windows implementation server.
	 *
	 * @return whether this is a windows implementation server
	 */
	boolean isWindowsImplementation();
	
	/**
	 * Removes the client from the {@link IndexedList}
	 * because the client has disconnected.
	 *
	 * @param clientsIndex to remove from {@link IndexedList}
	 */
	void clientDisconnected(int clientsIndex);
}