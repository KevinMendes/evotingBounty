/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.electioncontext;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UncheckedIOException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.ElectionEventEntity;
import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.domain.configuration.ElectionEventContext;

@Service
public class ElectionContextService {

	private final ObjectMapper objectMapper;
	private final ElectionEventService electionEventService;
	private final ElectionContextRepository electionContextRepository;

	public ElectionContextService(final ObjectMapper objectMapper,
			final ElectionEventService electionEventService,
			final ElectionContextRepository electionContextRepository) {
		this.electionEventService = electionEventService;
		this.objectMapper = objectMapper;
		this.electionContextRepository = electionContextRepository;
	}

	@Transactional
	public void save(final ElectionEventContext electionEventContext) {
		checkNotNull(electionEventContext);

		byte[] serializedCombinedControlComponentPublicKeys;
		byte[] serializedElectoralBoardPublicKey;
		byte[] serializedElectionPublicKey;
		byte[] serializedChoiceReturnCodesPublicKey;
		try {
			serializedCombinedControlComponentPublicKeys = objectMapper.writeValueAsBytes(
					electionEventContext.getCombinedControlComponentPublicKeys());
			serializedElectoralBoardPublicKey = objectMapper.writeValueAsBytes(electionEventContext.getElectoralBoardPublicKey());
			serializedElectionPublicKey = objectMapper.writeValueAsBytes(electionEventContext.getElectionPublicKey());
			serializedChoiceReturnCodesPublicKey = objectMapper.writeValueAsBytes(electionEventContext.getChoiceReturnCodesEncryptionPublicKey());

		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException("Failed to serialize election event context.", e);
		}

		final ElectionEventEntity electionEventEntity = electionEventService.getElectionEventEntity(electionEventContext.getElectionEventId());

		final ElectionContextEntity electionContextEntity = new ElectionContextEntity.Builder()
				.setElectionEventEntity(electionEventEntity)
				.setCombinedControlComponentPublicKey(serializedCombinedControlComponentPublicKeys)
				.setElectoralBoardPublicKey(serializedElectoralBoardPublicKey)
				.setElectionPublicKey(serializedElectionPublicKey)
				.setChoiceReturnCodesEncryptionPublicKey(serializedChoiceReturnCodesPublicKey)
				.setStartTime(electionEventContext.getStartTime())
				.setFinishTime(electionEventContext.getFinishTime())
				.build();
		electionContextRepository.save(electionContextEntity);
	}

	@Transactional
	public ElectionContextEntity getElectionContextEntity(final String electionEventId) {
		validateUUID(electionEventId);

		final Optional<ElectionContextEntity> electionContextEntity = electionContextRepository.findByElectionEventId(electionEventId);

		return electionContextEntity.orElseThrow(
				() -> new IllegalStateException(String.format("Election context entity not found. [electionEventId: %s]", electionEventId)));
	}
}
