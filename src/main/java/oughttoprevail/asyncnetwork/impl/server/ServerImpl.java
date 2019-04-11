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

import java.io.IOException;
import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.IServerClient;
import oughttoprevail.asyncnetwork.SelectorImplementation;
import oughttoprevail.asyncnetwork.Server;
import oughttoprevail.asyncnetwork.ServerClient;
import oughttoprevail.asyncnetwork.util.LinuxMacSelector;

public class ServerImpl extends SelectableServer<Server, ServerClient> implements Server
{
	/**
	 * Returns the extending class.
	 *
	 * @return the extending class
	 */
	@Override
	protected Server getThis()
	{
		return this;
	}
	
	/**
	 * Constructs a new {@link SelectableServer} with the default parameters.
	 * Sets the {@link SelectorImplementation} to {@link SelectorImplementation#NATIVE}.
	 */
	public ServerImpl()
	{
		super();
	}
	
	/**
	 * Constructs a new {@link ServerImpl} and uses the specified bufferSize when creating a {@link ServerImpl},
	 * uses the specified selectTimeout when using a {@code select} function,
	 * uses the specified selectArraySize when creating an {@link oughttoprevail.asyncnetwork.util.IndexesBuffer}
	 * and the specified threadsCount to know how many threads this should {@link ServerImpl} should use.
	 *
	 * @param bufferSize used when creating a new {@link IServerClient}
	 * @param selectTimeout used when using a {@code select} function
	 * @param selectArraySize is <b>only</b> used for {@link LinuxMacSelector}.
	 * This will determine how many events {@link LinuxMacSelector} can pick up
	 * per {@code select} function call
	 * @param threadsCount how many threads will be used with this {@link ServerImpl}
	 * @param implementation the {@link SelectorImplementation} of this {@link ServerImpl}
	 */
	public ServerImpl(int bufferSize,
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
	 * @param channel the channel which will be transformed into S
	 * @param clientsIndex the index that will be used when removing the client when a client closes
	 * @return the new initialized {@link ServerClient}
	 */
	@Override
	protected ServerClient initializeClient(SocketChannel channel, int clientsIndex)
	{
		try
		{
			if(channel == null)
			{
				return null;
			}
			Channel.initializeDefaultOptions(channel, getBufferSize());
			if(getSelectorImplementation() != SelectorImplementation.THREAD_PER_CLIENT)
			{
				channel.configureBlocking(false);
			}
			return new ServerClientImpl(this, channel, clientsIndex)
			{
				protected boolean isWindowsImplementedServer()
				{
					return ServerImpl.this.manager().isWindowsImplementation();
				}
			};
		} catch(IOException e)
		{
			manager().exception(e);
			return null;
		}
	}
}
