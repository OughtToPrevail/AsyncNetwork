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
package oughttoprevail.asyncnetwork.impl.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import oughttoprevail.asyncnetwork.impl.packet.ByteBufferElement;
import oughttoprevail.asyncnetwork.impl.packet.ByteBufferPool;
import oughttoprevail.asyncnetwork.util.IndexesBuffer;

public class IndexesBufferImpl implements IndexesBuffer
{
	private final ByteBufferElement byteBufferElement;
	private final ByteBuffer byteBuffer;
	
	public IndexesBufferImpl(int size)
	{
		this.byteBufferElement = ByteBufferPool.getInstance().take(size);
		this.byteBuffer = byteBufferElement.getByteBuffer();
		byteBuffer.order(ByteOrder.nativeOrder());
	}
	
	/**
	 * Returns the next index.
	 *
	 * @return the next index
	 */
	@Override
	public int get()
	{
		return byteBuffer.getInt();
	}
	
	/**
	 * Resets the position of the buffer to 0.
	 */
	@Override
	public void clear()
	{
		byteBuffer.clear();
	}
	
	/**
	 * Returns the address of the buffer.
	 *
	 * @return the address of the buffer
	 */
	@Override
	public long getAddress()
	{
		return byteBufferElement.address();
	}
	
	/**
	 * Closes the buffer.
	 */
	@Override
	public void close()
	{
		ByteBufferPool.getInstance().give(byteBufferElement);
	}
}
