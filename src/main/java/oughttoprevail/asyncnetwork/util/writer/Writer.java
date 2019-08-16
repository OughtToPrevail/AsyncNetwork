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
package oughttoprevail.asyncnetwork.util.writer;

import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Consumer;

/**
 * Implementations at {@link oughttoprevail.asyncnetwork.util.writer.server.ServerWriter}, {@link oughttoprevail.asyncnetwork.util.writer.server.WindowsWriter}, {@link oughttoprevail.asyncnetwork.util.writer.client.ClientWriter}
 */
public interface Writer
{
	/**
	 * Writes the specified writeBuffer into the specified socket.
	 * Once a write has finished the specified onWriteFinished is invoked with the specified writeBuffer.
	 *
	 * @param socket which will write the specified writeBuffer
	 * @param writeBuffer to write into the specified socket
	 * @param onWriteFinished which will be invoked when the write has finished
	 */
	void write(Socket socket, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished);
	
	/**
	 * Continues writing any pending buffers.
	 *
	 * @return whether there is anything more to write
	 */
	boolean continueWriting();
}