/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.InMemoryPartialResultsRepository;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;

/**
 * Tests of {@link ReactivePartialResultsHandlerImpl}.
 */
public class ReactivePartialResultsHandlerImplTest {
	private static final UUID CORRELATION_ID = new UUID(0, 0);

	private static final Duration TIMEOUT = Duration.ofMillis(100);

	private static final Object RESULT1 = new Object();

	private static final Object RESULT2 = new Object();

	private PartialResultsRepository<Object> repository;

	private ReactivePartialResultsHandlerImpl<Object> handler;

	@Before
	public void setUp() {
		repository = new InMemoryPartialResultsRepository<>();
		repository.save(CORRELATION_ID, RESULT1);
		repository.save(CORRELATION_ID, RESULT2);
		handler = new ReactivePartialResultsHandlerImpl<>(repository, TIMEOUT);
	}

	@Test
	public void testHandleResultsIfReady() {
		assertFalse("Results are not ready.", handler.handleResultsIfReady(CORRELATION_ID).isPresent());

		handler.resultsReady(CORRELATION_ID);
		List<Object> results = handler.handleResultsIfReady(CORRELATION_ID).get();
		assertEquals("Results size is correct.", 2, results.size());
		assertTrue("Result 1 is included.", results.contains(RESULT1));
		assertTrue("Result 2 is included.", results.contains(RESULT2));

		assertFalse("Results are already handled.", handler.handleResultsIfReady(CORRELATION_ID).isPresent());
		assertTrue("Results are deleted", repository.listAll(CORRELATION_ID).isEmpty());
	}

	@Test
	public void testStart() throws InterruptedException {
		handler.start();
		handler.resultsReady(CORRELATION_ID);
		Thread.sleep(TIMEOUT.toMillis() * 10);
		assertFalse(handler.handleResultsIfReady(CORRELATION_ID).isPresent());
	}

	@Test
	public void testStop() throws InterruptedException {
		handler.start();
		handler.stop();
		handler.resultsReady(CORRELATION_ID);
		Thread.sleep(TIMEOUT.toMillis());
		assertTrue(handler.handleResultsIfReady(CORRELATION_ID).isPresent());
	}

	@Test
	public void testRemoveExpiredResults() throws InterruptedException {
		handler.resultsReady(CORRELATION_ID);
		Thread.sleep(TIMEOUT.toMillis() * 2);
		handler.removeExpiredResults();
		assertFalse(handler.handleResultsIfReady(CORRELATION_ID).isPresent());
		assertTrue("Results are deleted", repository.listAll(CORRELATION_ID).isEmpty());
	}
}
