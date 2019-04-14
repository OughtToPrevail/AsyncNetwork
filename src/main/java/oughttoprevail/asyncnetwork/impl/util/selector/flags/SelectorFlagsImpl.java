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
package oughttoprevail.asyncnetwork.impl.util.selector.flags;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import oughttoprevail.asyncnetwork.IServerClient;
import oughttoprevail.asyncnetwork.impl.Util;
import oughttoprevail.asyncnetwork.impl.server.AbstractServer;
import oughttoprevail.asyncnetwork.impl.util.StatedCount;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.IndexedList;
import oughttoprevail.asyncnetwork.util.LinuxMacSelector;
import oughttoprevail.asyncnetwork.util.OS;
import oughttoprevail.asyncnetwork.util.SelectorFlags;

public abstract class SelectorFlagsImpl<S extends IServerClient> implements SelectorFlags
{
	private final AbstractServer<?, S> server;
	private final ExecutorService executor;
	private final IndexedList<S> clients;
	private final int read;
	private final int write;
	
	SelectorFlagsImpl(AbstractServer<?, S> server, IndexedList<S> clients, int read, int write)
	{
		this.server = server;
		this.clients = clients;
		this.read = read;
		this.write = write;
		executor = OS.ANDROID ? Executors.newFixedThreadPool(server.getThreadsCount()) : Executors.newWorkStealingPool(server.getThreadsCount());
	}
	
	/**
	 * If the specified count isn't null then you switch a thread and handle correctly the specified flag
	 * while using the specified index as a {@link oughttoprevail.asyncnetwork.IServerClient}.
	 *
	 * @param index the index of the channel who got selected or -5 if it was an accept call.
	 * @param flags the flags of the select call.
	 * @param count the count of how many invocations have finished, if null it means that this is the only call for this select.
	 */
	@Override
	public void call(int index, int flags, StatedCount count)
	{
		if(SelectorFlags.ANDOperator(flags, read))
		{
			readCalled(executor, index, count);
		} else if(SelectorFlags.ANDOperator(flags, write))
		{
			writeCalled(executor, index, count);
		}
	}
	
	private void submit(ExecutorService executor, Consumer<S> consumer, int index, StatedCount count)
	{
		S client = index == -5 ? null : server.getClients().get(index);
		if(count != null)
		{
			executor.submit(() ->
			{
				consumer.accept(client);
				count.countDown();
			});
		} else
		{
			consumer.accept(client);
		}
	}
	
	/**
	 * Gets called if a read event has occurred to a selector and needs to be handled.
	 *
	 * @param executor the executor of the selector
	 * @param index the index of the channel in which the read event occurred
	 * @param count the count which will be {@link StatedCount#countDown()} once finishes
	 */
	private void readCalled(ExecutorService executor, int index, StatedCount count)
	{
		submit(executor, client ->
		{
			if(client == null)
			{
				try
				{
					SocketChannel socketChannel = server.getServerChannel().accept();
					int clientsIndex = clients.index();
					client = initializeClient(socketChannel, clientsIndex);
					if(client == null)
					{
						clients.fail(clientsIndex);
					} else
					{
						clients.add(clientsIndex, client);
						int fd = Util.getFD(client.getSocketChannel());
						LinuxMacSelector selector = (LinuxMacSelector) server.manager().getSelector();
						try
						{
							selector.registerClient(fd, clientsIndex);
							connected(client);
						} catch(IOException e)
						{
							client.close();
							server.manager().exception(e);
						}
					}
				} catch(IOException e)
				{
					server.manager().exception(e);
				}
			} else
			{
				client.manager().callRead();
			}
		}, index, count);
	}
	
	/**
	 * Gets called if a write event has occurred to a selector and needs to be handled.
	 *
	 * @param executor the executor of the selector
	 * @param index the index of the channel in which the write event occurred
	 * @param count the count which will be {@link StatedCount#countDown()} once finishes
	 */
	private void writeCalled(ExecutorService executor, int index, StatedCount count)
	{
		submit(executor, client -> client.manager().callWrite(), index, count);
	}
	
	protected abstract S initializeClient(SocketChannel channel, int clientsIndex);
	
	protected abstract void connected(S client);
}
