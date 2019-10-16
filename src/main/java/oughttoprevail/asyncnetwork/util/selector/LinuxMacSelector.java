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
package oughttoprevail.asyncnetwork.util.selector;

import java.io.Closeable;
import java.io.IOException;

import oughttoprevail.asyncnetwork.exceptions.LoadException;
import oughttoprevail.asyncnetwork.server.IndexedList;
import oughttoprevail.asyncnetwork.util.IndexesBuffer;
import oughttoprevail.asyncnetwork.util.OS;

public class LinuxMacSelector implements Closeable
{
	private static boolean implemented;
	
	static
	{
		if(OS.LINUX)
		{
			implemented = NativeLoader.load("LinuxSelector", ".so");
		} else if(OS.MAC)
		{
			implemented = NativeLoader.load("MacSelector", ".dylib");
		}
	}
	
	public static boolean isImplemented()
	{
		return implemented;
	}
	
	private int fd;
	private long arrayAddress;
	
	public LinuxMacSelector() throws LoadException
	{
		if(!implemented)
		{
			NativeLoader.exception("LinuxMacSelector");
		}
	}
	
	/**
	 * Creates a file descriptor for the selector and registers the server for accept connections
	 * using the specified serverFd and also creates an array for the select invocations.
	 *
	 * @param serverFd the file descriptor that will be used when registering the server for accept
	 * connections
	 * @param arraySize the arraySize that will be used for creating an array for select invocations
	 */
	public void createSelector(int serverFd, int arraySize) throws IOException
	{
		fd = createSelector0(serverFd);
		if(fd == -1)
		{
			return;
		}
		this.arrayAddress = createArray0(arraySize);
	}
	
	/**
	 * Registers the specified socket file descriptor to the selector.
	 *
	 * @param socketFd the socket that will be used when registering the file descriptor
	 * @param index the index of the specified socket file descriptor in the {@link IndexedList}
	 */
	public void registerClient(int socketFd, int index) throws IOException
	{
		registerClient0(this.fd, socketFd, index);
	}
	
	/**
	 * Selects with the {@link LinuxMacSelector} array, the specified {@link IndexesBuffer} address
	 * and the specified timeout to call operating system dependent select.
	 *
	 * @param indexesAddress the {@link IndexesBuffer#getAddress()}
	 * @param arraySize the array size that the {@link LinuxMacSelector} will use when reading the
	 * {@link LinuxMacSelector} array
	 * @param timeout the timeout that will be used for the select call
	 * @return the amount of indexes set to the {@link IndexesBuffer} or -1 if an error occurred
	 */
	public int select(long indexesAddress, int arraySize, int timeout) throws IOException
	{
		return select0(fd, indexesAddress, arrayAddress, arraySize, timeout);
	}
	
	/**
	 * Closes the selector.
	 *
	 * @throws IOException if an error has occurred while closing the selector
	 */
	public void close() throws IOException
	{
		close0(fd);
	}
	
	/**
	 * Creates the selector file descriptor and registers the specified serverFd.
	 *
	 * @param serverFd the serverSocket file descriptor that will be registered to the new selector file descriptor
	 * @return the new selector file descriptor
	 * @throws IOException if an exception has occurred while performing this operation
	 */
	private native int createSelector0(int serverFd) throws IOException;
	
	/**
	 * Creates an array of events that can be put in {@link #select(long, int, int)} as the eventAddress.
	 *
	 * @param size the size of the events array
	 * @return the array of events address
	 */
	private native long createArray0(int size);
	
	/**
	 * Registers the specified socket file descriptor to the specified selectorFd
	 * and saves the specified index with it.
	 *
	 * @param selectorFd the selector file descriptor which the socket will be registered to
	 * @param socketFd the socket file descriptor which will be registered
	 * @param index the index where the socket is saved
	 * @throws IOException if an exception has occurred while performing this operation
	 */
	private native void registerClient0(int selectorFd, int socketFd, int index) throws IOException;
	
	/**
	 * Blocks until it can find ready sockets or timeouts then puts the each socket index in the indexesAddress.
	 *
	 * @param selectorFd the selector file descriptor
	 * @param indexesAddress the {@link IndexesBuffer} address
	 * @param eventsAddress the events array address
	 * @param eventsSize the events array size
	 * @param timeout the timeout for this select call
	 * @return how many indexes were put in the {@link IndexesBuffer}
	 * @throws IOException if an exception has occurred while performing this operation
	 */
	private native int select0(int selectorFd, long indexesAddress, long eventsAddress, int eventsSize, int timeout) throws IOException;
	
	/**
	 * Closes the selector file descriptor.
	 *
	 * @param selectorFd the selector file descriptor that will be closed.
	 * @throws IOException if an error has occurred while closing the selector
	 */
	private native void close0(int selectorFd) throws IOException;
}
