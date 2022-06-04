/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.voting.sendvote.PartialDecryptPCCService.PartialDecryptPCCInput;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.PartialDecryptPCCService.PartialDecryptPCCInputBuilder;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.PartialDecryptPCCService.PartialDecryptPCCOutput;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.VerifyBallotCCRService.VerifyBallotCCRInput;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.VerifyBallotCCRService.VerifyBallotCCRInputBuilder;

import java.math.BigInteger;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.VerificationCardService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVote;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCC;

/**
 * Verifies the encrypted vote's zero-knowledge proofs and partially decrypts the partial Choice Return Codes.
 */
@Service
public class PartialDecryptService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartialDecryptService.class);

	private final VerifyBallotCCRService verifyBallotCCRService;
	private final VerificationCardService verificationCardService;
	private final PartialDecryptPCCService partialDecryptPCCService;

	@Value("${nodeID}")
	private int nodeId;

	public PartialDecryptService(
			final VerifyBallotCCRService verifyBallotCCRService,
			final VerificationCardService verificationCardService,
			final PartialDecryptPCCService partialDecryptPCCService) {
		this.verifyBallotCCRService = verifyBallotCCRService;
		this.verificationCardService = verificationCardService;
		this.partialDecryptPCCService = partialDecryptPCCService;
	}

	/**
	 * Verifies the {@link EncryptedVerifiableVote} in the VerifyBallotCCR_j algorithm and then partially decrypts the encrypted partial Choice Return
	 * Codes with the CCR_j Choice Return Codes encryption secret key in the PartialDecryptPCC_j algorithm.
	 *
	 * @param encryptedVerifiableVote the object containing the encrypted vote and the corresponding zero-knowledge proofs.
	 * @return the partially decrypted encrypted Partial Choice Return Codes as a {@link PartiallyDecryptedEncryptedPCC}.
	 */
	PartiallyDecryptedEncryptedPCC performPartialDecrypt(final EncryptedVerifiableVote encryptedVerifiableVote) {

		final ContextIds contextIds = encryptedVerifiableVote.getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();
		final GqGroup gqGroup = encryptedVerifiableVote.getEncryptedVote().getGroup();

		LOGGER.debug("Starting partial decryption of partial Choice Return Codes. [contextIds: {}]", contextIds);

		// Verify the encrypted vote's zero-knowledge proofs.
		final ReturnCodesNodeContext context = new ReturnCodesNodeContext(nodeId, electionEventId, verificationCardSetId, gqGroup);

		final GqElement verificationCardPublicKey = verificationCardService.getVerificationCard(verificationCardId).getVerificationCardPublicKey()
				.get(0);

		final GqElement identityGqElement = GqElement.GqElementFactory.fromValue(BigInteger.ONE, gqGroup);

		final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey = Stream.generate(() -> identityGqElement)
				.limit(VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS)
				.collect(Collectors.collectingAndThen(Collectors.toList(), ElGamalMultiRecipientPublicKey::new));

		final ElGamalMultiRecipientPublicKey electionPublicKey = new ElGamalMultiRecipientPublicKey(Collections.singletonList(identityGqElement));

		final VerifyBallotCCRInput input = new VerifyBallotCCRInputBuilder()
				.setVerificationCardId(verificationCardId)
				.setEncryptedVote(encryptedVerifiableVote.getEncryptedVote())
				.setExponentiatedEncryptedVote(encryptedVerifiableVote.getExponentiatedEncryptedVote())
				.setEncryptedPartialChoiceReturnCodes(encryptedVerifiableVote.getEncryptedPartialChoiceReturnCodes())
				.setVerificationCardPublicKey(verificationCardPublicKey)
				.setElectionPublicKey(electionPublicKey)
				.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
				.setExponentiationProof(encryptedVerifiableVote.getExponentiationProof())
				.setPlaintextEqualityProof(encryptedVerifiableVote.getPlaintextEqualityProof())
				.build();

		if (!verifyBallotCCRService.verifyBallotCCR(context, input)) {
			LOGGER.error("The client's encrypted vote zero-knowledge proofs are invalid. [contextIds: {}]", contextIds);
			throw new IllegalArgumentException("The client's encrypted vote zero-knowledge proofs are invalid.");
		}

		LOGGER.debug("The client's encrypted vote zero-knowledge proofs are valid. [contextIds: {}]", contextIds);

		final ZqElement zeroZqElement = ZqElement.create(BigInteger.ZERO, ZqGroup.sameOrderAs(gqGroup));
		final ElGamalMultiRecipientPrivateKey ccrjChoiceReturnCodesEncryptionSecretKey = Stream.generate(() -> zeroZqElement)
				.limit(VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS)
				.collect(Collectors.collectingAndThen(Collectors.toList(), ElGamalMultiRecipientPrivateKey::new));

		// Perform partial decryption of the encrypted partial Choice Return codes.
		final PartialDecryptPCCInput partialDecryptPCCInput = new PartialDecryptPCCInputBuilder()
				.setVerificationCardId(verificationCardId)
				.setEncryptedVote(encryptedVerifiableVote.getEncryptedVote())
				.setExponentiatedEncryptedVote(encryptedVerifiableVote.getExponentiatedEncryptedVote())
				.setEncryptedPartialChoiceReturnCodes(encryptedVerifiableVote.getEncryptedPartialChoiceReturnCodes())
				.setCcrjChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
				.setCcrjChoiceReturnCodesEncryptionSecretKey(ccrjChoiceReturnCodesEncryptionSecretKey)
				.createPartialDecryptPCCInput();
		final PartialDecryptPCCOutput partialDecryptPCCOutput = partialDecryptPCCService.partialDecryptPCC(context, partialDecryptPCCInput);

		LOGGER.info("Successfully partially decrypted the encrypted partial Choice Return Codes. [contextIds: {}]", contextIds);

		final GroupVector<GqElement, GqGroup> exponentiatedGammas = partialDecryptPCCOutput.getExponentiatedGammas();
		final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs = partialDecryptPCCOutput.getExponentiationProofs();

		return new PartiallyDecryptedEncryptedPCC(contextIds, nodeId, exponentiatedGammas, exponentiationProofs);
	}

}
