/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.domain.SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.tally.MixDecryptOnlineRequestPayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.MessageBrokerOrchestratorApplication;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.CommandFacade;

@Controller
public class MixDecryptOnlineProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecryptOnlineProducer.class);

	private static final String RABBITMQ_EXCHANGE = "evoting-exchange";

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;
	private final CommandFacade commandFacade;

	public MixDecryptOnlineProducer(final ObjectMapper objectMapper, final RabbitTemplate rabbitTemplate,
			final CommandFacade commandFacade) {
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.commandFacade = commandFacade;
	}

	public void initialSend(final String electionEventId, final String ballotBoxId) {
		final String correlationId = UUID.randomUUID().toString();
		final int initialNodeId = 1;

		validateUUID(electionEventId); // Election event Id must not be null
		validateUUID(ballotBoxId); // Ballot Box Id should not be null

		send(electionEventId, ballotBoxId, correlationId, initialNodeId, Collections.emptyList());
	}

	public void send(final String electionEventId, final String ballotBoxId, final String correlationId, final int nodeId,
			final List<MixnetShufflePayload> shufflePayloads) {

		validateUUID(electionEventId); // Election event Id must not be null
		validateUUID(ballotBoxId); // Ballot Box Id should not be null
		checkNotNull(correlationId, "correlationId should not be null");
		checkNotNull(shufflePayloads, "shufflePayloads should not be null");
		checkArgument(MessageBrokerOrchestratorApplication.NODE_IDS.contains(nodeId), "nodeId is not valid");

		final String contextId = String.join("-", Arrays.asList(electionEventId, ballotBoxId));

		final MixDecryptOnlineRequestPayload mixDecryptOnlineRequestPayload =
				new MixDecryptOnlineRequestPayload(electionEventId, ballotBoxId, nodeId, shufflePayloads);

		final byte[] payload;
		try {
			payload = objectMapper.writeValueAsBytes(mixDecryptOnlineRequestPayload);

			commandFacade.saveRequest(payload, correlationId, contextId, Context.MIXING_TALLY_MIX_DEC_ONLINE, nodeId);
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to process the mixing DTO", e);
		}

		final MessageProperties messageProperties = new MessageProperties();
		messageProperties.setCorrelationId(correlationId);
		final Message message = new Message(payload, messageProperties);
		rabbitTemplate.send(RABBITMQ_EXCHANGE, NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN + nodeId, message);
		LOGGER.info("Sent request [node: {}, correlationId:{}]", nodeId, correlationId);
	}
}
