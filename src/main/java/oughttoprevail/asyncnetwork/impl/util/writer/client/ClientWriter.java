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
package oughttoprevail.asyncnetwork.impl.util.writer.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.Client;
import oughttoprevail.asyncnetwork.DisconnectionType;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.impl.util.writer.Writer;
import oughttoprevail.asyncnetwork.util.Consumer;

public class ClientWriter implements Writer<Client>
{
	/**
	 * Writes the specified writeBuffer into the specified channel.
	 * Once a write has finished the specified onWriteFinished is invoked with the specified writeBuffer.
	 *
	 * @param channel which will write the specified writeBuffer
	 * @param writeBuffer to write into the specified channel
	 * @param onWriteFinished which will be invoked when the write has finished
	 */
	@Override
	public void write(Client channel, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished)
	{
		SocketChannel socketChannel = channel.getSocketChannel();
		try
		{
			while(writeBuffer.hasRemaining())
			{
				if(socketChannel.write(writeBuffer) == -1)
				{
					channel.manager().close(DisconnectionType.REMOTE_CLOSE);
				}
			}
			Channel.finishWrite(channel, onWriteFinished, writeBuffer);
		} catch(IOException e)
		{
			Validator.handleRemoteHostCloseException(e, channel);
		}
	}
	
	/**
	 * Continues writing any pending buffers.
	 *
	 * @return whether there is anything more to write
	 */
	@Override
	public boolean continueWriting()
	{
		//ignored this is a blocking process
		return false;
	}

	/**
	 * Waits until the writer has finished all pending writes
	 */
	@Override
	public void close()
	{

	}
}