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

import java.nio.ByteBuffer;

public class CleanerAccess
{
	private static class NullCleaner implements Cleaner
	{
		@Override
		public void clean(ByteBuffer directByteBuffer)
		{
			//ignored
		}
		
	}
	
	private static Cleaner createCleaner()
	{
		if(JavaCleaner.isSupported())
		{
			return new JavaCleaner();
		} else if(Java9UnsafeCleaner.isSupported())
		{
			return new Java9UnsafeCleaner();
		} else if(ReflectionCleaner.isSupported())
		{
			return new ReflectionCleaner();
		} else
		{
			return new NullCleaner();
		}
	}
	
	private static final Cleaner CLEANER = createCleaner();
	
	public static void cleanByteBuffer(ByteBuffer directByteBuffer)
	{
		CLEANER.clean(directByteBuffer);
	}
}