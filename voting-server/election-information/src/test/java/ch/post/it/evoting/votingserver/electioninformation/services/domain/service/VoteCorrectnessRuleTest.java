/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Ballot;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule.VoteCorrectnessRule;

/**
 * Test class for VoteCorrectness
 */
@RunWith(MockitoJUnitRunner.class)
public class VoteCorrectnessRuleTest {

	@InjectMocks
	@Spy
	private final VoteCorrectnessRule voteCorrectnessRule = new VoteCorrectnessRule();

	@Mock
	BallotRepository ballotRepository;

	@Mock
	Ballot ballot1;

	@Mock
	Vote vote;

	@Before
	public void initTest() {
		MockitoAnnotations.initMocks(VoteCorrectnessRuleTest.class);
	}

	@Test
	public void correctnessTrue() throws URISyntaxException, IOException, ResourceNotFoundException {

		Path ballotPath = getCurrentPath(Paths.get("l_c_ballot_lc.json"));
		String ballotString = readFile(ballotPath);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(any(), any(), any())).thenReturn(ballot1);
		when(ballot1.getJson()).thenReturn(ballotString);
		when(vote.getCorrectnessIds()).thenReturn("[[\"26d0d77f147c4620b9264977feb868f7\"]]");

		ValidationError resultExpected = new ValidationError(ValidationErrorType.SUCCESS);
		assertEquals(resultExpected.getValidationErrorType(), voteCorrectnessRule.execute(vote).getValidationErrorType());
	}

	@Test
	public void clauseMINFailed() throws URISyntaxException, IOException, ResourceNotFoundException {
		Path ballotPath = getCurrentPath(Paths.get("l_c_ballot_lc.json"));
		String ballotString = readFile(ballotPath);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(any(), any(), any())).thenReturn(ballot1);
		when(ballot1.getJson()).thenReturn(ballotString);
		when(vote.getCorrectnessIds()).thenReturn("[[\"cf2a9da6b424478fa5e535b0174c5e26\"]]");
		ValidationError resultExpected = new ValidationError(ValidationErrorType.INVALID_VOTE_CORRECTNESS);
		assertEquals(voteCorrectnessRule.execute(vote).getValidationErrorType(), resultExpected.getValidationErrorType());

	}

	@Test
	public void clauseMAXFailed() throws URISyntaxException, IOException, ResourceNotFoundException {
		Path ballotPath = getCurrentPath(Paths.get("l_c_ballot_lc.json"));
		String ballotString = readFile(ballotPath);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(any(), any(), any())).thenReturn(ballot1);
		when(ballot1.getJson()).thenReturn(ballotString);
		when(vote.getCorrectnessIds())
				.thenReturn("[[\"26d0d77f147c4620b9264977feb868f7\",\"26d0d77f147c4620b9264977feb868f7\"],[\"26d0d77f147c4620b9264977feb868f7\"]]");
		ValidationError resultExpected = new ValidationError(ValidationErrorType.INVALID_VOTE_CORRECTNESS);
		assertEquals(voteCorrectnessRule.execute(vote).getValidationErrorType(), resultExpected.getValidationErrorType());

	}

	private Path getCurrentPath(final Path path) throws URISyntaxException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL resource = classLoader.getResource(path.toString());
		return Paths.get(resource.toURI());

	}

	private String readFile(final Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

}
