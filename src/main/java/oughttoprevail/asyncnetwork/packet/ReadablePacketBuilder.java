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

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.Function;
import oughttoprevail.asyncnetwork.util.Predicate;
import oughttoprevail.asyncnetwork.util.TriConsumer;
import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.util.Validator;

public class ReadablePacketBuilder
{
	public static ReadablePacketBuilder create()
	{
		return new ReadablePacketBuilder();
	}
	
	public static ReadablePacketBuilder create(boolean skip)
	{
		return new ReadablePacketBuilder(skip);
	}
	
	private ReadableElement topMostParent;
	private ReadableElement currentReadableElement;
	private final boolean skip;
	
	public ReadablePacketBuilder()
	{
		this(false);
	}
	
	public ReadablePacketBuilder(boolean skip)
	{
		this.skip = skip;
		topMostParent = new ReadableElement(null, null);
		currentReadableElement = topMostParent;
	}
	
	/**
	 * Adds the specified consumer and size to the {@code currentReadableElement}.
	 *
	 * @param consumer which will add to the {@link ReadResult}
	 * @param size which will define the size of how many results will be added
	 * to the {@link ReadResult}
	 * @return this
	 */
	private ReadablePacketBuilder add(Consumer<ReadResultImpl> consumer, int size)
	{
		System.out.println("Add consumer");
		currentReadableElement.add(consumer, size);
		return this;
	}
	
	/**
	 * Invokes {@link #add(Consumer, int)} and uses the specified consumer to read from the
	 * {@link Socket} a {@link ByteBuffer} then invokes the specified consumer with the
	 * read {@link ByteBuffer} and the given {@link ReadResultImpl}.
	 *
	 * @param consumer to invoke with the read {@link ByteBuffer} and {@link ReadResultImpl}
	 * @param bytes to read
	 * @return this
	 */
	private ReadablePacketBuilder offerByteBuffer(BiConsumer<ByteBuffer, ReadResultImpl> consumer, int bytes)
	{
		return add((readResult -> readResult.socket().readByteBuffer(byteBuffer -> consumer.accept(byteBuffer,
				readResult), bytes)), 1);
	}
	
	/**
	 * Invokes {@link #add(Consumer, int)} and uses the specified consumer to read from the
	 * {@link Socket} a {@link ByteBuffer} then invokes the specified consumer with the
	 * read {@link ByteBuffer}, then the specified passedNumber is invoked and the number returned
	 * is added to the readResult if the specified skip is false, the returned numbere is also
	 * later used when invoking the specified consumer and when invoking {@link Socket#readByteBuffer(Consumer, int)}
	 * as the length.
	 * Then the final read byteBuffer is given to the {@link ReadResultImpl}.
	 *
	 * @param consumer to invoke with the read {@link ByteBuffer} and {@link ReadResultImpl}
	 * @param passedNumber which will define how many bytes the read requires
	 * @param skip determines whether this should add the length returned by the specified
	 * passedNumber into the readResult
	 * @return this
	 */
	private ReadablePacketBuilder offerByteBuffer(TriConsumer<ByteBuffer, ReadResultImpl, Integer> consumer,
			PassedNumber passedNumber, boolean skip)
	{
		Validator.requireNonNull(passedNumber, "PassedNumber");
		return add(readResult -> readResult.socket().readByteBuffer(byteBuffer ->
		{
			Number length = passedNumber.apply(byteBuffer);
			int intLength = length.intValue();
			if(!skip)
			{
				readResult.add(length);
			}
			Socket socket = readResult.socket();
			if(intLength < 0)
			{
				socket.manager().exception(new IllegalArgumentException("Received length less then zero! (" + intLength + ")"));
				return;
			}
			socket.readByteBuffer(byteBuffer1 -> consumer.accept(byteBuffer1, readResult, intLength), intLength);
		}, passedNumber.getSize()), skip ? 1 : 2);
	}
	
