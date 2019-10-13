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

import oughttoprevail.asyncnetwork.util.Consumer;

public class RegisteredPacket<E extends Enum<E>>
{
	private final ReadablePacket packet;
	private final Consumer<ReadResult> readResultConsumer;
	
	public RegisteredPacket(ReadablePacket packet, Consumer<ReadResult> readResultConsumer)
	{
		this.packet = packet;
		this.readResultConsumer = readResultConsumer;
	}
	
	public ReadablePacket getPacket()
	{
		return packet;
	}
	
	public Consumer<ReadResult> getReadResultConsumer()
	{
		return readResultConsumer;
	}
	
	/**
	 * Permission required to run the packet
	 */
	private E permission;
	
	/**
	 * Sets the permission required to run the packet to the specified permission.
	 *
	 * @param permission to set as the required permission to run the packet
	 */
	public void setPermission(E permission)
	{
		this.permission = permission;
	}
	
	/**
	 * @return the required permission to run the packet or {@code null} if one was not set
	 */
	public E getPermission()
	{
		return permission;
	}
}