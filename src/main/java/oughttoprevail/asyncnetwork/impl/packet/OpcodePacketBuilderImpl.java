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

import java.util.HashMap;
import java.util.Map;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.packet.OpcodePacket;
import oughttoprevail.asyncnetwork.packet.OpcodePacketBuilder;
import oughttoprevail.asyncnetwork.packet.PassedNumber;
import oughttoprevail.asyncnetwork.packet.ReadResult;
import oughttoprevail.asyncnetwork.packet.ReadablePacket;
import oughttoprevail.asyncnetwork.packet.ReadablePacketBuilder;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

public class OpcodePacketBuilderImpl implements OpcodePacketBuilder
{
	private final PassedNumber<?> passedNumber;
	private final Map<Integer, RegisteredPacket> registeredPackets;
	private BiConsumer<Channel<?>, Integer> onInvalidOpcode;
	
	public OpcodePacketBuilderImpl()
	{
		this(ReadablePacketBuilder.PASSABLE_UNSIGNED_BYTE);
	}
	
	public OpcodePacketBuilderImpl(PassedNumber<?> passedNumber)
	{
		Validator.requireNonNull(passedNumber, "PassedNumber");
		this.passedNumber = passedNumber;
		registeredPackets = new HashMap<>();
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
	@Override
	public OpcodePacketBuilder register(int opcode, ReadablePacket packet, Consumer<ReadResult> readResultConsumer)
	{
		ensureValidOpcode(opcode);
		Validator.requireNonNull(packet, "Packet");
		Validator.requireNonNull(readResultConsumer, "ReadResultConsumer");
		if(registeredPackets.containsKey(opcode))
		{
			throw new IllegalArgumentException("A packet with the specified opcode (Specified opcode: " + opcode + ") already exists!");
		}
		registeredPackets.put(opcode, new RegisteredPacket(packet, readResultConsumer));
		return this;
	}
	
	/**
	 * Invokes the specified onInvalidOpcode when an invalid opcode (a non-registered opcode) is
	 * received in a {@link OpcodePacket#listen(Channel, boolean)} operation.
	 *
	 * @param onInvalidOpcode to invoke when an invalid opcode is received
	 * @return this
	 */
	@Override
	public OpcodePacketBuilder onInvalidOpcode(BiConsumer<Channel<?>, Integer> onInvalidOpcode)
	{
		this.onInvalidOpcode = onInvalidOpcode;
		return this;
	}
	
	private void throwOpcodeException(int largerThan)
	{
		throw new IllegalArgumentException("Opcode cannot be larger than " + largerThan);
	}
	
	private static final int MAX_BYTES = Math.abs(Byte.MAX_VALUE - Byte.MIN_VALUE);
	private static final int MAX_SHORT = Math.abs(Short.MAX_VALUE - Short.MIN_VALUE);
	
	private void ensureValidOpcode(int opcode)
	{
		int size = passedNumber.getSize();
		switch(size)
		{
			//byte
			case 1:
			{
				if(opcode > MAX_BYTES)
				{
					throwOpcodeException(MAX_BYTES);
				}
				break;
			}
			
			//short
			case 2:
			{
				if(opcode > MAX_SHORT)
				{
					throwOpcodeException(MAX_SHORT);
				}
				break;
			}
		}
	}
	
	/**
	 * Returns a new {@link OpcodePacket} based on the entered parameters.
	 *
	 * @return a new {@link OpcodePacket} based on the entered parameters
	 */
	@Override
	public OpcodePacket build()
	{
		return new OpcodePacketImpl(registeredPackets, passedNumber, onInvalidOpcode);
	}
}