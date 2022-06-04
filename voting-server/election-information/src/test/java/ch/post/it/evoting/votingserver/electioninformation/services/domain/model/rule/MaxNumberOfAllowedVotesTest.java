/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;

@RunWith(MockitoJUnitRunner.class)
public class MaxNumberOfAllowedVotesTest {

	@InjectMocks
	private final MaxNumberOfAllowedVotes rule = new MaxNumberOfAllowedVotes();

	@Mock
	private BallotBoxRepository ballotBoxRepository;

	private Vote vote;

	@Before
	public void setup() {

		final String tenantId = "100";
		final String electionEventId = "100";
		final String votingCardId = "100";
		final String ballotBoxId = "100";
		final String ballotId = "100";
		final String authenticationToken = "{\"id\": \"lnniWSgf+XDd4dasaIX9rQ==\",\"voterInformation\": {\"electionEventId\": \"100\","
				+ "\"votingCardId\": \"100\",\"ballotId\": \"100\",\"verificationCardId\": \"100\",\"tenantId\":\"100\",\"ballotBoxId\": \"100\",\"votingCardSetId\": \"100\",\"credentialId\":\"100\","
				+ "\"verificationCardSetId\": \"100\"},\"timestamp\": \"1430759337499\",\"signature\": \"base64encodedSignature==\"}";

		vote = new Vote();
		vote.setTenantId(tenantId);
		vote.setElectionEventId(electionEventId);
		vote.setVotingCardId(votingCardId);
		vote.setBallotBoxId(ballotBoxId);
		vote.setBallotId(ballotId);
		vote.setAuthenticationToken(authenticationToken);
		vote.setCipherTextExponentiations("cipher");
		vote.setCredentialId("100");
		vote.setEncryptedOptions("opts");
		vote.setEncryptedPartialChoiceCodes("partial");
	}

	@Test
	public void emptyStoredVotesInBallotBoxPerVotingCardId() {
		final List<BallotBox> ballotBoxList = new ArrayList<>();

		when(ballotBoxRepository
				.findByTenantIdElectionEventIdVotingCardIdBallotBoxIdBallotId(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxList);

		assertEquals(ValidationErrorType.SUCCESS, rule.execute(vote).getValidationErrorType());
	}

	@Test
	public void notEmptyStoredVotesInBallotBoxPerVotingCardId() {
		final List<BallotBox> ballotBoxList = new ArrayList<>();
		ballotBoxList.add(new BallotBox());

		when(ballotBoxRepository
				.findByTenantIdElectionEventIdVotingCardIdBallotBoxIdBallotId(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxList);

		assertEquals(ValidationErrorType.FAILED, rule.execute(vote).getValidationErrorType());
	}

	@Test
	public void getRuleName() {
		assertEquals(rule.getName(), RuleNames.VOTE_MAX_NUMBER_OF_ALLOWED_VOTES.getText());
	}
}
