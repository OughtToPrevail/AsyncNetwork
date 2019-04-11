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
package oughttoprevail.asyncnetwork.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.Client;
import oughttoprevail.asyncnetwork.ClientManager;
import oughttoprevail.asyncnetwork.DisconnectionType;
import oughttoprevail.asyncnetwork.IServer;
import oughttoprevail.asyncnetwork.ServerClientManager;
import oughttoprevail.asyncnetwork.exceptions.ChannelClosedException;
import oughttoprevail.asyncnetwork.impl.packet.ByteBufferPool;
import oughttoprevail.asyncnetwork.impl.server.ServerClientManagerImpl;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.impl.util.reader.Reader;
import oughttoprevail.asyncnetwork.impl.util.writer.Writer;
import oughttoprevail.asyncnetwork.packet.Deserializer;
import oughttoprevail.asyncnetwork.util.Consumer;

;

public abstract class ChannelImpl<T extends Channel> implements Channel<T>
{
	/**
	 * Returns the extending class.
	 *
	 * @return the extending class
	 */
	protected abstract T getThis();
	
	private final int bufferSize;
	private final ByteBuffer readBuffer;
	
	private final Reader reader;
	private final Writer<T> writer;
	
	protected ChannelImpl(int bufferSize)
	{
		this.bufferSize = bufferSize;
		this.readBuffer = ByteBufferPool.INSTANCE.take(bufferSize);
		reader = createReader();
		writer = createWriter();
	}
	
	/**
	 * <b>This method is for future designs or for libraries who want to change the reader or for
	 * extending classes.</b>
	 * Creates a new {@link Reader}.
	 *
	 * @return the new reader
	 */
	protected Reader createReader()
	{
		return new Reader(this);
	}
	
	/**
	 * <b>This method is for future designs or for libraries who want to change the writer or for
	 * extending classes.</b>
	 * Creates a new {@link Writer}.
	 *
	 * @return the new writer
	 */
	protected abstract Writer<T> createWriter();
	
	protected ClientManager createClientManager()
	{
		return new ClientManager()
		{
			@Override
			public void close(DisconnectionType disconnectionType)
			{
				ChannelImpl.this.close(disconnectionType);
			}
			
			@Override
			public ByteBuffer getReadBuffer()
			{
				return readBuffer;
			}
			
			@Override
			public void exception(Throwable throwable)
			{
				ChannelImpl.this.exception(throwable);
			}
			
			@Override
			public void bufferOverflow(ByteBuffer byteBuffer)
			{
				ChannelImpl.this.bufferOverflow(byteBuffer);
			}
			
			@Override
			public void callRead()
			{
				reader.read(readBuffer);
			}
			
			@Override
			public void callOnRead(ByteBuffer byteBuffer)
			{
				runOnRead(byteBuffer);
			}
		};
	}
	
	protected <S extends IServer> ServerClientManager<S> createServerClientManager(S server, int clientsIndex)
	{
		return new ServerClientManagerImpl<S>()
		{
			@Override
			public S getServer()
			{
				return server;
			}
			
			@Override
			public void close(DisconnectionType disconnectionType)
			{
				boolean closedBefore = isClosed();
				ChannelImpl.this.close(disconnectionType);
				if(isClosed() != closedBefore)
				{
					ByteBufferPool.INSTANCE.give(readBuffer);
					server.manager().clientDisconnected(clientsIndex);
				}
			}
			
			@Override
			public void callRequests()
			{
				reader.callRequests(readBuffer);
			}
			
			@Override
			public void callRead()
			{
				reader.read(readBuffer);
			}
			
			@Override
			public boolean callWrite()
			{
				return writer.continueWriting();
			}
			
			@Override
			public ByteBuffer getReadBuffer()
			{
				return readBuffer;
			}
			
			@Override
			public void exception(Throwable throwable)
			{
				ChannelImpl.this.exception(throwable);
			}
			
			@Override
			public void bufferOverflow(ByteBuffer byteBuffer)
			{
				ChannelImpl.this.bufferOverflow(byteBuffer);
			}
			
			@Override
			public void callOnRead(ByteBuffer byteBuffer)
			{
				runOnRead(byteBuffer);
			}
		};
	}
	
	private void runOnRead(ByteBuffer byteBuffer)
	{
		if(onRead != null)
		{
			onRead.accept(byteBuffer);
		}
	}
	
	private void close(DisconnectionType disconnectionType)
	{
		synchronized(closed)
		{
			if(!closed.get())
			{
				try
				{
					closed.set(true);
					writer.close();
					getSocketChannel().close();
					reader.clear();
					//remove all variables for memory and to make sure none get called after the channel has closed
					onRead = null;
					onException = null;
					onBufferOverflow = null;
					System.out.println("Ondisconnect " + onDisconnect);
					if(onDisconnect != null)
					{
						onDisconnect.accept(disconnectionType);
					}
				} catch(IOException e)
				{
					manager().exception(e);
				}
			}
		}
	}
	
