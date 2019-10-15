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
import java.util.Deque;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Consumer;
import oughttoprevail.asyncnetwork.util.Validator;

public class ReadablePacket
{
	public static final ReadablePacket EMPTY = new ReadablePacket(new ReadableElement(null, null), true, 0);
	
	private final ReadableElement topMostParent;
	private final boolean skip;
	private final int totalSize;
	
	public ReadablePacket(ReadableElement topMostParent, boolean skip, int totalSize)
	{
		this.topMostParent = topMostParent;
		this.skip = skip;
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
		ReadResultImpl readResult = new ReadResultImpl(socket, queue, consumer, totalSize);
		LoopUtil loopUtil = new LoopUtil(this, socket, readResult);
		performLoop(loopUtil, topMostParent);
		return this;
	}
	
	void performLoop(LoopUtil loopUtil, ReadableElement element)
	{
		if(element.hasPredicate())
		{
			loopUtil.then(() ->
			{
				if(element.test(loopUtil.getReadResult()))
				{
					loopUtil.read(element);
				}
			});
		} else if(element.hasTimesToRepeat())
		{
			PassedNumber timesToRepeat = element.getTimesToRepeat();
			loopUtil.getSocket().readByteBuffer(byteBuffer ->
			{
				Number value = timesToRepeat.apply(byteBuffer);
				System.out.println("VALUE: " + value);
				if(!skip)
				{
					System.out.println("add");
					loopUtil.getReadResult().add(value);
					loopUtil.incrementSize();
				}
				int intValue = value.intValue();
				for(int i = 0; i < intValue; i++)
				{
					loopUtil.read(element);
				}
			}, timesToRepeat.getSize());
		} else
		{
			loopUtil.read(element);
		}
	}
}