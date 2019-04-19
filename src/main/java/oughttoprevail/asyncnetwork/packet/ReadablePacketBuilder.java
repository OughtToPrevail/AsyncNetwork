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
package oughttoprevail.asyncnetwork.packet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import oughttoprevail.asyncnetwork.impl.Util;
import oughttoprevail.asyncnetwork.impl.packet.ReadablePacketBuilderImpl;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.Predicate;

/**
 * Implementation at {@link ReadablePacketBuilderImpl}.
 */
public interface ReadablePacketBuilder
{
	/**
	 * Constructs a new {@link ReadablePacketBuilder}.
	 *
	 * @return a new {@link ReadablePacketBuilder}
	 */
	static ReadablePacketBuilder create()
	{
		return new ReadablePacketBuilderImpl();
	}
	
	/**
	 * Constructs a new {@link ReadablePacketBuilder}.
	 *
	 * @param skip whether to skip adding the length read from {@link PassedNumber}
	 * to the {@link ReadResult}
	 * @return a new {@link ReadablePacketBuilder}
	 */
	static ReadablePacketBuilder create(boolean skip)
	{
		return new ReadablePacketBuilderImpl(skip);
	}
	
	/**
	 * Reads a single {@code byte}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aByte();
	
	/**
	 * Reads a {@code byte[]} with the length of the specified bytes.
	 *
	 * @param bytes length of the wanted {@code byte[]}.
	 * @return this
	 */
	ReadablePacketBuilder bytes(int bytes);
	
	/**
	 * Reads a {@code byte[]} with the length the specified passedNumber returns.
	 *
	 * @param passedNumber which will return the length of the wanted {@code byte[]}
	 * @return this
	 */
	ReadablePacketBuilder bytes(PassedNumber<?> passedNumber);
	
	/**
	 * Reads a single {@code byte} and converts it to a {@code boolean}.
	 * The conversation is done by checking whether the {@code byte} is equal to 1,
	 * if it does equal to 1 then the {@code boolean} is {@code true} else, it is {@code false}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aBoolean();
	
	/**
	 * Reads a single {@code char}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aChar();
	
	/**
	 * Reads a single {@code short}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aShort();
	
	/**
	 * Reads a single {@code int}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aInt();
	
	/**
	 * Reads a single {@code float}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aFloat();
	
	/**
	 * Reads a single {@code long}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aLong();
	
	/**
	 * Reads a single {@link String} by reading an unsigned {@code short} which will define
	 * the length of the {@link String} then a {@code byte[]} which is then converted
	 * to a {@link String} with {@link String#String(byte[], Charset)} with the {@link Charset}
	 * specified as {@link Util#UTF_8}.
	 *
	 * @return this
	 */
	ReadablePacketBuilder aString();
	
	/**
	 * Reads a single {@link Object}. If the specified serDes returns
	 * {@code true} in {@link SerDes#isFixedLength()} then reads a {@link ByteBuffer} which
	 * contains {@link SerDes#getSerializedLength(Object)} amount of bytes and deserializes it with
	 * the specified serDes. If {@link SerDes#isFixedLength()} returns {@code false} then it first reads an
	 * unsigned {@code short} which defines the length of the {@link Object} and then reads a
	 * {@link ByteBuffer} and deserializes it with the specified serDes.
	 *
	 * @param serDes which will deserialize the read {@link ByteBuffer}.
	 * @return this
	 */
	ReadablePacketBuilder aObject(SerDes<?> serDes);
	
	/**
	 * Reads a {@code boolean} then add a {@link #conditioned(Predicate, Consumer)} and if the
	 * {@code boolean} is true then rea a {@link #aObject(SerDes)} else add {@code null} to
	 * the {@link ReadResult}.
	 *
	 * @param serDes which will deserialize the read {@link ByteBuffer}.
	 * @return this
	 */
	ReadablePacketBuilder aNullableObject(SerDes<?> serDes);
	
	/**
	 * Invokes the specified consumer if the specified condition returns {@code true}.
	 *
	 * @param condition which will decide whether to invoke the specified consumer
	 * @param consumer to invoke if the specified condition is {@code true}
	 * @return this
	 */
	ReadablePacketBuilder conditioned(boolean condition, Consumer<ReadablePacketBuilder> consumer);
	
	/**
	 * Invokes the specified consumer and all read requests invocations of this packet
	 * within the consumer will only run if the specified predicate returns {@code true}.
	 *
	 * @param predicate which will decide whether to continue with the invocations in the specified consumer
	 * @param consumer which will run the operations conditioned by the specified predicate
	 * @return this
	 */
	ReadablePacketBuilder conditioned(Predicate<ReadResult> predicate, Consumer<ReadablePacketBuilder> consumer);
	
	/**
	 * Repeats all read requests.
	 *
	 * @return this
	 */
	ReadablePacketBuilder repeat();
	
	/**
	 * Repeats all read requests specified timesToRepeat times.
	 *
	 * @param timesToRepeat amount of times to repeat all read requests
	 * @return this
	 */
	ReadablePacketBuilder repeat(int timesToRepeat);
	
	/**
	 * Returns a new {@link ReadablePacket} based on the entered parameters.
	 *
	 * @return a new {@link ReadablePacket} based on the entered parameters
	 */
	ReadablePacket build();
	
	PassedNumber<Byte> PASSABLE_BYTE = new PassedNumber<Byte>()
	{
		@Override
		public Byte get(ByteBuffer byteBuffer)
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
		public Short get(ByteBuffer byteBuffer)
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
		public Short get(ByteBuffer byteBuffer)
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
		public Integer get(ByteBuffer byteBuffer)
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
		public Integer get(ByteBuffer byteBuffer)
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