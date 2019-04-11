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
package oughttoprevail.asyncnetwork;

import oughttoprevail.asyncnetwork.impl.client.ClientImpl;

/**
 * Implementation at {@link ClientImpl}.
 */
public interface Client extends IClient<Client>
{
	/**
	 * Creates a new client with the default values.
	 *
	 * @return a new client
	 */
	static Client newClient()
	{
		return new ClientImpl();
	}
	
	/**
	 * Creates a new client with the specified writeBufferSize, readBufferSize.
	 *
	 * @param bufferSize used by this client for allocating buffers and initializing default options
	 * @return a new client with the specified writeBufferSize and readBufferSize
	 */
	static Client newClient(int bufferSize)
	{
		return new ClientImpl(bufferSize);
	}
}