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

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Copied from {@link java.util.concurrent.CountDownLatch}. Added method {@link
 * StatedCount#set(int)}.
 */
public class StatedCount
{
	private static final class Sync extends AbstractQueuedSynchronizer
	{
		private static final long serialVersionUID = 4982264981922014374L;
		
		protected int tryAcquireShared(int acquires)
		{
			return (getState() == 0) ? 1 : -1;
		}
		
		protected boolean tryReleaseShared(int releases)
		{
			// Decrement count; signal when transition to zero
			for(; ; )
			{
				int c = getState();
				if(c == 0)
					return false;
				int nextc = c - 1;
				if(compareAndSetState(c, nextc))
					return nextc == 0;
			}
		}
		
		private void set(int value)
		{
			setState(value);
		}
	}
	
	private final Sync sync;
	
	public StatedCount()
	{
		sync = new Sync();
	}
	
	/**
	 * Waits until the countDown has been set to 0
	 */
	public void await()
	{
		try
		{
			sync.acquireSharedInterruptibly(1);
		} catch(InterruptedException ignored)
		{
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Counts down.
	 */
	public void countDown()
	{
		sync.releaseShared(1);
	}
	
	/**
	 * Sets the value of the count down.
	 *
	 * @param value the value that will be set for the count down.
	 */
	public void set(int value)
	{
		sync.set(value);
	}
}