/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.Jdbc;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.ResultsHandler;

@ChoiceCodesGeneration
public class ChoiceCodesGenerationContributionsConsumer implements MessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceCodesGenerationContributionsConsumer.class);

	@Inject
	@Jdbc
	private PartialResultsRepository<byte[]> resultsRepository;

	@Inject
	private ResultsHandler<List<byte[]>> resultsHandler;

	@Inject
	private ComputedValuesService computedValuesService;

	@Inject
	private ObjectMapper objectMapper;

	@Override
	public void onMessage(final Object message) {
		final byte[] messageBytes = (byte[]) message;

		final ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload> result;
		try {
			result = objectMapper.readValue(messageBytes, new TypeReference<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>() {
			});
		} catch (IOException e) {
			LOGGER.error("Failed to deserialize received message into a ChoiceCodeGenerationDTO.");
			return;
		}

		LOGGER.info("OR - Message accepted in {}", this.getClass().getSimpleName());
		persistPartialResult(result);
	}

	private void persistPartialResult(ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload> choiceCodeGenerationDTO) {
		LOGGER.info("OR - Persisting partial result in {}", this.getClass().getSimpleName());

		UUID key = choiceCodeGenerationDTO.getCorrelationId();

		try {
			resultsRepository.save(key, objectMapper.writeValueAsString(choiceCodeGenerationDTO).getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to serialize choice codes generation DTO.", e);
		}

		Optional<List<byte[]>> results = resultsHandler.handleResultsIfReady(key);
		if (results.isPresent()) {
			LOGGER.info("OR - Persisting all computation results for electionEventId {} verificationCardSetId {} chunkId {}",
					choiceCodeGenerationDTO.getPayload().getElectionEventId(), choiceCodeGenerationDTO.getPayload().getVerificationCardSetId(),
					choiceCodeGenerationDTO.getPayload().getChunkId());

			ReturnCodeGenerationResponsePayload payload = choiceCodeGenerationDTO.getPayload();

			String tenantId = payload.getTenantId();
			String electionEventId = payload.getElectionEventId();
			String verificationCardSetId = payload.getVerificationCardSetId();
			int chunkId = payload.getChunkId();

			List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> resultMessages = new ArrayList<>();
			for (byte[] bytes : results.get()) {
				try {
					resultMessages
							.add(objectMapper.readValue(bytes, new TypeReference<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>() {
							}));
				} catch (IOException e) {
					throw new IllegalStateException("Failed to deserialize choice codes generation DTO.", e);
				}
			}

			try {
				computedValuesService.update(tenantId, electionEventId, verificationCardSetId, chunkId, resultMessages);
			} catch (EntryPersistenceException e) {
				LOGGER.error("OR - Failed to store results for tenantId {}, electionEventId {}, verificationCardSetId {} and chunkId {}", tenantId,
						electionEventId, verificationCardSetId, chunkId, e);
			}
		}
	}
}
