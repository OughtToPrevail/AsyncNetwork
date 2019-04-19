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
package oughttoprevail.asyncnetwork.impl.packet;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.packet.ReadResult;
import oughttoprevail.asyncnetwork.packet.ReadablePacket;
import oughttoprevail.asyncnetwork.util.Consumer;

class RegisteredPacket
{
	private final ReadablePacket packet;
	private final Consumer<ReadResult> readResultConsumer;
	
	RegisteredPacket(ReadablePacket packet, Consumer<ReadResult> readResultConsumer)
	{
		this.packet = packet;
		this.readResultConsumer = readResultConsumer;
	}
	
	void read(Channel<?> channel)
	{
		packet.read(channel, readResultConsumer);
	}
}