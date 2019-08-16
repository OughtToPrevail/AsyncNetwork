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
import java.util.ArrayDeque;
import java.util.Deque;

import oughttoprevail.asyncnetwork.util.Util;
import oughttoprevail.asyncnetwork.pool.PooledByteBuffer;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

public class WritablePacketBuilder
{
	public static WritablePacketBuilder create()
	{
		return new WritablePacketBuilder();
	}
	
	/**
	 * A queue of instructions of how to create a {@link ByteBuffer}
	 * for a packet.
	 */
	private final Deque<Consumer<ByteBuffer>> instructions;
	/**
	 * Size all {@link #instructions} put in the {@link ByteBuffer}.
	 */
	private int size;
	
	public WritablePacketBuilder()
	{
		instructions = new ArrayDeque<>();
	}
	
	public WritablePacketBuilder(WritablePacket writablePacket)
	{
		this();
		ByteBuffer byteBuffer = writablePacket.getByteBuffer();
		putBytes(Util.getBytes(byteBuffer, byteBuffer.remaining()));
	}
	
	/**
	 * Enqueues the specified instruction and adds the specified instructionSize to
	 * the current {@link #size}.
	 *
	 * @param instruction to enqueue to instructions deque
	 * @param instructionSize is how much in bytes this instruction will add
	 * @return this
	 */
	private WritablePacketBuilder enqueue(Consumer<ByteBuffer> instruction, int instructionSize)
	{
		size += instructionSize;
		instructions.addLast(instruction);
		return this;
	}
	
	/**
	 * Puts the specified byte in the packet
	 *
	 * @param b the byte that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putByte(int b)
	{
		return enqueue(byteBuffer -> byteBuffer.put((byte) b), Util.BYTE_BYTES);
	}
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putBytes(byte[] bytes)
	{
		return enqueue(byteBuffer -> byteBuffer.put(bytes), bytes.length);
	}
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @param offset the offset that will be used when putting in the packet
	 * @param length the length that will be used when putting in the packet
	 * @return this
	 */
	public WritablePacketBuilder putBytes(byte[] bytes, int offset, int length)
	{
		int totalLength = length - offset;
		if(offset < 0 || length < 0 || length <= offset || bytes.length < totalLength)
		{
			throw new IndexOutOfBoundsException("Parameters (bytesLength: " + bytes.length + ", offset: " + offset + ", length: " + length + ") are wrong!");
		}
		return enqueue(byteBuffer -> byteBuffer.put(bytes, offset, length), totalLength);
	}
	
	/**
	 * Puts the specified char in the packet.
	 *
	 * @param c the char that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putChar(char c)
	{
		return enqueue(byteBuffer -> byteBuffer.putChar(c), Util.CHAR_BYTES);
	}
	
	/**
	 * Puts the specified double in packet.
	 *
	 * @param d the double that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putDouble(double d)
	{
		return enqueue(byteBuffer -> byteBuffer.putDouble(d), Util.DOUBLE_BYTES);
	}
	
	/**
	 * Puts the specified float in the packet.
	 *
	 * @param f the float that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putFloat(float f)
	{
		return enqueue(byteBuffer -> byteBuffer.putFloat(f), Util.FLOAT_BYTES);
	}
	
	/**
	 * Puts the specified int in the packet.
	 *
	 * @param i the int that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putInt(int i)
	{
		return enqueue(byteBuffer -> byteBuffer.putInt(i), Util.INT_BYTES);
	}
	
	/**
	 * Puts the specified long in the packet.
	 *
	 * @param l the long that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putLong(long l)
	{
		return enqueue(byteBuffer -> byteBuffer.putLong(l), Util.LONG_BYTES);
	}
	
	/**
	 * Puts the specified short in the packet.
	 *
	 * @param s the short that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putShort(short s)
	{
		return enqueue(byteBuffer -> byteBuffer.putShort(s), Util.SHORT_BYTES);
	}
	
	/**
	 * Puts the specified boolean in the packet.
	 *
	 * @param b the boolean that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putBoolean(boolean b)
	{
		return putByte(Util.toByte(b));
	}
	
	/**
	 * Puts the specified string in thee packet.
	 *
	 * @param s the string that will be put in the packet
	 * @return this
	 */
	public WritablePacketBuilder putString(String s)
	{
		byte[] bytes = s.getBytes(Util.UTF_8);
		int length = bytes.length;
		return putInt(length).putBytes(bytes);
	}
	
	/**
	 * Puts the specified object in the packet after serialization made by specified serDes.
	 *
	 * @param object to put in the packet
	 * @param serDes serializes the specified object
	 * @return this
	 */
	public <T> WritablePacketBuilder putObject(T object, SerDes<T> serDes)
	{
		int serializedLength = serDes.getSerializedLength(object);
		if(serializedLength <= 0)
		{
			throw new IllegalArgumentException("Serialized length cannot be less than 0!");
		}
		if(serDes.isFixedLength())
		{
			return enqueue(byteBuffer -> serDes.serialize(object, byteBuffer), serializedLength);
		} else
		{
			return enqueue(byteBuffer ->
			{
				byteBuffer.putInt(serializedLength);
				serDes.serialize(object, byteBuffer);
			}, serializedLength + Util.INT_BYTES);
		}
	}
	
	/**
	 * Invokes the specified consumer specified timesToRepeat times in a loop.
	 * First parameter of the consumer will be equal to {@code this} and the second
	 * parameter is the index in the loop.
	 *
	 * @param timesToRepeat is the amount of times to repeat the specified consumer
	 * @param consumer to repeat
	 * @return this
	 */
	public WritablePacketBuilder repeat(int timesToRepeat, BiConsumer<WritablePacketBuilder, Integer> consumer)
	{
		for(int i = 0; i < timesToRepeat; i++)
		{
			consumer.accept(this, i);
		}
		return this;
	}
	
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	public int size()
	{
		return size;
	}
	
	/**
	 * Returns the total amount elements in this packet (each "put" call is an element).
	 *
	 * @return the total amount of elements in this packet
	 */
	public int elements()
	{
		return instructions.size();
	}
	
	/**
	 * Returns a new {@link WritablePacket} based on the entered parameters.
	 *
	 * @return a new {@link WritablePacket} based on the entered parameters
	 */
	public WritablePacket build()
	{
		PooledByteBuffer pooledPacketBuffer = new PooledByteBuffer(size);
		ByteBuffer packetBuffer = pooledPacketBuffer.getByteBuffer();
		for(Consumer<ByteBuffer> instruction : instructions)
		{
			instruction.accept(packetBuffer);
		}
		return new WritablePacket(pooledPacketBuffer);
	}
}