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
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

import oughttoprevail.asyncnetwork.ServerClientManager;
import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.writer.Writer;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.DisconnectionType;

public class ServerWriter implements Writer
{
	private final Queue<ServerPendingWrite> pendingWrites = new ArrayDeque<>();
	
	private void write(Socket socket,
			ByteBuffer writeBuffer,
			Consumer<ByteBuffer> onWriteFinished,
			boolean continueWriting)
	{
		if(!continueWriting)
		{
			synchronized(pendingWrites)
			{
				if(!pendingWrites.isEmpty())
				{
					pendingWrites.offer(new ServerPendingWrite(socket, writeBuffer, onWriteFinished));
					return;
				}
			}
		}
		try
		{
			SocketChannel socketChannel = socket.getSocketChannel();
			int writtenBytes = socketChannel.write(writeBuffer);
			if(writtenBytes == -1)
			{
				socket.manager().close(DisconnectionType.REMOTE_CLOSE);
				return;
			}
			if(writtenBytes == writeBuffer.limit())
			{
				socket.manager().finishWrite(onWriteFinished, writeBuffer);
				return;
			}
			SelectionKey selectionKey = ((ServerClientManager) socket.manager()).getSelectionKey();
			if(selectionKey != null)
			{
				//interest writing
				selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				selectionKey.selector().wakeup();
			}
			synchronized(pendingWrites)
			{
				pendingWrites.offer(new ServerPendingWrite(socket, writeBuffer, onWriteFinished));
			}
		} catch(IOException e)
		{
			Validator.handleRemoteHostCloseException(socket, e);
		}
	}
	
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
		write(socket, writeBuffer, onWriteFinished, false);
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
			while((pendingWrite = pendingWrites.poll()) != null)
			{
				int currentSize = pendingWrites.size();
				write(pendingWrite.socket, pendingWrite.getWriteBuffer(), pendingWrite.getOnWriteFinished(), true);
				boolean changed = pendingWrites.size() == currentSize;
				if(changed)
				{
					//if the size has changed it means that a write has failed to finish so you have to wait until you can write again
					return true;
				}
			}
			return false;
		}
	}
	
	private static class ServerPendingWrite extends PendingWrite
	{
		private final Socket socket;
		
		private ServerPendingWrite(Socket socket, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished)
		{
			super(writeBuffer, onWriteFinished);
			this.socket = socket;
		}
	}
}
