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
package oughttoprevail.asyncnetwork.exceptions;

import java.io.IOException;

/**
 * Thrown when the {@link oughttoprevail.asyncnetwork.util.WindowsSelector#select(int, long)} has failed
 * GetQueuedCompletionStatus with overlapped setValue (not null).
 */
public class SelectException extends IOException
{
	private final int index;
	
	/**
	 * Constructs an {@link SelectException} which is thrown when {@link oughttoprevail.asyncnetwork.util.WindowsSelector#select(int, long)}
	 * has failed GetQueuedCompletionStatus with overlapped setValue (not null).
	 *
	 * @param message the explanation of the exception
	 * @param index the index of the channel with the error
	 */
	public SelectException(String message, int index)
	{
		super(message);
		this.index = index;
	}
	
	/**
	 * Returns the index of the channel.
	 *
	 * @return the index of the channel
	 */
	public int getIndex()
	{
		return index;
	}
}
