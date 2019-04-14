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
package oughttoprevail.asyncnetwork.impl.util.writer;

import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.util.Consumer;

/**
 * Implementations at {@link oughttoprevail.asyncnetwork.impl.util.writer.server.ServerWriter}, {@link oughttoprevail.asyncnetwork.impl.util.writer.server.WindowsWriter}, {@link oughttoprevail.asyncnetwork.impl.util.writer.client.ClientWriter}
 */
public interface Writer<T extends Channel>
{
	/**
	 * Writes the specified writeBuffer into the specified channel.
	 * Once a write has finished the specified onWriteFinished is invoked with the specified writeBuffer.
	 *
	 * @param channel which will write the specified writeBuffer
	 * @param writeBuffer to write into the specified channel
	 * @param onWriteFinished which will be invoked when the write has finished
	 */
	void write(T channel, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished);
	
	/**
	 * Continues writing any pending buffers.
	 *
	 * @return whether there is anything more to write
	 */
	boolean continueWriting();
	
	/**
	 * Waits until the writer has finished all pending writes
	 */
	void close();
}