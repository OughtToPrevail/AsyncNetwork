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
package oughttoprevail.asyncnetwork.packet;

import java.nio.ByteBuffer;
import java.util.Collection;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.Client;
import oughttoprevail.asyncnetwork.util.Consumer;

public interface WritablePacket
{
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	int getSize();
	
	/**
	 * Writes this packet to the specified channels.
	 *
	 * @param channels who write the packet
	 * @return this
	 */
	WritablePacket write(Channel<?>... channels);
	
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
	WritablePacket write(Consumer<ByteBuffer> onWriteFinished, Channel<?>... channels);
	
	/**
	 * Writes this packet to the specified channels.
	 *
	 * @param channels who write the packet
	 * @return this
	 */
	WritablePacket write(Collection<? extends Channel<?>> channels);
	
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
	WritablePacket write(Consumer<ByteBuffer> onWriteFinished, Collection<? extends Channel<?>> channels);
	
	/**
	 * Writes this packet to the specified channels and then closes this packet.
	 *
	 * @param channels who write the packet
	 */
	void writeAndClose(Channel<?>... channels);
	
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
	void writeAndClose(Consumer<ByteBuffer> onWriteFinished, Channel<?>... channels);
	
	/**
	 * Writes this packet to the specified channels and then closes this packet.
	 *
	 * @param channels who write the packet
	 */
	void writeAndClose(Collection<? extends Channel<?>> channels);
	
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
	void writeAndClose(Consumer<ByteBuffer> onWriteFinished, Collection<? extends Channel<?>> channels);
	
	/**
	 * Returns this packet's {@link ByteBuffer}.
	 *
	 * @return this packet's {@link ByteBuffer}
	 */
	ByteBuffer getByteBuffer();
	
	/**
	 * Closes this packet.
	 */
	void close();
	
	/**
	 * Returns whether this packet has closed.
	 *
	 * @return whether this packet has closed
	 */
	boolean isClosed();
}