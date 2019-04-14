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
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.exceptions.ChannelClosedException;
import oughttoprevail.asyncnetwork.impl.ChannelImpl;
import oughttoprevail.asyncnetwork.packet.SerDes;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.OS;

/**
 * Implementation at {@link ChannelImpl}.
 *
 * @param <T> the extending class
 */
public interface Channel<T extends Channel>
{
	/**
	 * Local host address.
	 */
	String LOCAL_ADDRESS = "127.0.0.1";
	
	/**
	 * Default buffer size.
	 */
	int DEFAULT_BUFFER_SIZE = 4096;

	/**
	 * Prints an exception by calling {@link PrintStream#println()} of {@link System#err} with {@link
	 * Throwable#toString()}.
	 *
	 * @param throwable The exception that will be called when calling {@link PrintStream#println()}
	 * of {@link System#err} with {@link Throwable#toString()}
	 */
	static void printException(Throwable throwable)
	{
		String thrown = throwable.toString();
		if(!thrown.isEmpty() && thrown.charAt(thrown.length() - 1) == '\n')
		{
			System.err.print(thrown);
		} else
		{
			System.err.println(thrown);
		}
	}
	
	/**
	 * Sets the default options to the specified SocketChannel.
	 *
	 * @param channel the socketChannel that the options will be set for@
	 * @param bufferSize the buffer size that will be used when calling {@link
	 * SocketChannel#setOption(SocketOption, Object)} with {@link StandardSocketOptions#SO_RCVBUF}
	 * and {@link StandardSocketOptions#SO_SNDBUF}
	 * @throws IOException {@link SocketChannel#setOption(SocketOption, Object)} or
	 * {@link Socket#setSendBufferSize(int)} or {@link Socket#setReceiveBufferSize(int)} have
	 * thrown one.
	 */
	static void initializeDefaultOptions(SocketChannel channel, int bufferSize)
			throws IOException
	{
		if(OS.ANDROID)
		{
			Socket socket = channel.socket();
			socket.setSendBufferSize(bufferSize);
			socket.setReceiveBufferSize(bufferSize);
		} else
		{
			channel.setOption(StandardSocketOptions.SO_SNDBUF, bufferSize);
			channel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
		}
	}
	
