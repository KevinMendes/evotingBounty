/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateEntity;
import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@Service
public class VerificationCardService {

	private final ObjectMapper objectMapper;
	private final ElectionEventService electionEventService;
	private final VerificationCardSetService verificationCardSetService;
	private final VerificationCardRepository verificationCardRepository;

	public VerificationCardService(
			final ObjectMapper objectMapper,
			final ElectionEventService electionEventService,
			final VerificationCardSetService verificationCardSetService,
			final VerificationCardRepository verificationCardRepository) {
		this.objectMapper = objectMapper;
		this.electionEventService = electionEventService;
		this.verificationCardSetService = verificationCardSetService;
		this.verificationCardRepository = verificationCardRepository;
	}

	@Transactional
	public VerificationCardEntity save(final VerificationCard verificationCard) {
		checkNotNull(verificationCard);

		final VerificationCardEntity verificationCardEntity = verificationCardToEntity(verificationCard);

		return verificationCardRepository.save(verificationCardEntity);
	}

	@Transactional
	public void saveAll(final List<VerificationCard> verificationCards) {
		checkNotNull(verificationCards);
		checkArgument(verificationCards.stream().allMatch(Objects::nonNull));

		final List<VerificationCardEntity> verificationCardEntities = verificationCards.stream()
				.map(this::verificationCardToEntity)
				.collect(Collectors.toList());

		verificationCardRepository.saveAll(verificationCardEntities);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public boolean exist(final List<String> verificationCardIds) {
		verificationCardIds.forEach(UUIDValidations::validateUUID);

		return verificationCardRepository.existsAllByVerificationCardIdIn(new HashSet<>(verificationCardIds));
	}

	@Transactional
	public VerificationCard getVerificationCard(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardEntity verificationCardEntity = verificationCardRepository.findByVerificationCardId(verificationCardId)
				.orElseThrow(
						() -> new IllegalStateException(String.format("Verification card not found. [verificationCardId: %s]", verificationCardId)));

		final String electionEventId = verificationCardEntity.getVerificationCardSetEntity().getElectionEventEntity().getElectionEventId();
		final GqGroup encryptionGroup = electionEventService.getEncryptionGroup(electionEventId);

		final ElGamalMultiRecipientPublicKey verificationCardPublicKey;
		try {
			verificationCardPublicKey = objectMapper.reader()
					.withAttribute("group", encryptionGroup)
					.readValue(verificationCardEntity.getVerificationCardPublicKey(), ElGamalMultiRecipientPublicKey.class);
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Failed to deserialize verification card. [verificationCardId: %s]", verificationCardId), e);
		}

		final String verificationCardSetId = verificationCardEntity.getVerificationCardSetEntity().getVerificationCardSetId();

		return new VerificationCard(verificationCardEntity.getVerificationCardId(), verificationCardSetId, verificationCardPublicKey);
	}

	@Transactional
	public VerificationCardEntity getVerificationCardEntity(final String verificationCardId) {
		validateUUID(verificationCardId);

		return verificationCardRepository.findByVerificationCardId(verificationCardId)
				.orElseThrow(() -> new IllegalStateException("No corresponding verificationCard found. [verificationCardId: %s]"));
	}

	private VerificationCardEntity verificationCardToEntity(final VerificationCard verificationCard) {
		final String verificationCardId = verificationCard.getVerificationCardId();
		final String verificationCardSetId = verificationCard.getVerificationCardSetId();

		final byte[] publicKeyBytes;
		try {
			publicKeyBytes = objectMapper.writeValueAsBytes(verificationCard.getVerificationCardPublicKey());
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(
					String.format("Failed to serialize verification card public key. [verificationCardId: %s]", verificationCardId), e);
		}

		// Retrieve verification card set associated to this verification card.
		final VerificationCardSetEntity verificationCardSetEntity = verificationCardSetService.getVerificationCardSet(verificationCardSetId);

		final VerificationCardEntity verificationCardEntity = new VerificationCardEntity(verificationCardId, verificationCardSetEntity,
				publicKeyBytes);
		final VerificationCardStateEntity verificationCardStateEntity = new VerificationCardStateEntity();

		// One to one bidirectional mapping.
		verificationCardStateEntity.setVerificationCardEntity(verificationCardEntity);
		verificationCardEntity.setVerificationCardStateEntity(verificationCardStateEntity);

		return verificationCardEntity;
	}

}
