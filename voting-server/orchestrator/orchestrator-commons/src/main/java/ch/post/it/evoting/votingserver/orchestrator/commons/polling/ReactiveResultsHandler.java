/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import java.util.UUID;

/**
 * <p>
 * Extension of {@link ResultsHandler} which allows external notification about of the readiness of the results.
 * <p>
 * Implementation must be thread-safe.
 */
public interface ReactiveResultsHandler<T> extends ResultsHandler<T> {
	/**
	 * Notifies the handler that the results with a given correlation identifier are ready for {@link #handleResultsIfReady(UUID)} method.
	 *
	 * @param correlationId the identifier.
	 */
	void resultsReady(UUID correlationId);
}