	/**
	 * Does final operations on the specified writeBuffer depending
	 * on the specified channel and if onWriteFinished is not null
	 * then invoke it.
	 *
	 * @param channel who wrote the specified writeBuffer
	 * @param onWriteFinished to be invoked if not null
	 * @param writeBuffer to be handled
	 */
	static void finishWrite(Channel<?> channel, Consumer<ByteBuffer> onWriteFinished, ByteBuffer writeBuffer)
	{
		if(channel.isClearAfterWrite())
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
	
	/**
	 * Sets whether the channel's write buffer will be cleared after finishing the write. if the
	 * specified clearAfterWrite is true the write buffer will be cleared if false it will not be
	 * cleared.
	 *
	 * @param clearAfterWrite whether the channel's write buffer will be cleared after finishing the
	 * write. if true the write buffer will be cleared if false it will not be cleared
	 * @return this
	 */
	T setClearAfterWrite(boolean clearAfterWrite);
	
	/**
	 * Returns whether the channel will clear the write buffer after finishing write.
	 *
	 * @return whether the channel will clear the write buffer after finishing write
	 */
	boolean isClearAfterWrite();

	/**
	 * Writes the specified buffer to this channel's {@link SocketChannel}
	 * is invoked with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer to write to socket
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T write(ByteBuffer byteBuffer);

	/**
	 * Writes the specified buffer to this channel's {@link SocketChannel} and once
	 * the write has finished the specified onWriteFinished is invoked with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer to write to socket
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T write(ByteBuffer byteBuffer, Consumer<ByteBuffer> onWriteFinished);
	
	/**
	 * Invokes the specified consumer with the channel's read {@link ByteBuffer} when
	 * {@link oughttoprevail.asyncnetwork.impl.util.reader.Reader#callRequests(ByteBuffer)} is called.
	 *
	 * @param onRead the consumer that will be called with the channel's read {@link ByteBuffer}
	 * @return this
	 */
	T onRead(Consumer<ByteBuffer> onRead);
	
	/**
	 * Invokes the specified consumer with the channel's read {@link ByteBuffer} when {@link
	 * ByteBuffer#remaining()} returns the specified length.
	 *
	 * @param consumer the consumer that will be called with the channel's read {@link ByteBuffer}
	 * when {@link ByteBuffer#remaining()} returns the specified length
	 * @param length the amount of bytes that will be received
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readByteBuffer(Consumer<ByteBuffer> consumer, int length);
	
	/**
	 * Invokes the specified consumer with the channel's read {@link ByteBuffer} when {@link
	 * ByteBuffer#remaining()} returns is equal or more to the specified length
	 * and queues this again at the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with the channel's read {@link ByteBuffer}
	 * when {@link ByteBuffer#remaining()} returns the specified length
	 * @param length the amount of bytes that will be received
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readByteBufferAlways(Consumer<ByteBuffer> consumer, int length);
	
	/**
	 * Invokes the specified consumer with a received byte.
	 *
	 * @param consumer the consumer that will be called with a received byte
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readByte(Consumer<Byte> consumer);
	
	/**
	 * Invokes the specified consumer with a received byte and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received byte
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readByteAlways(Consumer<Byte> consumer);
	
	/**
	 * Invokes the specified consumer with the received bytes.
	 *
	 * @param consumer the consumer that will be called with the received bytes
	 * @param length the amount of bytes that will be received
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readBytes(Consumer<byte[]> consumer, int length);
	
	/**
	 * Invokes the specified consumer with the received bytes and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with the received bytes
	 * @param length the amount of bytes that will be received
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readBytesAlways(Consumer<byte[]> consumer, int length);
	
	/**
	 * Invokes the specified consumer with a received char.
	 *
	 * @param consumer the consumer that will be called with a received char
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readChar(Consumer<Character> consumer);
	
	/**
	 * Invokes the specified consumer with a received char and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received char
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readCharAlways(Consumer<Character> consumer);
	
	/**
	 * Invokes the specified consumer with a received double.
	 *
	 * @param consumer the consumer that will be called with a received double
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readDouble(Consumer<Double> consumer);
	
	/**
	 * Invokes the specified consumer with a received double and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received double
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readDoubleAlways(Consumer<Double> consumer);
	
	/**
	 * Invokes the specified consumer with a received float.
	 *
	 * @param consumer the consumer that will be called with a received float
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readFloat(Consumer<Float> consumer);
	
	/**
	 * Invokes the specified consumer with a received float and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received float
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readFloatAlways(Consumer<Float> consumer);
	
	/**
	 * Invokes the specified consumer with a received int.
	 *
	 * @param consumer the consumer that will be called with a received int
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readInt(Consumer<Integer> consumer);
	
	/**
	 * Invokes the specified consumer with a received int and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received int
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readIntAlways(Consumer<Integer> consumer);
	
	/**
	 * Invokes the specified consumer with a received long.
	 *
	 * @param consumer the consumer that will be called with a received long
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readLong(Consumer<Long> consumer);
	
	/**
	 * Invokes the specified consumer with a received long and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received long
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readLongAlways(Consumer<Long> consumer);
	
	/**
	 * Invokes the specified consumer with a received short.
	 *
	 * @param consumer the consumer that will be called with a received short
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readShort(Consumer<Short> consumer);
	
	/**
	 * Invokes the specified consumer with a received short and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received short
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readShortAlways(Consumer<Short> consumer);
	
	/**
	 * Invokes the specified consumer with a received boolean.
	 *
	 * @param consumer the consumer that will be called with a received boolean
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readBoolean(Consumer<Boolean> consumer);
	
	/**
	 * Invokes the specified consumer with a received boolean and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received boolean
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readBooleanAlways(Consumer<Boolean> consumer);
	
	/**
	 * Invokes the specified consumer with a received string.
	 *
	 * @param consumer the consumer that will be called with a received string
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readString(Consumer<String> consumer);
	
	/**
	 * Invokes the specified consumer with a received string and queues this again at
	 * the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received string
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	T readStringAlways(Consumer<String> consumer);
	
	/**
	 * Invokes the specified consumer with a received object after deserialization made by specified serDes.
	 *
	 * @param consumer the consumer that will be called with a received object
	 * @param serDes which will deserialize the {@link ByteBuffer} to the {@link Object}
	 * @param <O> the type of {@link Object} being read.
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	<O> T readObject(Consumer<O> consumer, SerDes<O> serDes);
	
	/**
	 * Invokes the specified consumer with a received object after deserialization made by specified serDes
	 * and queues this again at the end of the queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received object
	 * @param serDes which will deserialize the {@link ByteBuffer} to the {@link Object}
	 * @param <O> the type of {@link Object} being read.
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	<O> T readObjectAlways(Consumer<O> consumer, SerDes<O> serDes);
	
	/**
	 * Invokes the specified consumer with a received object or null if the object was sent as null
	 * after deserialization made by specified serDes.
	 *
	 * @param consumer the consumer that will be called with a received object
	 * @param serDes which will deserialize the {@link ByteBuffer} to the {@link Object}
	 * @param <O> the type of {@link Object} being read.
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	<O> T readNullableObject(Consumer<O> consumer, SerDes<O> serDes);
	
	/**
	 * Invokes the specified consumer with a received object or null if the object was sent as null
	 * after deserialization made by specified serDes and queues this again at the end of the
	 * queue after the consumer was invoked.
	 *
	 * @param consumer the consumer that will be called with a received object
	 * @param serDes which will deserialize the {@link ByteBuffer} to the {@link Object}
	 * @param <O> the type of {@link Object} being read.
	 * @return this
	 * @throws ChannelClosedException throws {@link ChannelClosedException} if the channel is closed
	 */
	<O> T readNullableObjectAlways(Consumer<O> consumer, SerDes<O> serDes);
	
	/**
	 * Returns the {@link ByteBuffer} size.
	 *
	 * @return the {@link ByteBuffer} size
	 */
	int getBufferSize();
	
	/**
	 * Invokes the specified runnable when the client disconnects.
	 *
	 * @param onDisconnect the runnable that will get called when the client disconnects
	 * @return this
	 */
	T onDisconnect(Consumer<DisconnectionType> onDisconnect);
	
	/**
	 * Invokes the specified consumer with a throwable when a caught exception occurs.
	 *
	 * @param onException the consumer that will be called with the caught exceptions
	 * @return this
	 */
	T onException(Consumer<Throwable> onException);
	
	/**
	 * Invokes the specified consumer when the {@link Channel} read {@link ByteBuffer} needs more space
	 * and must be cleared.
	 *
	 * @param onBufferOverflow the consumer that would be called when a channel's read {@link
	 * ByteBuffer} needs more space and must be cleared
	 * @return this
	 */
	T onBufferOverflow(Consumer<ByteBuffer> onBufferOverflow);
	
	/**
	 * Returns the {@link SocketChannel} this channel uses.
	 *
	 * @return the {@link SocketChannel} this channel uses
	 */
	SocketChannel getSocketChannel();
	
	/**
	 * If the channel has yet to be closed the method closes the {@link SocketChannel} and invokes the
	 * onDisconnect that is specified by {@link Channel#onDisconnect(Consumer)}.
	 *
	 * @return this
	 */
	T close();
	
	/**
	 * Returns whether the channel is connected.
	 *
	 * @return whether the channel is connected
	 */
	boolean isConnected();
	
	/**
	 * Returns whether the channel is closed.
	 *
	 * @return whether the channel is closed
	 */
	boolean isClosed();
	
	/**
	 * Returns the channel manager.
	 *
	 * @return the channel manager
	 */
	ChannelManager manager();
}