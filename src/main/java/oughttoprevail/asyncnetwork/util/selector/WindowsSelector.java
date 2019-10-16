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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import oughttoprevail.asyncnetwork.exceptions.LoadException;
import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.server.IndexedList;
import oughttoprevail.asyncnetwork.server.ServerClientSocket;
import oughttoprevail.asyncnetwork.server.ServerSocket;
import oughttoprevail.asyncnetwork.util.OS;
import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.util.writer.server.PendingWrite;

public class WindowsSelector implements Closeable
{
	private static boolean implemented;
	private static int addressSize;
	
	static
	{
		if(OS.WINDOWS)
		{
			implemented = NativeLoader.load("WindowsSelector", ".dll");
			if(implemented)
			{
				addressSize = getAddressSize0();
			}
		}
	}
	
	public static boolean isImplemented()
	{
		return implemented;
	}
	
	private long handle;
	private PooledByteBuffer pooledAddressesBuffer;
	private long addressesBufferAddress;
	
	public WindowsSelector() throws LoadException
	{
		if(!implemented)
		{
			NativeLoader.exception("WindowsSelector");
		}
	}
	
	/**
	 * Creates a new completion port handle with the specified threads count.
	 *
	 * @param serverSocket the server socket who is using this selector.
	 * @param threads threads count of the owner server
	 * @throws IOException if the call had failed
	 */
	public void createSelector(int serverSocket, int threads) throws IOException
	{
		handle = createSelector0(serverSocket, threads);
		pooledAddressesBuffer = new PooledByteBuffer(addressSize * 2);
		ByteBuffer addressesBuffer = pooledAddressesBuffer.getByteBuffer();
		addressesBuffer.order(ByteOrder.nativeOrder());
		addressesBufferAddress = Util.address(addressesBuffer);
	}
	
	/**
	 * Registers the specified socket file descriptor to the selector.
	 *
	 * @param serverSocket the serverSocket who accepted the specified clientSocket file descriptor
	 * @param clientSocket the socket that will be used when registering the file descriptor
	 */
	public void registerClient(int serverSocket, int clientSocket) throws IOException
	{
		registerClient0(serverSocket, clientSocket);
	}
	
	private static final int TIMEOUT_CODE = -2;
	private final IndexedList<PendingWrite> pendingWrites = new IndexedList<>();
	
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
	 */
	public Object select(int timeout, long result) throws IOException
	{
		int index;
		while((index = select0(handle, timeout, result)) == TIMEOUT_CODE)
			;
		if(index == -1)
		{
			return -1;
		}
		Object pendingWrite = pendingWrites.get(index);
		pendingWrites.remove(index);
		return pendingWrite;
	}
	
	/**
	 * Gets the addresses from the specified bufferAddress and puts the address data and port in
	 * the specified socketAddressesAddress.
	 *
	 * @param socketAddressesAddress the buffer address in which the address data and port will be put in.
	 */
	public void getAddress(long socketAddressesAddress)
	{
		getAddress0(addressesBufferAddress, socketAddressesAddress, addressSize);
	}
	
	/**
	 * Accepts and returns a new socket from the specified serverSocket.
	 *
	 * @param serverSocket the server socket that the new client socket will be accepted from.
	 * @param index the index that will be saved as extra data (completion key) when registering
	 * @param threads the {@link ServerSocket} threads count that will be set
	 * @return the new accepted client socket.
	 */
	public int AcceptEx(int serverSocket, int index, int threads)
	{
		return AcceptEx0(handle, serverSocket, index, threads, addressesBufferAddress, addressSize);
	}
	
	/**
	 * Closes the selector completion port handle.
	 *
	 * @throws IOException if closing the selector completion port handle failed
	 */
	@Override
	public void close() throws IOException
	{
		close0(handle);
		pooledAddressesBuffer.close();
	}
	
	/**
	 * Receives specified length as bytes to the specified clientSocket which will be added to the
	 * specified readBufferAddress as the buffer.
	 *
	 * @param clientSocket the socket file descriptor
	 * @param readBufferAddress the address of the {@link ByteBuffer} from the {@link
	 * ByteBuffer#position()}
	 * @param length the amount of bytes that will be sent
	 */
	public void WSARecv(int clientSocket, long readBufferAddress, int length) throws IOException
	{
		WSARecv0(clientSocket, readBufferAddress, length);
	}
	
	/**
	 * Sends specified length as bytes to the specified clientSocket using the specified
	 * writeBufferAddress as the buffer.
	 *
	 * @param clientSocket the socket file descriptor
	 * @param writeBufferAddress the address of the {@link ByteBuffer} from the {@link
	 * ByteBuffer#position()}
	 * @param pendingWrite to be called once the write has finished
	 * @param length the amount of bytes that will be sent
	 */
	public void WSASend(int clientSocket, long writeBufferAddress, int length, PendingWrite pendingWrite) throws IOException
	{
		int index = pendingWrites.index();
		pendingWrites.add(index, pendingWrite);
		WSASend0(clientSocket, writeBufferAddress, length, index);
	}
	
