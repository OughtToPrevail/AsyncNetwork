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

import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.impl.packet.ByteBufferElement;
import oughttoprevail.asyncnetwork.util.Consumer;

/**
 * Implementations at {@link ServerClientManager} {@link ClientManager}.
 */
public interface ChannelManager
{
	/**
	 * Closes the channel and invokes {@link Channel#onDisconnect(Consumer)} specified consumer with the
	 * specified disconnectionType.
	 * If specified urgent is set to true then pending write operations will not continue else
	 * close call will block until write operations have finished.
	 *
	 * @param disconnectionType the disconnectionType that will be used when calling the {@link
	 * Channel#onDisconnect(Consumer)} specified consumer
	 */
	void close(DisconnectionType disconnectionType);
	
	/**
	 * Returns the channel's read buffer.
	 *
	 * @return the channel's read buffer
	 */
	ByteBufferElement getReadBuffer();
	
	/**
	 * Invokes the channel's {@link Channel#onException(Consumer)} consumer with the specified
	 * exception.
	 *
	 * @param throwable the exception that the channel's {@link Channel#onException(Consumer)}
	 * consumer will be called with
	 */
	void exception(Throwable throwable);
	
	/**
	 * Invokes the channel's {@link Channel#onBufferOverflow(Consumer)} consumer with the specified
	 * byteBuffer.
	 *
	 * @param byteBuffer the {@link ByteBuffer} that the channel's {@link Channel#onBufferOverflow(Consumer)}
	 * consumer will be called with
	 */
	void bufferOverflow(ByteBuffer byteBuffer);
	
	/**
	 * Reads pending channel reads with the readBuffer and invokes the pending read consumers.
	 */
	void callRead();
	
	/**
	 * Invokes the {@link Channel#onRead(Consumer)} consumer with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer the {@link ByteBuffer} which will be used when calling the onRead conusmer
	 */
	void callOnRead(ByteBuffer byteBuffer);
}