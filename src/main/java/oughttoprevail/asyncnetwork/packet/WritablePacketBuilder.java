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

import oughttoprevail.asyncnetwork.impl.packet.WritablePacketBuilderImpl;

/**
 * Implementation at {@link WritablePacketBuilderImpl}.
 */
public interface WritablePacketBuilder
{
	/**
	 * Returns a new {@link WritablePacketBuilder}.
	 *
	 * @return a new {@link WritablePacketBuilder}
	 */
	static WritablePacketBuilder create()
	{
		return new WritablePacketBuilderImpl();
	}
	
	/**
	 * Puts the specified byte in the packet
	 *
	 * @param b the byte that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putByte(int b);
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putBytes(byte[] bytes);
	
	/**
	 * Puts the specified bytes in the packet.
	 *
	 * @param bytes the bytes that will be put in the packet
	 * @param offset the offset that will be used when putting in the packet
	 * @param length the length that will be used when putting in the packet
	 * @return this
	 */
	WritablePacketBuilder putBytes(byte[] bytes, int offset, int length);
	
	/**
	 * Puts the specified char in the packet.
	 *
	 * @param c the char that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putChar(char c);
	
	/**
	 * Puts the specified double in packet.
	 *
	 * @param d the double that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putDouble(double d);
	
	/**
	 * Puts the specified float in the packet.
	 *
	 * @param f the float that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putFloat(float f);
	
	/**
	 * Puts the specified int in the packet.
	 *
	 * @param i the int that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putInt(int i);
	
	/**
	 * Puts the specified long in the packet.
	 *
	 * @param l the long that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putLong(long l);
	
	/**
	 * Puts the specified short in the packet.
	 *
	 * @param s the short that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putShort(short s);
	
	/**
	 * Puts the specified boolean in the packet.
	 *
	 * @param b the boolean that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putBoolean(boolean b);
	
	/**
	 * Puts the specified string in the packet.
	 *
	 * @param s the string that will be put in the packet
	 * @return this
	 */
	WritablePacketBuilder putString(String s);
	
	/**
	 * Puts the specified object in the packet after serialization made by specified serDes.
	 *
	 * @param object to put in the packet
	 * @param serDes serializes the specified object
	 * @return this
	 */
	<T> WritablePacketBuilder putObject(T object, SerDes<T> serDes);
	
	/**
	 * Puts the a boolean declaring whether the specified object is null
	 * if it isn't null then specified object will be put in the packet after serialization made by specified serDes.
	 *
	 * @param object to put in the packet
	 * @param serDes serializes the specified object
	 * @return this
	 */
	<T> WritablePacketBuilder putNullableObject(T object, SerDes<T> serDes);
	
	/**
	 * Returns the total size in bytes of the packet.
	 *
	 * @return the total size in bytes of the packet
	 */
	int getSize();
	
	/**
	 * Returns a new {@link WritablePacket} based on the entered parameters.
	 *
	 * @return a new {@link WritablePacket} based on the entered parameters
	 */
	WritablePacket build();
}