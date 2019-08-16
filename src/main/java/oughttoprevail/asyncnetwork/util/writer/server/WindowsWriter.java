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
package oughttoprevail.asyncnetwork.util.writer.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.ServerClientManager;
import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.selector.WindowsSelector;
import oughttoprevail.asyncnetwork.util.writer.Writer;
import oughttoprevail.asyncnetwork.util.Consumer;

public class WindowsWriter implements Writer
{
	/**
	 * Writes the specified writeBuffer into the specified socket.
	 * Once a write has finished the specified onWriteFinished is invoked with the specified writeBuffer.
	 *
	 * @param socket which will write the specified writeBuffer
	 * @param writeBuffer to write into the specified socket
	 * @param onWriteFinished which will be invoked when the write has finished
	 */
	@Override
	public void write(Socket socket, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished)
	{
		ServerClientManager manager = (ServerClientManager) socket.manager();
		WindowsSelector selector = (WindowsSelector) manager.getServer().manager().getSelector();
		int length = writeBuffer.remaining();
		try
		{
			selector.WSASend(manager.getFD(),
					Util.address(writeBuffer) + writeBuffer.position(),
					length,
					new PendingWrite(writeBuffer, onWriteFinished));
		} catch(IOException e)
		{
			Validator.handleRemoteHostCloseException(socket, e);
		}
		writeBuffer.position(writeBuffer.position() + length);
	}
	
	/**
	 * Continues writing any pending buffers.
	 *
	 * @return whether there is anything more to write
	 */
	@Override
	public boolean continueWriting()
	{
		//ignored writes always finish with WSASend
		return false;
	}
}