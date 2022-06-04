/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;

public class BeanUtils {

	public static final String TENANT_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String BALLOT_BOX_ID = "100";

	public static final String BALLOT_ID = "100";

	public static final String VERIFICATION_CARD_ID = "100";

	public static final String CREDENTIAL_ID = "100";

	public static final String VERIFICATION_CARD_SET_ID = "100";

	public static final String VOTING_CARD_ID = "100";

	public static final String VOTING_CARD_SET_ID = "100";

	public static final String ID = "id";

	public static final String SIGNATURE = Base64.getEncoder().encodeToString("signature".getBytes(StandardCharsets.UTF_8));

	public static AuthenticationToken createAuthenticationToken() {

		VoterInformation voterInformation = new VoterInformation();
		voterInformation.setElectionEventId(ELECTION_EVENT_ID);
		voterInformation.setTenantId(TENANT_ID);
		voterInformation.setBallotBoxId(BALLOT_BOX_ID);
		voterInformation.setBallotId(BALLOT_ID);
		voterInformation.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformation.setCredentialId(CREDENTIAL_ID);
		voterInformation.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);
		voterInformation.setVotingCardId(VOTING_CARD_ID);
		voterInformation.setVotingCardSetId(VOTING_CARD_SET_ID);

		return new AuthenticationToken(voterInformation, ID, "", SIGNATURE);
	}

}
