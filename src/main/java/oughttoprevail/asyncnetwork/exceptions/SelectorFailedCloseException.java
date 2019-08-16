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

import oughttoprevail.asyncnetwork.server.ServerSocket;
import oughttoprevail.asyncnetwork.util.selector.LinuxMacSelector;

/**
 * Thrown when {@link LinuxMacSelector#close()} returns true which is returned when {@link
 * LinuxMacSelector} fails to close.
 */
public class SelectorFailedCloseException extends IllegalArgumentException
{
	/**
	 * Constructs an {@link SelectorFailedCloseException} which is thrown when {@link ServerSocket#close()} tries to {@link AutoCloseable#close()}
	 * on the selector and fails due to an {@link java.io.IOException}.
	 *
	 * @param throwable the {@link java.io.IOException} thrown by {@link AutoCloseable#close()}
	 */
	public SelectorFailedCloseException(Throwable throwable)
	{
		super(throwable);
	}
}
