/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;

/**
 * Basic abstract implementation of {@link ResultsHandler}.
 *
 * @param <T> the type of the result.
 */
public abstract class AbstractPartialResultsHandler<T> implements ResultsHandler<List<T>> {

	private final PartialResultsRepository<T> repository;

	/**
	 * Constructor.
	 *
	 * @param repository
	 */
	public AbstractPartialResultsHandler(PartialResultsRepository<T> repository) {
		this.repository = repository;
	}

	@Override
	public final Optional<List<T>> handleResultsIfReady(UUID correlationId) {
		int count = getPartialResultsCount();

		Optional<List<T>> results = repository.deleteListIfHasAll(correlationId, count);

		return results;
	}

	protected abstract int getPartialResultsCount();
}
