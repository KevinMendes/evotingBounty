/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.utils;

import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;

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

	public static AuthenticationToken createAuthenticationToken() {

		final VoterInformation voterInformation = new VoterInformation();
		voterInformation.setElectionEventId(ELECTION_EVENT_ID);
		voterInformation.setTenantId(TENANT_ID);
		voterInformation.setBallotBoxId(BALLOT_BOX_ID);
		voterInformation.setBallotId(BALLOT_ID);
		voterInformation.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformation.setCredentialId(CREDENTIAL_ID);
		voterInformation.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);
		voterInformation.setVotingCardId(VOTING_CARD_ID);
		voterInformation.setVotingCardSetId(VOTING_CARD_SET_ID);
		final AuthenticationToken token = new AuthenticationToken();
		token.setVoterInformation(voterInformation);
		return token;

	}
}
