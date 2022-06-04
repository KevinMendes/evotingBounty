/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.configuration.setupvoting;

import static ch.post.it.evoting.domain.SharedQueue.GEN_KEYS_CCR_REQUEST_PATTERN;
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
import ch.post.it.evoting.domain.configuration.ControlComponentKeyGenerationRequestPayload;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.BroadcastProducerService;

@RestController
@RequestMapping("/api/v1/configuration/setupvoting")
public class KeyGenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyGenerationController.class);

	private final ObjectMapper objectMapper;
	private final BroadcastProducerService broadcastProducerService;

	public KeyGenerationController(final ObjectMapper objectMapper, BroadcastProducerService broadcastProducerService) {
		this.broadcastProducerService = broadcastProducerService;
		this.objectMapper = objectMapper;
	}

	@PostMapping("/keygeneration/electionevent/{electionEventId}")
	public List<ControlComponentPublicKeysPayload> getKeyGenerations(
			@PathVariable
			final String electionEventId,
			@RequestBody
			final ControlComponentKeyGenerationRequestPayload controlComponentKeyGenerationRequestPayload)
			throws ExecutionException, InterruptedException, TimeoutException {

		checkNotNull(electionEventId, "Election event Id must not be null");
		checkNotNull(controlComponentKeyGenerationRequestPayload, "Key generation payload must not be null");
		final String correlationId = UUID.randomUUID().toString();

		LOGGER.info("Processing Key Generation calculation. [contextId: {}, correlationId: {}]", electionEventId, correlationId);

		return broadcastProducerService.sendMessagesAwaitingNotification(electionEventId, Context.CONFIGURATION_RETURN_CODES_GEN_KEYS_CCR,
				controlComponentKeyGenerationRequestPayload, GEN_KEYS_CCR_REQUEST_PATTERN, this::deserializePayload);
	}

	private ControlComponentPublicKeysPayload deserializePayload(final byte[] payload) {
		try {
			return objectMapper.readValue(payload, ControlComponentPublicKeysPayload.class);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
