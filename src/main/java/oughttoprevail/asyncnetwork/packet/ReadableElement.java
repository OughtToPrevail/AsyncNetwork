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
import java.util.List;

import oughttoprevail.asyncnetwork.Socket;
import oughttoprevail.asyncnetwork.util.BiConsumer;
import oughttoprevail.asyncnetwork.util.Predicate;

class ReadableElement
{
	private final List<BiConsumer<Socket, ReadResultImpl>> consumers;
	private final Predicate<ReadResult> predicate;
	private final PassedNumber timesToRepeat;
	private int size;
	
	ReadableElement(Predicate<ReadResult> predicate, PassedNumber timesToRepeat)
	{
		this.consumers = new ArrayList<>();
		this.predicate = predicate;
		this.timesToRepeat = timesToRepeat;
	}
	
	/**
	 * Adds the specified consumer to the consumers list and increases the size by the specified size.
	 *
	 * @param consumer to add to the consumers list
	 * @param size of how many results this consumer will add
	 */
	void add(BiConsumer<Socket, ReadResultImpl> consumer, int size)
	{
		this.size += size;
		consumers.add(consumer);
	}
	
	/**
	 * Returns the consumers of this element.
	 *
	 * @return the consumers of this element
	 */
	List<BiConsumer<Socket, ReadResultImpl>> getConsumers()
	{
		return consumers;
	}
	
	/**
	 * Evaluates whether this element should be read using the predicate given in
	 * {@link ReadableElement#ReadableElement(Predicate, PassedNumber)}.
	 *
	 * @param readResult to evaluate with
	 * @return whether this element should be read
	 */
	boolean test(ReadResult readResult)
	{
		return predicate.test(readResult);
	}
	
	/**
	 * Returns whether this {@link ReadableElement} has a predicate.
	 *
	 * @return whether this {@link ReadableElement} has a predicate
	 */
	boolean hasPredicate()
	{
		return predicate != null;
	}
	
	/**
	 * Returns this {@link ReadableElement} timesToRepeat value.
	 *
	 * @return this {@link ReadableElement} timesToRepeat value
	 */
	PassedNumber getTimesToRepeat()
	{
		return timesToRepeat;
	}
	
	/**
	 * Returns whether this {@link ReadableElement} has a passed number.
	 *
	 * @return whether this {@link ReadableElement} has a passed number
	 */
	boolean hasTimesToRepeat()
	{
		return timesToRepeat != null;
	}
	
	/**
	 * Repeats all consumers
	 */
	void repeat()
	{
		int size = consumers.size();
		for(int i = 0; i < size; i++)
		{
			consumers.add(consumers.get(i));
		}
	}
	
	/**
	 * Returns the size of how many results will be added by this element.
	 *
	 * @return the size of how many results will be added by this element
	 */
	int size()
	{
		return size;
	}
}