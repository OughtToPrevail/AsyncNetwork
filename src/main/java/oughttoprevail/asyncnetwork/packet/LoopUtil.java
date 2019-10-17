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
	private final List<Runnable> afterThen = new ArrayList<>();
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
		Iterator<Consumer<ReadResult>> iterator = element.getConsumers().iterator();
		performReadIterator(element, iterator);
	}
	
	private void performReadIterator(ReadableElement element, Iterator<Consumer<ReadResult>> iterator)
	{
		totalRunningIterators++;
		try
		{
			while(iterator.hasNext())
			{
				if(stopLoop)
				{
					afterThen.add(() -> performReadIterator(element, iterator));
					return;
				}
				Consumer<ReadResult> consumer = iterator.next();
				consumer.accept(readResult);
			}
			size += element.size();
			continueIterator(element.getChildren().iterator());
		} finally
		{
			totalRunningIterators--;
		}
		possiblyFinished();
	}
	
	private void continueIterator(Iterator<ReadableElement> iterator)
	{
		while(iterator.hasNext())
		{
			if(stopLoop)
			{
				afterThen.add(() -> continueIterator(iterator));
				return;
			}
			ReadableElement child = iterator.next();
			readablePacket.performLoop(this, child);
		}
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
			possiblyFinished();
		}
	}
	
	private void possiblyFinished()
	{
		if(totalRunningIterators == 0 && waitingTimesRepeat == 0 && !finished && !stopLoop)
		{
			finished = true;
			readResult.notifyWhen(size, () -> onFinish.accept(readResult));
		}
	}
	
	void then(Runnable runnable)
	{
		stopLoop = true;
		readResult.notifyWhen(size, () ->
		{
			stopLoop = false;
			runnable.run();
			Iterator<Runnable> iterator = afterThen.iterator();
			while(iterator.hasNext())
			{
				if(stopLoop)
				{
					break;
				}
				Runnable after = iterator.next();
				iterator.remove();
				after.run();
			}
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