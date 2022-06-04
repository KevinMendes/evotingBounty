/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.confirmvote;

import static ch.post.it.evoting.controlcomponents.voting.confirmvote.CreateLVCCShareService.CreateLVCCShareOutput;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.CcrjReturnCodesKeysService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.voting.confirmvote.ConfirmationKey;
import ch.post.it.evoting.domain.voting.sendvote.LongVoteCastReturnCodesShare;

@Service
public class LVCCShareService {

	private final CreateLVCCShareService createLVCCShareService;
	private final CcrjReturnCodesKeysService ccrjReturnCodesKeysService;

	@Value("${nodeID}")
	private int nodeId;

	public LVCCShareService(
			final CreateLVCCShareService createLVCCShareService,
			final CcrjReturnCodesKeysService ccrjReturnCodesKeysService) {
		this.createLVCCShareService = createLVCCShareService;
		this.ccrjReturnCodesKeysService = ccrjReturnCodesKeysService;
	}

	LongVoteCastReturnCodesShare computeLVCCShares(final ConfirmationKey confirmationKey, final String requestId) {

		final ContextIds contextIds = confirmationKey.getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		final ZqElement secretKey = ccrjReturnCodesKeysService.getCcrjReturnCodesKeys(electionEventId).getCcrjReturnCodesGenerationSecretKey();

		// Long Vote Cast Return Code share computation.
		final ReturnCodesNodeContext context = new ReturnCodesNodeContext(nodeId, electionEventId, verificationCardSetId, confirmationKey.getElement()
				.getGroup());
		final CreateLVCCShareOutput createLVCCShareOutput = createLVCCShareService.createLVCCShare(confirmationKey.getElement(), secretKey,
				verificationCardId, context);

		return new LongVoteCastReturnCodesShare(UUID.randomUUID(), electionEventId, verificationCardSetId, verificationCardId, requestId, nodeId,
				createLVCCShareOutput.getLongVoteCastReturnCodeShare(), createLVCCShareOutput.getVoterVoteCastReturnCodeGenerationPublicKey(),
				createLVCCShareOutput.getExponentiationProof());
	}

}
