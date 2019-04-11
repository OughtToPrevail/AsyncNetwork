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
package oughttoprevail.asyncnetwork.impl.util;

import java.io.IOException;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.DisconnectionType;
import oughttoprevail.asyncnetwork.IServer;

public interface Validator
{
	static void requireNonNull(Object o, String str)
	{
		if(o == null)
		{
			throw new NullPointerException(str + " cannot be null!");
		}
	}
	
	static void requireNonNullString(String str)
	{
		requireNonNull(str, "String");
	}
	
	static void validatePort(int port)
	{
		if(port < 0 || port > 0xFFFF)
		{
			throw new IllegalArgumentException("The port must be between 0 and 65535!");
		}
	}
	
	static void higherThan0(String str, int n)
	{
		if(n < 0)
		{
			throw new IndexOutOfBoundsException(str + " cannot be less than 0!");
		}
	}
	
	String REMOTE_HOST_CLOSE_ERROR = "An existing connection was forcibly closed by the remote host";
	String RESET_BY_PEER_ERROR = "Connection reset by peer";
	String BROKEN_PIPE_ERROR = "Broken pipe";
	String NETWORK_NO_LONGER_AVAILABLE = "The specified network name is no longer available.";
	
	static void handleRemoteHostCloseException(IOException e, Channel<?> channel)
	{
		String message = e.getMessage();
		if(message != null)
		{
			switch(message)
			{
				case REMOTE_HOST_CLOSE_ERROR:
				case RESET_BY_PEER_ERROR:
				case BROKEN_PIPE_ERROR:
				case NETWORK_NO_LONGER_AVAILABLE:
				{
					channel.manager().close(DisconnectionType.REMOTE_CLOSE_BY_EXCEPTION);
					channel.manager().exception(e);
					return;
				}
			}
		}
		exceptionClose(channel, e);
	}
	
	static void exceptionClose(Channel<?> channel, Throwable throwable)
	{
		channel.manager().close(DisconnectionType.EXCEPTION_CLOSE);
		channel.manager().exception(throwable);
	}
	
	static void exceptionClose(IServer<?, ?> server, Throwable throwable)
	{
		server.manager().close(DisconnectionType.EXCEPTION_CLOSE);
		server.manager().exception(throwable);
	}
}