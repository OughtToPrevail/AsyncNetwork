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

import java.nio.ByteBuffer;

public class AddressReaderAccess
{
	private static class NullAddressReader implements AddressReader
	{
		@Override
		public long readAddress(ByteBuffer directByteBuffer)
		{
			return -1;
		}
	}
	
	private static AddressReader createAddressReader()
	{
		if(JavaAddressReader.isSupported())
		{
			return new JavaAddressReader();
		} else if(ReflectionAddressReader.isSupported())
		{
			return new ReflectionAddressReader();
		} else
		{
			return new NullAddressReader();
		}
	}
	
	private static final AddressReader ADDRESS_READER = createAddressReader();
	
	public static long readByteBufferAddress(ByteBuffer directByteBuffer)
	{
		return ADDRESS_READER.readAddress(directByteBuffer);
	}
}