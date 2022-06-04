/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.confirmvote;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

@Service
public class VerifyLVCCHashService {

	private final int nodeId;
	private final HashService hashService;
	private final VerificationCardSetService verificationCardSetService;
	private final VerificationCardStateService verificationCardStateService;

	public VerifyLVCCHashService(
			@Value("${nodeID}")
			final int nodeId,
			final HashService hashService,
			final VerificationCardSetService verificationCardSetService,
			final VerificationCardStateService verificationCardStateService) {
		this.nodeId = nodeId;
		this.hashService = hashService;
		this.verificationCardSetService = verificationCardSetService;
		this.verificationCardStateService = verificationCardStateService;
	}

	@SuppressWarnings("java:S117")
	public boolean verifyLVCCHash(final String hashedLongVoteCastReturnCode, final List<String> otherCCRsHashedLongVoteCastReturnCodes,
			final String verificationCardId, final ReturnCodesNodeContext context) {
		checkNotNull(hashedLongVoteCastReturnCode);
		checkNotNull(otherCCRsHashedLongVoteCastReturnCodes);
		checkNotNull(verificationCardId);
		checkNotNull(context);

		final String ee = context.getElectionEventId();
		final String vcs = context.getVerificationCardSetId();
		final String vc_id = verificationCardId;
		checkArgument(otherCCRsHashedLongVoteCastReturnCodes.size() == 3, "There must be exactly 3 other CCRs hashed long vote cast return codes.");
		final List<String> hlVCC = createHashedLongVoteCastReturnCodesList(hashedLongVoteCastReturnCode, otherCCRsHashedLongVoteCastReturnCodes);
		final List<String> L_lVCC = verificationCardSetService.getVerificationCardSet(vcs).getAllowList();

		checkArgument(verificationCardStateService.isLCCShareCreated(vc_id),
				String.format("The CCR_j did not compute the long Choice Return Code shares for verification card %s.", verificationCardId));
		checkArgument(verificationCardStateService.isNotConfirmed(vc_id),
				String.format("The CCR_j did already confirm the long Choice Return Code shares for verification card %s.", verificationCardId));

		final List<HashableString> i_aux_list = Stream.of("VerifyLVCCHash", ee, vcs, vc_id)
				.map(HashableString::from)
				.collect(ImmutableList.toImmutableList());
		final HashableList i_aux = HashableList.from(i_aux_list);
		final HashableString hlVCC_id_1 = HashableString.from(hlVCC.get(0));
		final HashableString hlVCC_id_2 = HashableString.from(hlVCC.get(1));
		final HashableString hlVCC_id_3 = HashableString.from(hlVCC.get(2));
		final HashableString hlVCC_id_4 = HashableString.from(hlVCC.get(3));
		final String hhlVCC_id = Base64.getEncoder().encodeToString(hashService.recursiveHash(i_aux, hlVCC_id_1, hlVCC_id_2, hlVCC_id_3, hlVCC_id_4));
		if (L_lVCC.contains(hhlVCC_id)) {
			verificationCardStateService.setConfirmed(vc_id);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Creates a list containing all hashed Long Vote Cast Return Codes.
	 *
	 * @param hashedLongVoteCastReturnCode           the hashed long vote cast return code to be added to the list.
	 * @param otherCCRsHashedLongVoteCastReturnCodes the list of the other CRRs' hashed long vote cast return codes.
	 * @return a list containing all hashed long vote cast return codes ordered by their node IDs.
	 */
	private List<String> createHashedLongVoteCastReturnCodesList(final String hashedLongVoteCastReturnCode,
			final List<String> otherCCRsHashedLongVoteCastReturnCodes) {
		final List<String> hlvccList = new ArrayList<>();
		final int nodeIdIndex = nodeId - 1;
		for (int i = 0; i < otherCCRsHashedLongVoteCastReturnCodes.size() + 1; i++) {
			if (i < nodeIdIndex) {
				hlvccList.add(otherCCRsHashedLongVoteCastReturnCodes.get(i));
			} else if (i == nodeIdIndex) {
				hlvccList.add(hashedLongVoteCastReturnCode);
			} else {
				hlvccList.add(otherCCRsHashedLongVoteCastReturnCodes.get(i - 1));
			}
		}
		return hlvccList;
	}

}
