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

import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.util.IndexesBuffer;
import oughttoprevail.asyncnetwork.util.selector.LinuxMacSelector;
import oughttoprevail.asyncnetwork.util.SelectorImplementation;

public class ServerSocket extends SelectableServer
{
	/**
	 * Constructs a new {@link SelectableServer} with the default parameters.
	 * Sets the {@link SelectorImplementation} to {@link SelectorImplementation#NATIVE}.
	 */
	public ServerSocket()
	{
		super();
	}
	
	/**
	 * Constructs a new {@link ServerSocket} and uses the specified bufferSize when creating a {@link ServerSocket},
	 * uses the specified selectTimeout when using a {@code select} function,
	 * uses the specified selectArraySize when creating an {@link IndexesBuffer}
	 * and the specified threadsCount to know how many threads this should {@link ServerSocket} should use.
	 *
	 * @param bufferSize used when creating a new {@link oughttoprevail.asyncnetwork.server.ServerClientSocket}
	 * @param selectTimeout used when using a {@code select} function
	 * @param selectArraySize is <b>only</b> used for {@link LinuxMacSelector}.
	 * This will determine how many events {@link LinuxMacSelector} can pick up
	 * per {@code select} function call
	 * @param threadsCount how many threads will be used with this {@link ServerSocket}
	 * @param implementation the {@link SelectorImplementation} of this {@link ServerSocket}
	 */
	public ServerSocket(int bufferSize,
						int selectTimeout,
						int selectArraySize,
						int threadsCount,
						SelectorImplementation implementation)
	{
		super(bufferSize, selectTimeout, selectArraySize, threadsCount, implementation);
	}
	
	/**
	 * Returns a new initialized client.
	 *
	 * @param socketChannel the socket which will be transformed into S
	 * @param clientsIndex the index that will be used when removing the client when a client closes
	 * @return the new initialized {@link oughttoprevail.asyncnetwork.server.ServerClientSocket}
	 */
	@Override
	protected ServerClientSocket createServerClientSocket(SocketChannel socketChannel, int clientsIndex)
	{
		return new ServerClientSocket(this, socketChannel, clientsIndex)
		{
			protected boolean isWindowsImplementedServer()
			{
				return ServerSocket.this.manager().isWindowsImplementation();
			}
		};
	}
}
