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
package oughttoprevail.asyncnetwork.util;

import java.io.Closeable;
import java.io.IOException;

import oughttoprevail.asyncnetwork.exceptions.LoadException;
import oughttoprevail.asyncnetwork.impl.util.ExceptionThrower;
import oughttoprevail.asyncnetwork.impl.util.selector.LinuxMacSelectorImpl;

/**
 * Implementation at {@link LinuxMacSelectorImpl}.
 */
public interface LinuxMacSelector extends Closeable
{
	/**
	 * Creates a new selector that will transfer calls into C++ and increase performance for Linux/Mac
	 * decided by {@link OS#LINUX} if true it will use Linux or if {@link OS#MAC} if true it will use
	 * Mac.
	 *
	 * @return a new selector that will transfer calls into C++ and increase performance for Windows
	 */
	static LinuxMacSelector newSelector()
	{
		if(LinuxMacSelectorImpl.isImplemented())
		{
			try
			{
				return new LinuxMacSelectorImpl();
			} catch(LoadException e)
			{
				ExceptionThrower.throwException(e);
			}
		}
		return null;
	}
	
	/**
	 * Creates a file descriptor for the selector and registers the server for accept connections
	 * using the specified serverFd and also creates an array for the select calls.
	 *
	 * @param serverFd the file descriptor that will be used when registering the server for accept
	 * connections
	 * @param arraySize the arraySize that will be used for creating an array for select calls
	 * @throws IOException if the call had failed
	 */
	void createSelector(int serverFd, int arraySize) throws IOException;
	
	/**
	 * Registers the specified socket file descriptor to the selector.
	 *
	 * @param fd the socket that will be used when registering the file descriptor
	 * @param index the index of the specified socket file descriptor in the {@link IndexedList}
	 * @throws IOException if the call had failed
	 */
	void registerClient(int fd, int index) throws IOException;
	
	/**
	 * Selects with the {@link LinuxMacSelector} array, the specified {@link IndexesBuffer} address
	 * and the specified timeout to call operating system dependent select.
	 *
	 * @param indexesAddress the {@link IndexesBuffer#getAddress()}
	 * @param arraySize the array size that the {@link LinuxMacSelector} will use when reading the
	 * {@link LinuxMacSelector} array
	 * @param timeout the timeout that will be used for the select call
	 * @return the amount of indexes setValue to the {@link IndexesBuffer} or -1 if an error occurred
	 * @throws IOException if the call had failed
	 */
	int select(long indexesAddress, int arraySize, int timeout) throws IOException;
}