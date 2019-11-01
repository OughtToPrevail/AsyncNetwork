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
package oughttoprevail.asyncnetwork.packet.read;

import java.nio.ByteBuffer;

import oughttoprevail.asyncnetwork.packet.Deserializer;

public class BytesDeserializer implements Deserializer<byte[]>
{
	private static final String PASSED_NUMBER_SECTION = "BytesDeserializer_PassedNumber";
	
	private final PassedNumber passedNumber;
	
	public BytesDeserializer(PassedNumber passedNumber)
	{
		this.passedNumber = passedNumber;
	}
	
	@Override
	public void prepareDeserialization(ReadablePacketBuilder builder)
	{
		System.out.println("Prepare deserialization");
		builder.section(PASSED_NUMBER_SECTION).aObject(passedNumber).endSection().dependent((builder1, readResult) ->
		{
			Number value = readResult.peekLast();
			int intLength = value.intValue();
			boolean hasBytes = intLength != 0;
			if(hasBytes)
			{
				builder1.aByteBuffer(intLength);
			}
		});
	}
	
	@Override
	public byte[] deserialize(ReadResult readResult)
	{
		System.out.println("Bytes Deserialize");
		int length = ((Number) readResult.section(PASSED_NUMBER_SECTION).poll()).intValue();
		System.out.println("Has bytes: " + length + " " + readResult);
		if(length == 0)
		{
			return new byte[]{};
		}
		ByteBuffer byteBuffer = readResult.pollLast();
		byte[] bytes = new byte[byteBuffer.remaining()];
		byteBuffer.get(bytes);
		return bytes;
	}
}