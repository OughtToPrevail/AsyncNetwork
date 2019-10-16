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
package oughttoprevail.asyncnetwork.util.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.DisconnectionType;
import oughttoprevail.asyncnetwork.util.Predicate;
import oughttoprevail.asyncnetwork.util.Validator;

public class Reader
{
	private final Deque<Request> pendingRequests;
	private final Deque<Request> prepend;
	
	public Reader()
	{
		this.pendingRequests = new ArrayDeque<>();
		this.prepend = new ArrayDeque<>();
	}
	
	/**
	 * Reads from the socket who owns this {@link Reader} with the socket's readBuffer.
	 */
	public void read(Socket socket)
	{
		try
		{
			SocketChannel socketChannel = socket.getSocketChannel();
			ByteBuffer readBuffer = socket.manager().getReadByteBuffer().getByteBuffer();
			while(tryRead(socket, socketChannel, readBuffer))
				;
		} catch(IOException e)
		{
			Validator.handleRemoteHostCloseException(socket, e);
		}
	}
	
	protected boolean tryRead(Socket socket, SocketChannel socketChannel, ByteBuffer readBuffer) throws IOException
	{
		int read = socketChannel.read(readBuffer);
		if(read == 0)
		{
			return false;
		} else if(read == -1)
		{
			socket.manager().close(DisconnectionType.REMOTE_CLOSE);
			return false;
		}
		callRequests(readBuffer);
		readBuffer.limit(readBuffer.capacity());
		if(socket.isClosed())
		{
			return false;
		}
		if(readBuffer.position() == readBuffer.capacity())
		{
			socket.manager().bufferOverflow(readBuffer);
		}
		return true;
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
	
	private boolean test(Predicate<ByteBuffer> predicate, ByteBuffer byteBuffer, int requestLength)
	{
		ByteBuffer sliced = slice(byteBuffer, requestLength);
		byteBuffer.position(byteBuffer.position() + requestLength);
		return predicate.test(sliced);
	}
	
	/**
	 * Invokes the pending requests with the specified {@link ByteBuffer}.
	 *
	 * @param byteBuffer the {@link ByteBuffer} that will be used for calling the requests
	 */
	public void callRequests(ByteBuffer byteBuffer)
	{
		if(byteBuffer == null)
		{
			return;
		}
		synchronized(pendingRequests)
		{
			byte[] bytes = new byte[byteBuffer.flip().limit()];
			byteBuffer.get(bytes);
			byteBuffer.limit(byteBuffer.capacity());
			if(pendingRequests.isEmpty() || byteBuffer.position() == 0)
			{
				return;
			}
			callingRequests = true;
			try
			{
				byteBuffer.flip();
				do
				{
					int leftInBuffer = byteBuffer.remaining();
					Request request = pendingRequests.peekLast();
					int requestLength = request.getRequestLength();
					if(leftInBuffer < requestLength)
					{
						break;
					}
					if(!test(request.getRequest(), byteBuffer, requestLength))
					{
						pendingRequests.pollLast();
					}
					prepend();
				} while(!pendingRequests.isEmpty());
			} finally
			{
				callingRequests = false;
				reset(byteBuffer);
			}
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
	 * created by {@link Request#Request(Predicate, int)}.
	 * If there is no pending requests it will check if there is enough data in the
	 * specified readBuffer for the specified request, if yes then invoke it.
	 *
	 * @param readBuffer to call requests with
	 * @param request the consumer that will be used when calling {@link Request#Request(Predicate, int)}
	 * @param requestLength the requestLength that will be used when calling {@link
	 * Request#Request(Predicate, int)}
	 */
	public void addRequest(ByteBuffer readBuffer, Predicate<ByteBuffer> request, int requestLength)
	{
		synchronized(pendingRequests)
		{
			Request requestObject = new Request(request, requestLength);
			if(callingRequests)
			{
				prepend.offerFirst(requestObject);
			} else
			{
				pendingRequests.offerFirst(requestObject);
				callRequests(readBuffer);
			}
		}
	}
	
	private void prepend()
	{
		Request prependRequest;
		while((prependRequest = prepend.pollFirst()) != null)
		{
			pendingRequests.offerLast(prependRequest);
		}
	}
}