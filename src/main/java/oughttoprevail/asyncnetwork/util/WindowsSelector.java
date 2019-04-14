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
import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.exceptions.LoadException;
import oughttoprevail.asyncnetwork.impl.util.ExceptionThrower;
import oughttoprevail.asyncnetwork.impl.util.selector.WindowsSelectorImpl;
import oughttoprevail.asyncnetwork.impl.util.writer.server.PendingWrite;

/**
 * Implementation at {@link WindowsSelectorImpl}.
 */
public interface WindowsSelector extends Closeable
{
	/**
	 * Creates a new selector that will transfer invocations into C++ and increase performance for Windows.
	 *
	 * @return a new selector that will transfer invocations into C++ and increase performance for Windows
	 */
	static WindowsSelector newWindowsSelector()
	{
		if(WindowsSelectorImpl.isImplemented())
		{
			try
			{
				return new WindowsSelectorImpl();
			} catch(LoadException e)
			{
				ExceptionThrower.throwException(e);
			}
		}
		return null;
	}
	
	/**
	 * Creates a new completion port handle with the specified threads count.
	 *
	 * @param serverSocket the server socket who is using this selector.
	 * @param threads threads count of the owner server
	 * @throws IOException if the call has failed
	 */
	void createSelector(int serverSocket, int threads) throws IOException;
	
	/**
	 * Registers the specified socket file descriptor to the selector.
	 *
	 * @param serverSocket the serverSocket who accepted the specified clientSocket file descriptor
	 * @param clientSocket the socket that will be used when registering the file descriptor
	 * @throws IOException if the call has failed
	 */
	void registerClient(int serverSocket, int clientSocket) throws IOException;
	
	/**
	 * Selects with the {@link WindowsSelector} handle, waits until an event has finished
	 * then outputs the result into the {@link ByteBuffer} memory address.
	 * The output will be the following:
	 * If an exception was thrown, nothing was changed.
	 * If the event is an accept operation then the first byte will be 2.
	 * If it's none of these then it is a read or write operation which
	 * sets the first byte to 1, second byte to whether this is a read or a write, 1
	 * if it is a read 0 if it is a write, then the next 4 bytes are the integer
	 * defining the index of the socket that the event
	 * occurred with in the {@link IndexedList}.
	 *
	 * @param timeout the timeout that will be used for the select call
	 * @param result {@link ByteBuffer} address of the output for this select.
	 * @return the {@link Runnable} defining the onWriteComplete if one exists, else null
	 * @throws IOException if the call has failed
	 */
	Object select(int timeout, long result) throws IOException;
	
	/**
	 * Gets the addresses from the specified bufferAddress and puts the address data and port in
	 * the specified socketAddressesAddress.
	 *
	 * @param socketAddressesAddress the buffer address in which the address data and port will be put in.
	 */
	void getAddress(long socketAddressesAddress);
	
	/**
	 * Accepts and returns a new socket from the specified serverSocket.
	 *
	 * @param serverSocket the server socket that the new client socket will be accepted from.
	 * @param index the index that will be saved as extra data (completion key) when registering
	 * @param threads the {@link oughttoprevail.asyncnetwork.Server} threads count that will be set
	 * @return the new accepted client socket.
	 */
	int AcceptEx(int serverSocket, int index, int threads);
	
	/**
	 * Receives specified length as bytes to the specified clientSocket which will be added to the
	 * specified readBufferAddress as the buffer.
	 *
	 * @param clientSocket the socket file descriptor
	 * @param readBufferAddress the address of the {@link ByteBuffer} from the {@link
	 * ByteBuffer#position()}
	 * @param length the amount of bytes that will be sent
	 * @throws IOException if the call has failed
	 */
	void WSARecv(int clientSocket, long readBufferAddress, int length) throws IOException;
	
	/**
	 * Sends specified length as bytes to the specified clientSocket using the specified
	 * writeBufferAddress as the buffer.
	 *
	 * @param clientSocket the socket file descriptor
	 * @param writeBufferAddress the address of the {@link ByteBuffer} from the {@link
	 * ByteBuffer#position()}
	 * @param pendingWrite to be called once the write has finished
	 * @param length the amount of bytes that will be sent
	 * @throws IOException if the call has failed
	 */
	void WSASend(int clientSocket, long writeBufferAddress, int length, PendingWrite pendingWrite) throws IOException;
	
	/**
	 * Closes the selector completion port handle.
	 *
	 * @throws IOException if the call has failed
	 */
	void close() throws IOException;
}