/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesService;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.cryptoadapters.CryptoAdapters;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Implements the ExtractCRC algorithm.
 */
public class ExtractCRCService {

	private final CorrectnessParser correctnessParser;
	private final ShortCodesService shortCodesService;
	private final VoterCodesService voterCodesService;

	@Inject
	public ExtractCRCService(final CorrectnessParser correctnessParser, final ShortCodesService shortCodesService,
			final VoterCodesService voterCodesService) {

		this.correctnessParser = correctnessParser;
		this.shortCodesService = shortCodesService;
		this.voterCodesService = voterCodesService;
	}

	/**
	 * Extracts the short Choice Return Codes CC<sub>id</sub> from the Return Codes Mapping table CMtable.
	 *
	 * @param longChoiceReturnCodeShares (lCC<sub>1,id</sub>, lCC<sub>2,id</sub>, lCC<sub>3,id</sub>, lCC<sub>4,id</sub>) ∈
	 *                                   (G<sub>q</sub><sup>&#x1D713;</sup>)<sup>4</sup>, CCR long Choice Return Codes shares.
	 * @param verificationCardId         vc<sub>id</sub>, the verification card id.
	 * @param vote                       the encrypted vote corresponding to the {@code verificationCardId}.
	 * @return the Short Choice Return Codes CC<sub>id</sub>.
	 * @throws ResourceNotFoundException       if the corresponding short Choice Return Codes cannot be found.
	 * @throws CryptographicOperationException if an error occurs during the short Choice Return Codes retrieval.
	 * @throws NullPointerException            if any input parameters is null.
	 * @throws IllegalArgumentException        if
	 *                                         <ul>
	 *                                             <li>The verification card id is invalid.</li>
	 *                                             <li>The verification card id does not correspond to the vote.</li>
	 *                                             <li>There are not exactly 4 long Choice Return Codes shares.</li>
	 *                                         </ul>
	 */
	@SuppressWarnings("java:S117")
	public List<String> extractCRC(final List<GroupVector<GqElement, GqGroup>> longChoiceReturnCodeShares, final String verificationCardId,
			final Vote vote) throws ResourceNotFoundException, CryptographicOperationException {

		checkNotNull(longChoiceReturnCodeShares);
		validateUUID(verificationCardId);
		checkNotNull(vote);
		checkArgument(verificationCardId.equals(vote.getVerificationCardId()));
		checkArgument(longChoiceReturnCodeShares.size() == 4, "There must be long Choice Return Code shares from 4 control-components.");

		final int psi = longChoiceReturnCodeShares.get(0).size();
		final GqGroup gqGroup = longChoiceReturnCodeShares.get(0).getGroup();
		final GqElement identity = GqElement.GqElementFactory.fromValue(BigInteger.ONE, gqGroup);
		final String ee = vote.getElectionEventId();
		final String vc_id = verificationCardId;

		final List<String> CC_id = new ArrayList<>();
		for (int i = 0; i < psi; i++) {
			final int final_i = i;
			final GqElement pC_id_i = longChoiceReturnCodeShares.stream()
					.map(lCC_j_id -> lCC_j_id.get(final_i))
					.reduce(identity, GqElement::multiply);

			final List<String> correctnessIds = correctnessParser.parse(vote.getCorrectnessIds()).get(i);

			final ZpGroupElement preChoiceReturnCode = CryptoAdapters.convert(pC_id_i);
			final byte[] longChoiceReturnCode = voterCodesService.generateLongReturnCode(vote.getElectionEventId(), vc_id, preChoiceReturnCode,
					correctnessIds);

			final List<String> shortChoiceReturnCodes = shortCodesService.retrieveShortCodes(vote.getTenantId(), ee, vc_id,
					Collections.singletonList(longChoiceReturnCode));

			CC_id.add(shortChoiceReturnCodes.get(0));
		}

		return CC_id;
	}

}
