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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.Validator;

public class OpcodePacketBuilder<E extends Enum<E>>
{
	public static <E extends Enum<E>> OpcodePacketBuilder<E> create()
	{
		return new OpcodePacketBuilder<>();
	}
	
	public static <E extends Enum<E>> OpcodePacketBuilder<E> create(PassedNumber passedNumber)
	{
		return new OpcodePacketBuilder<>(passedNumber);
	}
	
	private final PassedNumber passedNumber;
	private final Map<Integer, RegisteredPacket<E>> registeredPackets;
	private final List<BiConsumer<Socket, Integer>> onInvalidOpcode = new ArrayList<>();
	private PermissionHandler<E> permissionHandler;
	
	public OpcodePacketBuilder()
	{
		this(PassedNumber.PASSABLE_UNSIGNED_BYTE);
	}
	
	public OpcodePacketBuilder(PassedNumber passedNumber)
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
	public OpcodePacketBuilder<E> register(int opcode, ReadablePacket packet, Consumer<ReadResult> readResultConsumer)
	{
		ensureValidOpcode(opcode);
		Validator.requireNonNull(packet, "Packet");
		Validator.requireNonNull(readResultConsumer, "ReadResultConsumer");
		if(registeredPackets.containsKey(opcode))
		{
			throw new IllegalArgumentException("A packet with the specified opcode (Specified opcode: " + opcode + ") already exists!");
		}
		registeredPackets.put(opcode, new RegisteredPacket<>(packet, readResultConsumer));
		return this;
	}
	
	/**
	 * Invokes {@link #register(int, ReadablePacket, Consumer)} with the int parameter being the specified enum {@link Enum#ordinal()}.
	 */
	public OpcodePacketBuilder<E> register(Enum e, ReadablePacket packet, Consumer<ReadResult> readResultConsumer)
	{
		return register(e.ordinal(), packet, readResultConsumer);
	}
	
	/**
	 * Invokes the specified onInvalidOpcode when an invalid opcode (a non-registered opcode) is
	 * received in a {@link OpcodePacket#listen(Socket, boolean)} operation.
	 *
	 * @param onInvalidOpcode to invoke when an invalid opcode is received
	 * @return this
	 */
	public OpcodePacketBuilder<E> onInvalidOpcode(BiConsumer<Socket, Integer> onInvalidOpcode)
	{
		this.onInvalidOpcode.add(onInvalidOpcode);
		return this;
	}
	
	public OpcodePacketBuilder<E> permissionHandler(PermissionHandler<E> permissionHandler)
	{
		this.permissionHandler = permissionHandler;
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
	public OpcodePacket<E> build()
	{
		return new OpcodePacket<>(registeredPackets, passedNumber, onInvalidOpcode, permissionHandler);
	}
}