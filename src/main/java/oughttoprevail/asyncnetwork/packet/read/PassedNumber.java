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
package oughttoprevail.asyncnetwork.packet.read;

import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.packet.Deserializer;
import oughttoprevail.asyncnetwork.util.Util;

public interface PassedNumber<T extends Number> extends Deserializer<T>
{
	/**
	 * Returns a {@link Number} read from the specified byteBuffer.
	 *
	 * @param byteBuffer containing the {@link Number} to return
	 * @return a {@link Number} read from the specified byteBuffer
	 */
	T convert(ByteBuffer byteBuffer);
	
	/**
	 * Returns the size in bytes of the number returned from {@link PassedNumber}.
	 *
	 * @return the size in bytes of the number returned from {@link PassedNumber}
	 */
	int getSize();
	
	PassedNumber<Byte> PASSABLE_BYTE = new PassedNumber<Byte>()
	{
		@Override
		public void prepareDeserialization(ReadablePacketBuilder builder)
		{
			builder.aByte();
		}
		
		@Override
		public Byte deserialize(ReadResult readResult)
		{
			return readResult.pollLast();
		}
		
		@Override
		public Byte convert(ByteBuffer byteBuffer)
		{
			return byteBuffer.get();
		}
		
		@Override
		public int getSize()
		{
			return Util.BYTE_BYTES;
		}
	};
	PassedNumber<Short> PASSABLE_UNSIGNED_BYTE = new PassedNumber<Short>()
	{
		@Override
		public void prepareDeserialization(ReadablePacketBuilder builder)
		{
			builder.aByte();
		}
		
		@Override
		public Short deserialize(ReadResult readResult)
		{
			return Util.toUnsignedShort(readResult.pollLast());
		}
		
		@Override
		public Short convert(ByteBuffer byteBuffer)
		{
			return Util.toUnsignedShort(byteBuffer.get());
		}
		
		@Override
		public int getSize()
		{
			return Util.BYTE_BYTES;
		}
	};
	PassedNumber<Short> PASSABLE_SHORT = new PassedNumber<Short>()
	{
		@Override
		public void prepareDeserialization(ReadablePacketBuilder builder)
		{
			builder.aShort();
		}
		
		@Override
		public Short deserialize(ReadResult readResult)
		{
			return readResult.pollLast();
		}
		
		@Override
		public Short convert(ByteBuffer byteBuffer)
		{
			return byteBuffer.getShort();
		}
		
		@Override
		public int getSize()
		{
			return Util.SHORT_BYTES;
		}
	};
	PassedNumber<Integer> PASSABLE_UNSIGNED_SHORT = new PassedNumber<Integer>()
	{
		@Override
		public void prepareDeserialization(ReadablePacketBuilder builder)
		{
			builder.aShort();
		}
		
		@Override
		public Integer deserialize(ReadResult readResult)
		{
			return Util.toUnsignedInt(readResult.pollLast());
		}
		
		@Override
		public Integer convert(ByteBuffer byteBuffer)
		{
			return Util.toUnsignedInt(byteBuffer.getShort());
		}
		
		@Override
		public int getSize()
		{
			return Util.SHORT_BYTES;
		}
	};
	PassedNumber<Integer> PASSABLE_INTEGER = new PassedNumber<Integer>()
	{
		@Override
		public void prepareDeserialization(ReadablePacketBuilder builder)
		{
			builder.aInt();
		}
		
		@Override
		public Integer deserialize(ReadResult readResult)
		{
			return readResult.pollLast();
		}
		
		@Override
		public Integer convert(ByteBuffer byteBuffer)
		{
			return byteBuffer.getInt();
		}
		
		@Override
		public int getSize()
		{
			return Util.INT_BYTES;
		}
	};
}