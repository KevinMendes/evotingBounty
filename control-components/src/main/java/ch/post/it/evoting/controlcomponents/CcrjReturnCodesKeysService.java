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
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

@Service
public class CcrjReturnCodesKeysService {

	private static final String GROUP = "group";

	private final ObjectMapper objectMapper;
	private final ElectionEventService electionEventService;
	private final CcrjReturnCodesKeysRepository ccrjReturnCodesKeysRepository;

	public CcrjReturnCodesKeysService(
			final ObjectMapper objectMapper,
			final ElectionEventService electionEventService,
			final CcrjReturnCodesKeysRepository ccrjReturnCodesKeysRepository) {
		this.objectMapper = objectMapper;
		this.electionEventService = electionEventService;
		this.ccrjReturnCodesKeysRepository = ccrjReturnCodesKeysRepository;
	}

	@Transactional
	public CcrjReturnCodesKeysEntity save(final CcrjReturnCodesKeys ccrjReturnCodesKeys) {
		checkNotNull(ccrjReturnCodesKeys);

		final String electionEventId = ccrjReturnCodesKeys.getElectionEventId();

		final ZqElement ccrjReturnCodesGenerationSecretKey = ccrjReturnCodesKeys.getCcrjReturnCodesGenerationSecretKey();
		final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair = ccrjReturnCodesKeys.getCcrjChoiceReturnCodesEncryptionKeyPair();

		final byte[] ccrjReturnCodesGenerationSecretKeyBytes;
		final byte[] ccrjChoiceReturnCodesEncryptionKeyPairBytes;
		try {
			ccrjReturnCodesGenerationSecretKeyBytes = objectMapper.writeValueAsBytes(ccrjReturnCodesGenerationSecretKey);
			ccrjChoiceReturnCodesEncryptionKeyPairBytes = objectMapper.writeValueAsBytes(ccrjChoiceReturnCodesEncryptionKeyPair);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(String.format("Failed to serialize ccrj Return Codes keys. [electionEventId: %s]", electionEventId), e);
		}

		final CcrjReturnCodesKeysEntity entityToSave = new CcrjReturnCodesKeysEntity(ccrjReturnCodesGenerationSecretKeyBytes,
				ccrjChoiceReturnCodesEncryptionKeyPairBytes);
		final ElectionEventEntity electionEventEntity = electionEventService.getElectionEventEntity(electionEventId);
		entityToSave.setElectionEventEntity(electionEventEntity);

		return ccrjReturnCodesKeysRepository.save(entityToSave);
	}

	@Transactional
	@Cacheable("ccrjReturnCodesKeys")
	public CcrjReturnCodesKeys getCcrjReturnCodesKeys(final String electionEventId) {
		validateUUID(electionEventId);

		final CcrjReturnCodesKeysEntity ccrjReturnCodesKeysEntity = ccrjReturnCodesKeysRepository.findByElectionEventId(electionEventId)
				.orElseThrow(() -> new IllegalStateException(
						String.format("The ccrj Return Codes Keys are missing. [electionEventId: %s]", electionEventId)));

		final GqGroup encryptionGroup = electionEventService.getEncryptionGroup(electionEventId);

		final ZqElement ccrjReturnCodesGenerationSecretKey;
		final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair;
		try {
			ccrjReturnCodesGenerationSecretKey = objectMapper.reader()
					.withAttribute(GROUP, encryptionGroup)
					.readValue(ccrjReturnCodesKeysEntity.getCcrjReturnCodesGenerationSecretKey(), ZqElement.class);
			ccrjChoiceReturnCodesEncryptionKeyPair = objectMapper.reader()
					.withAttribute(GROUP, encryptionGroup)
					.readValue(ccrjReturnCodesKeysEntity.getCcrjChoiceReturnCodesEncryptionKeyPair(), ElGamalMultiRecipientKeyPair.class);
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Failed to deserialize ccrj Return Codes keys. [electionEventId: %s]", electionEventId), e);
		}

		return new CcrjReturnCodesKeys(electionEventId, ccrjReturnCodesGenerationSecretKey, ccrjChoiceReturnCodesEncryptionKeyPair);
	}

}
