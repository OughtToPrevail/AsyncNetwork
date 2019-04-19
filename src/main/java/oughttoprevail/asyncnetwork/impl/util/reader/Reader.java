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
package oughttoprevail.asyncnetwork.impl.util.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.DisconnectionType;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.util.Consumer;

public class Reader
{
	private final Queue<Request> pendingRequests;
	private final Channel<?> channel;
	
	public Reader(Channel<?> channel)
	{
		this.pendingRequests = new ArrayDeque<>();
		this.channel = channel;
	}
	
	/**
	 * Reads from the channel who owns this {@link Reader} with the specified readBuffer.
	 *
	 * @param readBuffer the readBuffer which will be used when reading from the channel who owns this {@link Reader}
	 */
	public void read(ByteBuffer readBuffer)
	{
		try
		{
			SocketChannel socketChannel = channel.getSocketChannel();
			int read;
			while((read = socketChannel.read(readBuffer)) != 0 && read != -1)
			{
				callRequests(readBuffer);
				readBuffer.limit(readBuffer.capacity());
				if(readBuffer.position() == readBuffer.capacity())
				{
					channel.manager().bufferOverflow(readBuffer);
				}
			}
			if(read == -1)
			{
				channel.manager().close(DisconnectionType.REMOTE_CLOSE);
			}
		} catch(IOException e)
		{
			Validator.handleRemoteHostCloseException(e, channel);
		}
	}
	
	private boolean callingRequests;
	
	private ByteBuffer slice(ByteBuffer byteBuffer, int bytes)
	{
		int currentLimit = byteBuffer.limit();
		try
		{
			byteBuffer.limit(byteBuffer.position() + bytes);
			return byteBuffer.slice();
		} finally
		{
			byteBuffer.limit(currentLimit);
		}
	}
	
	private void accept(Consumer<ByteBuffer> consumer, ByteBuffer byteBuffer, int requestLength)
	{
		ByteBuffer sliced = slice(byteBuffer, requestLength);
		byteBuffer.position(byteBuffer.position() + requestLength);
		consumer.accept(sliced);
	}
	
	/**
	 * Invokes the pending requests with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer the {@link ByteBuffer} that will be used for calling the requests
	 */
	public void callRequests(ByteBuffer byteBuffer)
	{
		synchronized(pendingRequests)
		{
			channel.manager().callOnRead(byteBuffer);
			if(pendingRequests.isEmpty() || byteBuffer.position() == 0)
			{
				return;
			}
			callingRequests = true;
			byteBuffer.flip();
			do
			{
				int leftInBuffer = byteBuffer.remaining();
				int requestLength = pendingRequests.peek().getRequestLength();
				if(leftInBuffer < requestLength)
				{
					break;
				}
				Request request = pendingRequests.poll();
				accept(request.getRequest(), byteBuffer, requestLength);
			} while(!pendingRequests.isEmpty());
			reset(byteBuffer);
			callingRequests = false;
		}
	}
	
	private void reset(ByteBuffer byteBuffer)
	{
		if(byteBuffer.hasRemaining())
		{
			byteBuffer.compact();
		} else
		{
			byteBuffer.clear();
		}
	}
	
	/**
	 * Invokes {@link Queue#offer(Object)} with a new request
	 * created by {@link Request#Request(Consumer, int)}
	 * then invokes {@link #callRequests(ByteBuffer)} with te specified readBuffer.
	 *
	 * @param readBuffer to call requests with
	 * @param request the consumer that will be used when calling {@link Request#Request(Consumer, int)}
	 * @param requestLength the requestLength that will be used when calling {@link
	 * Request#Request(Consumer, int)}
	 */
	public void addRequest(ByteBuffer readBuffer, Consumer<ByteBuffer> request, int requestLength)
	{
		synchronized(pendingRequests)
		{
			if(pendingRequests.isEmpty())
			{
				if(!callingRequests)
				{
					readBuffer.flip();
				}
				int leftInBuffer = readBuffer.remaining();
				boolean accepted = false;
				if(leftInBuffer >= requestLength)
				{
					boolean setCallingRequests = !callingRequests;
					if(setCallingRequests)
					{
						callingRequests = true;
					}
					accept(request, readBuffer, requestLength);
					if(setCallingRequests)
					{
						callingRequests = false;
					}
					accepted = true;
				}
				if(!callingRequests)
				{
					reset(readBuffer);
				}
				if(accepted)
				{
					return;
				}
			}
			pendingRequests.offer(new Request(request, requestLength));
		}
	}
	
	/**
	 * Clears readers requests
	 */
	public void clear()
	{
		synchronized(pendingRequests)
		{
			pendingRequests.clear();
		}
	}
}