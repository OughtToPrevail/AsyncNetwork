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
package oughttoprevail.asyncnetwork.impl.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

import oughttoprevail.asyncnetwork.exceptions.LoadException;

public class UnsafeGetter
{
	private static Unsafe theUnsafe;
	
	static
	{
		try
		{
			Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			theUnsafe = (Unsafe) theUnsafeField.get(null);
		} catch(NoSuchFieldException ignored)
		{
		
		} catch(IllegalAccessException e)
		{
			new LoadException("Failed to access Unsafe!", e).printStackTrace();
		}
	}
	
	public static Unsafe getUnsafe()
	{
		return theUnsafe;
	}
}