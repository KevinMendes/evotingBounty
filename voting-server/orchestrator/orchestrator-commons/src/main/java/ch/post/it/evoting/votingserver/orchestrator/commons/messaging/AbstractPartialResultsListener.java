/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.messaging;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectWriter;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectWriterImpl;
import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.Topic;
import ch.post.it.evoting.votingserver.orchestrator.commons.config.TopicsConfig;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;

/**
 * Basic abstract implementation of {@link MessageListener} which persists partial results.
 */
public abstract class AbstractPartialResultsListener implements MessageListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPartialResultsListener.class);

	private final StreamSerializableObjectWriter writer;

	private final PartialResultsRepository<byte[]> repository;

	private final MessagingService messagingService;

	private final Topic topic;

	protected AbstractPartialResultsListener(PartialResultsRepository<byte[]> repository, MessagingService messagingService) {
		this(new StreamSerializableObjectWriterImpl(), repository, messagingService, TopicsConfig.HA_TOPIC);
	}

	AbstractPartialResultsListener(StreamSerializableObjectWriter writer, PartialResultsRepository<byte[]> repository,
			MessagingService messagingService, Topic topic) {
		this.messagingService = messagingService;
		this.repository = repository;
		this.writer = writer;
		this.topic = topic;
	}

	@Override
	public final void onMessage(Object message) {
		UUID correlationId = getCorrelationId(message);
		StreamSerializable result = getPartialResult(message);

		LOGGER.info("OR - Message with correlation identifier {} is accepted in {}", correlationId, getClass().getSimpleName());

		persistPartialResult(correlationId, result);

		if (hasPartialResultsReady(correlationId)) {

			LOGGER.info("OR - All partial results with correlation identifier {} are ready in {}", correlationId, getClass().getSimpleName());

			sendPartialResultsReadyNotification(correlationId);

			LOGGER.info("OR - Correlation identifier {} is sent to {}.", correlationId, topic);
		}
	}

	/**
	 * Returns the correlation identifier of a given message.
	 *
	 * @param message the message
	 * @return the correlation identifier.
	 */
	protected abstract UUID getCorrelationId(Object message);

	/**
	 * Returns the partial result of a given message.
	 *
	 * @param message the message
	 * @return the partial result.
	 */
	protected abstract StreamSerializable getPartialResult(Object message);

	/**
	 * Returns the number of partial results to be received for a single correlation identifier.
	 *
	 * @return the number of partial results to be received for a single correlation identifier.
	 */
	protected abstract int partialResultCount();

	private boolean hasPartialResultsReady(UUID correlationId) {
		return repository.hasAll(correlationId, partialResultCount());
	}

	private void persistPartialResult(UUID correlationId, StreamSerializable result) {
		byte[] bytes;
		try {
			bytes = writer.write(result);
		} catch (IOException e) {
			throw new IllegalStateException(format("Failed to persist partial result for correlation identifier {0}", correlationId), e);
		}
		repository.save(correlationId, bytes);
	}

	private void sendPartialResultsReadyNotification(UUID correlationId) {
		ByteBuffer message = ByteBuffer.allocate(2 * Long.BYTES);
		message.putLong(correlationId.getMostSignificantBits());
		message.putLong(correlationId.getLeastSignificantBits());
		try {
			messagingService.send(topic, message.array());
		} catch (MessagingException e) {
			throw new IllegalStateException(
					format("Failed to send partial results ready notification for correlation identifier ''{0}''.", correlationId), e);
		}
	}
}
