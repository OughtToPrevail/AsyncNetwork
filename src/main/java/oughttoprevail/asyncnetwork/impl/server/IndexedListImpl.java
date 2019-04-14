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
package oughttoprevail.asyncnetwork.impl.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import oughttoprevail.asyncnetwork.util.IndexedList;

public class IndexedListImpl<T> implements IndexedList<T>
{
	/**
	 * List of specified T.
	 */
	private final List<T> list = new ArrayList<>();
	/**
	 * Unmodifiable list of specified T.
	 */
	private final List<T> listUnmodifiable = Collections.unmodifiableList(list);
	/**
	 * Free spaces left in the {@link #list}.
	 */
	private final Queue<Integer> freeSpaces = new LinkedList<>();
	
	/**
	 * Returns a possible index you can add to the list using {@link IndexedList#add(int, Object)}.
	 *
	 * @return a possible index you can add to the list using {@link IndexedList#add(int, Object)}
	 */
	@Override
	public int index()
	{
		if(freeSpaces.isEmpty())
		{
			int index = list.size();
			list.add(null);
			return index;
		} else
		{
			return freeSpaces.poll();
		}
	}
	
	/**
	 * Adds the specified index into the queue for empty spaces.
	 *
	 * @param index the index which will be added into the empty spaces queue
	 */
	@Override
	public void fail(int index)
	{
		freeSpaces.add(index);
	}
	
	/**
	 * Sets the specified index to the specified value.
	 *
	 * @param index the index in which the value will be put in
	 * @param value the value which will be put in the index
	 */
	@Override
	public void add(int index, T value)
	{
		list.set(index, value);
	}
	
	/**
	 * Returns the client in the specified index.
	 *
	 * @param index the index in which the client will taken from
	 * @return the client in the specified index
	 */
	@Override
	public T get(int index)
	{
		return list.get(index);
	}
	
	/**
	 * Removes the client in the specified index.
	 *
	 * @param index the client in which the specified index will be removed from
	 */
	@Override
	public void remove(int index)
	{
		freeSpaces.offer(index);
		list.set(index, null);
	}
	
	/**
	 * Returns an unmodifiable list of the values
	 *
	 * @return an unmodifiable list of the values
	 */
	@Override
	public List<T> list()
	{
		return listUnmodifiable;
	}
}
