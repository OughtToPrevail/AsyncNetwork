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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.TriConsumer;

/**
 * A class to handle permissions
 * By default, a socket has all permissions.
 * This is basically just a map of permissions each socket has.
 *
 * @param <P> enum of permissions
 */
public class PermissionHandler<P extends Enum<P>>
{
	/**
	 * Map from Socket to a set of permissions the socket has
	 */
	private final Map<Socket, Set<P>> permissionMap = new ConcurrentHashMap<>();
	/**
	 * Consumer to be invoked when a socket doesn't have permission to a certain opcode
	 */
	private final List<TriConsumer<Socket, P, Integer>> onNoPermission = new ArrayList<>();
	
	/**
	 * Adds the specified permission to the specified socket.
	 *
	 * @param socket to add permission to
	 * @param permission to add to the socket
	 */
	public void addPermission(Socket socket, P permission)
	{
		Set<P> permissions = getPermissions(socket);
		if(permissions == null)
		{
			permissions = new HashSet<>();
			permissionMap.put(socket, permissions);
		}
		permissions.add(permission);
	}
	
	/**
	 * Removes the specified permission from the specified socket.
	 *
	 * @param socket to remove permission from
	 * @param permission to remove
	 * @return whether the socket has the permission in the first place
	 */
	public boolean removePermission(Socket socket, P permission)
	{
		Set<P> permissions = getPermissions(socket);
		return permissions != null && permissions.remove(permission);
	}
	
	/**
	 * @param socket to check whether it has the specified permission
	 * @param permission to check whether the socket has
	 * @return whether the specified socket has the specified permission
	 */
	public boolean hasPermission(Socket socket, P permission)
	{
		Set<P> permissions = getPermissions(socket);
		return permissions == null || permissions.contains(permission);
	}
	
	/**
	 * @param socket to get permissions from
	 * @return all permissions the specified socket has or {@code null} if it isn't registered
	 */
	public Set<P> getPermissions(Socket socket)
	{
		return permissionMap.get(socket);
	}
	
	/**
	 * @return the map of permissions
	 */
	public Map<Socket, Set<P>> getPermissionMap()
	{
		return permissionMap;
	}
	
	/**
	 * Adds the specified consumer to a list that'll be invoked when a packet in {@link OpcodePacket} is received on a {@link Socket} which
	 * is missing the permission required for the packet.
	 *
	 * @param onNoPermission to be invoked when a {@link Socket} is missing the required permission
	 */
	public void onNoPermission(TriConsumer<Socket, P, Integer> onNoPermission)
	{
		this.onNoPermission.add(onNoPermission);
	}
	
	/**
	 * Invokes all no permission event listeners with the specified parameters.
	 *
	 * @param socket parameter for the consumer
	 * @param permission parameter for the consumer
	 * @param opcode parameter for the consumer
	 */
	void noPermission(Socket socket, P permission, int opcode)
	{
		for(TriConsumer<Socket, P, Integer> consumer : onNoPermission)
		{
			consumer.accept(socket, permission, opcode);
		}
	}
}