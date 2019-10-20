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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import oughttoprevail.asyncnetwork.exceptions.SelectException;
import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.util.IndexesBuffer;
import oughttoprevail.asyncnetwork.util.OS;
import oughttoprevail.asyncnetwork.util.SelectorImplementation;
import oughttoprevail.asyncnetwork.util.StatedCount;
import oughttoprevail.asyncnetwork.util.ThreadCreator;
import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.selector.LinuxMacSelector;
import oughttoprevail.asyncnetwork.util.selector.WindowsSelector;
import oughttoprevail.asyncnetwork.util.selector.flags.LinuxSelectorFlags;
import oughttoprevail.asyncnetwork.util.selector.flags.MacSelectorFlags;
import oughttoprevail.asyncnetwork.util.selector.flags.SelectorFlags;
import oughttoprevail.asyncnetwork.util.selector.flags.WindowsSelectorFlags;

public abstract class SelectableServer extends AbstractServer
{
	/**
	 * The {@link SelectorImplementation} of this {@link SelectableServer}.
	 */
	private SelectorImplementation implementation;
	
	/**
	 * Constructs a new {@link SelectableServer} with the default parameters.
	 * Sets the {@link SelectorImplementation} to {@link SelectorImplementation#NATIVE}.
	 */
	protected SelectableServer()
	{
		super();
		this.implementation = SelectorImplementation.NATIVE;
	}
	
	/**
	 * Constructs a new {@link SelectableServer} and uses the specified bufferSize when creating a {@link SelectableServer},
	 * uses the specified selectTimeout when using a {@code select} function,
	 * uses the specified selectArraySize when creating an {@link IndexesBuffer}
	 * and the specified threadsCount to know how many threads this should {@link SelectableServer} should use.
	 *
	 * @param bufferSize used when creating a new {@link ServerClientSocket}
	 * @param selectTimeout used when using a {@code select} function
	 * @param selectArraySize is <b>only</b> used for {@link LinuxMacSelector}.
	 * This will determine how many events {@link LinuxMacSelector} can pick up
	 * per {@code select} function call
	 * @param threadsCount how many threads will be used with this {@link SelectableServer}
	 * @param implementation the {@link SelectorImplementation} of this {@link SelectableServer}
	 */
	protected SelectableServer(int bufferSize, int selectTimeout, int selectArraySize, int threadsCount, SelectorImplementation implementation)
	{
		super(bufferSize, selectTimeout, selectArraySize, threadsCount);
		if(implementation == null)
		{
			implementation = SelectorImplementation.NATIVE;
		}
		this.implementation = implementation;
	}
	
	/**
	 * Creates a new selector which will be closed when the server is closed.
	 * The selector must be either {@link LinuxMacSelector} or {@link WindowsSelector} or {@link Selector} or a exception will be thrown.
	 *
	 * if {@link OS#LINUX} and {@link OS#MAC} aren't {@code true} it will be set to null
	 *
	 * @return the new closeable selector
	 */
	@Override
	protected Closeable createSelector()
	{
		switch(implementation)
		{
			case NATIVE:
			{
				if(LinuxMacSelector.isImplemented())
				{
					return newLinuxMacSelector(new LinuxMacSelector());
				} else if(WindowsSelector.isImplemented())
				{
					return newWindowsSelector(new WindowsSelector());
				} else
				{
					manager().exception(new IllegalStateException("Operating System doesn't have an implementation, using Java implementations"));
					return newJavaSelector();
				}
			}
			
			case JAVA:
			{
				return newJavaSelector();
			}
			
			case THREAD_PER_CLIENT:
			{
				newThreadPerClient();
				return null;
			}
			
			default:
			{
				manager().exception(new IllegalArgumentException("Failed to find selector implementation (" + implementation + ")!"));
				return newJavaSelector();
			}
		}
	}
	
