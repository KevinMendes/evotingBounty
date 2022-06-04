/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.errormanagement;

import java.util.concurrent.Callable;

/**
 * Provides a means to call a function and deal with the eventual errors it produces.
 * <p>
 * While this interface is usable as-is, it is meant to be extended, along with FallibleFunction, out to classes that restrict the exception to a more
 * concrete one, e.g.:
 *
 * <pre>
 * {@code
 * interface ConcreteErrorManagementPolicy {
 *     void call(ConcreteFallibleFunction function) throws ConcreteException;
 * }
 * }
 * </pre>
 */
public interface ExecutionPolicy {
	/**
	 * Manages possible errors when executing a function.
	 *
	 * @param function the function to call
	 * @param <T>      the return type of the function
	 * @return the result of executing the function
	 */
	<T> T execute(Callable<T> function) throws Throwable;
}
