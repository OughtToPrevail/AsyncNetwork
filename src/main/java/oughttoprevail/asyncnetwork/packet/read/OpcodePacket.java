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

import java.util.List;
import java.util.Map;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

public class OpcodePacket<E extends Enum<E>>
{
	private final Map<Integer, RegisteredPacket<E>> registeredPackets;
	private final List<BiConsumer<Socket, Integer>> onInvalidOpcode;
	private final PermissionHandler<E> permissionHandler;
	private final ReadablePacket packet;
	
	public OpcodePacket(Map<Integer, RegisteredPacket<E>> registeredPackets,
	                    PassedNumber passedNumber,
	                    List<BiConsumer<Socket, Integer>> onInvalidOpcode,
	                    PermissionHandler<E> permissionHandler)
	{
		this.registeredPackets = registeredPackets;
		this.onInvalidOpcode = onInvalidOpcode;
		this.permissionHandler = permissionHandler;
		this.packet = ReadablePacketBuilder.create().aObject(passedNumber).build();
	}
	
	/**
	 * Listens for opcodes in the specified {@link Socket}.
	 * Once an opcode is received a {@link java.util.Map#get(Object)} is invoked if null is returned then
	 * {@link OpcodePacketBuilder#onInvalidOpcode(BiConsumer)} consumer is invoked if it is not null. If {@link java.util.Map#get(Object)}
	 * returns a non-null packet then it is read with the {@link Consumer} specified in
	 * {@link OpcodePacketBuilder#register(int, ReadablePacket, Consumer)}.
	 *
	 * @param socket to listen for opcodes
	 * @param repeat whether this listen operation should repeat until the specified socket has closed
	 * @return this
	 */
	public OpcodePacket listen(Socket socket, boolean repeat)
	{
		packet.read(socket, new Consumer<ReadResult>()
		{
			@Override
			public void accept(ReadResult readResult)
			{
				int opcode = ((Number) readResult.poll()).intValue();
				System.out.println("Opcode: " + opcode);
				RegisteredPacket<E> packet = registeredPackets.get(opcode);
				if(packet == null)
				{
					for(BiConsumer<Socket, Integer> invalidOpcodeConsumer : onInvalidOpcode)
					{
						invalidOpcodeConsumer.accept(socket, opcode);
					}
					return;
				}
				Consumer<ReadResult> readResultConsumer = packet.getReadResultConsumer();
				packet.getPacket().read(socket, readResult1 ->
				{
					try
					{
						if(permissionHandler != null)
						{
							//check here instead of outside since it may change
							E permission = packet.getPermission();
							if(permission == null || !permissionHandler.hasPermission(socket, permission))
							{
								permissionHandler.noPermission(socket, permission, opcode);
								return;
							}
						}
						readResultConsumer.accept(readResult1);
					} finally
					{
						if(repeat)
						{
							OpcodePacket.this.packet.read(socket, this);
						}
					}
				});
			}
		});
		return this;
	}
	
	/**
	 * Returns all registered packets passed from the {@link OpcodePacketBuilder}.
	 *
	 * @return all registered packets passed from the {@link OpcodePacketBuilder}
	 */
	public Map<Integer, RegisteredPacket<E>> getRegisteredPackets()
	{
		return registeredPackets;
	}
	
	/**
	 * @return permission handler if exists ({@code null} if it doesn't exist)
	 */
	public PermissionHandler<E> getPermissionHandler()
	{
		return permissionHandler;
	}
}