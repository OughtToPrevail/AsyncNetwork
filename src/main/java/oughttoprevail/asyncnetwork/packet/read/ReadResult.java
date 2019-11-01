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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import oughttoprevail.asyncnetwork.Socket;

public class ReadResult
{
	private final Socket socket;
	private final Deque<Object> results;
	private int collected;
	private int sectionEntries;
	private final Object lock = new Object();
	private Runnable whenReachedGoal;
	private int goal;
	
	public ReadResult(Socket socket)
	{
		this.results = new ArrayDeque<>();
		this.socket = socket;
	}
	
	private ReadResult(Socket socket, Deque<Object> results)
	{
		this.results = results;
		this.socket = socket;
	}
	
	void futureAdd()
	{
		goal++;
	}
	
	/**
	 * Adds the specified obj to the results queue.
	 * If after adding the specified obj the results queue size is equal
	 * to the size specified in the {@link ReadResult} constructor
	 * then the runnable specified in the {@link ReadResult} constructor
	 * is invoked.
	 *
	 * @param obj to add to the results queue
	 */
	void add(Object obj)
	{
		synchronized(lock)
		{
			results.add(obj);
			System.out.println("Add " + results + " " + obj.getClass().getSimpleName() + ", " + obj);
			if(whenReachedGoal != null && results.size() >= goal)
			{
				Runnable temp = whenReachedGoal;
				whenReachedGoal = null;
				temp.run();
			}
		}
	}
	
	void notifyWhenGoal(Runnable runnable)
	{
		synchronized(lock)
		{
			System.out.println("Notify when " + goal + " " + results.size());
			if(results.size() >= goal)
			{
				runnable.run();
				return;
			}
			this.whenReachedGoal = runnable;
		}
	}
	
	void startSection(StartSection startSection)
	{
		sectionAdd(startSection);
	}
	
	void endSection()
	{
		System.out.println("End section add");
		sectionAdd(new EndSection());
	}
	
	private void sectionAdd(Object obj)
	{
		futureAdd();
		sectionEntries++;
		results.add(obj);
		System.out.println("Results: " + results);
	}
	
	/**
	 * Throws an {@link IllegalStateException} if {@link #hasNext()} returns false.
	 *
	 * @throws IllegalStateException if {@link #hasNext()} returns false
	 */
	private void ensureHasNext()
	{
		if(!hasNext())
		{
			throw new IllegalStateException("No more results available!");
		}
	}
	
	/**
	 * Casts the specified obj to T.
	 *
	 * @param obj to cast to T
	 * @param <T> type of result to return
	 * @return specified obj casted to T
	 */
	private <T> T cast(Object obj)
	{
		//if the types don't match a ClassCastException should be thrown to notify the user
		return (T) obj;
	}
	
	/**
	 * Returns {@link #pollFirst()}.
	 *
	 * @param <T> type of result to return
	 * @return {@link #pollFirst()}
	 */
	public <T> T poll()
	{
		return pollFirst();
	}
	
	/**
	 * Returns {@link #peekFirst()}.
	 *
	 * @param <T> type of result to return
	 * @return {@link #peekFirst()}
	 */
	public <T> T peek()
	{
		return peekFirst();
	}
	
	/**
	 * Returns the first element by polling an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return polled {@link Object} cast to T
	 * @throws ClassCastException if the polled object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	public <T> T pollFirst()
	{
		ensureHasNext();
		//make sure there is no section at the start
		Object object;
		while((object = results.peekFirst()) != null && isSection(object))
		{
			results.pollFirst();
			goal--;
		}
		
		T t = cast(results.pollFirst());
		collected++;
		System.out.println("Poll first");
		goal--;
		return t;
	}
	
	/**
	 * Returns the first element by peeking an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return peeked {@link Object} cast to T
	 * @throws ClassCastException if the peeked object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	public <T> T peekFirst()
	{
		System.out.println("peek first");
		ensureHasNext();
		Object first = results.peekLast();
		if(isSection(first))
		{
			Iterator<Object> iterator = results.iterator();
			return peekIterator(iterator);
		}
		
		return cast(first);
	}
	
	/**
	 * Returns the last element by polling an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return polled {@link Object} cast to T
	 * @throws ClassCastException if the polled object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	public <T> T pollLast()
	{
		ensureHasNext();
		//make sure there is no section at the end
		Object object;
		while((object = results.peekLast()) != null && isSection(object))
		{
			results.pollLast();
			goal--;
		}
		
		T t = cast(results.pollLast());
		collected++;
		System.out.println("Poll last");
		goal--;
		return t;
	}
	
	/**
	 * Returns the last element by peeking an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return peeked {@link Object} cast to T
	 * @throws ClassCastException if the peeked object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	public <T> T peekLast()
	{
		System.out.println("peek last");
		ensureHasNext();
		Object last = results.peekLast();
		if(isSection(last))
		{
			Iterator<Object> iterator = results.descendingIterator();
			return peekIterator(iterator);
		}
		
		return cast(last);
	}
	
	private <T> T peekIterator(Iterator<Object> iterator)
	{
		iterator.next();
		while(iterator.hasNext())
		{
			Object next = iterator.next();
			if(isSection(next))
			{
				continue;
			}
			return cast(next);
		}
		ensureHasNext();
		return null;
	}
	
	private boolean isSection(Object object)
	{
		return object instanceof StartSection || object instanceof EndSection;
	}
	
	public ReadResult section(String name)
	{
		System.out.println("Try section " + name);
		Iterator<Object> iterator = results.iterator();
		while(iterator.hasNext())
		{
			Object object = iterator.next();
			if(object instanceof StartSection)
			{
				if(((StartSection) object).getName().equals(name))
				{
					System.out.println("Found section with name " + name);
					//start section here
					Deque<Object> sectionResults = new ArrayDeque<>();
					iterator.remove();
					goal--;
					while(iterator.hasNext())
					{
						try
						{
							Object sectionResult = iterator.next();
							System.out.println("Found value " + sectionResult);
							if(sectionResult instanceof EndSection)
							{
								System.out.println("Section: " + sectionResults);
								return new ReadResult(socket, sectionResults);
							}
							sectionResults.offer(sectionResult);
							collected++;
						} finally
						{
							iterator.remove();
							goal--;
						}
					}
					throw new IllegalStateException("Found section with no end section");
				}
			}
		}
		throw new IllegalStateException("No section with the name: " + name);
	}
	
	/**
	 * Returns whether there is anymore data in the queue.
	 *
	 * @return whether there is anymore data in the queue
	 */
	public boolean hasNext()
	{
		return !results.isEmpty();
	}
	
	/**
	 * Returns how many times entries have been polled from the {@link Queue} has been called.
	 *
	 * @return how many times entries have been polled from the {@link Queue} has been called
	 */
	public int collected()
	{
		return collected;
	}
	
	/**
	 * Returns how much entries are available in this {@link Queue}.
	 *
	 * @return how much entries are available in this {@link Queue}
	 */
	public int available()
	{
		return results.size() - sectionEntries;
	}
	
	/**
	 * Returns the socket who obtained this {@link ReadResult}.
	 *
	 * @return the socket who obtained this {@link ReadResult}
	 */
	public Socket socket()
	{
		return socket;
	}
	
	@Override
	public String toString()
	{
		return results.toString();
	}
}