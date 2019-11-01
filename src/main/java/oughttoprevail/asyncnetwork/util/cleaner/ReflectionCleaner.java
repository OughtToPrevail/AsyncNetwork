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
package oughttoprevail.asyncnetwork.util.cleaner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.util.UnsafeGetter;
import oughttoprevail.asyncnetwork.util.UnsafeSupport;

public class ReflectionCleaner implements Cleaner
{
	private static boolean SUPPORTED = false;
	private static long CLEANER_FIELD_OFFSET = -1;
	private static Field CLEANER_FIELD;
	private static Method CLEAN;
	
	static
	{
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1);
		try
		{
			Field cleanerField = byteBuffer.getClass().getDeclaredField("cleaner");
			Object cleaner;
			if(UnsafeSupport.isSupported())
			{
				CLEANER_FIELD_OFFSET = UnsafeGetter.getUnsafe().objectFieldOffset(cleanerField);
				cleaner = UnsafeGetter.getUnsafe().getObject(byteBuffer, CLEANER_FIELD_OFFSET);
			} else
			{
				cleanerField.setAccessible(true);
				CLEANER_FIELD = cleanerField;
				cleaner = cleanerField.get(byteBuffer);
			}
			cleanerField.setAccessible(true);
			CLEAN = cleaner.getClass().getMethod("clean");
			CLEAN.invoke(cleaner);
			SUPPORTED = true;
		} catch(Throwable ignored)
		{
		}
	}
	
	static boolean isSupported()
	{
		return SUPPORTED;
	}
	
	public void clean(ByteBuffer directByteBuffer)
	{
		if(CLEANER_FIELD_OFFSET == -1)
		{
			try
			{
				Object cleaner = CLEANER_FIELD.get(directByteBuffer);
				CLEAN.invoke(cleaner);
			} catch(IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		} else
		{
			Object cleaner = UnsafeGetter.getUnsafe().getObject(directByteBuffer, CLEANER_FIELD_OFFSET);
			try
			{
				CLEAN.invoke(cleaner);
			} catch(IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
	}
}