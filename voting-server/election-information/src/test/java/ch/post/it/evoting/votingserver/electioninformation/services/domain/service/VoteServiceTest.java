/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote.VoteServiceImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence.BallotBoxFactory;

@RunWith(MockitoJUnitRunner.class)
public class VoteServiceTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@InjectMocks
	VoteServiceImpl voteService = new VoteServiceImpl();
	@Mock
	BallotBoxRepository ballotBoxRepositoryMock;
	@Mock
	BallotBoxFactory ballotBoxFactoryMock;
	@Mock
	BallotBox ballotBoxMock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(VoteServiceTest.class);
	}

	@Test
	public void retrieveVoteOk() throws ResourceNotFoundException, ApplicationException, IOException {
		when(ballotBoxRepositoryMock.findByTenantIdElectionEventIdVotingCardId(anyString(), anyString(), anyString())).thenReturn(ballotBoxMock);
		when(ballotBoxFactoryMock.to(ballotBoxMock)).thenReturn(new VoteAndComputeResults());

		VoteAndComputeResults results = voteService.retrieveVote("1", "1", "1");

		assertThat(results, notNullValue());
	}

}
