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
package oughttoprevail.asyncnetwork.packet.write;

import java.nio.ByteBuffer;
import java.util.Collection;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.client.ClientSocket;
import oughttoprevail.asyncnetwork.exceptions.PacketClosedException;
import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.Validator;

public class WritablePacket
{
	/**
	 * The {@link PooledByteBuffer} the {@link #packetBuffer} is taken from.
	 * This will be given back to the {@link PooledByteBuffer} when the packet has closed.
	 */
	private PooledByteBuffer pooledPacketBuffer;
	/**
	 * The {@link ByteBuffer} used for writing operations.
	 * This will be {@code null} if the {@link WritablePacket} has closed.
	 */
	private ByteBuffer packetBuffer;
	
	protected WritablePacket(PooledByteBuffer pooledPacketBuffer)
	{
		this.pooledPacketBuffer = pooledPacketBuffer;
		this.packetBuffer = pooledPacketBuffer.getByteBuffer();
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
	public int getSize()
	{
		ensureNotClosed();
		return packetBuffer.limit();
	}
	
	private void ensureWritable(Object sockets)
	{
		ensureNotClosed();
		Validator.requireNonNull(sockets, "Sockets");
	}
	
	/**
	 * Writes this packet to the specified sockets.
	 *
	 * @param sockets who write the packet
	 * @return this
	 */
	public WritablePacket write(Socket... sockets)
	{
		ensureWritable(sockets);
		for(Socket socket : sockets)
		{
			if(socket != null)
			{
				socket.write(packetBuffer.duplicate());
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet specified sockets and once
	 * a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link ClientSocket} to prevent {@link StackOverflowError}
	 * @param sockets who write the packet
	 * @return this
	 */
	public WritablePacket write(Consumer<ByteBuffer> onWriteFinished, Socket... sockets)
	{
		ensureWritable(sockets);
		for(Socket socket : sockets)
		{
			if(socket != null)
			{
				socket.write(packetBuffer.duplicate(), onWriteFinished);
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet to the specified sockets.
	 *
	 * @param sockets who write the packet
	 * @return this
	 */
	public WritablePacket write(Collection<? extends Socket> sockets)
	{
		ensureWritable(sockets);
		for(Socket socket : sockets)
		{
			if(socket != null)
			{
				socket.write(packetBuffer.duplicate());
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet specified sockets and once
	 * a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link ClientSocket} to prevent {@link StackOverflowError}
	 * @param sockets who write the packet
	 * @return this
	 */
	public WritablePacket write(Consumer<ByteBuffer> onWriteFinished, Collection<? extends Socket> sockets)
	{
		ensureWritable(sockets);
		for(Socket socket : sockets)
		{
			if(socket != null)
			{
				socket.write(packetBuffer.duplicate(), onWriteFinished);
			}
		}
		return this;
	}
	
	/**
	 * Writes this packet to the specified sockets and then closes this packet.
	 *
	 * @param sockets who write the packet
	 */
	public void writeAndClose(Socket... sockets)
	{
		write(sockets).close();
	}
	
	/**
	 * Writes this packet specified sockets and then closes this packet,
	 * once a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link ClientSocket} to prevent {@link StackOverflowError}
	 * @param sockets who write the packet
	 */
	public void writeAndClose(Consumer<ByteBuffer> onWriteFinished, Socket... sockets)
	{
		write(onWriteFinished, sockets).close();
	}
	
	/**
	 * Writes this packet to the specified sockets and then closes this packet.
	 *
	 * @param sockets who write the packet
	 */
	public void writeAndClose(Collection<? extends Socket> sockets)
	{
		write(sockets).close();
	}
	
	/**
	 * Writes this packet specified sockets and then closes this packet,
	 * once a write has finished the specified onWriteFinished
	 * is invoked.
	 *
	 * @param onWriteFinished the runnable that will be called when write operation has successfully
	 * finished (nullable) NOTE: onWriteFinished should be set to null when using
	 * {@link ClientSocket} to prevent {@link StackOverflowError}
	 * @param sockets who write the packet
	 */
	public void writeAndClose(Consumer<ByteBuffer> onWriteFinished, Collection<? extends Socket> sockets)
	{
		write(onWriteFinished, sockets).close();
	}
	
	/**
	 * Returns this packet as a {@link ByteBuffer}.
	 *
	 * @return this packet as a {@link ByteBuffer}
	 */
	public ByteBuffer getByteBuffer()
	{
		ensureNotClosed();
		return packetBuffer;
	}
	
	/**
	 * Closes this packet.
	 */
	public void close()
	{
		ensureNotClosed();
		pooledPacketBuffer.close();
		pooledPacketBuffer = null;
		packetBuffer = null;
	}
	
	/**
	 * Returns whether this packet has closed.
	 *
	 * @return whether this packet has closed
	 */
	public boolean isClosed()
	{
		return packetBuffer == null;
	}
}