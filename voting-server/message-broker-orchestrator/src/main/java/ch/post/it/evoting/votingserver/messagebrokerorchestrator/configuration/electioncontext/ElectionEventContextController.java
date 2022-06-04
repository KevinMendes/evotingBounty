/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.configuration.electioncontext;

import static ch.post.it.evoting.domain.SharedQueue.ELECTION_CONTEXT_REQUEST_PATTERN;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.BroadcastProducerService;

@RestController
@RequestMapping("api/v1/configuration/electioncontext")
public class ElectionEventContextController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventContextController.class);

	private final ObjectMapper objectMapper;
	private final BroadcastProducerService broadcastProducerService;

	public ElectionEventContextController(
			final ObjectMapper objectMapper,
			final BroadcastProducerService broadcastProducerService) {
		this.broadcastProducerService = broadcastProducerService;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/electionevent/{electionEventId}")
	public List<ElectionContextResponsePayload> getElectionContextPayloads(
			@PathVariable
			final String electionEventId,
			@RequestBody
			final ElectionEventContextPayload electionEventContextPayload)
			throws ExecutionException, InterruptedException, TimeoutException {

		checkNotNull(electionEventId, "Election event Id must not be null");
		checkNotNull(electionEventContextPayload, "Election event context payload must not be null");
		final String correlationId = UUID.randomUUID().toString();

		LOGGER.info("Processing election event context. [electionEventId: {}, correlationId: {}]", electionEventId, correlationId);

		return broadcastProducerService.sendMessagesAwaitingNotification(electionEventId, Context.CONFIGURATION_ELECTION_CONTEXT,
				electionEventContextPayload, ELECTION_CONTEXT_REQUEST_PATTERN, this::deserializePayload);
	}

	private ElectionContextResponsePayload deserializePayload(final byte[] payload) {
		try {
			return objectMapper.readValue(payload, ElectionContextResponsePayload.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}