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
	private final ReadResultImpl readResult;
	private final Consumer<ReadResult> onFinish;
	private int size;
	private List<Runnable> afterThen = new ArrayList<>();
	private boolean stopLoop;
	private boolean finished;
	private int totalRunningIterators;
	
	LoopUtil(ReadablePacket readablePacket, Socket socket, ReadResultImpl readResult, Consumer<ReadResult> onFinish)
	{
		this.readablePacket = readablePacket;
		this.socket = socket;
		this.readResult = readResult;
		this.onFinish = onFinish;
	}
	
	void read(ReadableElement element)
	{
		Iterator<Consumer<ReadResultImpl>> iterator = element.getConsumers().iterator();
		performReadIterator(element, iterator);
	}
	
	private void performReadIterator(ReadableElement element, Iterator<Consumer<ReadResultImpl>> iterator)
	{
		while(iterator.hasNext())
		{
			if(stopLoop)
			{
				afterThen.add(() -> performReadIterator(element, iterator));
				return;
			}
			Consumer<ReadResultImpl> consumer = iterator.next();
			consumer.accept(readResult);
		}
		size += element.size();
		continueIterator(element.getChildren().iterator());
	}
	
	private void continueIterator(Iterator<ReadableElement> iterator)
	{
		totalRunningIterators++;
		while(iterator.hasNext())
		{
			if(stopLoop)
			{
				afterThen.add(() -> continueIterator(iterator));
				totalRunningIterators--;
				return;
			}
			ReadableElement child = iterator.next();
			readablePacket.performLoop(this, child);
		}
		totalRunningIterators--;
		if(totalRunningIterators == 0 && !finished)
		{
			finished = true;
			onFinish.accept(readResult);
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
		});
	}
	
	ReadResultImpl getReadResult()
	{
		return readResult;
	}
	
	Socket getSocket()
	{
		return socket;
	}
	
	public void incrementSize()
	{
		size++;
	}
}