	/**
	 * Reads a single {@code byte}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aByte()
	{
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(byteBuffer.get()), Util.BYTE_BYTES);
	}
	
	/**
	 * Reads a {@code byte[]} with the length of the specified bytes.
	 *
	 * @param bytes length of the wanted {@code byte[]}.
	 * @return this
	 */
	public ReadablePacketBuilder bytes(int bytes)
	{
		Validator.higherThan0(bytes, "Bytes");
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(Util.getBytes(byteBuffer, bytes)), bytes);
	}
	
	/**
	 * Reads a {@code byte[]} with the length the specified passedNumber returns.
	 *
	 * @param passedNumber which will return the length of the wanted {@code byte[]}
	 * @return this
	 */
	public ReadablePacketBuilder bytes(PassedNumber passedNumber)
	{
		Validator.requireNonNull(passedNumber, "PassedNumber");
		return offerByteBuffer((byteBuffer, readResult, length) -> readResult.add(Util.getBytes(byteBuffer, length)),
				passedNumber, skip);
	}
	
	/**
	 * Reads a single {@code byte} and converts it to a {@code boolean}.
	 * The conversation is done by checking whether the {@code byte} is equal to 1,
	 * if it does equal to 1 then the {@code boolean} is {@code true} else, it is {@code false}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aBoolean()
	{
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(Util.toBoolean(byteBuffer)), Util.BYTE_BYTES);
	}
	
	/**
	 * Reads a single {@code char}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aChar()
	{
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(byteBuffer.getChar()), Util.CHAR_BYTES);
	}
	
	/**
	 * Reads a single {@code short}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aShort()
	{
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(byteBuffer.getShort()), Util.SHORT_BYTES);
	}
	
	/**
	 * Reads a single {@code int}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aInt()
	{
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(byteBuffer.getInt()), Util.INT_BYTES);
	}
	
	/**
	 * Reads a single {@code float}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aFloat()
	{
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(byteBuffer.getFloat()), Util.FLOAT_BYTES);
	}
	
	/**
	 * Reads a single {@code long}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aLong()
	{
		return offerByteBuffer((byteBuffer, readResult) -> readResult.add(byteBuffer.getLong()), Util.LONG_BYTES);
	}
	
	/**
	 * Reads a single {@link E} as if the received ordinal was a byte.
	 *
	 * @param cls class of {@link E}
	 * @param <E> type of {@link E}
	 * @return this
	 */
	public <E extends Enum<E>> ReadablePacketBuilder aEnumByte(Class<E> cls)
	{
		return aEnum(byteBuffer -> (int) byteBuffer.get(), Util.BYTE_BYTES, cls);
	}
	
	/**
	 * Reads a single {@link E} as if the received ordinal was a short.
	 *
	 * @param cls class of {@link E}
	 * @param <E> type of {@link E}
	 * @return this
	 */
	public <E extends Enum<E>> ReadablePacketBuilder aEnumShort(Class<E> cls)
	{
		return aEnum(byteBuffer -> (int) byteBuffer.getShort(), Util.SHORT_BYTES, cls);
	}
	
	/**
	 * Reads a single {@link E} as if the received ordinal was a int.
	 *
	 * @param cls class of {@link E}
	 * @param <E> type of {@link E}
	 * @return this
	 */
	public <E extends Enum<E>> ReadablePacketBuilder aEnumInt(Class<E> cls)
	{
		return aEnum(ByteBuffer::getInt, Util.INT_BYTES, cls);
	}
	
	/**
	 * Reads a single {@link E} using the received ordinal returned from the consumer.
	 *
	 * @param consumer to read the ordinal
	 * @param bytes the byteBuffer should contain for the consumer to return the ordinal
	 * @param cls class of {@link E}
	 * @param <E> type of {@link E}
	 * @return this
	 */
	private <E extends Enum<E>> ReadablePacketBuilder aEnum(Function<ByteBuffer, Integer> consumer, int bytes, Class<E> cls)
	{
		return offerByteBuffer((byteBuffer, readResult) ->
		{
			E[] enumConstants = cls.getEnumConstants();
			int aInt = consumer.apply(byteBuffer);
			if(aInt > enumConstants.length || aInt < 0)
			{
				readResult.socket().manager().exception(new IllegalArgumentException("Received illegal enum number: " + aInt + "!"));
				return;
			}
			readResult.add(enumConstants[aInt]);
		}, bytes);
	}
	
	/**
	 * Reads a single {@link String} by reading an unsigned {@code short} which will define
	 * the length of the {@link String} then a {@code byte[]} which is then converted
	 * to a {@link String} with {@link String#String(byte[], Charset)} with the {@link Charset}
	 * specified as {@link Util#UTF_8}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aString()
	{
		return offerByteBuffer((byteBuffer, readResult, length) -> readResult.add(new String(Util.getBytes(byteBuffer,
				length), Util.UTF_8)), PassedNumber.PASSABLE_INTEGER, true);
	}
	
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
	public ReadablePacketBuilder aObject(SerDes<?> serDes)
	{
		if(serDes.isFixedLength())
		{
			int serializedLength = serDes.getSerializedLength(null);
			Validator.higherThan0(serializedLength, "SerializedLength");
			return offerByteBuffer((byteBuffer, readResult) -> readResult.add(serDes.deserialize(byteBuffer,
					serializedLength)), serializedLength);
		} else
		{
			return offerByteBuffer((byteBuffer, readResult, length) -> readResult.add(serDes.deserialize(byteBuffer,
					length)), PassedNumber.PASSABLE_INTEGER, true);
		}
	}
	
	/**
	 * Invokes the specified consumer if the specified condition returns {@code true}.
	 *
	 * @param condition which will decide whether to invoke the specified consumer
	 * @param consumer to invoke if the specified condition is {@code true}
	 * @return this
	 */
	public ReadablePacketBuilder conditioned(boolean condition, Consumer<ReadablePacketBuilder> consumer)
	{
		Validator.requireNonNull(consumer, "Consumer");
		if(condition)
		{
			consumer.accept(this);
		}
		return this;
	}
	
	/**
	 * Invokes the specified consumer and all read requests invocations of this packet
	 * within the consumer will only run if the specified predicate returns {@code true}.
	 *
	 * @param predicate which will decide whether to continue with the invocations in the specified consumer
	 * @param consumer which will run the operations conditioned by the specified predicate
	 * @return this
	 */
	public ReadablePacketBuilder conditioned(Predicate<ReadResult> predicate, Consumer<ReadablePacketBuilder> consumer)
	{
		Validator.requireNonNull(predicate, "Condition");
		Validator.requireNonNull(consumer, "Consumer");
		ReadableElement newElement = new ReadableElement(predicate, null);
		currentReadableElement.addChild(newElement);
		ReadableElement previous = currentReadableElement;
		currentReadableElement = newElement;
		consumer.accept(this);
		currentReadableElement = previous;
		return this;
	}
	
	/**
	 * Repeats all read requests.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder repeat()
	{
		return repeat(1);
	}
	
	/**
	 * Repeats all read requests specified timesToRepeat times.
	 *
	 * @param timesToRepeat amount of times to repeat all read requests
	 * @return this
	 */
	public ReadablePacketBuilder repeat(int timesToRepeat)
	{
		for(int i = 0; i < timesToRepeat; i++)
		{
			topMostParent.repeat();
		}
		return this;
	}
	
	/**
	 * Repeats all read requests in the specified consumer specified timesToRepeat times.
	 *
	 * @param timesToRepeat amount of times to repeat all read requests in the specified consumer
	 * @param consumer which will add the read requests to be repeated
	 * @return this
	 */
	public ReadablePacketBuilder repeatInstructions(int timesToRepeat, Consumer<ReadablePacketBuilder> consumer)
	{
		for(int i = 0; i < timesToRepeat; i++)
		{
			consumer.accept(this);
		}
		return this;
	}
	
	/**
	 * Repeats all read requests in the specified consumer specified passedNumber return value times.
	 *
	 * @param passedNumber amount of times to repeat all read requests in the specified consumer
	 * @param consumer which will add the read requests to be repeated
	 * @return this
	 */
	public ReadablePacketBuilder repeatInstructions(PassedNumber passedNumber,
			Consumer<ReadablePacketBuilder> consumer)
	{
		ReadableElement newElement = new ReadableElement(null, passedNumber);
		currentReadableElement.addChild(newElement);
		ReadableElement previous = currentReadableElement;
		currentReadableElement = newElement;
		consumer.accept(this);
		currentReadableElement = previous;
		return this;
	}
	
	/**
	 * Returns a new {@link ReadablePacket} based on the entered parameters.
	 *
	 * @return a new {@link ReadablePacket} based on the entered parameters
	 */
	public ReadablePacket build()
	{
		//delete empty instructions
		if(topMostParent.size() == 0 && topMostParent.getChildren().size() == 0)
		{
			return ReadablePacket.EMPTY;
		}
		return new ReadablePacket(topMostParent, skip);
	}
}