	private void exception(Throwable throwable)
	{
		if(onException == null)
		{
			Channel.printException(throwable);
		} else
		{
			onException.accept(throwable);
		}
	}
	
	private void bufferOverflow(ByteBuffer byteBuffer)
	{
		if(onBufferOverflow == null)
		{
			byteBuffer.clear();
		} else
		{
			onBufferOverflow.accept(byteBuffer);
			if(!byteBuffer.hasRemaining())
			{
				byteBuffer.clear();
			}
		}
	}
	
	private boolean clearAfterWrite = true;
	
	/**
	 * Sets whether the channel's write buffer will be cleared after finishing the write. if the
	 * specified clearAfterWrite is true the write buffer will be cleared if false it will not be
	 * cleared.
	 *
	 * @param clearAfterWrite whether the channel's write buffer will be cleared after finishing the
	 * write. if true the write buffer will be cleared if false it will not be cleared
	 * @return this
	 */
	@Override
	public T setClearAfterWrite(boolean clearAfterWrite)
	{
		this.clearAfterWrite = clearAfterWrite;
		return getThis();
	}
	
	/**
	 * Returns whether the channel will clear the write buffer after finishing write.
	 *
	 * @return whether the channel will clear the write buffer after finishing write
	 */
	@Override
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
	 * Writes the specified buffer to this channel's {@link SocketChannel} and once
	 * the write has finished the specified onWriteFinished is invoked with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer to write to socket
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T write(ByteBuffer byteBuffer)
	{
		return write(byteBuffer, null);
	}
	
	/**
	 * Writes the specified buffer to this channel's {@link SocketChannel} and once
	 * the write has finished the specified onWriteFinished is invoked with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer to write to socket
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be setValue to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T write(ByteBuffer byteBuffer, Consumer<ByteBuffer> onWriteFinished)
	{
		ensureCanWrite();
		int bytes = byteBuffer.position();
		if(bytes != 0)
		{
			byteBuffer.flip();
			writer.write(getThis(), byteBuffer, onWriteFinished);
		}
		return getThis();
	}
	
	private void ensureCanRead(Consumer<?> consumer)
	{
		ensureNotClosed();
		Validator.requireNonNull(consumer, "Consumer");
	}
	
	private Consumer<ByteBuffer> onRead;
	
	@Override
	public T onRead(Consumer<ByteBuffer> consumer)
	{
		this.onRead = consumer;
		return null;
	}
	
	/**
	 * Calls the specified consumer with the channel's read {@link ByteBuffer} when {@link
	 * ByteBuffer#remaining()} returns the specified length.
	 *
	 * @param consumer the consumer that will be called with the channel's read {@link ByteBuffer}
	 * when {@link ByteBuffer#remaining()} returns the specified length
	 * @param length the amount of bytes that will be received
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readByteBuffer(Consumer<ByteBuffer> consumer, int length, boolean always)
	{
		ensureCanRead(consumer);
		if(length <= 0)
		{
			manager().exception(new IndexOutOfBoundsException("Request length must be larger than 0!"));
			return getThis();
		}
		if(length > readBuffer.capacity())
		{
			manager().exception(new IndexOutOfBoundsException("Request length larger than the read buffer size!"));
			return getThis();
		}
		reader.addRequest(readBuffer, consumer, length, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received byte.
	 *
	 * @param consumer the consumer that will be called with a received byte
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readByte(Consumer<Byte> consumer, boolean always)
	{
		ensureCanRead(consumer);
		return readByteBuffer(byteBuffer -> consumer.accept(byteBuffer.get()), Util.BYTE_BYTES, always);
	}
	
	/**
	 * Calls the specified consumer with the received bytes.
	 *
	 * @param consumer the consumer that will be called with the received bytes
	 * @param length the amount of bytes that will be received
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readBytes(Consumer<byte[]> consumer, int length, boolean always)
	{
		ensureCanRead(consumer);
		return readByteBuffer(byteBuffer ->
		{
			byte[] bytes = new byte[length];
			byteBuffer.get(bytes);
			consumer.accept(bytes);
		}, length, always);
	}
	
	/**
	 * Calls the specified consumer with a received char.
	 *
	 * @param consumer the consumer that will be called with a received char
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readChar(Consumer<Character> consumer, boolean always)
	{
		ensureCanRead(consumer);
		readByteBuffer(byteBuffer -> consumer.accept(byteBuffer.getChar()), Util.CHAR_BYTES, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received double.
	 *
	 * @param consumer the consumer that will be called with a received double
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readDouble(Consumer<Double> consumer, boolean always)
	{
		ensureCanRead(consumer);
		readByteBuffer(byteBuffer -> consumer.accept(byteBuffer.getDouble()), Util.DOUBLE_BYTES, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received float.
	 *
	 * @param consumer the consumer that will be called with a received float
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readFloat(Consumer<Float> consumer, boolean always)
	{
		ensureCanRead(consumer);
		readByteBuffer(byteBuffer -> consumer.accept(byteBuffer.getFloat()), Util.FLOAT_BYTES, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received int.
	 *
	 * @param consumer the consumer that will be called with a received int
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readInt(Consumer<Integer> consumer, boolean always)
	{
		ensureCanRead(consumer);
		readByteBuffer(byteBuffer -> consumer.accept(byteBuffer.getInt()), Util.INT_BYTES, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received long.
	 *
	 * @param consumer the consumer that will be called with a received long
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readLong(Consumer<Long> consumer, boolean always)
	{
		ensureCanRead(consumer);
		readByteBuffer(byteBuffer -> consumer.accept(byteBuffer.getLong()), Util.LONG_BYTES, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received short.
	 *
	 * @param consumer the consumer that will be called with a received short
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readShort(Consumer<Short> consumer, boolean always)
	{
		ensureCanRead(consumer);
		readByteBuffer(byteBuffer -> consumer.accept(byteBuffer.getShort()), Util.SHORT_BYTES, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received boolean.
	 *
	 * @param consumer the consumer that will be called with a received boolean
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readBoolean(Consumer<Boolean> consumer, boolean always)
	{
		readByte(aByte -> consumer.accept(aByte == 1), always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received string.
	 *
	 * @param consumer the consumer that will be called with a received string
	 * @param always whether it will read until the channel is closed
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public T readString(Consumer<String> consumer, boolean always)
	{
		ensureCanRead(consumer);
		readByteBuffer(byteBuffer ->
		{
			short lengthAsShort = byteBuffer.getShort();
			int length = Short.toUnsignedInt(lengthAsShort);
			if(length > byteBuffer.capacity())
			{
				manager().exception(new IndexOutOfBoundsException(
						"Received a string with the length bigger than the bufferSize"));
				return;
			}
			readByteBuffer(byteBuf1 ->
			{
				byte[] bytes = new byte[length];
				byteBuf1.get(bytes);
				consumer.accept(new String(bytes, Util.UTF_8));
			}, length, false);
		}, Util.SHORT_BYTES, always);
		return getThis();
	}
	
	/**
	 * Calls the specified consumer with a received object after deserialization.
	 *
	 * @param consumer the consumer that will be called with a received object
	 * @param deserializer which can deserialize the {@link ByteBuffer} to the {@link Object}
	 * @param always whether it will read until the channel is closed
	 * @param <O> the type of {@link Object} being read.
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	@Override
	public <O> T readObject(Consumer<O> consumer, Deserializer<O> deserializer, boolean always)
	{
		return readByteBuffer(byteBuffer -> consumer.accept(deserializer.deserialize(byteBuffer)),
				deserializer.getSerializedLength(),
				always);
	}
	
	/**
	 * Returns the {@link ByteBuffer} size.
	 *
	 * @return the {@link ByteBuffer} size
	 */
	@Override
	public int getBufferSize()
	{
		return bufferSize;
	}
	
