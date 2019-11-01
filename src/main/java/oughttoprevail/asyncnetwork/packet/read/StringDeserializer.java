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

import java.util.Arrays;

import oughttoprevail.asyncnetwork.packet.Deserializer;
import oughttoprevail.asyncnetwork.util.Util;

public class StringDeserializer implements Deserializer<String>
{
	public static final Deserializer<String> STRING_DESERIALIZER = new StringDeserializer();
	
	@Override
	public void prepareDeserialization(ReadablePacketBuilder builder)
	{
		builder.bytes(PassedNumber.PASSABLE_INTEGER);
	}
	
	@Override
	public String deserialize(ReadResult readResult)
	{
		byte[] bytes = readResult.pollLast();
		System.out.println("Bytes: " + Arrays.toString(bytes));
		return new String(bytes, Util.UTF_8);
	}
}