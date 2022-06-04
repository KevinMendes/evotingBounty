/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;

/**
 * Implementation of {@link ReactiveResultsHandler}.
 */
public final class ReactivePartialResultsHandlerImpl<T> implements ReactiveResultsHandler<List<T>> {
	private final ConcurrentMap<UUID, Instant> readyFromMap = new ConcurrentHashMap<>();

	private final Timer timer = new Timer(false);

	private final PartialResultsRepository<T> repository;

	private final Duration timeout;

	/**
	 * Constructor.
	 *
	 * @param repository
	 * @param timeout
	 */
	public ReactivePartialResultsHandlerImpl(PartialResultsRepository<T> repository, Duration timeout) {
		this.repository = repository;
		this.timeout = timeout;
	}

	public Optional<List<T>> handleResultsIfReady(UUID correlationId) {
		if (readyFromMap.remove(correlationId) == null) {
			return Optional.empty();
		}
		Optional<List<T>> results = Optional.of(repository.listAll(correlationId));
		repository.deleteAll(correlationId);
		return results;
	}

	@Override
	public void resultsReady(UUID correlationId) {
		readyFromMap.putIfAbsent(correlationId, Instant.now());
	}

	/**
	 * Starts the handler.
	 */
	public void start() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				removeExpiredResults();
			}
		};
		long period = timeout.toMillis();
		timer.schedule(task, period, period);
	}

	/**
	 * Stops the handler.
	 */
	public void stop() {
		timer.cancel();
	}

	/**
	 * Removes the expired results. For internal use only.
	 */
	void removeExpiredResults() {
		Instant now = Instant.now();
		Collection<UUID> correlationIds = new LinkedList<>();
		for (Map.Entry<UUID, Instant> entry : readyFromMap.entrySet()) {
			Instant readyFrom = entry.getValue();
			if (readyFrom.isBefore(now.minus(timeout))) {
				correlationIds.add(entry.getKey());
			}
		}
		for (UUID correlationId : correlationIds) {
			if (readyFromMap.remove(correlationId) != null) {
				repository.deleteAll(correlationId);
			}
		}
	}
}