	private Consumer<DisconnectionType> onDisconnect;
	
	/**
	 * Calls the specified runnable when the client disconnects.
	 *
	 * @param onDisconnect the runnable that will get called when the client disconnects
	 * @return this
	 */
	@Override
	public T onDisconnect(Consumer<DisconnectionType> onDisconnect)
	{
		Validator.requireNonNull(onDisconnect, "onDisconnect");
		synchronized(closed)
		{
			System.out.println("SET TO " + onDisconnect);
			this.onDisconnect = onDisconnect;
		}
		return getThis();
	}
	
	private void ensureNotClosed()
	{
		if(isClosed())
		{
			throw new ChannelClosedException();
		}
	}
	
	private final AtomicBoolean closed = new AtomicBoolean();
	
	/**
	 * If the channel has yet to be closed the method closes the {@link SocketChannel} and calls the
	 * onDisconnect that is specified by {@link Channel#onDisconnect(Consumer)}.
	 *
	 * @return this
	 */
	@Override
	public T close()
	{
		manager().close(DisconnectionType.USER_CLOSE);
		return getThis();
	}
	
	private Consumer<Throwable> onException;
	
	/**
	 * Calls the specified consumer with a throwable when a caught exception occurs.
	 *
	 * @param onException the consumer that will be called with the caught exceptions
	 * @return this
	 */
	@Override
	public T onException(Consumer<Throwable> onException)
	{
		this.onException = onException;
		return getThis();
	}
	
	private Consumer<ByteBuffer> onBufferOverflow;
	
	/**
	 * Calls the specified consumer when the {@link Channel} read {@link ByteBuffer} needs more space
	 * and must be cleared.
	 *
	 * @param onBufferOverflow the consumer that would be called when a channel's read {@link
	 * ByteBuffer} needs more space and must be cleared
	 * @return this
	 */
	@Override
	public T onBufferOverflow(Consumer<ByteBuffer> onBufferOverflow)
	{
		this.onBufferOverflow = onBufferOverflow;
		return getThis();
	}
	
	/**
	 * Returns whether the channel is closed.
	 *
	 * @return whether the channel is closed
	 */
	@Override
	public boolean isClosed()
	{
		synchronized(closed)
		{
			return closed.get();
		}
	}
}