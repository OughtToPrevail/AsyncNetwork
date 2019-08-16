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

@FunctionalInterface
public interface TriConsumer<A, B, C>
{
	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param a the first input argument
	 * @param b the second input argument
	 * @param c the third input argument
	 */
	void accept(A a, B b, C c);
	
	/**
	 * Returns a composed {@code TriConsumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation.  If performing this operation throws an exception,
	 * the {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code TriConsumer} that performs in sequence this
	 * operation followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after)
	{
		Validator.requireNonNull(after);
		return (a, b, c) ->
		{
			accept(a, b, c);
			after.accept(a, b, c);
		};
	}
}