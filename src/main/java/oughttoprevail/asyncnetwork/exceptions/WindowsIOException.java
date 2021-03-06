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
 * Thrown when an exception occurs within {@link oughttoprevail.asyncnetwork.util.selector.WindowsSelector}
 */
public class WindowsIOException extends IOException
{
	/**
	 * Constructs an {@link WindowsIOException} which is thrown when an exception
	 * occurs within {@link oughttoprevail.asyncnetwork.util.selector.WindowsSelector}.
	 *
	 * @param message the explanation of the exception
	 */
	public WindowsIOException(String message)
	{
		super(/*substring the ".\r\n" at the end*/message.substring(0, message.length() - 3));
	}
}