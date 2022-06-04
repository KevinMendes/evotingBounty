/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.returncodes.ComputeResults;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

@RunWith(MockitoJUnitRunner.class)
public class VoteCastCodeServiceTest {

	private final String TENANT_ID = "100";

	private final String ELECTION_EVENT_ID = "1";

	private final String VOTING_CARD_ID = "3cc7e2a0dd394fae8d9bd2ebd4fa4b95";

	private final String VERIFICATION_CARD_ID = "4dffac3879e443d3a3634929f6a2eb07";

	private final String VOTE_CAST_CODE = "919191";

	@Mock
	private VoteRepository voteRepository;

	@InjectMocks
	private VoteCastCodeService sut;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.initMocks(VoteCastCodeServiceTest.class);
	}

	@Test
	public void testGenerateVoteCastResultSuccess() throws ResourceNotFoundException, IOException {
		ShortVoteCastReturnCodeAndComputeResults vcMsg = new ShortVoteCastReturnCodeAndComputeResults();
		vcMsg.setShortVoteCastReturnCode(VOTE_CAST_CODE);

		VoteAndComputeResults voteAndResults = new VoteAndComputeResults();
		Vote vote = new Vote();
		vote.setAuthenticationToken(DomainObjectMapper.getNewInstance().writeValueAsString(new AuthenticationToken()));
		voteAndResults.setVote(vote);
		voteAndResults.setComputeResults(new ComputeResults());

		VoteCastResult voteCast = sut.generateVoteCastResult(ELECTION_EVENT_ID, VOTING_CARD_ID, VERIFICATION_CARD_ID, vcMsg);

		Assert.assertEquals(ELECTION_EVENT_ID, voteCast.getElectionEventId());
		Assert.assertEquals(VERIFICATION_CARD_ID, voteCast.getVerificationCardId());
		Assert.assertEquals(VOTE_CAST_CODE, voteCast.getVoteCastMessage().getVoteCastCode());
	}

	@Test
	public void testGenerateVoteCastNotFoundException() throws ResourceNotFoundException, IOException {
		ShortVoteCastReturnCodeAndComputeResults vcMsg = new ShortVoteCastReturnCodeAndComputeResults();
		vcMsg.setShortVoteCastReturnCode(VOTE_CAST_CODE);

		VoteCastResult voteCast = sut.generateVoteCastResult(ELECTION_EVENT_ID, VOTING_CARD_ID, VERIFICATION_CARD_ID, vcMsg);

		Assert.assertEquals(ELECTION_EVENT_ID, voteCast.getElectionEventId());
		Assert.assertEquals(VERIFICATION_CARD_ID, voteCast.getVerificationCardId());
		Assert.assertEquals(VOTE_CAST_CODE, voteCast.getVoteCastMessage().getVoteCastCode());
	}

}
