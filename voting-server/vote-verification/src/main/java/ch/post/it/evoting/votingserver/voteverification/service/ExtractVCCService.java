/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.cryptoadapters.CryptoAdapters;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Implements the ExtractVCC algorithm.
 */
public class ExtractVCCService {

	private final VoterCodesService voterCodesService;
	private final ShortCodesService shortCodesService;

	@Inject
	public ExtractVCCService(final VoterCodesService voterCodesService, final ShortCodesService shortCodesService) {
		this.voterCodesService = voterCodesService;
		this.shortCodesService = shortCodesService;
	}

	/**
	 * Extracts the short Vote Cast Return Code VCC_id from the Return Codes Mapping table CMtable.
	 *
	 * @param longVoteCastReturnCodeShares (lCC<sub>1,id</sub>, lCC<sub>2,id</sub>, lCC<sub>3,id</sub>, lCC<sub>4,id</sub>) ∈
	 *                                     G<sub>q</sub><sup>4</sup>, CCR long Vote Cast Return Code shares.
	 * @param verificationCardId           vc<sub>id</sub>, the verification card id.
	 * @param electionEventId              the corresponding election event id.
	 * @param tenantId                     the corresponding tenant id.
	 * @return the short Vote Cast Return Code VCC<sub>id</sub>.
	 * @throws ResourceNotFoundException       if the corresponding short Vote Cast Return Code cannot be found.
	 * @throws CryptographicOperationException if an error occurs during the short Vote Cast Return Code retrieval.
	 * @throws NullPointerException            if any input parameter is null.
	 * @throws IllegalArgumentException        if
	 *                                         <ul>
	 *                                             <li>Any id is invalid.</li>
	 *                                             <li>There are not exactly 4 long Vote Cast Return Code shares.</li>
	 *                                         </ul>
	 */
	@SuppressWarnings("java:S117")
	public String extractVCC(final List<GqElement> longVoteCastReturnCodeShares, final String verificationCardId, final String electionEventId,
			final String tenantId)
			throws ResourceNotFoundException, CryptographicOperationException {

		checkNotNull(longVoteCastReturnCodeShares);
		validateUUID(verificationCardId);
		validateUUID(electionEventId);
		checkNotNull(tenantId);
		checkArgument(longVoteCastReturnCodeShares.size() == 4, "There must be long Vote Cast Return Code shares from 4 control-components.");

		// Variables.
		final GqGroup gqGroup = longVoteCastReturnCodeShares.get(0).getGroup();
		final GqElement identity = GqElementFactory.fromValue(BigInteger.ONE, gqGroup);
		final String vc_id = verificationCardId;
		final String ee = electionEventId;

		// Operation.
		final GqElement pVCC_id = longVoteCastReturnCodeShares.stream()
				.reduce(identity, GqElement::multiply);

		final ZpGroupElement preVoteCastReturnCode = CryptoAdapters.convert(pVCC_id);
		final byte[] longVoteCastReturnCode = voterCodesService
				.generateLongReturnCode(ee, vc_id, preVoteCastReturnCode, Collections.emptyList());

		final List<String> shortVoteCastReturnCodes = shortCodesService
				.retrieveShortCodes(tenantId, ee, vc_id, Collections.singletonList(longVoteCastReturnCode));
		final String[] shortVoteCastReturnCode = shortVoteCastReturnCodes.get(0).split(";");

		return shortVoteCastReturnCode[0];
	}

}
