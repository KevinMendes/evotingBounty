/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>
 * Partial results repository is responsible for storing the partial results of some asynchronous distributed computation.
 * <p>
 * Implementation must be thread-safe.
 *
 * @param <T> the type of partial result
 */
public interface PartialResultsRepository<T> {

	/**
	 * Deletes all partial results associated with a given correlation identifier.
	 *
	 * @param correlationId the identifier.
	 */
	void deleteAll(UUID correlationId);

	/**
	 * Returns whether the repository has all the partial results associated with a given correlation identifier.
	 *
	 * @param correlationId the identifier
	 * @param count         the required number of the partial results
	 * @return the repository has all the partial results.
	 */
	boolean hasAll(UUID correlationId, int count);

	/**
	 * Returns the list of all currently available partial results associated with a given correlation identifier.
	 *
	 * @param correlationId the correlation identifier
	 * @return the list of partial results
	 */
	List<T> listAll(UUID correlationId);

	/**
	 * Returns the list of partial results associated with a given correlation identifier if the repository has all of them.
	 *
	 * @param correlationId the identifier
	 * @param count         the required number of partial results to exist in the repository.
	 * @return the partial results or nothing.
	 */
	Optional<List<T>> listIfHasAll(UUID correlationId, int count);

	/**
	 * Saves a single partial result associated with given correlation identifier.
	 *
	 * @param correlationId the identifier
	 * @param result        the result
	 */
	void save(UUID correlationId, T result);

	/**
	 * Returns and deletes the list of partial results associated with a given correlation identifier if the repository has all of them.
	 *
	 * @param correlationId the identifier
	 * @param count         the required number of partial results to exist in the repository.
	 * @return the partial results or nothing.
	 */
	Optional<List<T>> deleteListIfHasAll(UUID correlationId, int count);
}
