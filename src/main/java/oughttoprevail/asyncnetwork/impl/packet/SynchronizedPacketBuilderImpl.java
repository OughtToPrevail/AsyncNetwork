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

import oughttoprevail.asyncnetwork.packet.Packet;
import oughttoprevail.asyncnetwork.packet.PacketBuilder;
import oughttoprevail.asyncnetwork.packet.SerDes;

public class SynchronizedPacketBuilderImpl extends PacketBuilderImpl
{
	private final Object lock = new Object();
	
	/**
	 * Puts the specified byte in the packet
	 *
	 * @param b the byte that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putByte(int b)
	{
		synchronized(lock)
		{
			return super.putByte(b);
		}
	}
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putBytes(byte[] bytes)
	{
		synchronized(lock)
		{
			return super.putBytes(bytes);
		}
	}
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @param offset the offset that will be used when putting in the packet
	 * @param length the length that will be used when putting in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putBytes(byte[] bytes, int offset, int length)
	{
		synchronized(lock)
		{
			return super.putBytes(bytes, offset, length);
		}
	}
	
	/**
	 * Puts the specified char in the packet.
	 *
	 * @param c the char that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putChar(char c)
	{
		synchronized(lock)
		{
			return super.putChar(c);
		}
	}
	
	/**
	 * Puts the specified double in packet.
	 *
	 * @param d the double that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putDouble(double d)
	{
		synchronized(lock)
		{
			return super.putDouble(d);
		}
	}
	
	/**
	 * Puts the specified float in the packet.
	 *
	 * @param f the float that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putFloat(float f)
	{
		synchronized(lock)
		{
			return super.putFloat(f);
		}
	}
	
	/**
	 * Puts the specified int in the packet.
	 *
	 * @param i the int that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putInt(int i)
	{
		synchronized(lock)
		{
			return super.putInt(i);
		}
	}
	
	/**
	 * Puts the specified long in the packet.
	 *
	 * @param l the long that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putLong(long l)
	{
		synchronized(lock)
		{
			return super.putLong(l);
		}
	}
	
	/**
	 * Puts the specified short in the packet.
	 *
	 * @param s the short that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putShort(short s)
	{
		synchronized(lock)
		{
			return super.putShort(s);
		}
	}
	
	/**
	 * Puts the specified boolean in the packet.
	 *
	 * @param b the boolean that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putBoolean(boolean b)
	{
		synchronized(lock)
		{
			return super.putBoolean(b);
		}
	}
	
	/**
	 * Puts the specified string in the packet.
	 *
	 * @param s the string that will be put in the packet
	 * @return this
	 */
	@Override
	public PacketBuilder putString(String s)
	{
		synchronized(lock)
		{
			return super.putString(s);
		}
	}
	
	/**
	 * Puts the specified object in the packet after serialization made by specified serDes.
	 *
	 * @param object to put in the packet
	 * @param serDes serializes the specified object
	 * @return this
	 */
	@Override
	public <T> PacketBuilder putObject(T object, SerDes<T> serDes)
	{
		synchronized(lock)
		{
			return super.putObject(object, serDes);
		}
	}
	
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	@Override
	public int getSize()
	{
		synchronized(lock)
		{
			return super.getSize();
		}
	}
	
	/**
	 * Returns a new {@link Packet} based on the entered parameters.
	 *
	 * @return a new {@link Packet} based on the entered parameters
	 */
	@Override
	public Packet build()
	{
		synchronized(lock)
		{
			return super.build();
		}
	}
	
	/**
	 * Returns a new {@link Packet} based on the entered parameters.
	 * This {@link Packet} will be thread safe and use a {@code synchronized}
	 * with a lock.
	 *
	 * @return a new {@link Packet} based on the entered parameters
	 */
	@Override
	public Packet threadSafeBuild()
	{
		synchronized(lock)
		{
			return super.threadSafeBuild();
		}
	}
}