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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import oughttoprevail.asyncnetwork.ServerClient;
import oughttoprevail.asyncnetwork.impl.Util;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.impl.util.writer.Writer;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.WindowsSelector;

public class WindowsWriter implements Writer<ServerClient>
{
	private final AtomicInteger pendingWrites = new AtomicInteger();
	private final AtomicBoolean exceptingNotification = new AtomicBoolean();
	private CountDownLatch notifiable;
	
	/**
	 * Writes the specified writeBuffer into the specified channel.
	 * Once a write has finished the specified onWriteFinished is invoked with the specified writeBuffer.
	 *
	 * @param channel which will write the specified writeBuffer
	 * @param writeBuffer to write into the specified channel
	 * @param onWriteFinished which will be invoked when the write has finished
	 */
	@Override
	public void write(ServerClient channel, ByteBuffer writeBuffer, Consumer<ByteBuffer> onWriteFinished)
	{
		WindowsSelector selector = (WindowsSelector) channel.manager().getServer().manager().getSelector();
		int length = writeBuffer.remaining();
		try
		{
			synchronized(pendingWrites)
			{
				if(exceptingNotification.get())
				{
					return;
				}
				pendingWrites.getAndIncrement();
				selector.WSASend(channel.manager().getFD(),
						Util.address(writeBuffer) + writeBuffer.position(),
						length,
						new PendingWrite(writeBuffer, pendingWriteBuffer ->
						{
							synchronized(pendingWrites)
							{
								if(exceptingNotification.get())
								{
									notifiable.countDown();
								} else
								{
									pendingWrites.getAndDecrement();
								}
							}
							if(onWriteFinished != null)
							{
								onWriteFinished.accept(pendingWriteBuffer);
							}
						}));
			}
		} catch(IOException e)
		{
			Validator.exceptionClose(channel, e);
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
	
	/**
	 * Waits until the writer has finished all pending writes
	 */
	@Override
	public void close()
	{
		//make sure you don't wait inside the {@code synchronized}
		boolean needWaiting = false;
		synchronized(pendingWrites)
		{
			int writes = pendingWrites.getAndSet(0);
			if(writes > 0)
			{
				needWaiting = true;
				notifiable = new CountDownLatch(writes);
				exceptingNotification.set(true);
			}
		}
		if(needWaiting)
		{
			try
			{
				notifiable.await();
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
