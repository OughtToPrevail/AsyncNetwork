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

public interface ThreadCreator
{
	/**
	 * Creates a new thread with the specified name and runnable then starts it.
	 *
	 * @param threadName the thread name that will be used when calling {@link Thread#Thread(Runnable, String)}
	 * @param runnable the runnable that will be used when calling {@link Thread#Thread(Runnable, String)}
	 */
	static void newThread(String threadName, Runnable runnable)
	{
		new Thread(runnable, threadName).start();
	}
}