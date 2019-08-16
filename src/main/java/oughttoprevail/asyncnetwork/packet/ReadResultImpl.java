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

import java.util.Deque;
import java.util.Queue;

import oughttoprevail.asyncnetwork.Socket;

public class ReadResultImpl implements ReadResult
{
	private final Socket socket;
	private final Deque<Object> results;
	private final Object lock = new Object();
	private Runnable runnable;
	private int runnableRequestSize = -1;
	private int collected;
	
	public ReadResultImpl(Socket socket, Deque<Object> results)
	{
		this.results = results;
		this.socket = socket;
	}
	
	/**
	 * Adds the specified obj to the results queue.
	 * If after adding the specified obj the results queue size is equal
	 * to the size specified in the {@link ReadResultImpl} constructor
	 * then the runnable specified in the {@link ReadResultImpl} constructor
	 * is invoked.
	 *
	 * @param obj to add to the results queue
	 */
	void add(Object obj)
	{
		synchronized(lock)
		{
			results.add(obj);
			if(runnable != null && results.size() >= runnableRequestSize)
			{
				Runnable temp = runnable;
				runnableRequestSize = -1;
				runnable = null;
				temp.run();
			}
		}
	}
	
	void notifyWhen(int requestSize, Runnable runnable)
	{
		synchronized(lock)
		{
			if(results.size() >= requestSize)
			{
				runnable.run();
				return;
			}
			this.runnableRequestSize = requestSize;
			this.runnable = runnable;
		}
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
			ReadResult.throwEnsureHasNext();
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
	@Override
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
	@Override
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
	@Override
	public <T> T pollFirst()
	{
		ensureHasNext();
		
		T t = cast(results.pollFirst());
		collected++;
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
	@Override
	public <T> T peekFirst()
	{
		ensureHasNext();
		
		return cast(results.peekFirst());
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
	@Override
	public <T> T pollLast()
	{
		ensureHasNext();
		
		T t = cast(results.pollLast());
		collected++;
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
	@Override
	public <T> T peekLast()
	{
		ensureHasNext();
		
		return cast(results.peekLast());
	}
	
	/**
	 * Skips an index in the {@link Queue}.
	 *
	 * @return this
	 */
	@Override
	public ReadResult skip()
	{
		ensureHasNext();
		results.poll();
		collected++;
		return this;
	}
	
	/**
	 * Repeats {@link #skip()} specified n amount of times.
	 *
	 * @param n amount of times to repeat {@link #skip()}.
	 * @return this
	 */
	@Override
	public ReadResult skip(int n)
	{
		for(int i = 0; i < n; i++)
		{
			skip();
		}
		return this;
	}
	
	/**
	 * Returns whether there is anymore data in the queue.
	 *
	 * @return whether there is anymore data in the queue
	 */
	@Override
	public boolean hasNext()
	{
		return !results.isEmpty();
	}
	
	/**
	 * Returns how many times entries have been polled from the {@link Queue} has been called.
	 *
	 * @return how many times entries have been polled from the {@link Queue} has been called
	 */
	@Override
	public int collected()
	{
		return collected;
	}
	
	/**
	 * Returns how much entries are available in this {@link Queue}.
	 *
	 * @return how much entries are available in this {@link Queue}
	 */
	@Override
	public int available()
	{
		return results.size();
	}
	
	/**
	 * Returns the socket who obtained this {@link ReadResult}.
	 *
	 * @return the socket who obtained this {@link ReadResult}
	 */
	@Override
	public Socket socket()
	{
		return socket;
	}
}