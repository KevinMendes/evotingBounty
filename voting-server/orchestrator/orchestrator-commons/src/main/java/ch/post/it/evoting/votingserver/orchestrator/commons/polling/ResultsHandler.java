/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import java.util.Optional;
import java.util.UUID;

/**
 * To be used in combination with PollingService {@link PollingService}, a PollingResultsHandler interacts with the resource to be polled defining
 * when the results are ready to be fetched, and what actions to perform with them before being returned (if any).
 *
 * @param <T> Type of the value to be returned when ready
 */
public interface ResultsHandler<T> {

	/**
	 * Handles the results if they are ready.
	 *
	 * @param correlationId key to be used to identify the required value in the resource
	 * @return An optional with the handled results if they are ready or an empty optional otherwise
	 */
	Optional<T> handleResultsIfReady(UUID correlationId);
}
