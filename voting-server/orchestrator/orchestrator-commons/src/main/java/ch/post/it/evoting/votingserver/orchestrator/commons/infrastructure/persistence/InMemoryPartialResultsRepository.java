/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.enterprise.context.Dependent;

/**
 * Implementation of {@link PartialResultsRepository} which stores the partial results in memory.
 *
 * @param <T>
 */
@Singleton
@Dependent
@Local(PartialResultsRepository.class)
@InMemory
public final class InMemoryPartialResultsRepository<T> implements PartialResultsRepository<T> {
	private final ConcurrentMap<UUID, List<T>> resultsMap = new ConcurrentHashMap<>();

	@Override
	public void deleteAll(final UUID correlationId) {
		resultsMap.remove(correlationId);
	}

	@Override
	public List<T> listAll(final UUID correlationId) {
		final List<T> results = resultsMap.get(correlationId);
		if (results == null) {
			return new ArrayList<>();
		}
		synchronized (results) {
			return new ArrayList<>(results);
		}
	}

	@Override
	public Optional<List<T>> listIfHasAll(final UUID correlationId, final int count) {
		final List<T> results = resultsMap.get(correlationId);
		if (results == null) {
			return Optional.empty();
		}
		synchronized (results) {
			if (count != results.size()) {
				return Optional.empty();
			}
			return Optional.of(new ArrayList<>(results));
		}
	}

	@Override
	public void save(final UUID correlationId, final T result) {
		List<T> results = resultsMap.get(correlationId);
		if (results == null) {
			resultsMap.putIfAbsent(correlationId, new ArrayList<>());
			results = resultsMap.get(correlationId);
		}
		synchronized (results) {
			results.add(result);
		}
	}

	@Override
	public boolean hasAll(final UUID correlationId, final int count) {
		final List<T> results = resultsMap.get(correlationId);
		if (results == null) {
			return false;
		}
		synchronized (results) {
			return count == results.size();
		}
	}

	@Override
	public Optional<List<T>> deleteListIfHasAll(final UUID correlationId, final int count) {
		final Optional<List<T>> results = listIfHasAll(correlationId, count);
		if (results.isPresent()) {
			deleteAll(correlationId);
		}
		return results;
	}
}
