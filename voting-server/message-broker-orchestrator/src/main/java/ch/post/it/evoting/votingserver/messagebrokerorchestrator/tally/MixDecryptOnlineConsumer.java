/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.tally.MixDecryptOnlineResponsePayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.MessageBrokerOrchestratorApplication;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.CommandFacade;

@Controller
public class MixDecryptOnlineConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecryptOnlineConsumer.class);

	private final ObjectMapper objectMapper;
	private final CommandFacade commandFacade;

	private final MixDecryptOnlineProducer producer;
	private final MixnetPayloadService mixnetPayloadService;

	public MixDecryptOnlineConsumer(final ObjectMapper objectMapper,
			final CommandFacade commandFacade,
			final MixDecryptOnlineProducer producer,
			final MixnetPayloadService mixnetPayloadService) {
		this.objectMapper = objectMapper;
		this.commandFacade = commandFacade;

		this.producer = producer;
		this.mixnetPayloadService = mixnetPayloadService;
	}

	@RabbitListener(queues = "#{queueNameResolver.get(\"NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN\")}")
	public void consumer(final Message message) {
		try {
			final String correlationId = message.getMessageProperties().getCorrelationId();
			final byte[] byteContent = message.getBody();

			final MixDecryptOnlineResponsePayload payload = objectMapper.readValue(byteContent, MixDecryptOnlineResponsePayload.class);

			final String electionEventId = payload.getShufflePayload().getElectionEventId();
			final String ballotBoxId = payload.getShufflePayload().getBallotBoxId();
			final int nodeId = payload.getShufflePayload().getNodeId();

			if (nodeId == 1) {
				checkState(payload.getInitialPayload() != null, "InitialPayload is missing from node 1");
			}
			checkState(payload.getShufflePayload() != null, "ShufflePayload is expected");

			final String contextId = String.join("-", Arrays.asList(electionEventId, ballotBoxId));
			final Context context = Context.MIXING_TALLY_MIX_DEC_ONLINE;

			LOGGER.info("Received mix online response [nodeId:{}, electionEventId:{}, ballotBoxId:{}, correlationId:{}]", nodeId, electionEventId, ballotBoxId,
					correlationId);

			new TransactionalCommandRepositorySave().save(correlationId, contextId, context, nodeId, payload, byteContent);

			if (nodeId < MessageBrokerOrchestratorApplication.NODE_IDS.size()) {
				sendNextRequest(correlationId, electionEventId, ballotBoxId, nodeId);
			}

		} catch (final IOException ex) {
			throw new UncheckedIOException("Failed to process the mixing DTO", ex);
		}
	}

	private void sendNextRequest(String correlationId, String electionEventId, String ballotBoxId, int nodeId) {
		final int nextNodeId = nodeId + 1;
		final List<MixnetShufflePayload> shufflePayloads = mixnetPayloadService.getMixnetShufflePayloads(electionEventId, ballotBoxId);
		producer.send(electionEventId, ballotBoxId, correlationId, nextNodeId, shufflePayloads);
		LOGGER.info("Sent next request to node {} [electionEventId:{}, ballotBoxId:{}, correlationId:{}]", nextNodeId, electionEventId,
				ballotBoxId, correlationId);
	}

	private class TransactionalCommandRepositorySave {
		@Transactional(propagation = Propagation.REQUIRES_NEW)
		public void save(final String correlationId, final String contextId, final Context context, final int nodeId,
				final MixDecryptOnlineResponsePayload payload, final byte[] byteContent) {

			commandFacade.saveResponse(byteContent, correlationId, contextId, context, nodeId);

			if (nodeId == 1) {
				final MixnetInitialPayload initialPayload = payload.getInitialPayload();
				mixnetPayloadService.saveInitialPayload(initialPayload);
			}

			final MixnetShufflePayload mixnetShufflePayload = payload.getShufflePayload();

			mixnetPayloadService.saveShufflePayload(mixnetShufflePayload);
		}
	}
}
