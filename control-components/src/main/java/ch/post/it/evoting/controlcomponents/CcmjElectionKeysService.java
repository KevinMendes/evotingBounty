/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@Service
public class CcmjElectionKeysService {

	private final ObjectMapper objectMapper;
	private final ElectionEventService electionEventService;
	private final CcmjElectionKeysRepository ccmjElectionKeysRepository;

	public CcmjElectionKeysService(
			final ObjectMapper objectMapper,
			final ElectionEventService electionEventService,
			final CcmjElectionKeysRepository ccmjElectionKeysRepository) {
		this.objectMapper = objectMapper;
		this.electionEventService = electionEventService;
		this.ccmjElectionKeysRepository = ccmjElectionKeysRepository;
	}

	@Transactional
	public CcmjElectionKeysEntity save(final String electionEventId, final ElGamalMultiRecipientKeyPair ccmjElectionKeyPair) {
		validateUUID(electionEventId);
		checkNotNull(ccmjElectionKeyPair);

		final byte[] ccmjElectionKeyPairBytes;
		try {
			ccmjElectionKeyPairBytes = objectMapper.writeValueAsBytes(ccmjElectionKeyPair);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(String.format("Failed to serialize ccmj election key pair. [electionEventId: %s]", electionEventId), e);
		}

		final CcmjElectionKeysEntity entityToSave = new CcmjElectionKeysEntity(ccmjElectionKeyPairBytes);
		final ElectionEventEntity electionEventEntity = electionEventService.getElectionEventEntity(electionEventId);
		entityToSave.setElectionEventEntity(electionEventEntity);

		return ccmjElectionKeysRepository.save(entityToSave);
	}

	@Transactional
	@Cacheable("ccmjElectionKeyPairs")
	public ElGamalMultiRecipientKeyPair getCcmjElectionKeyPair(final String electionEventId) {
		validateUUID(electionEventId);

		final CcmjElectionKeysEntity ccmjElectionKeysEntity = ccmjElectionKeysRepository.findByElectionEventId(electionEventId)
				.orElseThrow(() -> new IllegalStateException(
						String.format("The ccmj election key pair are missing. [electionEventId: %s]", electionEventId)));

		final GqGroup encryptionGroup = electionEventService.getEncryptionGroup(electionEventId);

		final ElGamalMultiRecipientKeyPair ccmjElectionKeyPair;
		try {
			ccmjElectionKeyPair = objectMapper.reader()
					.withAttribute("group", encryptionGroup)
					.readValue(ccmjElectionKeysEntity.getCcmjElectionKeyPair(), ElGamalMultiRecipientKeyPair.class);
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Failed to deserialize ccmj election key pair. [electionEventId: %s]", electionEventId), e);
		}

		return ccmjElectionKeyPair;
	}

}
