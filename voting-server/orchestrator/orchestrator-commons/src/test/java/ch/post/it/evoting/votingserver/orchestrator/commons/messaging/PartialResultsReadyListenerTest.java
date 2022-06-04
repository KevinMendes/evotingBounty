/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Test;

import ch.post.it.evoting.votingserver.orchestrator.commons.polling.ReactiveResultsHandler;

/**
 * Tests of {@link PartialResultsReadyListener}.
 */
public class PartialResultsReadyListenerTest {

	@Test
	public void testOnMessage() {
		UUID correlationId = UUID.randomUUID();
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
		buffer.putLong(correlationId.getMostSignificantBits());
		buffer.putLong(correlationId.getLeastSignificantBits());

		@SuppressWarnings("unchecked")
		ReactiveResultsHandler<Object> handler = mock(ReactiveResultsHandler.class);
		PartialResultsReadyListener listener = new PartialResultsReadyListener(handler);

		listener.onMessage(buffer.array());

		verify(handler).resultsReady(correlationId);
	}
}
