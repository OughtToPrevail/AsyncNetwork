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

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.DisconnectionType;
import oughttoprevail.asyncnetwork.util.OS;

public class SocketManager
{
	private final Socket socket;
	private final PooledByteBuffer readByteBuffer;
	
	public SocketManager(Socket socket, PooledByteBuffer readByteBuffer)
	{
		this.socket = socket;
		this.readByteBuffer = readByteBuffer;
	}
	
	public void init() throws IOException
	{
		SocketChannel socketChannel = socket.getSocketChannel();
		int bufferSize = socket.getBufferSize();
		if(OS.ANDROID)
		{
			java.net.Socket socket = socketChannel.socket();
			socket.setSendBufferSize(bufferSize);
			socket.setReceiveBufferSize(bufferSize);
		} else
		{
			socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, bufferSize);
			socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
		}
	}
	
	/**
	 * Closes the socket and invokes {@link Socket#onDisconnect(Consumer)} specified consumer with the
	 * specified disconnectionType.
	 * If specified urgent is set to true then pending write operations will not continue else
	 * close call will block until write operations have finished.
	 *
	 * @param disconnectionType the disconnectionType that will be used when calling the {@link
	 * Socket#onDisconnect(Consumer)} specified consumer
	 */
	public void close(DisconnectionType disconnectionType)
	{
		socket.close(disconnectionType);
	}
	
	/**
	 * Returns the socket's read buffer.
	 *
	 * @return the socket's read buffer
	 */
	public PooledByteBuffer getReadByteBuffer()
	{
		return readByteBuffer;
	}
	
	/**
	 * Invokes the socket's {@link Socket#onException(Consumer)} consumer with the specified
	 * exception.
	 *
	 * @param throwable the exception that the socket's {@link Socket#onException(Consumer)}
	 * consumer will be called with
	 */
	public void exception(Throwable throwable)
	{
		socket.exception(throwable);
	}
	
	/**
	 * Invokes the socket's {@link Socket#onBufferOverflow(Consumer)} consumer with the specified
	 * byteBuffer.
	 *
	 * @param byteBuffer the {@link ByteBuffer} that the socket's {@link Socket#onBufferOverflow(Consumer)}
	 * consumer will be called with
	 */
	public void bufferOverflow(ByteBuffer byteBuffer)
	{
		socket.bufferOverflow(byteBuffer);
	}
	
	/**
	 * Reads pending socket reads with the readBuffer and invokes the pending read consumers.
	 */
	public void callRead()
	{
		socket.callRead();
	}
	
	/**
	 * Invokes the {@link Socket#onRead(Consumer)} consumer with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer the {@link ByteBuffer} which will be used when calling the onRead conusmer
	 */
	public void callOnRead(ByteBuffer byteBuffer)
	{
		socket.callOnRead(byteBuffer);
	}
	
	public void finishWrite(Consumer<ByteBuffer> onWriteFinished, ByteBuffer writeBuffer)
	{
		if(socket.isClearAfterWrite())
		{
			writeBuffer.clear();
		} else
		{
			writeBuffer.limit(writeBuffer.capacity());
		}
		if(onWriteFinished != null)
		{
			onWriteFinished.accept(writeBuffer);
		}
	}
}