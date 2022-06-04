/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.polling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests of {@link PollingServiceImpl}.
 */
public class PollingServiceImplTest {

	private static final UUID CORRELATION_ID = new UUID(0, 0);

	private static final Object RESULT = new Object();

	private ResultsHandler<Object> handler;

	private PollingServiceImpl<Object> service;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		handler = mock(ResultsHandler.class);
		service = new PollingServiceImpl<>(handler, Duration.ofSeconds(1), Duration.ofMillis(5));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetResults() throws TimeoutException {
		when(handler.handleResultsIfReady(CORRELATION_ID)).thenReturn(Optional.empty(), Optional.empty(), Optional.of(RESULT));
		assertEquals(RESULT, service.getResults(CORRELATION_ID));
	}

	@Test(expected = TimeoutException.class)
	public void testGetResultsTimeout() throws TimeoutException {
		when(handler.handleResultsIfReady(CORRELATION_ID)).thenReturn(Optional.empty());
		service.getResults(CORRELATION_ID);
	}

	@Test(expected = TimeoutException.class)
	public void testGetResultsInterrupted() throws TimeoutException {
		when(handler.handleResultsIfReady(CORRELATION_ID)).thenReturn(Optional.empty());
		Thread.currentThread().interrupt();
		try {
			service.getResults(CORRELATION_ID);
		} finally {
			assertTrue(Thread.interrupted());
		}
	}
}
