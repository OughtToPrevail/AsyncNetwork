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
				System.out.println("Dependent object");
				obj = ((DependentObject) obj).getUnderlyingObject();
				readInstructions.remove(--index);
			}
			System.out.println("Index: " + (index - 1) + ", obj: " + obj + " " + hashCode());
			if(obj instanceof Consumer)
			{
				//registered consumer
				System.out.println("Accept consumer");
				Consumer<ReadResult> consumer = (Consumer<ReadResult>) obj;
				consumer.accept(readResult);
				readResult.futureAdd();
			} else if(obj instanceof BiConsumer)
			{
				//dependent consumer
				System.out.println("Dependent");
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
				System.out.println("Deserializer");
				Deserializer deserializer = (Deserializer) obj;
				notifyWhenGoal(() ->
				{
					System.out.println("Notified");
					Object deserializedObject = deserializer.deserialize(readResult);
					readResult.futureAdd();
					System.out.println("Deserialized: " + deserializedObject);
					readResult.add(deserializedObject);
				});
				return;
			} else if(obj instanceof TimesRepeat)
			{
				//repeat
				TimesRepeat timesRepeat = (TimesRepeat) obj;
				TimesRepeatHelper helper;
				readInstructions.set(index - 1, helper = new TimesRepeatHelper(index - 1 - timesRepeat.getLength(), timesRepeat.getTimesToRepeat()));
				System.out.println("Return to " +
				                   helper.beginIndex +
				                   " " +
				                   helper.remainingLoops +
				                   " from " +
				                   index +
				                   ", length: " +
				                   timesRepeat.getLength());
				index = helper.beginIndex;
			} else if(obj instanceof TimesRepeatHelper)
			{
				TimesRepeatHelper helper = (TimesRepeatHelper) obj;
				System.out.println("Helper: " + helper.beginIndex + " " + helper.remainingLoops);
				if(helper.loop())
				{
					readInstructions.remove(--index);
				} else
				{
					System.out.println("Go back");
					index = helper.beginIndex;
				}
			} else if(obj instanceof StartSection)
			{
				StartSection startSection = (StartSection) obj;
				System.out.println("Start section");
				notifyWhenGoal(() -> readResult.startSection(startSection));
				return;
			} else if(obj instanceof EndSection)
			{
				System.out.println("End section");
				notifyWhenGoal(readResult::endSection);
				return;
			}
		}
		System.out.println("Finish");
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