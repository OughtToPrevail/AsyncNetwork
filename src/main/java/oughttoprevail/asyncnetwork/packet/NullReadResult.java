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

import oughttoprevail.asyncnetwork.Socket;

public class NullReadResult implements ReadResult
{
	private final Socket socket;
	
	public NullReadResult(Socket socket)
	{
		this.socket = socket;
	}
	
	private void throwException()
	{
		ReadResult.throwEnsureHasNext();
	}
	
	@Override
	public <T> T poll()
	{
		throwException();
		return null;
	}
	
	@Override
	public <T> T peek()
	{
		throwException();
		return null;
	}
	
	@Override
	public <T> T pollFirst()
	{
		throwException();
		return null;
	}
	
	@Override
	public <T> T peekFirst()
	{
		throwException();
		return null;
	}
	
	@Override
	public <T> T pollLast()
	{
		throwException();
		return null;
	}
	
	@Override
	public <T> T peekLast()
	{
		throwException();
		return null;
	}
	
	@Override
	public ReadResult skip()
	{
		throwException();
		return null;
	}
	
	@Override
	public ReadResult skip(int n)
	{
		throwException();
		return null;
	}
	
	@Override
	public boolean hasNext()
	{
		return false;
	}
	
	@Override
	public int collected()
	{
		return 0;
	}
	
	@Override
	public int available()
	{
		return 0;
	}
	
	@Override
	public Socket socket()
	{
		return socket;
	}
}