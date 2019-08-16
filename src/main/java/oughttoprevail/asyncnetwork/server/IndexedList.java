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
package oughttoprevail.asyncnetwork.server;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class IndexedList<T>
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
	 * Free spaces available in the {@link #list}.
	 */
	private final Queue<Integer> freeSpaces = new ArrayDeque<>();
	
	/**
	 * Returns a possible index you can add to the list using {@link IndexedList#add(int, Object)}.
	 *
	 * @return a possible index you can add to the list using {@link IndexedList#add(int, Object)}
	 */
	public int index()
	{
		synchronized(freeSpaces)
		{
			if(freeSpaces.isEmpty())
			{
				synchronized(list)
				{
					int index = list.size();
					list.add(null);
					return index;
				}
			} else
			{
				return freeSpaces.poll();
			}
		}
	}
	
	/**
	 * Adds the specified index into the queue for empty spaces.
	 *
	 * @param index the index which will be added into the empty spaces queue
	 */
	public void fail(int index)
	{
		synchronized(freeSpaces)
		{
			freeSpaces.offer(index);
		}
	}
	
	/**
	 * Sets the specified index to the specified value.
	 *
	 * @param index the index in which the value will be put in
	 * @param value the value which will be put in the index
	 */
	public void add(int index, T value)
	{
		synchronized(list)
		{
			list.set(index, value);
		}
	}
	
	/**
	 * Returns the client in the specified index.
	 *
	 * @param index the index in which the client will taken from
	 * @return the client in the specified index
	 */
	public T get(int index)
	{
		synchronized(list)
		{
			return list.get(index);
		}
	}
	
	/**
	 * Removes the client in the specified index.
	 *
	 * @param index the client in which the specified index will be removed from
	 */
	public void remove(int index)
	{
		fail(index);
		synchronized(list)
		{
			list.set(index, null);
		}
	}
	
	/**
	 * Returns an unmodifiable list of the values
	 *
	 * @return an unmodifiable list of the values
	 */
	public List<T> list()
	{
		return listUnmodifiable;
	}
}
