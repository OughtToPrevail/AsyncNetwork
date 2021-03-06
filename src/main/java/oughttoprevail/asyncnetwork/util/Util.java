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
package oughttoprevail.asyncnetwork.util;

import sun.nio.ch.SelChImpl;

import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.charset.Charset;
import java.util.List;

import oughttoprevail.asyncnetwork.util.address.AddressReaderAccess;
import oughttoprevail.asyncnetwork.util.cleaner.CleanerAccess;

public interface Util
{
	Charset UTF_8 = Charset.forName("UTF-8");
	int BYTE_SIZE = Byte.SIZE;
	int BYTE_BYTES = 1;
	int CHAR_BYTES = Character.SIZE / BYTE_SIZE;
	int SHORT_BYTES = Short.SIZE / BYTE_SIZE;
	int INT_BYTES = Integer.SIZE / BYTE_SIZE;
	int FLOAT_BYTES = Float.SIZE / BYTE_SIZE;
	int LONG_BYTES = Long.SIZE / BYTE_SIZE;
	int DOUBLE_BYTES = Double.SIZE / BYTE_SIZE;
	
	static void dispose(ByteBuffer byteBuffer)
	{
		CleanerAccess.cleanByteBuffer(byteBuffer);
	}
	
	static long address(ByteBuffer byteBuffer)
	{
		return AddressReaderAccess.readByteBufferAddress(byteBuffer);
	}
	
	/**
	 * Returns the file descriptor of the specified socketChannel.
	 *
	 * @param socketChannel the socketChannel which contains the file descriptor
	 * @return the file descriptor of the specified socketChannel
	 */
	static int getFD(AbstractSelectableChannel socketChannel)
	{
		return ((SelChImpl) socketChannel).getFDVal();
	}
	
	static int toUnsignedInt(short value)
	{
		return ((int) value) & 0xFFFF;
	}
	
	static short toUnsignedShort(byte value)
	{
		return (short) (((short) value) & 0xFF);
	}
	
	static boolean toBoolean(ByteBuffer byteBuffer)
	{
		return byteBuffer.get() == 1;
	}
	
	static byte toByte(boolean b)
	{
		return (byte) (b ? 1 : 0);
	}
	
	/**
	 * Gets specified bytes amount of bytes from the specified byteBuffer into a
	 * {@code byte[]}.
	 *
	 * @param byteBuffer to extract bytes from
	 * @param total amount of bytes to extract
	 * @return extracted {@code byte[]}
	 */
	static byte[] getBytes(ByteBuffer byteBuffer, int total)
	{
		if(byteBuffer.isDirect() || total != byteBuffer.capacity())
		{
			byte[] bytes = new byte[total];
			byteBuffer.get(bytes);
			return bytes;
		}
		return byteBuffer.array();
	}
	
	static void exception(List<Consumer<Throwable>> onException, Throwable throwable)
	{
		if(onException.isEmpty())
		{
			throwable.printStackTrace();
		} else
		{
			for(Consumer<Throwable> exceptionConsumer : onException)
			{
				exceptionConsumer.accept(throwable);
			}
		}
	}
}