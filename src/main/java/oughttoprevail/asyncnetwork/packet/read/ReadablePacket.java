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
package oughttoprevail.asyncnetwork.packet.read;

import java.util.Collections;
import java.util.List;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.Validator;

public class ReadablePacket
{
	public static final ReadablePacket EMPTY = new ReadablePacket(Collections.emptyList());
	
	private final List<Object> readInstructions;
	
	public ReadablePacket(List<Object> readInstructions)
	{
		this.readInstructions = readInstructions;
	}
	
	/**
	 * Reads from the specified socket, once the read has finished the specified consumer will be
	 * invoked with the results.
	 *
	 * @param socket to read from
	 * @param consumer to invoke with the results once the read operation has completed
	 * @return this
	 */
	public ReadablePacket read(Socket socket, Consumer<ReadResult> consumer)
	{
		Validator.requireNonNull(consumer, "Consumer");
		ReadResult readResult = new ReadResult(socket);
		LoopUtil loopUtil = new LoopUtil(readResult, consumer, readInstructions);
		loopUtil.continueLoop();
		return this;
	}
}