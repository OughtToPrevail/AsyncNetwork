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
package oughttoprevail.asyncnetwork.util.address;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.util.UnsafeGetter;
import oughttoprevail.asyncnetwork.util.UnsafeSupport;

public class ReflectionAddressReader implements AddressReader
{
	private static Unsafe UNSAFE;
	
	private static boolean SUPPORTED = false;
	private static long ADDRESS_FIELD_OFFSET = -1;
	private static Field ADDRESS_FIELD;
	private static boolean ADDRESS_AS_LONG = true;
	
	static
	{
		try
		{
			Field addressField;
			try
			{
				addressField = Buffer.class.getDeclaredField("address");
			} catch(NoSuchFieldException e)
			{
				//try old android address
				try
				{
					addressField = Buffer.class.getDeclaredField("effectiveDirectAddress");
					ADDRESS_AS_LONG = false;
				} catch(NoSuchFieldException e1)
				{
					addressField = null;
				}
			}
			if(addressField != null)
			{
				if(UnsafeSupport.isSupported())
				{
					UNSAFE = UnsafeGetter.getUnsafe();
					ADDRESS_FIELD_OFFSET = UNSAFE.objectFieldOffset(addressField);
				} else
				{
					addressField.setAccessible(true);
					ADDRESS_FIELD = addressField;
				}
				SUPPORTED = true;
			}
		} catch(Throwable ignored)
		{
		}
	}
	
	static boolean isSupported()
	{
		return SUPPORTED;
	}
	
	@Override
	public long readAddress(ByteBuffer directByteBuffer)
	{
		if(ADDRESS_FIELD_OFFSET == -1)
		{
			try
			{
				return ADDRESS_AS_LONG ? ADDRESS_FIELD.getLong(directByteBuffer) : ADDRESS_FIELD.getInt(directByteBuffer);
			} catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		} else
		{
			return ADDRESS_AS_LONG ? UNSAFE.getLong(directByteBuffer, ADDRESS_FIELD_OFFSET) : UNSAFE.getInt(
					directByteBuffer,
					ADDRESS_FIELD_OFFSET);
		}
		return -1;
	}
}