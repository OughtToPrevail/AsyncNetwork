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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.Consumer;

class LoopUtil
{
	private final ReadablePacket readablePacket;
	private final Socket socket;
	private final ReadResult readResult;
	private final Consumer<ReadResult> onFinish;
	private int size;
	private final List<Iterator<Object>> afterThen = new ArrayList<>();
	private boolean stopLoop;
	private int totalRunningIterators;
	private int waitingTimesRepeat;
	private boolean finished;
	
	LoopUtil(ReadablePacket readablePacket, Socket socket, ReadResult readResult, Consumer<ReadResult> onFinish)
	{
		this.readablePacket = readablePacket;
		this.socket = socket;
		this.readResult = readResult;
		this.onFinish = onFinish;
	}
	
	void read(ReadableElement element)
	{
		performIterator(element.getChildrenNConsumers().iterator(), false);
	}
	
	private void performIterator(Iterator<Object> iterator, boolean first)
	{
		totalRunningIterators++;
		try
		{
			while(iterator.hasNext())
			{
				if(stopLoop)
				{
					System.out.println("Stop");
					if(first)
					{
						afterThen.add(0, iterator);
					} else
					{
						afterThen.add(iterator);
					}
					return;
				}
				System.out.println("Next");
				Object obj = iterator.next();
				if(obj instanceof RegisteredConsumer)
				{
					RegisteredConsumer consumer = (RegisteredConsumer) obj;
					consumer.getConsumer().accept(readResult);
					size += consumer.getSize();
					System.out.println("add to size: " + size + " " + consumer.getSize());
				} else
				{
					ReadableElement child = (ReadableElement) obj;
					System.out.println("perform child element : " + child.getTimesToRepeat());
					readablePacket.performLoop(this, child);
				}
			}
		} finally
		{
			totalRunningIterators--;
		}
		System.out.println("Finished iterator");
		possiblyFinished();
	}
	
	void waitTimesRepeat()
	{
		waitingTimesRepeat++;
	}
	
	void finishedTimesRepeat(boolean read)
	{
		waitingTimesRepeat--;
		if(!read)
		{
			System.out.println("Finished times repeat");
			possiblyFinished();
		}
	}
	
	private void possiblyFinished()
	{
		System.out.println("Possibly finished: " + totalRunningIterators + " " + waitingTimesRepeat + " " + finished + " " + stopLoop);
		if(totalRunningIterators == 0 && waitingTimesRepeat == 0 && !finished && !stopLoop)
		{
			finished = true;
			readResult.notifyWhen(size, () -> onFinish.accept(readResult));
		}
	}
	
	void then(Runnable runnable)
	{
		stopLoop = true;
		System.out.println("Then: " + size);
		readResult.notifyWhen(size, () ->
		{
			System.out.println("Finished then");
			stopLoop = false;
			runnable.run();
			Iterator<Iterator<Object>> iterator = afterThen.iterator();
			while(iterator.hasNext())
			{
				if(stopLoop)
				{
					return;
				}
				Iterator<Object> childrenNConsumersIterator = iterator.next();
				iterator.remove();
				System.out.println("perform iterator from then " + afterThen.size());
				performIterator(childrenNConsumersIterator, true);
			}
			System.out.println("Invoke possibly finished");
			possiblyFinished();
		});
	}
	
	ReadResult getReadResult()
	{
		return readResult;
	}
	
	Socket getSocket()
	{
		return socket;
	}
	
	void incrementSize()
	{
		size++;
	}
}