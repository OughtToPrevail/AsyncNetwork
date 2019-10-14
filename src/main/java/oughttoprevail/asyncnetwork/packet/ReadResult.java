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

import java.util.Queue;

import oughttoprevail.asyncnetwork.Socket;

/**
 * Implementation at {@link oughttoprevail.asyncnetwork.packet.ReadResultImpl}
 */
public interface ReadResult
{
	static void throwEnsureHasNext() throws IllegalStateException
	{
		throw new IllegalStateException("No more results available!");
	}
	
	/**
	 * Returns {@link #pollFirst()}.
	 *
	 * @param <T> type of result to return
	 * @return {@link #pollFirst()}
	 */
	<T> T poll();
	
	/**
	 * Returns {@link #peekFirst()}.
	 *
	 * @param <T> type of result to return
	 * @return {@link #peekFirst()}
	 */
	<T> T peek();
	
	/**
	 * Returns the first element by polling an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return polled {@link Object} cast to T
	 * @throws ClassCastException if the polled object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	<T> T pollFirst();
	
	/**
	 * Returns the first element by peeking an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return peeked {@link Object} cast to T
	 * @throws ClassCastException if the peeked object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	<T> T peekFirst();
	
	/**
	 * Returns the last element by polling an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return polled {@link Object} cast to T
	 * @throws ClassCastException if the polled object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	<T> T pollLast();
	
	/**
	 * Returns the last element by peeking an {@link Object} from the read {@link Queue}
	 * and casting it to T.
	 *
	 * @param <T> type of result to return
	 * @return peeked {@link Object} cast to T
	 * @throws ClassCastException if the peeked object isn't type T
	 * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
	 */
	<T> T peekLast();
	
	/**
	 * Returns whether there is anymore data in the queue.
	 *
	 * @return whether there is anymore data in the queue
	 */
	boolean hasNext();
	
	/**
	 * Returns how many times entries have been polled from the {@link Queue} has been called.
	 *
	 * @return how many times entries have been polled from the {@link Queue} has been called
	 */
	int collected();
	
	/**
	 * Returns how much entries are available in this {@link Queue}.
	 *
	 * @return how much entries are available in this {@link Queue}
	 */
	int available();
	
	/**
	 * Returns the socket who obtained this {@link ReadResult}.
	 *
	 * @return the socket who obtained this {@link ReadResult}
	 */
	Socket socket();
}