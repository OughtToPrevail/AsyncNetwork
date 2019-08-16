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
package oughttoprevail.asyncnetwork.util.selector.flags;

import oughttoprevail.asyncnetwork.server.AbstractServer;
import oughttoprevail.asyncnetwork.server.IndexedList;
import oughttoprevail.asyncnetwork.server.ServerClientSocket;

public class LinuxSelectorFlags extends SelectorFlags
{
	private static final int LINUX_READ = 0x001;
	private static final int LINUX_WRITE = 0x004;
	
	public LinuxSelectorFlags(AbstractServer server, IndexedList<ServerClientSocket> clients)
	{
		super(server, clients, LINUX_READ, LINUX_WRITE);
	}
}
