/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.sendvote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesShare;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator.AggregatorService;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.CommandFacade;

@Component
public class ReturnCodesLCCShareContributionsConsumer {

	private final CommandFacade commandFacade;
	private final ObjectMapper objectMapper;
	private final AggregatorService aggregatorService;

	public ReturnCodesLCCShareContributionsConsumer(final CommandFacade commandFacade,
			final ObjectMapper objectMapper,
			final AggregatorService aggregatorService) {
		this.commandFacade = commandFacade;
		this.objectMapper = objectMapper;
		this.aggregatorService = aggregatorService;
	}

	@RabbitListener(queues = "#{queueNameResolver.get(\"CREATE_LCC_SHARE_RESPONSE_PATTERN\")}")
	public void consumer(final Message message) throws IOException {

		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id should not be null");

		final byte[] encodedResponse = message.getBody();
		final LongReturnCodesSharePayload longReturnCodesSharePayload = objectMapper.readValue(encodedResponse,
				LongReturnCodesSharePayload.class);

		final LongReturnCodesShare longReturnCodesShare = longReturnCodesSharePayload.getLongReturnCodesShare();
		final String contextId = String.join("-",
				Arrays.asList(longReturnCodesShare.getElectionEventId(), longReturnCodesShare.getVerificationCardSetId(),
						longReturnCodesShare.getVerificationCardId()));
		final Integer nodeId = longReturnCodesShare.getNodeId();

		commandFacade.saveResponse(encodedResponse, correlationId, contextId, Context.VOTING_RETURN_CODES_CREATE_LCC_SHARE, nodeId);

		aggregatorService.notifyPartialResponseReceived(correlationId, contextId);
	}
}
