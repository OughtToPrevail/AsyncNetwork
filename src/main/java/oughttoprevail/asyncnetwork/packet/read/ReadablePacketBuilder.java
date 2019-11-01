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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import oughttoprevail.asyncnetwork.packet.Deserializer;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.Function;
import oughttoprevail.asyncnetwork.util.Predicate;
import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.util.Validator;

public class ReadablePacketBuilder
{
	public static ReadablePacketBuilder create()
	{
		return new ReadablePacketBuilder();
	}
	
	private final List<Object> readInstructions;
	private int index;
	private int size;
	private int timesRepeat;
	private boolean built;
	
	public ReadablePacketBuilder()
	{
		this(new ArrayList<>(), -1);
	}
	
	ReadablePacketBuilder(List<Object> readInstructions, int index)
	{
		this.readInstructions = readInstructions;
		this.index = index;
	}
	
	private void add(Object o)
	{
		if(built)
		{
			throw new IllegalStateException("Cannot add more instructions when already built!");
		}
		if(index == -1)
		{
			readInstructions.add(o);
		} else
		{
			readInstructions.add(index + size, timesRepeat > 0 ? o : new DependentObject(o));
			size++;
		}
	}
	
	/**
	 * Reads a {@link ByteBuffer} containing the specified amount of bytes and invokes the specified function with it.
	 * The result (return value) of the function will go in the {@link ReadResult}.
	 *
	 * @param function to invoke with the read {@link ByteBuffer} and {@link ReadResult}
	 * @param bytes to read
	 * @return this
	 */
	private ReadablePacketBuilder aByteBuffer(Function<ByteBuffer, Object> function, int bytes)
	{
		add((Consumer<ReadResult>) readResult -> readResult.socket().readByteBuffer(byteBuffer ->
		{
			try
			{
				Object result = function.apply(byteBuffer);
				System.out.println("Add " + result);
				readResult.add(result);
			} catch(Throwable e)
			{
				readResult.socket().manager().exception(e);
			}
		}, bytes));
		return this;
	}
	
	private static final int MIN_DIRECT_SIZE = 256;
	
	public ReadablePacketBuilder aByteBuffer(int bytes)
	{
		return aByteBuffer(byteBuffer ->
		{
			int remaining = byteBuffer.remaining();
			ByteBuffer copy = remaining >= MIN_DIRECT_SIZE ? ByteBuffer.allocateDirect(remaining) : ByteBuffer.allocate(remaining);
			copy.put(byteBuffer);
			copy.flip();
			return copy;
		}, bytes);
	}
	
	/**
	 * Reads a single {@code byte}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aByte()
	{
		return aByteBuffer(ByteBuffer::get, Util.BYTE_BYTES);
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
		return aByteBuffer(byteBuffer -> Util.getBytes(byteBuffer, bytes), bytes);
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
		return aObject(new BytesDeserializer(passedNumber));
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
		return aByteBuffer(Util::toBoolean, Util.BYTE_BYTES);
	}
	
	/**
	 * Reads a single {@code char}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aChar()
	{
		return aByteBuffer(ByteBuffer::getChar, Util.CHAR_BYTES);
	}
	
	/**
	 * Reads a single {@code short}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aShort()
	{
		return aByteBuffer(ByteBuffer::getShort, Util.SHORT_BYTES);
	}
	
	/**
	 * Reads a single {@code int}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aInt()
	{
		return aByteBuffer(ByteBuffer::getInt, Util.INT_BYTES);
	}
	
	/**
	 * Reads a single {@code float}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aFloat()
	{
		return aByteBuffer(ByteBuffer::getFloat, Util.FLOAT_BYTES);
	}
	
	/**
	 * Reads a single {@code long}.
	 *
	 * @return this
	 */
	public ReadablePacketBuilder aLong()
	{
		return aByteBuffer(ByteBuffer::getLong, Util.LONG_BYTES);
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
		return aByteBuffer(byteBuffer ->
		{
			E[] enumConstants = cls.getEnumConstants();
			int aInt = consumer.apply(byteBuffer);
			if(aInt >= enumConstants.length || aInt < 0)
			{
				throw new IllegalArgumentException("Received illegal enum number: " + aInt + "!");
			}
			return enumConstants[aInt];
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
		return aObject(StringDeserializer.STRING_DESERIALIZER);
	}
	
	/**
	 * Reads a single {@link Object}.
	 *
	 * @param deserializer which will deserialize to the {@link Object}.
	 * @return this
	 */
	public ReadablePacketBuilder aObject(Deserializer<?> deserializer)
	{
		deserializer.prepareDeserialization(this);
		add(deserializer);
		return this;
	}
	
	private int totalSections;
	
	/**
	 * Creates a new section with the specified name.
	 *
	 * @param name of the section
	 * @return this
	 */
	public ReadablePacketBuilder section(String name)
	{
		add(new StartSection(name));
		totalSections++;
		return this;
	}
	
	/**
	 * Ends the current section
	 *
	 * @return this
	 */
	public ReadablePacketBuilder endSection()
	{
		if(totalSections == 0)
		{
			throw new IllegalStateException("No current sections!");
		}
		add(new EndSection());
		totalSections--;
		return this;
	}
	
	public ReadablePacketBuilder section(String name, Consumer<ReadablePacketBuilder> consumer)
	{
		section(name);
		consumer.accept(this);
		return endSection();
	}
	
	public ReadablePacketBuilder dependent(BiConsumer<ReadablePacketBuilder, ReadResult> biConsumer)
	{
		add(biConsumer);
		return this;
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
		dependent((builder, readResult) ->
		{
			if(predicate.test(readResult))
			{
				consumer.accept(builder);
			}
		});
//		addSection(new Condition(predicate), consumer);
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
		timesRepeat++;
		int sizeBefore = readInstructions.size();
		consumer.accept(this);
		int sizeAfter = readInstructions.size();
		add(new TimesRepeat(timesToRepeat - 1, sizeAfter - sizeBefore));
		timesRepeat--;
		return this;
	}
	
	/**
	 * Repeats all read requests in the specified consumer specified passedNumber return value times.
	 *
	 * @param passedNumber amount of times to repeat all read requests in the specified consumer
	 * @param consumer which will add the read requests to be repeated
	 * @return this
	 */
	public ReadablePacketBuilder repeatInstructions(PassedNumber passedNumber, Consumer<ReadablePacketBuilder> consumer)
	{
		aObject(passedNumber).dependent((builder, readResult) ->
		{
			Number number = readResult.peekLast();
			int timesRepeat = number.intValue();
			builder.repeatInstructions(timesRepeat, consumer);
		});
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
		built();
		if(readInstructions.isEmpty())
		{
			return ReadablePacket.EMPTY;
		}
		return new ReadablePacket(readInstructions);
	}
	
	void built()
	{
		built = true;
	}
}