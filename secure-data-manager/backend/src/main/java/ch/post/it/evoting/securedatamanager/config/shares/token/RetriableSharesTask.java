/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iaik.pkcs.pkcs11.TokenException;

/**
 * A task that returns a &lt;T&gt; and may throw a {@link SharesRuntimeException}. This task tries the action N times, with a delay of M milliseconds
 * between them. It will also consume 1 retry on {@link TokenException}, and fail fast and exit completely on any other {@link Exception}
 * <p>
 * Most pkcs11wrapper operations may fail due to a variable and unknown set of reasons, by throwing {@link TokenException} plus an error code. The
 * retry schema leaves room for the underlying hardware to fix itself. It would be difficult and incomplete to try to deal with all of the different
 * cases and on the other hand we cannot assume any condition that arises is going to auto fix.
 *
 * @param <T> the type this task will return on success.
 */
public class RetriableSharesTask<T> implements Callable<T> {

	public static final int DEFAULT_NUMBER_OF_RETRIES = 5;
	public static final long DEFAULT_WAIT_TIME = 1000;
	private static final Logger L = LoggerFactory.getLogger(RetriableSharesTask.class);
	private final Callable<T> task;
	// total number of tries
	private final int numberOfRetries;
	// wait interval
	private final long timeToWait;
	// number left
	private int numberOfTriesLeft;

	/**
	 * Create a retriable task dealing with token operations. Will retry {@link #DEFAULT_NUMBER_OF_RETRIES} times, waiting {@link #DEFAULT_WAIT_TIME}
	 * ms between them.
	 *
	 * @param task The task to make retriable.
	 */
	public RetriableSharesTask(final Callable<T> task) {
		this(DEFAULT_NUMBER_OF_RETRIES, DEFAULT_WAIT_TIME, task);
	}

	/**
	 * Create a retriable task dealing with token operations. Will retry numberOfRetries times, waiting timeToWait m. between them.
	 *
	 * @param numberOfRetries The number of time to retry the task.
	 * @param timeToWait      The milliseconds to wait between retries.
	 * @param task            The task to make retriable.
	 */
	public RetriableSharesTask(final int numberOfRetries, final long timeToWait, final Callable<T> task) {
		this.numberOfRetries = numberOfRetries;
		numberOfTriesLeft = numberOfRetries;
		this.timeToWait = timeToWait;
		this.task = task;
	}

	@Override
	public T call() {
		while (true) {
			try {
				L.debug("Calling task {}. Try number {}, max {}", task, numberOfRetries - numberOfTriesLeft + 1, numberOfRetries);
				final T t = task.call();
				L.debug("Finished successfully with result {}", t);
				return t;
			} catch (final TokenException te) {
				numberOfTriesLeft--;
				if (numberOfTriesLeft == 0) {
					throw new SharesRuntimeException(numberOfRetries + " attempts to retry failed at " + timeToWait + "ms interval", te);
				}
				try {
					L.debug("Waiting {} ms before retrying", timeToWait);
					Thread.sleep(timeToWait);
				} catch (final InterruptedException ie) {
					// Restore interrupted state...
					Thread.currentThread().interrupt();
					L.info("Interrupted", ie);
				}
			} catch (final Exception e) {
				throw new SharesRuntimeException(e);
			}
		}
	}

}
