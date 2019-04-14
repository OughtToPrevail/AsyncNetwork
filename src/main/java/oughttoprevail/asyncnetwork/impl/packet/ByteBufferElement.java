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
package oughttoprevail.asyncnetwork.impl.packet;

import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.impl.Util;

public class ByteBufferElement
{
	private final ByteBuffer byteBuffer;
	private final ByteBuffer original;
	
	public ByteBufferElement(ByteBuffer byteBuffer)
	{
		this(byteBuffer, null);
	}
	
	public ByteBufferElement(ByteBuffer byteBuffer, ByteBuffer original)
	{
		this.byteBuffer = byteBuffer;
		this.original = original;
	}
	
	public ByteBuffer getByteBuffer()
	{
		return byteBuffer;
	}
	
	ByteBuffer getOriginal()
	{
		if(original == null)
		{
			return byteBuffer;
		}
		return original;
	}
	
	private volatile long address = -1;
	
	public long address()
	{
		if(address == -1)
		{
			address = Util.address(byteBuffer);
		}
		return address;
	}
}