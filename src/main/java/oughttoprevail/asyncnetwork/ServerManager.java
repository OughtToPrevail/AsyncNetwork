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

import java.util.concurrent.ExecutorService;

import oughttoprevail.asyncnetwork.util.Consumer;

public interface ServerManager
{
	/**
	 * Closes the socket and invokes {@link Socket#onDisconnect(Consumer)} specified consumer with the
	 * specified closeType.
	 *
	 * @param closeType the closeType that will be used when calling the {@link
	 * Socket#onDisconnect(Consumer)} specified consumer
	 */
	void close(CloseType closeType);
	
	/**
	 * Invokes the socket's {@link Socket#onException(Consumer)} consumer with the specified
	 * exception.
	 *
	 * @param throwable the exception that the socket's {@link Socket#onException(Consumer)}
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
	 * Returns the {@link ExecutorService} this server is using.
	 *
	 * @return the {@link ExecutorService} this server is using
	 */
	ExecutorService getExecutorService();
	
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