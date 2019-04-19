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
import oughttoprevail.asyncnetwork.impl.packet.OpcodePacketBuilderImpl;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

public interface OpcodePacketBuilder
{
	static OpcodePacketBuilder create()
	{
		return new OpcodePacketBuilderImpl();
	}
	
	static OpcodePacketBuilder create(PassedNumber<?> passedNumber)
	{
		return new OpcodePacketBuilderImpl(passedNumber);
	}
	
	/**
	 * Registers the specified packet for opcodes on the specified opcode. Once a packet has successfully
	 * been received the specified readResultConsumer is invoked with the {@link ReadResult}.
	 *
	 * @param opcode which will listen for this packet
	 * @param packet to read from when an opcode matching the specified opcode is received
	 * @param readResultConsumer to invoke once a read has successfully completed
	 * @return this
	 */
	OpcodePacketBuilder register(int opcode, ReadablePacket packet, Consumer<ReadResult> readResultConsumer);
	
	/**
	 * Invokes the specified onInvalidOpcode when an invalid opcode (a non-registered opcode) is
	 * received in a {@link OpcodePacket#listen(Channel, boolean)} operation.
	 *
	 * @param onInvalidOpcode to invoke when an invalid opcode is received
	 * @return this
	 */
	OpcodePacketBuilder onInvalidOpcode(BiConsumer<Channel<?>, Integer> onInvalidOpcode);
	
	/**
	 * Returns a new {@link OpcodePacket} based on the entered parameters.
	 *
	 * @return a new {@link OpcodePacket} based on the entered parameters
	 */
	OpcodePacket build();
}