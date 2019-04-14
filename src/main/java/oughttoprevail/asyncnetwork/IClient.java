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

import java.net.SocketAddress;

import oughttoprevail.asyncnetwork.util.Consumer;

/**
 * Default implementation at {@link Client}.
 *
 * @param <T> the extending class
 */
public interface IClient<T extends IClient> extends Channel<T>
{
	/**
	 * Connects to the specified address.
	 *
	 * @param address the address that the channel will connect to
	 * @return this
	 * @throws java.nio.channels.AlreadyConnectedException if the channel is already connected
	 */
	T connect(SocketAddress address);
	
	/**
	 * Connects to the specified host and port.
	 *
	 * @param host the host that the channel will connect to
	 * @param port the port that the channel will connect to
	 * @return this
	 * @throws java.nio.channels.AlreadyConnectedException if the channel is already connected
	 */
	T connect(String host, int port);
	
	/**
	 * Connects to the specified port with {@link Channel#LOCAL_ADDRESS} as the host.
	 *
	 * @param port the port that the channel will connect to
	 * @return this
	 * @throws java.nio.channels.AlreadyConnectedException if the channel is already connected
	 */
	T connectLocalHost(int port);
	
	/**
	 * Invokes the specified runnable when the channel's connect process has successfully finished.
	 *
	 * @param onConnect the runnable that will be called the channel's connect has successfully
	 * finished
	 * @return this
	 */
	T onConnect(Runnable onConnect);
	
	/**
	 * Invokes the specified consumer when the channel's connect process has successfully finished.
	 *
	 * @param onConnect the consumer that will be called the channel's connect has successfully
	 * finished
	 * @return this
	 */
	T onConnect(Consumer<T> onConnect);
	
	/**
	 * Returns the client's manager.
	 *
	 * @return the client's manager
	 */
	ClientManager manager();
}