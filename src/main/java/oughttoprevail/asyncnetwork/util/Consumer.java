package oughttoprevail.asyncnetwork.util;

import java.util.Objects;

/**
 * Copy of {@link java.util.function.Consumer}.
 * This is for Android since it doesn't support {@link java.util.function.Consumer} until API level 24+.
 */
@FunctionalInterface
public interface Consumer<T>
{
	
	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 */
	void accept(T t);
	
	/**
	 * Returns a composed {@code Consumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation.  If performing this operation throws an exception,
	 * the {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code Consumer} that performs in sequence this
	 * operation followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	default Consumer<T> andThen(Consumer<? super T> after)
	{
		Objects.requireNonNull(after);
		return (T t) ->
		{
			accept(t);
			after.accept(t);
		};
	}
}