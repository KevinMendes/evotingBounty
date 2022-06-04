/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.electioncontext;

import static ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap.RABBITMQ_EXCHANGE;
import static ch.post.it.evoting.domain.SharedQueue.ELECTION_CONTEXT_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.ELECTION_CONTEXT_RESPONSE_PATTERN;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap;
import ch.post.it.evoting.controlcomponents.ExactlyOnceProcessor;
import ch.post.it.evoting.controlcomponents.ExactlyOnceTask;
import ch.post.it.evoting.controlcomponents.Messages;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.InvalidPayloadSignatureException;
import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContext;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;

/**
 * Consumes the messages asking for the Election Event Context.
 */
@Service
public class ElectionContextProcessor {

	public static final Logger LOGGER = LoggerFactory.getLogger(ElectionContextProcessor.class);

	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final ElectionContextService electionContextService;
	private final ExactlyOnceProcessor exactlyOnceProcessor;

	private String responseQueue;

	@Value("${nodeID}")
	private int nodeId;

	public ElectionContextProcessor(final ObjectMapper objectMapper,
			final RabbitTemplate rabbitTemplate,
			final ElectionContextService electionContextService,
			final ExactlyOnceProcessor exactlyOnceProcessor) {

		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.electionContextService = electionContextService;
		this.exactlyOnceProcessor = exactlyOnceProcessor;
	}

	@PostConstruct
	public void initQueue() {
		responseQueue = String.format("%s%s", ELECTION_CONTEXT_RESPONSE_PATTERN, nodeId);
	}

	@RabbitListener(queues = ELECTION_CONTEXT_REQUEST_PATTERN + "${nodeID}", id = ControlComponentsApplicationBootstrap.CHOICE_CODES_CONTAINER_PREFIX
			+ ".electioncontext.request.queue", autoStartup = "false")
	public void onMessage(final Message message) throws IOException {
		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id must not be null.");

		final byte[] messageBytes = message.getBody();
		final ElectionEventContextPayload electionEventContextPayload = objectMapper.readValue(messageBytes, ElectionEventContextPayload.class);

		verifyPayloadSignature(electionEventContextPayload);

		final String contextId = electionEventContextPayload.getElectionEventContext().getElectionEventId();
		LOGGER.info("Received election event context request. [contextId: {}, correlationId: {}, nodeId: {}]", contextId, correlationId, nodeId);

		final ExactlyOnceTask exactlyOnceTask = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(Context.CONFIGURATION_ELECTION_CONTEXT.toString())
				.setTask(() -> createElectionContextResponse(electionEventContextPayload.getElectionEventContext()))
				.setRequestContent(messageBytes)
				.build();
		final byte[] payloadBytes = exactlyOnceProcessor.process(exactlyOnceTask);

		final Message responseMessage = Messages.createMessage(correlationId, payloadBytes);

		rabbitTemplate.send(RABBITMQ_EXCHANGE, responseQueue, responseMessage);
		LOGGER.info("Election event context response sent. [contextId: {}]", contextId);
	}

	private void verifyPayloadSignature(final ElectionEventContextPayload electionEventContextPayload) {
		final CryptoPrimitivesPayloadSignature signature = electionEventContextPayload.getSignature();
		if (signature == null) {
			final String contextId = electionEventContextPayload.getElectionEventContext().getElectionEventId();
			throw new InvalidPayloadSignatureException(ElectionEventContextPayload.class, String.format("[contextId: %s]", contextId));
		}
	}

	private byte[] createElectionContextResponse(final ElectionEventContext electionEventContext) {
		electionContextService.save(electionEventContext);

		final ElectionContextResponsePayload electionContextResponsePayload = new ElectionContextResponsePayload(nodeId,
				electionEventContext.getElectionEventId());

		try {
			return objectMapper.writeValueAsBytes(electionContextResponsePayload);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Could not serialize election event context response", e);
		}
	}

}
