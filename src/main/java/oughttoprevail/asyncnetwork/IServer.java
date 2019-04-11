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
import java.nio.channels.ServerSocketChannel;
import java.util.List;

import oughttoprevail.asyncnetwork.util.Consumer;

;

/**
 * Default implementation at {@link Server}.
 *
 * @param <T> the extending class
 * @param <S> the server client
 */
public interface IServer<T extends IServer, S extends IServerClient>
{
	/**
	 * Infinite select timeout infinity is -1.
	 */
	int INFINITE_SELECT_TIMEOUT = -1;
	/**
	 * Default select timeout.
	 */
	int DEFAULT_SELECT_TIMEOUT = INFINITE_SELECT_TIMEOUT;
	/**
	 * Default select array size.
	 */
	int DEFAULT_SELECT_ARRAY_SIZE = 256;
	/**
	 * Default threads count.
	 */
	int DEFAULT_THREADS_COUNT = 0;
	
	static int getSystemThreadsCount()
	{
		return Runtime.getRuntime().availableProcessors() * 2;
	}
	
	/**
	 * Calls {@link ServerSocketChannel#bind(SocketAddress)} with the specified address.
	 *
	 * @param address the address that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress)}
	 * @return this
	 */
	T bind(SocketAddress address);
	
	/**
	 * Calls {@link ServerSocketChannel#bind(SocketAddress, int)} with the specified address and
	 * specified backlog.
	 *
	 * @param address the address that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress, int)}
	 * @param backlog maximum rate the server can accept new TCP connections on a socket
	 * if 0 Java will use it's default
	 * @return this
	 */
	T bind(SocketAddress address, int backlog);
	
	/**
	 * Bind to the specified port.
	 *
	 * @param port the port that will be used when calling {@link
	 * ServerSocketChannel#bind(SocketAddress)} with {@link
	 * java.net.InetSocketAddress#InetSocketAddress(int)}
	 * @return this
	 */
	T bind(int port);
	
	/**
	 * Binds to the specified address and the specified port.
	 *
	 * @param address the address that will be used when creating {@link
	 * java.net.InetSocketAddress#InetSocketAddress(String, int)}
	 * @param port the port that will be used when creating {@link
	 * java.net.InetSocketAddress#InetSocketAddress(String, int)}
	 * @return this
	 */
	T bind(String address, int port);
	
	/**
	 * Binds to the specified port with {@link Channel#LOCAL_ADDRESS} as the host.
	 *
	 * @param port the port that will be used when creating {@link
	 * java.net.InetSocketAddress#InetSocketAddress(int)}
	 * @return this
	 */
	T bindLocalHost(int port);
	
	/**
	 * Calls the specified runnable when the T has successfully binded.
	 *
	 * @param onBind the runnable that will be called when the T has successfully binded
	 * @return this
	 */
	T onBind(Runnable onBind);
	
	/**
	 * Calls the specified consumer with this T when the T has successfully binded.
	 *
	 * @param onBind the consumer that will be called when the T has successfully binded
	 * @return this
	 */
	T onBind(Consumer<T> onBind);
	
	/**
	 * Calls the specified consumer when a client has connected.
	 *
	 * @param onConnection the consumer that will be called when a client connects
	 * @return this
	 */
	T onConnection(Consumer<S> onConnection);
	
	/**
	 * Calls the specified consumer with a throwable when a caught exception occurs.
	 *
	 * @param onException the consumer that will be called with the caught exceptions
	 * @return this
	 */
	T onException(Consumer<Throwable> onException);
	
	/**
	 * Calls the specified consumer with a disconnectionType when the server is closed.
	 *
	 * @param onClose the consumer that will be called with the disconnectionType when the server is
	 * closed
	 * @return this
	 */
	T onClose(Consumer<DisconnectionType> onClose);
	
	/**
	 * Returns whether this server has binded.
	 *
	 * @return whether this server has binded
	 */
	boolean isBinded();
	
	/**
	 * Returns the {@link ServerSocketChannel} the T uses.
	 *
	 * @return the {@link ServerSocketChannel} the T uses
	 */
	ServerSocketChannel getServerChannel();
	
	/**
	 * Returns an unmodifiable list that contains the clients that have connected.
	 *
	 * @return an unmodifiable list that contains the clients that have connected
	 */
	List<S> getClients();
	
	/**
	 * Returns the bufferSize used when creating {@link IServerClient}.
	 *
	 * @return the bufferSize used when creating {@link IServerClient}
	 */
	int getBufferSize();
	
	/**
	 * Returns the selectTimeout.
	 *
	 * @return the selectTimeout
	 */
	int getSelectTimeout();
	
	/**
	 * Returns the selectArraySize.
	 *
	 * @return the selectArraySize
	 */
	int getSelectArraySize();
	
	/**
	 * Returns the server's thread count.
	 *
	 * @return the server's thread count
	 */
	int getThreadsCount();
	
	/**
	 * Returns the server's {@link SelectorImplementation}.
	 *
	 * @return the server's {@link SelectorImplementation}.
	 */
	SelectorImplementation getSelectorImplementation();
	
	/**
	 * If the T has yet to be closed the method closes the {@link ServerSocketChannel} and calls the
	 * onDisconnect on all clients that is specified by {@link Channel#onDisconnect(Consumer)}.
	 *
	 * @return this
	 */
	T close();
	
	/**
	 * Returns whether this server has been closed.
	 *
	 * @return whether this server has been closed
	 */
	boolean isClosed();
	
	/**
	 * Returns the {@link ServerManager}.
	 *
	 * @return the {@link ServerManager}
	 */
	ServerManager manager();
}