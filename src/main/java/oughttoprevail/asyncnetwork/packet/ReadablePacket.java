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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Validator;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

public class ReadablePacket
{
	public static final ReadablePacket EMPTY = new ReadablePacket(Collections.emptyList(), true);
	
	private final List<ReadableElement> readInstructions;
	private final boolean skip;
	private final int totalSize;
	
	public ReadablePacket(List<ReadableElement> readInstructions, boolean skip)
	{
		this.readInstructions = readInstructions;
		this.skip = skip;
		int totalSize = 0;
		for(ReadableElement readInstruction : readInstructions)
		{
			totalSize += readInstruction.size();
			if(readInstruction.hasTimesToRepeat() && !skip)
			{
				totalSize++;
			}
		}
		this.totalSize = totalSize;
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
		Deque<Object> queue = new ArrayDeque<>();
		ReadResultImpl readResult = new ReadResultImpl(socket, queue);
		loop(socket, readResult, 0, 0, totalSize, consumer);
		return this;
	}
	
	private void loop(Socket socket, ReadResultImpl readResult, int currentSize, int index, int totalSize, Consumer<ReadResult> consumer)
	{
		for(; index < readInstructions.size(); index++)
		{
			ReadableElement readInstruction = readInstructions.get(index);
			if(readInstruction.hasPredicate())
			{
				int finalCurrentSize = currentSize;
				int finalIndex = index + 1;
				readResult.notifyWhen(currentSize, () ->
				{
					int read = 0;
					if(readInstruction.test(readResult))
					{
						read = read(socket, readResult, readInstruction);
					}
					loop(socket, readResult, finalCurrentSize + read, finalIndex, totalSize, consumer);
				});
				return;
			} else if(readInstruction.hasTimesToRepeat())
			{
				int finalCurrentSize = currentSize;
				int finalIndex = index + 1;
				PassedNumber timesToRepeat = readInstruction.getTimesToRepeat();
				socket.readByteBuffer(byteBuffer ->
				{
					Number value = timesToRepeat.get(byteBuffer);
					if(!skip)
					{
						readResult.add(value);
					}
					int intValue = value.intValue();
					int newCurrentSize = finalCurrentSize + (skip ? 0 : 1);
					readResult.notifyWhen(newCurrentSize, () ->
					{
						int read = 0;
						for(int i = 0; i < intValue; i++)
						{
							read += read(socket, readResult, readInstruction);
						}
						loop(socket, readResult, newCurrentSize + read, finalIndex, totalSize, consumer);
					});
				}, timesToRepeat.getSize());
				return;
			} else
			{
				currentSize += read(socket, readResult, readInstruction);
			}
		}
		readResult.notifyWhen(totalSize, () -> consumer.accept(readResult));
	}
	
	private int read(Socket socket, ReadResultImpl readResult, ReadableElement readInstruction)
	{
		List<BiConsumer<Socket, ReadResultImpl>> consumers = readInstruction.getConsumers();
		for(BiConsumer<Socket, ReadResultImpl> instruction : consumers)
		{
			instruction.accept(socket, readResult);
		}
		return readInstruction.size();
	}
}