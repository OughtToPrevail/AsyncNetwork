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
package oughttoprevail.asyncnetwork.util.selector.flags;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.server.AbstractServer;
import oughttoprevail.asyncnetwork.server.IndexedList;
import oughttoprevail.asyncnetwork.server.ServerClientSocket;
import oughttoprevail.asyncnetwork.util.StatedCount;
import oughttoprevail.asyncnetwork.util.selector.LinuxMacSelector;
import oughttoprevail.asyncnetwork.util.Consumer;

public class SelectorFlags
{
	/**
	 * Does an AND operation and returns whether the result isn't 0.
	 *
	 * @param flags the flags to check in the AND operation
	 * @param i the integer that the flags will be checked with in the AND operation
	 * @return whether the AND operation returned 0
	 */
	private static boolean ANDOperator(int flags, int i)
	{
		return (flags & i) != 0;
	}
	
	private final AbstractServer server;
	private final ExecutorService executorService;
	private final IndexedList<ServerClientSocket> clients;
	private final int read;
	private final int write;
	
	SelectorFlags(AbstractServer server, IndexedList<ServerClientSocket> clients, int read, int write)
	{
		this.server = server;
		this.clients = clients;
		this.read = read;
		this.write = write;
		executorService = server.manager().getExecutorService();
	}
	
	/**
	 * If the specified count isn't null then you switch a thread and handle correctly the specified flag
	 * while using the specified index as a {@link ServerClientSocket}.
	 *
	 * @param index the index of the socket who got selected or -5 if it was an accept call.
	 * @param flags the flags of the select call.
	 * @param count the count of how many invocations have finished, if null it means that this is the only call for this select.
	 */
	public void call(int index, int flags, StatedCount count)
	{
		if(ANDOperator(flags, read))
		{
			readCalled(executorService, index, count);
		} else if(ANDOperator(flags, write))
		{
			writeCalled(executorService, index, count);
		}
	}
	
	private void execute(ExecutorService executor, Consumer<ServerClientSocket> consumer, int index, StatedCount count)
	{
		ServerClientSocket client = index == -5 ? null : server.getClients().get(index);
		if(count != null)
		{
			executor.execute(() ->
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
	 * @param executor the executorService of the selector
	 * @param index the index of the socket in which the read event occurred
	 * @param count the count which will be {@link StatedCount#countDown()} once finishes
	 */
	private void readCalled(ExecutorService executor, int index, StatedCount count)
	{
		execute(executor, client ->
		{
			if(client == null)
			{
				try
				{
					SocketChannel socketChannel = server.getServerChannel().accept();
					if(socketChannel == null)
					{
						return;
					}
					int clientsIndex = clients.index();
					try
					{
						client = server.initializeClient(socketChannel, clientsIndex);
					} catch(IOException e)
					{
						clients.fail(clientsIndex);
						server.manager().exception(e);
						return;
					}
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
							server.connected(client);
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
	 * @param executor the executorService of the selector
	 * @param index the index of the socket in which the write event occurred
	 * @param count the count which will be {@link StatedCount#countDown()} once finishes
	 */
	private void writeCalled(ExecutorService executor, int index, StatedCount count)
	{
		execute(executor, client -> client.manager().callWrite(), index, count);
	}
}
