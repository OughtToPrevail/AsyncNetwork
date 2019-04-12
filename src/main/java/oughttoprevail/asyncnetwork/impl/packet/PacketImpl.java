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

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.Client;
import oughttoprevail.asyncnetwork.exceptions.PacketClosedException;
import oughttoprevail.asyncnetwork.packet.Packet;
import oughttoprevail.asyncnetwork.util.Consumer;

public class PacketImpl implements Packet
{
	/**
	 * The {@link ByteBuffer} used for writing operations.
	 * This will be {@code null} if the {@link Packet} has closed.
	 */
	private ByteBuffer packetBuffer;
	
	public PacketImpl(ByteBuffer packetBuffer)
	{
		this.packetBuffer = packetBuffer;
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
	
	/**
	 * Writes this packet to the specified channel.
	 *
	 * @param channel for the packet to be written to
	 */
	@Override
	public Packet write(Channel<?> channel)
	{
		ensureNotClosed();
		channel.write(packetBuffer.duplicate());
		return this;
	}
	
	/**
	 * Writes this packet specified channel and once
	 * the write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param channel for the packet to be written to
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be setValue to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @return this
	 */
	@Override
	public Packet write(Channel<?> channel, Consumer<ByteBuffer> onWriteFinished)
	{
		ensureNotClosed();
		channel.write(packetBuffer.duplicate(), onWriteFinished);
		return this;
	}
	
	/**
	 * Writes this packet to the specified channel and closes this {@link Packet}.
	 *
	 * @param channel for the packet to be written to
	 */
	@Override
	public void writeAndClose(Channel<?> channel)
	{
		write(channel);
		close();
	}
	
	/**
	 * Writes this packet specified channel, closes this {@link Packet}
	 * and once the write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param channel for the packet to be written to
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be setValue to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 */
	@Override
	public void writeAndClose(Channel<?> channel, Consumer<ByteBuffer> onWriteFinished)
	{
		write(channel, onWriteFinished);
		close();
	}
	
	/**
	 * Returns this packet as a {@link ByteBuffer}.
	 *
	 * @return this packet as a {@link ByteBuffer}
	 */
	@Override
	public ByteBuffer getByteBuffer()
	{
		return packetBuffer;
	}
	
	/**
	 * Closes this packet.
	 */
	@Override
	public void close()
	{
		ByteBufferPool.getInstance().give(packetBuffer);
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