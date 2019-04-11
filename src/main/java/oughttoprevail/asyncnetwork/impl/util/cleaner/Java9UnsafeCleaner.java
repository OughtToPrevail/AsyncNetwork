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
package oughttoprevail.asyncnetwork.impl.util.cleaner;

import sun.misc.Unsafe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.impl.util.UnsafeSupport;

public class Java9UnsafeCleaner implements Cleaner
{
	private static boolean SUPPORTED = false;
	private static Method INVOKE_CLEANER;
	
	static
	{
		try
		{
			if(UnsafeSupport.isSupported())
			{
				Unsafe unsafe = Unsafe.getUnsafe();
				INVOKE_CLEANER = unsafe.getClass().getDeclaredMethod("invokeCleaner", ByteBuffer.class);
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
	
	public void clean(ByteBuffer directByteBuffer)
	{
		try
		{
			INVOKE_CLEANER.invoke(directByteBuffer);
		} catch(IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
}