	/**
	 * Returns the specified selector if no exception occurred
	 * else returns {@link #newJavaSelector()}.
	 * Makes a selector loop for the specified selector.
	 *
	 * @param selector {@link LinuxMacSelector} to make selector loop for
	 * @return the specified selector or if an exception occurred {@link #newJavaSelector()}
	 */
	private Closeable newLinuxMacSelector(LinuxMacSelector selector)
	{
		ServerSocketChannel serverSocketChannel = getServerChannel();
		int selectArraySize = getSelectArraySize();
		int selectTimeout = getSelectTimeout();
		try
		{
			selector.createSelector(Util.getFD(serverSocketChannel), selectArraySize);
			ThreadCreator.newThread("LinuxMacSelector", () ->
			{
				SelectorFlags selectorFlags;
				if(OS.LINUX)
				{
					selectorFlags = new LinuxSelectorFlags(this, getClientList());
				} else if(OS.MAC)
				{
					selectorFlags = new MacSelectorFlags(this, getClientList());
				} else
				{
					throw new UnsupportedOperationException("Selector created when OS can't be found. SelectableServer failed!");
				}
				//Allocate a buffer the size of the array size multiplied by Util.INT_BYTES * 2 because each array element should contain 2 integers.
				IndexesBuffer buffer = new IndexesBuffer(selectArraySize * (Util.INT_BYTES * 2));
				StatedCount count = new StatedCount();
				while(!isClosed())
				{
					try
					{
						int selected = selector.select(buffer.getAddress(), selectArraySize, selectTimeout);
						//check again. Maybe the server closed while selecting.
						if(isClosed())
						{
							break;
						}
						//if selected is 0 it means a timeout has occurred or if it is -1 it means an error has occurred.
						if(selected == -1 || selected == 0)
						{
							continue;
						}
						if(selected == 1)
						{
							int index = buffer.get();
							int flags = buffer.get();
							selectorFlags.call(index, flags, null);
						} else
						{
							count.set(selected);
							for(int i = 0; i < selected; i++)
							{
								int index = buffer.get();
								int flags = buffer.get();
								selectorFlags.call(index, flags, count);
							}
							count.await();
						}
						buffer.clear();
					} catch(IOException e)
					{
						manager().exception(e);
					}
				}
				buffer.close();
			});
			return selector;
		} catch(IOException e)
		{
			manager().exception(new IllegalStateException("Failed to copy native selector, using Java implementations", e));
			return newJavaSelector();
		}
	}
	
	/**
	 * Returns the specified selector.
	 * Makes a selector loop for the specified selector.
	 *
	 * @param selector {@link WindowsSelector} to make selector loop for
	 * @return the specified selector
	 */
	private Closeable newWindowsSelector(WindowsSelector selector)
	{
		int serverSocket = Util.getFD(getServerChannel());
		int threadsCount = getThreadsCount();
		try
		{
			selector.createSelector(serverSocket, threadsCount);
			IndexedList<ServerClientSocket> clients = getClientList();
			WindowsSelectorFlags flags = new WindowsSelectorFlags(this, clients, selector, serverSocket);
			ExecutorService executorService = manager().getExecutorService();
			for(int i = 0; i < threadsCount; i++)
			{
				executorService.execute(() ->
				{
					try(PooledByteBuffer pooledResult = new PooledByteBuffer(/*opcode*/Util.BYTE_BYTES + /*client index*/
					                                                                   Util.INT_BYTES + /*is read*/
					                                                                   Util.BYTE_BYTES + /*the received bytes*/
					                                                                   Util.INT_BYTES))
					{
						ByteBuffer result = pooledResult.getByteBuffer();
						result.order(ByteOrder.nativeOrder());
						int selectTimeout = getSelectTimeout();
						while(!isClosed())
						{
							try
							{
								Object finishedWrite = selector.select(selectTimeout, pooledResult.address());
								flags.select(result, finishedWrite);
							} catch(SelectException e)
							{
								int index = e.getIndex();
								ServerClientSocket client = clients.get(index);
								if(client == null)
								{
									manager().exception(e);
								} else
								{
									Validator.handleRemoteHostCloseException(client, e);
								}
							} catch(Throwable e)
							{
								Validator.exceptionClose(this, e);
							} finally
							{
								result.clear();
							}
						}
						flags.close();
					}
				});
			}
			flags.AcceptEx();
			return selector;
		} catch(IOException | NoSuchMethodException | ClassNotFoundException | NoSuchFieldException e)
		{
			manager().exception(new IllegalStateException("Failed to use windows selector, using Java implementations", e));
			return newJavaSelector();
		}
	}
	
