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

import oughttoprevail.asyncnetwork.impl.util.StatedCount;

/**
 * Implementation at {@link oughttoprevail.asyncnetwork.impl.util.selector.flags.SelectorFlagsImpl}.
 */
public interface SelectorFlags
{
	/**
	 * Does an AND operation and returns whether the result isn't 0.
	 *
	 * @param flags the flags to check in the AND operation
	 * @param i the integer that the flags will be checked with in the AND operation
	 * @return whether the AND operation returned 0
	 */
	static boolean ANDOperator(int flags, int i)
	{
		return (flags & i) != 0;
	}
	
	/**
	 * If the specified count isn't null then you switch a thread and handle correctly the specified flag
	 * while using the specified index as a {@link oughttoprevail.asyncnetwork.IServerClient}.
	 *
	 * @param index the index of the channel who got selected or -5 if it was an accept call.
	 * @param flags the flags of the select call.
	 * @param count the count of how many calls have finished, if null it means that this is the only call for this select.
	 */
	void call(int index, int flags, StatedCount count);
}