	/**
	 * Creates a new completion port handle with the specified threads count.
	 *
	 * @param serverSocket the server socket who is using this selector.
	 * @param threads the {@link ServerSocket} threads count that will be set
	 * when creating the completion port handle
	 * @return the completion port handle as long
	 * @throws IOException if the completion port has failed to copy
	 */
	private native long createSelector0(int serverSocket, int threads) throws IOException;
	
	/**
	 * Registers the client socket to the server socket.
	 *
	 * @param serverSocket the serverSocket who accepted the specified clientSocket file descriptor
	 * @param clientSocket the client (socket) file descriptor
	 * when registering the file descriptor to the completion port handle
	 * @throws IOException if the file descriptor (socket) failed to register to the completion port
	 * handle
	 */
	private native void registerClient0(int serverSocket, int clientSocket) throws IOException;
	
	/**
	 * Waits (blocks) until an event has occurred using
	 * {@code WINBOOL success = GetQueuedCompletionStatus(handle, receivedBytes, eventIndex, event, timeout)}
	 * if {@code success} is {@code false} then the {@code event} is freed ({@code free(event)})
	 * after the following checks if {@code WSAGetLastError()} is {@code WSA_WAIT_TIMEOUT} this is looped again
	 * else if it isn't {@code WSA_WAIT_TIMEOUT} then if the {@code event}
	 * is null then a {@link IOException} is thrown with the message "Failed GetQueuedCompletionStatus",
	 * else {@link oughttoprevail.asyncnetwork.exceptions.SelectException} is thrown with the {@code eventIndex}.
	 * If {@code success} is {@code true} then if the {@code event} is an accept {@code event} then the first byte
	 * of the result is set to 3 and null is returned else if it isn't an accept {@code event} then
	 * it must be a read or write {@code event} now whether this is a read or write {@code event} and pendingWrite is retrieved from the {@code event}.
	 * After this the {@code event} freed ({@code free(event)}).
	 * Now the first byte is set to 1, second byte to 1 if it is a read event and 0 if it is a write event, the next 4 bytes (third to seventh)
	 * are set to the integer value of the index that the {@link ServerClientSocket} is located in
	 * the {@link IndexedList}.
	 *
	 * @param handle the completion port handle value
	 * @param timeout the timeout until to return timeout.
	 * @throws IOException if {@code GetQueuedCompletionStatus} returned false which means it didn't finish
	 * successfully and the {@code event} (overlapped) is null
	 * @throws oughttoprevail.asyncnetwork.exceptions.SelectException if {@code GetQueuedCompletionStatus)
	 * returned false which means it didn't finish successfully and the {@code event} (overlapped) isn't null
	 */
	private native int select0(long handle, int timeout, long result) throws IOException;
	
	/**
	 * Closes the completion port handle.
	 *
	 * @param handle the completion port handle that will be closed
	 * @throws IOException if close operation failed
	 */
	private native void close0(long handle) throws IOException;
	
	/**
	 * Returns the size of a single address.
	 *
	 * @return the size of the a single address
	 */
	private static native int getAddressSize0();
	
	/**
	 * Gets the addresses from the specified bufferAddress and puts the address data and port in
	 * the specified socketAddressesAddress.
	 *
	 * @param bufferAddress the buffer address in which the addresses are in
	 * @param socketAddressesAddress the buffer address in which the address data and port will be put in.
	 * @param addressSize the integer returned from {@link #getAddressSize0()}
	 */
	private native void getAddress0(long bufferAddress, long socketAddressesAddress, int addressSize);
	
	/**
	 * Accepts and returns a new socket from the specified serverSocket.
	 *
	 * @param handle the completion port handle
	 * @param serverSocket the server socket that the new client socket will be accepted from.
	 * @param index the index that will be saved as extra data (completion key) when registering
	 * @param threads the {@link ServerSocket} threads count that will be set
	 * @param bufferAddress the buffer address in which the new socket addresses will go
	 * @return the new accepted client socket.
	 */
	private native int AcceptEx0(long handle, int serverSocket, int index, int threads, long bufferAddress, int addressSize);
	
	/**
	 * Receives specified length as bytes to the specified clientSocket which will be added to the
	 * specified readBufferAddress as the buffer.
	 *
	 * @param clientSocket the socket file descriptor
	 * @param readBufferAddress the address of the {@link ByteBuffer} from the {@link
	 * ByteBuffer#position()}
	 * @param length the amount of bytes that will be sent
	 */
	private native void WSARecv0(int clientSocket, long readBufferAddress, int length) throws IOException;
	
	/**
	 * Sends specified length as bytes to the specified clientSocket using the specified
	 * writeBufferAddress as the buffer.
	 *
	 * @param clientSocket the socket file descriptor
	 * @param writeBufferAddress the address of the {@link ByteBuffer} from the {@link
	 * ByteBuffer#position()}
	 * @param length the amount of bytes that will be sent
	 * @param pendingWrite will be called once the WSASend has finished
	 */
	private native void WSASend0(int clientSocket, long writeBufferAddress, int length, int pendingWrite) throws IOException;
}
