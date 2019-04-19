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
package oughttoprevail.asyncnetwork.packet;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

public interface OpcodePacket
{
	/**
	 * Listens for opcodes in the specified {@link Channel}.
	 * Once an opcode is received a {@link java.util.Map#get(Object)} is invoked if null is returned then
	 * {@link OpcodePacketBuilder#onInvalidOpcode(BiConsumer)} consumer is invoked if it is not null. If {@link java.util.Map#get(Object)}
	 * returns a non-null packet then it is read with the {@link Consumer} specified in
	 * {@link OpcodePacketBuilder#register(int, ReadablePacket, Consumer)}.
	 *
	 * @param channel to listen for opcodes
	 * @param repeat whether this listen operation should repeat until the specified channel has closed
	 * @return this
	 */
	OpcodePacket listen(Channel<?> channel, boolean repeat);
}