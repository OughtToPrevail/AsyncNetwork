/*
Copyright 2019 https://github.com/OughtToPrevail

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUvoid WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package oughttoprevail.asyncnetwork;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import oughttoprevail.asyncnetwork.client.ClientSocket;
import oughttoprevail.asyncnetwork.exceptions.SocketClosedException;
import oughttoprevail.asyncnetwork.packet.OpcodePacketBuilder;
import oughttoprevail.asyncnetwork.packet.ReadablePacket;
import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.server.ServerSocket;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.DisconnectionType;
import oughttoprevail.asyncnetwork.util.Predicate;
import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.reader.Reader;
import oughttoprevail.asyncnetwork.util.writer.Writer;

public abstract class Socket
{
	/**
	 * Local host address.
	 */
	public static final String LOCAL_ADDRESS = "127.0.0.1";
	
	/**
	 * Default buffer size.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	
	private final int bufferSize;
	private PooledByteBuffer pooledReadBuffer;
	private ByteBuffer readBuffer;
	
	private final Reader reader;
	private final Writer writer;
	
	protected Socket(int bufferSize, Reader reader, Writer writer)
	{
		this.bufferSize = bufferSize;
		this.pooledReadBuffer = new PooledByteBuffer(bufferSize);
		this.readBuffer = pooledReadBuffer.getByteBuffer();
		this.reader = reader;
		this.writer = writer;
	}
	
	protected ClientSocketManager createClientManager()
	{
		return new ClientSocketManager(this, pooledReadBuffer);
	}
	
	protected ServerClientManager createServerClientManager(ServerSocket server)
	{
		return new ServerClientManager(this, pooledReadBuffer)
		{
			@Override
			public ServerSocket getServer()
			{
				return server;
			}
			
			@Override
			public boolean callWrite()
			{
				return writer.continueWriting();
			}
			
			@Override
			public void callRequests()
			{
				reader.callRequests(readBuffer);
			}
		};
	}
	
	void close(DisconnectionType disconnectionType)
	{
		if(!preClose(disconnectionType))
		{
			return;
		}
		synchronized(closed)
		{
			if(closed.compareAndSet(null, disconnectionType))
			{
				try
				{
					getSocketChannel().close();
					pooledReadBuffer.close();
					//remove all variables for memory and to make sure none get invoked after the socket has closed
					pooledReadBuffer = null;
					readBuffer = null;
					onException.clear();
					onBufferOverflow.clear();
					for(Consumer<DisconnectionType> disconnectConsumer : onDisconnect)
					{
						disconnectConsumer.accept(disconnectionType);
					}
				} catch(IOException e)
				{
					manager().exception(e);
				}
			}
		}
	}
	
	void exception(Throwable throwable)
	{
		Util.exception(onException, throwable);
	}
	
	void bufferOverflow(ByteBuffer byteBuffer)
	{
		if(onBufferOverflow.isEmpty())
		{
			byteBuffer.clear();
		} else
		{
			for(Consumer<ByteBuffer> bufferOverflowConsumer : onBufferOverflow)
			{
				bufferOverflowConsumer.accept(byteBuffer);
			}
		}
	}
	
	void callRead()
	{
		reader.read(this);
	}

	/**
	 * Invoked before the closed has occurred, this is useful for extending classes to make final changes.
	 *
	 * @param disconnectionType is the reason why the disconnection should occur
	 * @return whether the close should continue, if {@code true} the close will continue if
	 * {@code false} the close will stop
	 */
	protected boolean preClose(DisconnectionType disconnectionType)
	{
		return true;
	}
	
	private boolean clearAfterWrite = true;
	
	/**
	 * Sets whether the socket's write buffer will be cleared after finishing the write. if the
	 * specified clearAfterWrite is true the write buffer will be cleared if false it will not be
	 * cleared.
	 *
	 * @param clearAfterWrite whether the socket's write buffer will be cleared after finishing the
	 * write. if true the write buffer will be cleared if false it will not be cleared
	 */
	public void setClearAfterWrite(boolean clearAfterWrite)
	{
		this.clearAfterWrite = clearAfterWrite;
	}
	
	/**
	 * Returns whether the socket will clear the write buffer after finishing write.
	 *
	 * @return whether the socket will clear the write buffer after finishing write
	 */
	public boolean isClearAfterWrite()
	{
		return clearAfterWrite;
	}
	
	private void ensureCanWrite()
	{
		ensureNotClosed();
		if(!isConnected())
		{
			throw new NotYetBoundException();
		}
	}
	
	/**
	 * Writes the specified buffer to this socket's {@link SocketChannel} and once
	 * the write has finished the specified onWriteFinished is invoked with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer to write to socket
	 * @throws SocketClosedException throws {@link SocketClosedException} if the socket is closed
	 */
	public void write(ByteBuffer byteBuffer)
	{
		write(byteBuffer, null);
	}
	
	/**
	 * Writes the specified buffer to this socket's {@link SocketChannel} and once
	 * the write has finished the specified onWriteFinished is invoked with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer to write to socket
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link ClientSocket} to prevent {@link StackOverflowError}
	 * @throws SocketClosedException throws {@link SocketClosedException} if the socket is closed
	 */
	public void write(ByteBuffer byteBuffer, Consumer<ByteBuffer> onWriteFinished)
	{
		ensureCanWrite();
		int bytes = byteBuffer.position();
		if(bytes != 0)
		{
			byteBuffer.flip();
			writer.write(this, byteBuffer, onWriteFinished);
		}
	}
	
	private void ensureCanRead(Consumer consumer)
	{
		Validator.requireNonNull(consumer, "Consumer");
	}
	
	private void ensureCanRead(Predicate predicate)
	{
		ensureNotClosed();
		Validator.requireNonNull(predicate, "Predicate");
	}
	
	private boolean always;
	
	/**
	 * Invokes the specified consumer with the socket's read {@link ByteBuffer} when {@link
	 * ByteBuffer#remaining()} returns the specified length.
	 *
	 * @param consumer the consumer that will be called with the socket's read {@link ByteBuffer}
	 * when {@link ByteBuffer#remaining()} returns the specified length
	 * @param length the amount of bytes that will be received
	 * @throws SocketClosedException throws {@link SocketClosedException} if the socket is closed
	 */
	public void readByteBuffer(Consumer<ByteBuffer> consumer, int length)
	{
		ensureCanRead(consumer);
		boolean thisAlways = always;
		always = false;
		readByteBufferUntil(byteBuffer ->
		{
			consumer.accept(byteBuffer);
			return thisAlways;
		}, length);
	}
	
	/**
	 * Sets the next {@link #readByteBuffer(Consumer, int)} repeat until the {@link Socket}
	 * is closed.
	 *
	 * @param always whether next {@link #readByteBuffer(Consumer, int)} should repeat until the {@link Socket}
	 * is closed
	 */
	public void always(boolean always)
	{
		this.always = always;
	}
	
	/**
	 * Invokes the specified predicate with the socket's read {@link ByteBuffer} when {@link
	 * ByteBuffer#remaining()} returns the specified length until the specified predicate
	 * returns {@code false}.
	 *
	 * @param predicate the predicate that will be called with the socket's read {@link ByteBuffer}
	 * when {@link ByteBuffer#remaining()} returns the specified length
	 * @param length the amount of bytes that will be received
	 * @throws SocketClosedException throws {@link SocketClosedException} if the socket is closed
	 */
	public void readByteBufferUntil(Predicate<ByteBuffer> predicate, int length)
	{
		ensureCanRead(predicate);
		Validator.higherThan0(length, "Request length");
		if(length > bufferSize)
		{
			manager().exception(new IndexOutOfBoundsException("Request length (request length: " +
															  length +
															  ") larger than the read buffer size (" +
															  bufferSize +
															  ")!"));
			return;
		}
		reader.addRequest(readBuffer, predicate, length);
	}
	
	/**
	 * Returns the {@link ByteBuffer} size.
	 *
	 * @return the {@link ByteBuffer} size
	 */
	public int getBufferSize()
	{
		return bufferSize;
	}
	
	private final List<Consumer<DisconnectionType>> onDisconnect = new ArrayList<>();
	
	/**
	 * Invokes the specified runnable when the client disconnects.
	 *
	 * @param onDisconnect the runnable that will get called when the client disconnects
	 */
	public void onDisconnect(Consumer<DisconnectionType> onDisconnect)
	{
		Validator.requireNonNull(onDisconnect, "onDisconnect");
		synchronized(closed)
		{
			DisconnectionType disconnectionType = closed.get();
			if(disconnectionType != null)
			{
				onDisconnect.accept(disconnectionType);
			}
			this.onDisconnect.add(onDisconnect);
		}
	}
	
	private void ensureNotClosed()
	{
		if(isClosed())
		{
			throw new SocketClosedException();
		}
	}
	
	private final AtomicReference<DisconnectionType> closed = new AtomicReference<>();
	
	/**
	 * If the socket has yet to be closed the method closes the {@link SocketChannel} and invokes the
	 * onDisconnect that is specified by {@link Socket#onDisconnect(Consumer)}.
	 */
	public void close()
	{
		manager().close(DisconnectionType.USER_CLOSE);
	}
	
	private final List<Consumer<Throwable>> onException = new ArrayList<>();
	
	/**
	 * Invokes the specified consumer with a throwable when a caught exception occurs.
	 *
	 * @param onException the consumer that will be called with the caught exceptions
	 */
	public void onException(Consumer<Throwable> onException)
	{
		this.onException.add(onException);
	}
	
	private final List<Consumer<ByteBuffer>> onBufferOverflow = new ArrayList<>();
	
	/**
	 * Invokes the specified consumer when the {@link Socket} read {@link ByteBuffer} needs more space
	 * and must be cleared.
	 *
	 * @param onBufferOverflow the consumer that would be called when a socket's read {@link
	 * ByteBuffer} needs more space and must be cleared
	 */
	public void onBufferOverflow(Consumer<ByteBuffer> onBufferOverflow)
	{
		this.onBufferOverflow.add(onBufferOverflow);
	}
	
	/**
	 * Returns whether the socket is closed.
	 *
	 * @return whether the socket is closed
	 */
	public boolean isClosed()
	{
		synchronized(closed)
		{
			return closed.get() != null;
		}
	}
	
	private final Object attachmentLock = new Object();
	private Object attachment;
	
	/**
	 * Attaches the specified attachment to this socket.
	 * Attachments are useful when using packets such as
	 * {@link OpcodePacketBuilder#register(int, ReadablePacket, Consumer)}
	 * and you need to know more information about this socket.
	 * Attachments could also be used to keep an attachment always with the socket instead of having to
	 * transfer it as a parameter.
	 *
	 * @param attachment to attach to this socket
	 */
	public void attach(Object attachment)
	{
		synchronized(attachmentLock)
		{
			this.attachment = attachment;
		}
	}
	
	/**
	 * Returns the current attachment set by {@link #attach(Object)} of this socket.
	 * If {@link #attach(Object)} has yet to be called this returns {@code null}.
	 *
	 * @return the current attachment
	 */
	public Object attachment()
	{
		synchronized(attachmentLock)
		{
			return attachment;
		}
	}
	
	public abstract boolean isConnected();
	
	public abstract SocketChannel getSocketChannel();
	
	public abstract SocketManager manager();
}