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
package oughttoprevail.asyncnetwork.impl.packet;

import java.nio.ByteBuffer;
import java.util.Collection;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.Client;
import oughttoprevail.asyncnetwork.exceptions.PacketClosedException;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.packet.WritablePacket;
import oughttoprevail.asyncnetwork.util.Consumer;

public class WritablePacketImpl implements WritablePacket
{
	/**
	 * The {@link ByteBufferElement} the {@link #packetBuffer} is taken from.
	 * This will be given back to the {@link ByteBufferPool} when the packet has closed.
	 */
	private ByteBufferElement packetBufferElement;
	/**
	 * The {@link ByteBuffer} used for writing operations.
	 * This will be {@code null} if the {@link WritablePacket} has closed.
	 */
	private ByteBuffer packetBuffer;
	
	protected WritablePacketImpl(ByteBufferElement packetBufferElement)
	{
		this.packetBufferElement = packetBufferElement;
		this.packetBuffer = packetBufferElement.getByteBuffer();
	}
	
	private void ensureNotClosed()
	{
		if(isClosed())
		{
			throw new PacketClosedException();
		}
	}
	
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	@Override
	public int getSize()
	{
		ensureNotClosed();
		return packetBuffer.limit();
	}
	
	private void ensureWritable(Object channels)
	{
		ensureNotClosed();
		Validator.requireNonNull(channels, "Channels");
	}
	
	/**
	 * Writes this packet to the specified channels.
	 *
	 * @param channels who write the packet
	 * @return this
	 */
	@Override
	public WritablePacket write(Channel<?>... channels)
	{
		ensureWritable(channels);
		for(Channel<?> channel : channels)
		{
			if(channel != null)
			{
				channel.write(packetBuffer.duplicate());
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet specified channels and once
	 * a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @param channels who write the packet
	 * @return this
	 */
	@Override
	public WritablePacket write(Consumer<ByteBuffer> onWriteFinished, Channel<?>... channels)
	{
		ensureWritable(channels);
		for(Channel<?> channel : channels)
		{
			if(channel != null)
			{
				channel.write(packetBuffer.duplicate(), onWriteFinished);
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet to the specified channels.
	 *
	 * @param channels who write the packet
	 * @return this
	 */
	@Override
	public WritablePacket write(Collection<? extends Channel<?>> channels)
	{
		ensureWritable(channels);
		for(Channel<?> channel : channels)
		{
			if(channel != null)
			{
				channel.write(packetBuffer.duplicate());
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet specified channels and once
	 * a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @param channels who write the packet
	 * @return this
	 */
	@Override
	public WritablePacket write(Consumer<ByteBuffer> onWriteFinished, Collection<? extends Channel<?>> channels)
	{
		ensureWritable(channels);
		for(Channel<?> channel : channels)
		{
			if(channel != null)
			{
				channel.write(packetBuffer.duplicate(), onWriteFinished);
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet to the specified channels and then closes this packet.
	 *
	 * @param channels who write the packet
	 */
	@Override
	public void writeAndClose(Channel<?>... channels)
	{
		write(channels).close();
	}
	
	/**
	 * Writes this packet specified channels and then closes this packet,
	 * once a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @param channels who write the packet
	 */
	@Override
	public void writeAndClose(Consumer<ByteBuffer> onWriteFinished, Channel<?>... channels)
	{
		write(onWriteFinished, channels).close();
	}
	
	/**
	 * Writes this packet to the specified channels and then closes this packet.
	 *
	 * @param channels who write the packet
	 */
	@Override
	public void writeAndClose(Collection<? extends Channel<?>> channels)
	{
		write(channels).close();
	}
	
	/**
	 * Writes this packet specified channels and then closes this packet,
	 * once a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @param channels who write the packet
	 */
	@Override
	public void writeAndClose(Consumer<ByteBuffer> onWriteFinished, Collection<? extends Channel<?>> channels)
	{
		write(onWriteFinished, channels).close();
	}
	
	/**
	 * Returns this packet as a {@link ByteBuffer}.
	 *
	 * @return this packet as a {@link ByteBuffer}
	 */
	@Override
	public ByteBuffer getByteBuffer()
	{
		ensureNotClosed();
		return packetBuffer;
	}
	
	/**
	 * Closes this packet.
	 */
	@Override
	public void close()
	{
		ensureNotClosed();
		ByteBufferPool.getInstance().give(packetBufferElement);
		packetBufferElement = null;
		packetBuffer = null;
	}
	
	/**
	 * Returns whether this packet has closed.
	 *
	 * @return whether this packet has closed
	 */
	@Override
	public boolean isClosed()
	{
		return packetBuffer == null;
	}
}