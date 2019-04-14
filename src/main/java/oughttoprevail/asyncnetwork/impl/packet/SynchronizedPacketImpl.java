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
import oughttoprevail.asyncnetwork.packet.Packet;
import oughttoprevail.asyncnetwork.util.Consumer;

public class SynchronizedPacketImpl extends PacketImpl
{
	/**
	 * Lock used for {@code synchronized} in order to add thread safety.
	 */
	private final Object lock = new Object();
	
	protected SynchronizedPacketImpl(ByteBufferElement packetBufferElement)
	{
		super(packetBufferElement);
	}
	
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	@Override
	public int getSize()
	{
		synchronized(lock)
		{
			return super.getSize();
		}
	}
	
	/**
	 * Writes this packet to the specified channel.
	 *
	 * @param channel for the packet to be written to
	 */
	@Override
	public Packet write(Channel<?> channel)
	{
		synchronized(lock)
		{
			return super.write(channel);
		}
	}
	
	/**
	 * Writes this packet specified channel and once
	 * the write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param channel for the packet to be written to
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link Client} to prevent {@link StackOverflowError}
	 * @return this
	 */
	@Override
	public Packet write(Channel<?> channel, Consumer<ByteBuffer> onWriteFinished)
	{
		synchronized(lock)
		{
			return super.write(channel, onWriteFinished);
		}
	}
	
	/**
	 * Returns this packet as a {@link ByteBuffer}.
	 *
	 * @return this packet as a {@link ByteBuffer}
	 */
	@Override
	public ByteBuffer getByteBuffer()
	{
		synchronized(lock)
		{
			return super.getByteBuffer();
		}
	}
	
	/**
	 * Closes this packet.
	 */
	@Override
	public void close()
	{
		synchronized(lock)
		{
			super.close();
		}
	}
	
	/**
	 * Returns whether this packet has closed.
	 *
	 * @return whether this packet has closed
	 */
	@Override
	public boolean isClosed()
	{
		synchronized(lock)
		{
			return super.isClosed();
		}
	}
}