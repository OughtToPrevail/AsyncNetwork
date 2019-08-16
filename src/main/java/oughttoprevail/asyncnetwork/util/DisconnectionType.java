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

import oughttoprevail.asyncnetwork.Socket;

/**
 * Defines the DisconnectionType options.
 */
public enum DisconnectionType
{
	/**
	 * The user close {@link DisconnectionType} is used when a user invokes {@link Socket#close()}.
	 */
	USER_CLOSE,
	/**
	 * The remote close {@link DisconnectionType} is used when a read operation returns -1.
	 */
	REMOTE_CLOSE,
	/**
	 * The remote close {@link DisconnectionType} is used when a IOException is returned and {@link
	 * oughttoprevail.asyncnetwork.util.Validator#handleRemoteHostCloseException(Socket, IOException)} finds it as a remote close exception.
	 */
	REMOTE_CLOSE_BY_EXCEPTION,
	/**
	 * The exception close {@link DisconnectionType} is used when an exception occurs. Also {@link
	 * Socket#onException(Consumer)} consumer will be called.
	 */
	EXCEPTION_CLOSE}