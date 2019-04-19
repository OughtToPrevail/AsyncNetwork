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
package oughttoprevail.asyncnetwork.impl.packet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import oughttoprevail.asyncnetwork.Channel;
import oughttoprevail.asyncnetwork.impl.util.Validator;
import oughttoprevail.asyncnetwork.packet.ReadResult;
import oughttoprevail.asyncnetwork.packet.ReadablePacket;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

public class ReadablePacketImpl implements ReadablePacket
{
	private final List<ReadableElement> readInstructions;
	
	public ReadablePacketImpl(List<ReadableElement> readInstructions)
	{
		this.readInstructions = readInstructions;
	}
	
	/**
	 * Reads from the specified channel, once the read has finished the specified consumer will be
	 * invoked with the results.
	 *
	 * @param channel to read from
	 * @param consumer to invoke with the results once the read operation has completed
	 * @return this
	 */
	@Override
	public ReadablePacket read(Channel<?> channel, Consumer<ReadResult> consumer)
	{
		Validator.requireNonNull(consumer, "Consumer");
		if(readInstructions.isEmpty())
		{
			return this;
		}
		Deque<Object> queue = new ArrayDeque<>();
		int totalSize = 0;
		for(ReadableElement readInstruction : readInstructions)
		{
			totalSize += readInstruction.size();
		}
		ReadResultImpl readResult = new ReadResultImpl(channel, queue);
		loop(channel, readResult, 0, 0, totalSize, consumer);
		return this;
	}
	
	private void loop(Channel<?> channel,
			ReadResultImpl readResult,
			int currentSize,
			int index,
			int totalSize,
			Consumer<ReadResult> consumer)
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
						read = read(channel, readResult, readInstruction);
					}
					loop(channel, readResult, finalCurrentSize + read, finalIndex, totalSize, consumer);
				});
				return;
			} else
			{
				currentSize += read(channel, readResult, readInstruction);
			}
		}
		readResult.notifyWhen(totalSize, () -> consumer.accept(readResult));
	}
	
	private int read(Channel<?> channel, ReadResultImpl readResult, ReadableElement readInstruction)
	{
		List<BiConsumer<Channel<?>, ReadResultImpl>> consumers = readInstruction.getConsumers();
		for(BiConsumer<Channel<?>, ReadResultImpl> instruction : consumers)
		{
			instruction.accept(channel, readResult);
		}
		return consumers.size();
	}
}