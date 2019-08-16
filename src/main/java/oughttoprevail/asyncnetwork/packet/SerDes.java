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

import oughttoprevail.asyncnetwork.util.Util;

/**
 * A serializer and deserializer that puts an {@link Object} into a {@link ByteBuffer} and
 * reverses the {@link ByteBuffer} into the same {@link Object}.
 * Note: it is recommended to make an inner private class of this {@link SerDes} instead of an
 * outer class so you can access private fields.
 *
 * @param <T> object which will be serialized and deserialized
 */
public interface SerDes<T>
{
	/**
	 * Serializes the specified t into the specified byteBuffer using the specified serializable.
	 *
	 * @param t to serialize
	 * @param serializable which will serialize the specified object
	 * @param <T> type of specified object
	 */
	static <T> void serialize(T t, SerDes<T> serializable, ByteBuffer byteBuffer)
	{
		int serializedLength = serializable.getSerializedLength(t);
		if(!serializable.isFixedLength())
		{
			byteBuffer.putInt(serializedLength);
		}
		serializable.serialize(t, byteBuffer);
	}
	
	/**
	 * Serializes the specified t into a byte[] using the specified serializable.
	 *
	 * @param t to serialize
	 * @param serializable which will serialize the specified object
	 * @param <T> type of specified object
	 * @return the specified object after serialization made by the specified serializable
	 */
	static <T> byte[] serializeToBytes(T t, SerDes<T> serializable)
	{
		int serializedLength = serializable.getSerializedLength(t);
		ByteBuffer byteBuffer = ByteBuffer.allocate(serializable.isFixedLength() ? serializedLength : serializedLength + Util.INT_BYTES);
		SerDes.serialize(t, serializable, byteBuffer);
		return byteBuffer.array();
	}
	
	/**
	 * Serializes the specified t to a {@link ByteBuffer}.
	 *
	 * @param t to be serialized
	 * @param byteBuffer which this {@link Object} will be put in
	 */
	void serialize(T t, ByteBuffer byteBuffer);
	
	/**
	 * Returns how many in bytes will be put in when using {@link SerDes#serialize(Object, ByteBuffer)}.
	 * if {@link #isFixedLength()} is true then the specified t is possibly null
	 *
	 * @param t which will be tested for the serialized length or possibly null if {@link #isFixedLength()} is true
	 * @return how many in bytes will be put in when using {@link SerDes#serialize(Object, ByteBuffer)}
	 */
	int getSerializedLength(T t);
	
	/**
	 * Deserializes the specified serialized into the specified T.
	 *
	 * @param serialized to deserialize
	 * @param serializedLength is the length of the serialized object
	 * @return T after deserialization from the specified serialized
	 */
	T deserialize(ByteBuffer serialized, int serializedLength);
	
	/**
	 * Returns whether this {@link SerDes} has a fixed length.
	 * By fixed length it means that {@link #getSerializedLength(Object)} will always return
	 * the same number.
	 *
	 * @return whether this {@link SerDes} has a fixed length
	 */
	boolean isFixedLength();
}