/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.errormanagement;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the managed function until successful for a number of times, increasingly delaying retries.
 */
public class ExponentialBackoffExecutionPolicy implements ExecutionPolicy {
	private static final Logger logger = LoggerFactory.getLogger(ExponentialBackoffExecutionPolicy.class);

	private final int maximumRetries;

	private final long initialRetryWaitMillis;

	private final Set<Class<? extends Throwable>> fatalExceptionClasses;

	/**
	 * Creates a sender strategy that will execute an action until successful, or until the number of retries reaches the specified maximum. The
	 * retries are separated in time in an exponentially increasing fashion.
	 *
	 * @param maximumRetries         the number of times an action will be retried if unsuccessful
	 * @param initialRetryWaitMillis the number of milliseconds between the first and second executions
	 * @param fatalExceptionClasses  exception classes that cause the action to fail immediately with no retries
	 */
	public ExponentialBackoffExecutionPolicy(int maximumRetries, long initialRetryWaitMillis, Set<Class<? extends Throwable>> fatalExceptionClasses) {
		this.maximumRetries = maximumRetries;
		this.initialRetryWaitMillis = initialRetryWaitMillis;
		this.fatalExceptionClasses = fatalExceptionClasses;
	}

	/**
	 * Shortcut for no fatal exceptions.
	 */
	public ExponentialBackoffExecutionPolicy(int maximumRetries, long initialRetryWaitMillis) {
		this(maximumRetries, initialRetryWaitMillis, Collections.emptySet());
	}

	/**
	 * Execute the action, retrying it at exponentially increasing intervals if unsuccessful.
	 */
	@Override
	public <T> T execute(Callable<T> function) throws Throwable {
		boolean keepRetrying = true;
		int retriesLeft = maximumRetries;
		long delayMillis = initialRetryWaitMillis;
		T result = null;

		// Try to execute the function until successful, or until attempted enough times.
		do {
			try {
				result = function.call();
				// The action was executed successfully.
				keepRetrying = false;
			} catch (Throwable e) {
				// Actions causing any of these exceptions should not be re-tried.
				if (fatalExceptionClasses.contains(e.getClass())) {
					logger.error("Action could not be completed and should not be retried ({}).", e.getMessage());
					throw e;
				}

				// Decrease the number of retries left.
				retriesLeft--;

				// Something failed. Check whether more retries are available.
				if (retriesLeft < 1) {
					logger.error("Action could not be completed ({}). No more retries left.", e.getMessage(), e);
					// No more retries left: fail, reporting the latest exception.
					throw new OutOfRetriesException(e, maximumRetries);
				}

				// Inform about the situation.
				logger.warn("Action could not be completed ({}). {} retries left; retrying in {} milliseconds", e.getMessage(), retriesLeft,
						delayMillis);

				// Wait for the next retry.
				try {
					Thread.sleep(delayMillis);
					// Increase wait time exponentially.
					delayMillis *= 2;
				} catch (InterruptedException ie) {
					logger.warn("Cancelled while waiting for the next retry");
					throw ie;
				}
			}
		} while (keepRetrying);

		return result;
	}
}
