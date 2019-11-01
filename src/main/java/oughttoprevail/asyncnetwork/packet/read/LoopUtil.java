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

import java.util.ArrayList;
import java.util.List;

import oughttoprevail.asyncnetwork.packet.Deserializer;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Consumer;

class LoopUtil
{
	private final ReadResult readResult;
	private final Consumer<ReadResult> onFinish;
	private final List<Object> readInstructions;
	private int index;
	
	LoopUtil(ReadResult readResult, Consumer<ReadResult> onFinish, List<Object> readInstructions)
	{
		this.readResult = readResult;
		this.onFinish = onFinish;
		this.readInstructions = new ArrayList<>(readInstructions);
	}
	
	void continueLoop()
	{
		while(index != readInstructions.size())
		{
			Object obj = readInstructions.get(index++);
			if(obj instanceof DependentObject)
			{
				obj = ((DependentObject) obj).getUnderlyingObject();
				readInstructions.remove(--index);
			}
			if(obj instanceof Consumer)
			{
				//registered consumer
				Consumer<ReadResult> consumer = (Consumer<ReadResult>) obj;
				consumer.accept(readResult);
				readResult.futureAdd();
			} else if(obj instanceof BiConsumer)
			{
				//dependent consumer
				BiConsumer<ReadablePacketBuilder, ReadResult> dependentConsumer = (BiConsumer<ReadablePacketBuilder, ReadResult>) obj;
				notifyWhenGoal(() ->
				{
					ReadablePacketBuilder builder = new ReadablePacketBuilder(readInstructions, index);
					dependentConsumer.accept(builder, readResult);
					builder.built();
				});
				return;
			} else if(obj instanceof Deserializer)
			{
				Deserializer deserializer = (Deserializer) obj;
				notifyWhenGoal(() ->
				{
					Object deserializedObject = deserializer.deserialize(readResult);
					readResult.futureAdd();
					readResult.add(deserializedObject);
				});
				return;
			} else if(obj instanceof TimesRepeat)
			{
				//repeat
				TimesRepeat timesRepeat = (TimesRepeat) obj;
				TimesRepeatHelper helper;
				readInstructions.set(index - 1, helper = new TimesRepeatHelper(index - 1 - timesRepeat.getLength(), timesRepeat.getTimesToRepeat()));
				index = helper.beginIndex;
			} else if(obj instanceof TimesRepeatHelper)
			{
				TimesRepeatHelper helper = (TimesRepeatHelper) obj;
				if(helper.loop())
				{
					readInstructions.remove(--index);
				} else
				{
					index = helper.beginIndex;
				}
			} else if(obj instanceof StartSection)
			{
				StartSection startSection = (StartSection) obj;
				notifyWhenGoal(() -> readResult.startSection(startSection));
				return;
			} else if(obj instanceof EndSection)
			{
				notifyWhenGoal(readResult::endSection);
				return;
			}
		}
		readResult.notifyWhenGoal(() -> onFinish.accept(readResult));
	}
	
	private void notifyWhenGoal(Runnable runnable)
	{
		readResult.notifyWhenGoal(() ->
		{
			runnable.run();
			continueLoop();
		});
	}
	
	private static class TimesRepeatHelper
	{
		private final int beginIndex;
		private int remainingLoops;
		
		private TimesRepeatHelper(int beginIndex, int remainingLoops)
		{
			this.beginIndex = beginIndex;
			this.remainingLoops = remainingLoops;
		}
		
		private boolean loop()
		{
			return --remainingLoops <= 0;
		}
	}
}