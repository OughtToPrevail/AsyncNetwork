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
package oughttoprevail.asyncnetwork.util;

import java.util.List;

import oughttoprevail.asyncnetwork.impl.server.IndexedListImpl;

/**
 * Implementation at {@link IndexedListImpl}.
 */
public interface IndexedList<T>
{
	/**
	 * Returns a possible index you can add to the list using {@link IndexedList#add(int, Object)}.
	 *
	 * @return a possible index you can add to the list using {@link IndexedList#add(int, Object)}
	 */
	int index();
	
	/**
	 * Adds the specified index into the queue for empty spaces.
	 *
	 * @param index the index which will be added into the empty spaces queue
	 */
	void fail(int index);
	
	/**
	 * Sets the specified index to the specified value.
	 *
	 * @param index the index in which the value will be put in
	 * @param value the value which will be put in the index
	 */
	void add(int index, T value);
	
	/**
	 * Returns the client in the specified index.
	 *
	 * @param index the index in which the client will taken from
	 * @return the client in the specified index
	 */
	T get(int index);
	
	/**
	 * Removes the client in the specified index.
	 *
	 * @param index the client in which the specified index will be removed from
	 */
	void remove(int index);
	
	/**
	 * Returns an unmodifiable list of the clients.
	 *
	 * @return an unmodifiable list of the clients
	 */
	List<T> list();
}