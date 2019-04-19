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

import oughttoprevail.asyncnetwork.impl.server.AbstractServer;
import oughttoprevail.asyncnetwork.impl.server.ServerImpl;

/**
 * Implementation at {@link AbstractServer}, {@link oughttoprevail.asyncnetwork.impl.server.SelectableServer}, {@link ServerImpl}.
 */
public interface Server extends IServer<Server, ServerClient>
{
	/**
	 * Creates a new server with the default values.
	 *
	 * @return a new server with the default values.
	 */
	static Server newServer()
	{
		return new ServerImpl();
	}
	
	/**
	 * Creates a new server with the specified parameters.
	 *
	 * @param bufferSize the bufferSize used when allocating a {@link ByteBuffer}
	 * @param selectTimeout the timeout that will be used when waiting for data.
	 * @param selectArraySize the array size that select will use, the larger the more clients that
	 * can be handled in 1 select call
	 * @param threadsCount the amount of the {@link Server} will use with the selector, if
	 * threadsCount is 0 it will be set to {@link Runtime#availableProcessors()}
	 * @param implementation the implementation type of the selector if this is null {@link SelectorImplementation#NATIVE} will be used. More details in the {@link SelectorImplementation} javadoc.
	 * @return a new server with the specified parameters.
	 */
	static Server newServer(int bufferSize,
			int selectTimeout,
			int selectArraySize,
			int threadsCount,
			SelectorImplementation implementation)
	{
		return new ServerImpl(bufferSize, selectTimeout, selectArraySize, threadsCount, implementation);
	}
}