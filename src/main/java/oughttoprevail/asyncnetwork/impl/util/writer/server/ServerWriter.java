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
package oughttoprevail.asyncnetwork.impl.util.writer.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.DisconnectionType;
import oughttoprevail.asyncnetwork.ServerClient;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.impl.util.writer.Writer;
import oughttoprevail.asyncnetwork.util.Consumer;

;

public class ServerWriter implements Writer<ServerClient>
{
	private final Deque<ServerPendingWrite> pendingWrites = new ArrayDeque<>();
	private final AtomicBoolean exceptingNotification = new AtomicBoolean();
	private CountDownLatch notifiable;
	
	/**
	 * Writes the specified writeBuffer into the specified channel.
	 * Once a write has finished the specified onWriteFinished is invoked with the specified writeBuffer.
	 *
	 * @param channel the channel that the specified writeBuffer will be written to
	 * @param writeBuffer the {@link ByteBuffer} that will be written into the channel
	 * @param onWriteFinished the {@link Runnable} to be called when the write has finished
	 */
	@Override
	public void write(ServerClient channel, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished)
	{
		synchronized(pendingWrites)
		{
			if(exceptingNotification.get())
			{
				return;
			}
			if(!pendingWrites.isEmpty())
			{
				pendingWrites.offerLast(new ServerPendingWrite(channel, writeBuffer, onWriteFinished));
				return;
			}
		}
		try
		{
			SocketChannel socketChannel = channel.getSocketChannel();
			int writtenBytes = socketChannel.write(writeBuffer);
			if(writtenBytes == -1)
			{
				channel.manager().close(DisconnectionType.REMOTE_CLOSE);
				return;
			}
			if(writtenBytes == writeBuffer.limit())
			{
				synchronized(pendingWrites)
				{
					if(exceptingNotification.get())
					{
						notifiable.countDown();
					}
				}
				Channel.finishWrite(channel, onWriteFinished, writeBuffer);
				return;
			}
			SelectionKey selectionKey = channel.manager().getSelectionKey();
			if(selectionKey != null)
			{
				//interest writing
				selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				selectionKey.selector().wakeup();
			}
			synchronized(pendingWrites)
			{
			pendingWrites.offerLast(new ServerPendingWrite(channel, writeBuffer, onWriteFinished));
		}
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
		synchronized(pendingWrites)
		{
			ServerPendingWrite pendingWrite;
			while((pendingWrite = pendingWrites.pollFirst()) != null)
			{
				int currentSize = pendingWrites.size();
				write(pendingWrite.client, pendingWrite.getWriteBuffer(), pendingWrite.getOnWriteFinished());
				boolean changed = pendingWrites.size() == currentSize;
				if(changed)
				{
					//if the size has changed it means that a write has failed to finish so you have to wait until you can write again
					break;
				}
			}
			return false;
		}
	}
	
	/**
	 * Waits until the writer has finished all pending writes
	 */
	@Override
	public void close()
	{
	
	}
	
	private static class ServerPendingWrite extends PendingWrite
	{
		private final ServerClient client;
		
		private ServerPendingWrite(ServerClient client, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished)
		{
			super(writeBuffer, onWriteFinished);
			this.client = client;
		}
	}
}
