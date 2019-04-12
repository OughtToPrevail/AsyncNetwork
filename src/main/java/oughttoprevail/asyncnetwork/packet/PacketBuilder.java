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

import oughttoprevail.asyncnetwork.impl.packet.PacketBuilderImpl;
import oughttoprevail.asyncnetwork.impl.packet.SynchronizedPacketBuilderImpl;

/**
 * Implementation at {@link PacketBuilderImpl} and {@link SynchronizedPacketBuilderImpl}.
 */
public interface PacketBuilder
{
	/**
	 * Returns a new {@link PacketBuilder}.
	 *
	 * @return a new {@link PacketBuilder}
	 */
	static PacketBuilder create()
	{
		return new PacketBuilderImpl();
	}
	
	/**
	 * Returns a new thread safe {@link PacketBuilder}.
	 *
	 * @return a new thread safe {@link PacketBuilder}
	 */
	static PacketBuilder createThreadSafe()
	{
		return new SynchronizedPacketBuilderImpl();
	}
	
	/**
	 * Puts the specified byte in the packet
	 *
	 * @param b the byte that will be put in the packet
	 * @return this
	 */
	PacketBuilder putByte(int b);
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @return this
	 */
	PacketBuilder putBytes(byte[] bytes);
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @param offset the offset that will be used when putting in the packet
	 * @param length the length that will be used when putting in the packet
	 * @return this
	 */
	PacketBuilder putBytes(byte[] bytes, int offset, int length);
	
	/**
	 * Puts the specified char in the packet.
	 *
	 * @param c the char that will be put in the packet
	 * @return this
	 */
	PacketBuilder putChar(char c);
	
	/**
	 * Puts the specified double in packet.
	 *
	 * @param d the double that will be put in the packet
	 * @return this
	 */
	PacketBuilder putDouble(double d);
	
	/**
	 * Puts the specified float in the packet.
	 *
	 * @param f the float that will be put in the packet
	 * @return this
	 */
	PacketBuilder putFloat(float f);
	
	/**
	 * Puts the specified int in the packet.
	 *
	 * @param i the int that will be put in the packet
	 * @return this
	 */
	PacketBuilder putInt(int i);
	
	/**
	 * Puts the specified long in the packet.
	 *
	 * @param l the long that will be put in the packet
	 * @return this
	 */
	PacketBuilder putLong(long l);
	
	/**
	 * Puts the specified short in the packet.
	 *
	 * @param s the short that will be put in the packet
	 * @return this
	 */
	PacketBuilder putShort(short s);

	/**
	 * Puts the specified boolean in the packet.
	 *
	 * @param b the boolean that will be put in the packet
	 * @return this
	 */
	PacketBuilder putBoolean(boolean b);
	
	/**
	 * Puts the specified string in the packet.
	 *
	 * @param s the string that will be put in the packet
	 * @return this
	 */
	PacketBuilder putString(String s);
	
	/**
	 * Puts the specified object in the packet after serialization made by specified serDes.
	 *
	 * @param object to put in the packet
	 * @param serDes serializes the specified object
	 * @return this
	 */
	<T> PacketBuilder putObject(T object, SerDes<T> serDes);
	
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	int getSize();
	
	/**
	 * Returns a new {@link Packet} based on the entered parameters.
	 *
	 * @return a new {@link Packet} based on the entered parameters
	 */
	Packet build();
	
	/**
	 * Returns a new {@link Packet} based on the entered parameters.
	 * This {@link Packet} will be thread safe and use a {@code synchronized}
	 * with a lock.
	 *
	 * @return a new {@link Packet} based on the entered parameters
	 */
	Packet threadSafeBuild();
}