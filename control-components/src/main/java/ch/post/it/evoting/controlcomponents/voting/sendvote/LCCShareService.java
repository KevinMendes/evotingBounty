/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.voting.sendvote.CreateLCCShareService.CreateLCCShareOutput;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.DecryptPCCService.DecryptPPCInput;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.CcrjReturnCodesKeys;
import ch.post.it.evoting.controlcomponents.CcrjReturnCodesKeysService;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVote;
import ch.post.it.evoting.domain.voting.sendvote.LongChoiceReturnCodesShare;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesShare;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCC;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCCPayload;

/**
 * Decrypts the partially decrypted encrypted Choice Return Codes and creates the CCR_j long Choice Return Code shares.
 */
@Service
public class LCCShareService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LCCShareService.class);

	private static final int PHI = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;

	private final DecryptPCCService decryptPCCService;
	private final CreateLCCShareService createLCCShareService;
	private final CcrjReturnCodesKeysService ccrjReturnCodesKeysService;
	private final EncryptedVerifiableVoteService encryptedVerifiableVoteService;

	@Value("${nodeID}")
	private int nodeId;

	public LCCShareService(
			final DecryptPCCService decryptPCCService,
			final CreateLCCShareService createLCCShareService,
			final CcrjReturnCodesKeysService ccrjReturnCodesKeysService,
			final EncryptedVerifiableVoteService encryptedVerifiableVoteService) {
		this.decryptPCCService = decryptPCCService;
		this.createLCCShareService = createLCCShareService;
		this.ccrjReturnCodesKeysService = ccrjReturnCodesKeysService;
		this.encryptedVerifiableVoteService = encryptedVerifiableVoteService;
	}

	/**
	 * Decrypts the partially decrypted encrypted Choice Return Codes with the DecryptPCC_j algorithm and computes the CCR_j long Choice Return Codes
	 * share with the CreateLCCShare_j algorithm.
	 *
	 * @param partiallyDecryptedEncryptedPCCPayloads the partially decrypted encrypted node contributions.
	 * @return the Long Choice Return Codes share as a {@link LongReturnCodesShare}.
	 */
	LongReturnCodesShare computeLCCShares(final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads) {
		final ContextIds contextIds = partiallyDecryptedEncryptedPCCPayloads.get(0).getPartiallyDecryptedEncryptedPCC().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();
		final String requestId = partiallyDecryptedEncryptedPCCPayloads.get(0).getRequestId();
		final GqGroup gqGroup = partiallyDecryptedEncryptedPCCPayloads.get(0).getEncryptionGroup();

		LOGGER.debug("Starting decryption of the partially decrypted encrypted Choice Return Codes. [contextIds: {}]", contextIds);

		// Decrypt.
		final ReturnCodesNodeContext context = new ReturnCodesNodeContext(nodeId, electionEventId, verificationCardSetId, gqGroup);
		final DecryptPPCInput decryptPPCInput = buildDecryptPPCInput(partiallyDecryptedEncryptedPCCPayloads, contextIds, gqGroup);

		final GroupVector<GqElement, GqGroup> decryptedPartialChoiceCodes = decryptPCCService.decryptPCC(context, decryptPPCInput);

		// Create LCC shares.
		final CcrjReturnCodesKeys ccrjReturnCodesKeys = ccrjReturnCodesKeysService.getCcrjReturnCodesKeys(electionEventId);
		final ZqElement ccrjReturnCodesGenerationSecretKey = ccrjReturnCodesKeys.getCcrjReturnCodesGenerationSecretKey();
		final CreateLCCShareOutput createLCCShareOutput = createLCCShareService.createLCCShare(decryptedPartialChoiceCodes,
				ccrjReturnCodesGenerationSecretKey, verificationCardId, context);

		return new LongChoiceReturnCodesShare(UUID.randomUUID(), electionEventId, verificationCardSetId, verificationCardId, requestId, nodeId,
				createLCCShareOutput.getLongChoiceReturnCodeShare(), createLCCShareOutput.getVoterChoiceReturnCodeGenerationPublicKey(),
				createLCCShareOutput.getExponentiationProof());
	}

	private DecryptPPCInput buildDecryptPPCInput(final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads,
			final ContextIds contextIds, final GqGroup gqGroup) {

		final String verificationCardId = contextIds.getVerificationCardId();

		final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC = partiallyDecryptedEncryptedPCCPayloads.stream()
				.map(PartiallyDecryptedEncryptedPCCPayload::getPartiallyDecryptedEncryptedPCC)
				.filter(pcc -> pcc.getNodeId() == nodeId)
				.findAny() // Uniqueness ensured by the combined payload.
				.orElseThrow(() -> new IllegalStateException(
						String.format("Missing node contribution. [contextIds: %s, nodeId: %d]", contextIds, nodeId)));
		final GroupVector<GqElement, GqGroup> exponentiatedGammas = partiallyDecryptedEncryptedPCC.getExponentiatedGammas();
		final List<PartiallyDecryptedEncryptedPCC> otherPartiallyDecryptedEncryptedPCC = partiallyDecryptedEncryptedPCCPayloads.stream()
				.map(PartiallyDecryptedEncryptedPCCPayload::getPartiallyDecryptedEncryptedPCC)
				.filter(pcc -> pcc.getNodeId() != nodeId)
				.sorted(Comparator.comparingInt(PartiallyDecryptedEncryptedPCC::getNodeId))
				.collect(Collectors.toList());
		final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherCcrExponentiatedGammas = otherPartiallyDecryptedEncryptedPCC.stream()
				.map(PartiallyDecryptedEncryptedPCC::getExponentiatedGammas)
				.collect(toGroupVector());
		final GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherCcrExponentiationProofs = otherPartiallyDecryptedEncryptedPCC.stream()
				.map(PartiallyDecryptedEncryptedPCC::getExponentiationProofs)
				.collect(toGroupVector());

		final EncryptedVerifiableVote encryptedVerifiableVote = encryptedVerifiableVoteService.getEncryptedVerifiableVote(verificationCardId);

		// Keys will be provided by sdm configuration in the future. For now, use dummy keys and skip proof verification in DecryptPCC_j.
		final ElGamalMultiRecipientPublicKey dummyOtherCcrEncryptionPublicKey = new ElGamalMultiRecipientPublicKey(
				Stream.generate(() -> GqElement.GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup))
						.limit(PHI)
						.collect(Collectors.toList()));

		return new DecryptPPCInput.Builder()
				.addVerificationCardId(verificationCardId)
				.addExponentiatedGammaElements(exponentiatedGammas)
				.addOtherCcrExponentiatedGammaElements(otherCcrExponentiatedGammas)
				.addOtherCcrExponentiationProofs(otherCcrExponentiationProofs)
				.addOtherCcrChoiceReturnCodesEncryptionKeys(GroupVector.from(Collections.nCopies(3, dummyOtherCcrEncryptionPublicKey)))
				.addEncryptedVote(encryptedVerifiableVote.getEncryptedVote())
				.addExponentiatedEncryptedVote(encryptedVerifiableVote.getExponentiatedEncryptedVote())
				.addEncryptedPartialChoiceReturnCodes(encryptedVerifiableVote.getEncryptedPartialChoiceReturnCodes())
				.build();
	}
}
