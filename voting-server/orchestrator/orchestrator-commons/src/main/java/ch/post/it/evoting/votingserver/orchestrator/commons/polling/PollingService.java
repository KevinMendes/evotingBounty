/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Uses a PollingResultsHandler {@link ResultsHandler} to repeatedly request a value to a key-value resource until it is returned, or a specified
 * timeout (in milliseconds) is reached. Optionally, a delay between requests (in milliseconds) can be defined.
 *
 * @param <U> Type of the value to be requested
 */
public interface PollingService<U> {

	/**
	 * Starts the polling of the resource to retrieve the desired value, returning it, or throwing a java.util.concurrent.TimeOutException if the
	 * value could not be retrieved before the specified timeout is reached.
	 *
	 * @param resultsId key to be used by the PollingResultsHandler to identify the required value in the resource
	 * @return The value retrieved from the resource
	 * @throws TimeoutException if the value could not be retrieved before the specified timeout
	 */
	U getResults(UUID resultsId) throws TimeoutException;
}
