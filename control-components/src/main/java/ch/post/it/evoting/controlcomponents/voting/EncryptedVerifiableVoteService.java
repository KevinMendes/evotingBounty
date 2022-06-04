/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.controlcomponents.VerificationCardEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardService;
import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVote;

@Service
public class EncryptedVerifiableVoteService {

	private final ObjectMapper objectMapper;
	private final ElectionEventService electionEventService;
	private final VerificationCardService verificationCardService;
	private final EncryptedVerifiableVoteRepository encryptedVerifiableVoteRepository;

	public EncryptedVerifiableVoteService(
			final ObjectMapper objectMapper,
			final ElectionEventService electionEventService,
			final VerificationCardService verificationCardService,
			final EncryptedVerifiableVoteRepository encryptedVerifiableVoteRepository) {
		this.objectMapper = objectMapper;
		this.electionEventService = electionEventService;
		this.verificationCardService = verificationCardService;
		this.encryptedVerifiableVoteRepository = encryptedVerifiableVoteRepository;
	}

	@Transactional
	public void save(final EncryptedVerifiableVote encryptedVerifiableVote) {
		checkNotNull(encryptedVerifiableVote);

		final ContextIds contextIds = encryptedVerifiableVote.getContextIds();

		byte[] serializedEncodeEncryptedVote;
		byte[] serializedExponentiatedEncryptedVote;
		byte[] serializedEncryptedPartialChoiceReturnCodes;
		byte[] serializedExponentiationProof;
		byte[] serializedPlaintextEqualityProof;
		byte[] serializedContextIds;
		try {
			serializedContextIds = objectMapper.writeValueAsBytes(contextIds);
			serializedEncodeEncryptedVote = objectMapper.writeValueAsBytes(encryptedVerifiableVote.getEncryptedVote());
			serializedExponentiatedEncryptedVote = objectMapper.writeValueAsBytes(encryptedVerifiableVote.getExponentiatedEncryptedVote());
			serializedEncryptedPartialChoiceReturnCodes = objectMapper.writeValueAsBytes(
					encryptedVerifiableVote.getEncryptedPartialChoiceReturnCodes());
			serializedExponentiationProof = objectMapper.writeValueAsBytes(encryptedVerifiableVote.getExponentiationProof());
			serializedPlaintextEqualityProof = objectMapper.writeValueAsBytes(encryptedVerifiableVote.getPlaintextEqualityProof());

		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException("Failed to serialize encrypted verifiable vote.", e);
		}

		final VerificationCardEntity verificationCardEntity = verificationCardService.getVerificationCardEntity(contextIds.getVerificationCardId());

		final EncryptedVerifiableVoteEntity encryptedVerifiableVoteEntity = new EncryptedVerifiableVoteEntity.Builder()
				.setContextIds(serializedContextIds)
				.setEncryptedVote(serializedEncodeEncryptedVote)
				.setExponentiatedEncryptedVote(serializedExponentiatedEncryptedVote)
				.setEncryptedPartialChoiceReturnCodes(serializedEncryptedPartialChoiceReturnCodes)
				.setExponentiationProof(serializedExponentiationProof)
				.setPlaintextEqualityProof(serializedPlaintextEqualityProof)
				.setVerificationCardEntity(verificationCardEntity)
				.build();
		encryptedVerifiableVoteRepository.save(encryptedVerifiableVoteEntity);
	}

	@Transactional
	public EncryptedVerifiableVote getEncryptedVerifiableVote(final String verificationCardId) {
		validateUUID(verificationCardId);

		final EncryptedVerifiableVoteEntity encryptedVerifiableVoteEntity = encryptedVerifiableVoteRepository.findByVerificationCardId(
						verificationCardId)
				.orElseThrow(() -> new IllegalStateException(
						String.format("Encrypted verifiable vote not found. [verificationCardId: %s]", verificationCardId)));

		final VerificationCardSetEntity verificationCardSetEntity = encryptedVerifiableVoteEntity.getVerificationCardEntity()
				.getVerificationCardSetEntity();
		final String electionEventId = verificationCardSetEntity.getElectionEventEntity().getElectionEventId();
		final String verificationCardSetId = verificationCardSetEntity.getVerificationCardSetId();

		final GqGroup encryptionGroup = electionEventService.getEncryptionGroup(electionEventId);
		final ObjectReader reader = objectMapper.reader().withAttribute("group", encryptionGroup);

		final ElGamalMultiRecipientCiphertext encryptedVote;
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;
		final ExponentiationProof exponentiationProof;
		final PlaintextEqualityProof plaintextEqualityProof;
		try {
			encryptedVote = reader.readValue(encryptedVerifiableVoteEntity.getEncryptedVote(), ElGamalMultiRecipientCiphertext.class);
			exponentiatedEncryptedVote = reader.readValue(encryptedVerifiableVoteEntity.getExponentiatedEncryptedVote(),
					ElGamalMultiRecipientCiphertext.class);
			encryptedPartialChoiceReturnCodes = reader.readValue(encryptedVerifiableVoteEntity.getEncryptedPartialChoiceReturnCodes(),
					ElGamalMultiRecipientCiphertext.class);
			exponentiationProof = reader.readValue(encryptedVerifiableVoteEntity.getExponentiationProof(), ExponentiationProof.class);
			plaintextEqualityProof = reader.readValue(encryptedVerifiableVoteEntity.getPlaintextEqualityProof(), PlaintextEqualityProof.class);
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to deserialize encrypted verifiable vote.", e);
		}

		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);

		return new EncryptedVerifiableVote(contextIds,
				encryptedVote,
				exponentiatedEncryptedVote,
				encryptedPartialChoiceReturnCodes,
				exponentiationProof,
				plaintextEqualityProof);
	}

}
