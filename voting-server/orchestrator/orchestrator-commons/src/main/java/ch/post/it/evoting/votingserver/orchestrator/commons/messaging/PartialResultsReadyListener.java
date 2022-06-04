/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.messaging;

import java.nio.ByteBuffer;
import java.util.UUID;

import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.ReactiveResultsHandler;

/**
 * Implementation of {@link MessageListener} which notifies a {@link ReactiveResultsHandler} instance about of the readynes of the results.
 */
public final class PartialResultsReadyListener implements MessageListener {

	private final ReactiveResultsHandler<?> handler;

	/**
	 * Constructor.
	 *
	 * @param handler
	 */
	public PartialResultsReadyListener(ReactiveResultsHandler<?> handler) {
		this.handler = handler;
	}

	@Override
	public void onMessage(Object message) {
		byte[] bytes = (byte[]) message;
		if (bytes.length != Long.BYTES * 2) {
			throw new IllegalArgumentException("Invalid UUID encoding.");
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		long mostSigBits = buffer.getLong();
		long leastSigBits = buffer.getLong();
		UUID correlationId = new UUID(mostSigBits, leastSigBits);
		handler.resultsReady(correlationId);
	}
}
