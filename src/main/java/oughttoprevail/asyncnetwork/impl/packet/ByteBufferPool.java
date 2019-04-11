/*
 * MIT License
 *
 * Copyright (c) 2019 Jacob Glickman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package oughttoprevail.asyncnetwork.impl.packet;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import oughttoprevail.asyncnetwork.impl.Util;

/**
 * A pool that can contain both {@code HeapByteBuffer}s and {@code DirectByteBuffer}s.
 * <br><br>
 * {@link ByteBuffer}s dispatched from this pool will be reused, resulting in significant performance improvements from
 * not having to constantly allocate new {@link ByteBuffer}s.
 *
 * @author Jacob G.
 * @since February 23, 2019
 */
public class ByteBufferPool
{
	/**
	 * Max direct buffer to be allocated, equals to 8 mega bytes.
	 */
	private static final int EIGHT_MB = 1000000 * 8;
	
	/**
	 * Static INSTANCE of this {@link ByteBufferPool}.
	 */
	public static final ByteBufferPool INSTANCE = new ByteBufferPool();
	
	/**
	 * The buffer map, the key is the size of the buffers in the value,
	 * the value is the {@link Deque} containing the buffers.
	 */
	private final NavigableMap<Integer, Deque<ByteBuffer>> buffers = new TreeMap<>();
	
	/**
	 * Current size of direct {@link ByteBuffer} allocated by this {@link ByteBufferPool}.
	 */
	private int size;
	
	/**
	 * Creates a new direct {@link ByteBuffer}.
	 * If the current {@link #size} is more than {@link #EIGHT_MB}
	 * then clean the biggest {@link ByteBuffer} from {@link #buffers}
	 * until we return to less than {@link #EIGHT_MB} or
	 * {@link #buffers} is empty.
	 *
	 * @param size to create the direct {@link ByteBuffer}
	 * @return a new direct {@link ByteBuffer}
	 */
	private ByteBuffer create(int size)
	{
		this.size += size;
		if(this.size > EIGHT_MB)
		{
			//Clean buffers until there isn't more than 8 MB of space or the buffers is empty
			mainLoop:
			for(Deque<ByteBuffer> deque : buffers.descendingMap().values())
			{
				while(!deque.isEmpty())
				{
					ByteBuffer value = deque.poll();
					Util.dispose(value);
					this.size -= value.capacity();
					if(this.size <= EIGHT_MB)
					{
						break mainLoop;
					}
				}
			}
		}
		return ByteBuffer.allocateDirect(size);
	}
	
	/**
	 * Takes a bigger or equal to the specified size direct {@link ByteBuffer} from the queue if one
	 * exists, if one doesn't exist a new {@link ByteBuffer} with the specified size is created.
	 *
	 * @param size of the requested {@link ByteBuffer}
	 * @return a direct {@link ByteBuffer} from the queue if one
	 * exists, if one doesn't exist a new {@link ByteBuffer} is created
	 */
	public ByteBuffer take(int size)
	{
		synchronized(buffers)
		{
			Entry<Integer, Deque<ByteBuffer>> entry = buffers.ceilingEntry(size);
			
			// If entry is null, there exists no ByteBuffer within the map with a capacity greater than or equal to
			// the value requested. For that reason, one should be created.
			if(entry == null)
			{
				buffers.put(size, new ArrayDeque<>(3));
				return create(size);
			}
			
			// Even though the entry isn't null, the deque that was found may not be. If it isn't, a ByteBuffer
			// should be taken from there and returned.
			Deque<ByteBuffer> deque = entry.getValue();
			
			if(!deque.isEmpty())
			{
				return (ByteBuffer) deque.poll().clear().limit(size);
			}
			
			// The first entry that was found had no ByteBuffers available, so we must now look at every greater
			// entry to see if one can be found. If one still cannot be found, allocate a new one.
			Collection<Deque<ByteBuffer>> tailMap = buffers.tailMap(size, false).values();
			for(Deque<ByteBuffer> byteBuffers : tailMap)
			{
				if(!byteBuffers.isEmpty())
				{
					return (ByteBuffer) byteBuffers.poll().clear().limit(size);
				}
			}
			return create(size);
		}
	}
	
	/**
	 * Takes a equal to the specified size direct {@link ByteBuffer} from the queue if one
	 * exists, if one doesn't exist a new {@link ByteBuffer} with the specified size is created.
	 *
	 * @param size of the requested {@link ByteBuffer}
	 * @return a direct {@link ByteBuffer} from the queue if one
	 * exists, if one doesn't exist a new {@link ByteBuffer} is created
	 */
	public ByteBuffer takeExactly(int size)
	{
		synchronized(buffers)
		{
			Deque<ByteBuffer> byteBuffers = buffers.get(size);
			if(byteBuffers == null)
			{
				buffers.put(size, new ArrayDeque<>(3));
				return create(size);
			}
			return byteBuffers.isEmpty() ? create(size) : byteBuffers.poll();
		}
	}
	
	/**
	 * Gives the specified byteBuffer to the queue.
	 *
	 * @param byteBuffer to give to the queue
	 */
	public void give(ByteBuffer byteBuffer)
	{
		synchronized(buffers)
		{
			Deque<ByteBuffer> byteBuffers = buffers.get(byteBuffer.capacity());
			if(byteBuffers == null)
			{
				byteBuffers = new ArrayDeque<>();
				buffers.put(byteBuffer.capacity(), byteBuffers);
			}
			byteBuffers.offerLast(byteBuffer);
		}
	}
}