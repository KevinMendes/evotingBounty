/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.configuration.electioncontext;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator.AggregatorService;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.CommandFacade;

@Component
public class ElectionEventContextConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventContextConsumer.class);

	private final CommandFacade commandFacade;
	private final ObjectMapper objectMapper;
	private final AggregatorService aggregatorService;

	public ElectionEventContextConsumer(final CommandFacade commandFacade, final ObjectMapper objectMapper,
			final AggregatorService aggregatorService) {
		this.commandFacade = commandFacade;
		this.objectMapper = objectMapper;
		this.aggregatorService = aggregatorService;
	}

	@RabbitListener(queues = "#{queueNameResolver.get(\"ELECTION_CONTEXT_RESPONSE_PATTERN\")}")
	public void consumer(final Message message) throws IOException {

		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id should not be null");

		final byte[] encodedResponse = message.getBody();
		final ElectionContextResponsePayload electionContextResponsePayload = objectMapper.readValue(encodedResponse,
				ElectionContextResponsePayload.class);
		final int nodeId = electionContextResponsePayload.getNodeId();
		final String electionEventId = electionContextResponsePayload.getElectionEventId();

		LOGGER.info("Received election context response [electionEventId: {}, correlationId: {}, nodeId: {}]", electionEventId, correlationId, nodeId);

		commandFacade.saveResponse(encodedResponse, correlationId, electionEventId, Context.CONFIGURATION_ELECTION_CONTEXT, nodeId);

		aggregatorService.notifyPartialResponseReceived(correlationId, electionEventId);
	}
}