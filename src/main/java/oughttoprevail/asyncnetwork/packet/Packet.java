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

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.Client;
import oughttoprevail.asyncnetwork.util.Consumer;

;

public interface Packet
{
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	int getSize();
	
	/**
	 * Writes this packet to the specified channel.
	 *
	 * @param channel for the packet to be written to
	 * @return this
	 */
	Packet write(Channel<?> channel);
	
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
	Packet write(Channel<?> channel, Consumer<ByteBuffer> onWriteFinished);
	
	/**
	 * Writes this packet to the specified channel and closes this {@link Packet}.
	 *
	 * @param channel for the packet to be written to
	 */
	void writeAndClose(Channel<?> channel);
	
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
	void writeAndClose(Channel<?> channel, Consumer<ByteBuffer> onWriteFinished);
	
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