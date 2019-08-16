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
package oughttoprevail.asyncnetwork.util;

import java.io.IOException;

import oughttoprevail.asyncnetwork.CloseType;
import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.server.AbstractServer;

public interface Validator
{
	static void requireNonNull(Object o)
	{
		if(o == null)
		{
			throw new NullPointerException();
		}
	}
	
	static void requireNonNull(Object o, String str)
	{
		if(o == null)
		{
			throw new NullPointerException(str + " cannot be null!");
		}
	}
	
	static void validatePort(int port)
	{
		if(port < 0 || port > 0xFFFF)
		{
			throw new IllegalArgumentException("The port must be between 0 and 65535!");
		}
	}
	
	static void higherThan0(int n, String str)
	{
		if(n <= 0)
		{
			throw new IndexOutOfBoundsException(str + " must be larger than 0!");
		}
	}
	
	String REMOTE_HOST_CLOSE_ERROR = "An existing connection was forcibly closed by the remote host";
	String RESET_BY_PEER_ERROR = "Connection reset by peer";
	String BROKEN_PIPE_ERROR = "Broken pipe";
	String NETWORK_NO_LONGER_AVAILABLE = "The specified network name is no longer available";
	String INTERRUPTED = "A blocking operation was interrupted by a call to WSACancelBlockingCall";
	
	static void handleRemoteHostCloseException(Socket socket, IOException e)
	{
		//if the socket is already closed no point in trying to guess the disconnectionType
		if(socket.isClosed())
		{
			socket.manager().exception(e);
			return;
		}
		String message = e.getMessage();
		if(message != null)
		{
			switch(message)
			{
				case REMOTE_HOST_CLOSE_ERROR:
				case RESET_BY_PEER_ERROR:
				case BROKEN_PIPE_ERROR:
				case NETWORK_NO_LONGER_AVAILABLE:
				case INTERRUPTED:
				{
					socket.manager().close(DisconnectionType.REMOTE_CLOSE_BY_EXCEPTION);
					socket.manager().exception(e);
					return;
				}
			}
		}
		exceptionClose(socket, e);
	}
	
	static void exceptionClose(Socket socket, Throwable throwable)
	{
		socket.manager().close(DisconnectionType.EXCEPTION_CLOSE);
		socket.manager().exception(throwable);
	}
	
	static void exceptionClose(AbstractServer server, Throwable throwable)
	{
		server.manager().close(CloseType.EXCEPTION_CLOSE);
		server.manager().exception(throwable);
	}
	
	static void runRunnable(Runnable runnable)
	{
		if(runnable != null)
		{
			runnable.run();
		}
	}
}