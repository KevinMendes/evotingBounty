/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import static java.text.MessageFormat.format;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Implementation of {@link PollingService}.
 *
 * @param <T> the type of returned result.
 */
public class PollingServiceImpl<T> implements PollingService<T> {

	private final ResultsHandler<T> handler;

	private final long timeout;

	private final long pause;

	/**
	 * Constructor.
	 *
	 * @param handler the result handler
	 * @param timeout the timeout for getting the results
	 * @param pause   the pause between two sunsequent polls
	 */
	public PollingServiceImpl(ResultsHandler<T> handler, Duration timeout, Duration pause) {
		this.handler = handler;
		this.timeout = timeout.toMillis();
		this.pause = pause.toMillis();
	}

	@Override
	public T getResults(UUID correlationId) throws TimeoutException {
		long start = System.currentTimeMillis();
		do {
			Optional<T> optional = handler.handleResultsIfReady(correlationId);
			if (optional.isPresent()) {
				return optional.get();
			}
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new TimeoutException(format("Failed to get result for correlation id {0}, thread is interrupted.", correlationId));
			}

		} while (System.currentTimeMillis() - start < timeout);
		throw new TimeoutException(format("Failed to get result for correlation id {0} within {1} milliseconds.", correlationId, timeout));
	}
}
