/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator.AggregatorService;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.CommandFacade;

@Component
public class KeyGenerationConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyGenerationConsumer.class);

	private final CommandFacade commandFacade;
	private final ObjectMapper objectMapper;
	private final AggregatorService aggregatorService;

	public KeyGenerationConsumer(CommandFacade commandFacade,
			final ObjectMapper objectMapper, final AggregatorService aggregatorService) {
		this.commandFacade = commandFacade;
		this.objectMapper = objectMapper;
		this.aggregatorService = aggregatorService;
	}

	@RabbitListener(queues = "#{queueNameResolver.get(\"GEN_KEYS_CCR_RESPONSE_PATTERN\")}")
	public void consumer(final Message message) throws IOException {

		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id should not be null");

		final byte[] encodedResponse = message.getBody();
		final ControlComponentPublicKeysPayload controlComponentPublicKeysPayload = objectMapper.readValue(encodedResponse, ControlComponentPublicKeysPayload.class);

		final int nodeId = controlComponentPublicKeysPayload.getControlComponentPublicKeys().getNodeId();

		final String contextId = controlComponentPublicKeysPayload.getElectionEventId();

		LOGGER.info("Received Key Generation calculation response [contextId: {}, correlationId: {}, nodeId: {}]", contextId, correlationId, nodeId);

		commandFacade.saveResponse(encodedResponse, correlationId, contextId, Context.CONFIGURATION_RETURN_CODES_GEN_KEYS_CCR, nodeId);

		aggregatorService.notifyPartialResponseReceived(correlationId, contextId);
	}
}