	/**
	 * Returns a new {@link Selector}.
	 * Makes a selector loop for a new {@link Selector}.
	 *
	 * @return a new {@link Selector}
	 */
	private Closeable newJavaSelector()
	{
		try
		{
			implementation = SelectorImplementation.JAVA;
			ServerSocketChannel serverSocketChannel = getServerChannel();
			Selector javaSelector = Selector.open();
			serverSocketChannel.register(javaSelector, SelectionKey.OP_ACCEPT);
			int selectTimeout = getSelectTimeout();
			IndexedList<ServerClientSocket> clients = getClientList();
			ThreadCreator.newThread("JavaSelector", () ->
			{
				try
				{
					ExecutorService executorService = manager().getExecutorService();
					StatedCount count = new StatedCount();
					int javaSelectTimeout = selectTimeout == -1 ? 0 : selectTimeout;
					while(javaSelector.isOpen())
					{
						int selected = javaSelector.select(javaSelectTimeout);
						if(javaSelector.isOpen())
						{
							if(selected == 0)
							{
								continue;
							}
							boolean changeThreads = selected != 1;
							if(changeThreads)
							{
								count.set(selected);
							}
							Iterator<SelectionKey> iterator = javaSelector.selectedKeys().iterator();
							while(iterator.hasNext())
							{
								try
								{
									SelectionKey key = iterator.next();
									if(key.isValid())
									{
										if(key.isReadable())
										{
											try
											{
												ServerClientSocket client = (ServerClientSocket) key.attachment();
												Runnable runnable = () ->
												{
													client.manager().callRead();
													if(changeThreads)
													{
														count.countDown();
													}
												};
												if(changeThreads)
												{
													executorService.execute(runnable);
												} else
												{
													runnable.run();
												}
											} catch(ClassCastException e)
											{
												manager().exception(e);
											}
										} else if(key.isWritable())
										{
											try
											{
												ServerClientSocket client = (ServerClientSocket) key.attachment();
												Runnable runnable = () ->
												{
													if(!client.manager().callWrite())
													{
														key.interestOps(SelectionKey.OP_READ);
													}
													if(changeThreads)
													{
														count.countDown();
													}
												};
												if(changeThreads)
												{
													executorService.execute(runnable);
												} else
												{
													runnable.run();
												}
											} catch(ClassCastException e)
											{
												manager().exception(e);
											}
										} else if(key.isAcceptable())
										{
											try
											{
												SocketChannel socketChannel = serverSocketChannel.accept();
												if(socketChannel != null)
												{
													int clientsIndex = clients.index();
													ServerClientSocket client;
													try
													{
														client = initializeClient(socketChannel, clientsIndex);
													} catch(IOException e)
													{
														clients.fail(clientsIndex);
														manager().exception(e);
														return;
													}
													client.manager().setSelectionKey(client.getSocketChannel()
													                                       .register(javaSelector, SelectionKey.OP_READ, client));
													clients.add(clientsIndex, client);
													connected(client);
												}
											} catch(IOException e)
											{
												manager().exception(e);
											}
											if(changeThreads)
											{
												count.countDown();
											}
										}
									}
								} finally
								{
									iterator.remove();
								}
							}
							if(changeThreads)
							{
								count.await();
							}
						}
					}
				} catch(IOException e)
				{
					Validator.exceptionClose(this, e);
				}
			});
			return javaSelector;
		} catch(IOException e)
		{
			Validator.exceptionClose(this, e);
		}
		return null;
	}
	
	/**
	 * Creates a new {@link Thread} which handles connections and creates a new
	 * thread per connection.
	 */
	private void newThreadPerClient()
	{
		ServerSocketChannel server = getServerChannel();
		IndexedList<ServerClientSocket> clients = getClientList();
		ThreadCreator.newThread("Thread per client server", () ->
		{
			while(!isClosed())
			{
				try
				{
					SocketChannel client = server.accept();
					if(client != null)
					{
						ThreadCreator.newThread("Server-Client Thread", () ->
						{
							int index = clients.index();
							ServerClientSocket serverClient;
							try
							{
								serverClient = initializeClient(client, index);
							} catch(IOException e)
							{
								clients.fail(index);
								manager().exception(e);
								return;
							}
							clients.add(index, serverClient);
							connected(serverClient);
							while(!serverClient.isClosed())
							{
								serverClient.manager().callRead();
							}
						});
					}
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Returns the server's {@link SelectorImplementation}.
	 *
	 * @return the server's {@link SelectorImplementation}.
	 */
	public SelectorImplementation getSelectorImplementation()
	{
		return implementation;
	}
}