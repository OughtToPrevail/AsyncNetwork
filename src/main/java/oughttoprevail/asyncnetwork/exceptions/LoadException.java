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

import oughttoprevail.asyncnetwork.impl.util.UnsafeGetter;
import oughttoprevail.asyncnetwork.util.LinuxMacSelector;

/**
 * Thrown when trying to use a not loaded {@link LinuxMacSelector} or when {@link UnsafeGetter}
 * fails to get Unsafe.
 */
public class LoadException extends Exception
{
	/**
	 * Constructs an {@link LoadException} which is thrown when the NativeLoader has failed.
	 *
	 * @param message the explanation of the exception
	 */
	public LoadException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructs an {@link LoadException} which is thrown when {@link UnsafeGetter} fails to load {@link sun.misc.Unsafe} when {@link IllegalAccessException} is thrown.
	 *
	 * @param message the explanation of the exception
	 * @param cause the cause of the exception
	 */
	public LoadException(String message, Throwable cause)
	{
		super(message, cause);
	}
}