/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.service.RuleExecutor;
import ch.post.it.evoting.votingserver.commons.logging.service.VoteHashService;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Ballot;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotElection;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Election;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.VoteValidationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote.VoteValidationServiceImpl;

/**
 * Junit tests for the class {@link VoteValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class VoteValidationServiceTest {

	private final String electionEventIdMock = "1";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private Vote voteMock;

	@Mock
	private RuleExecutor<Vote> ruleExecutor;

	@InjectMocks
	@Spy
	private VoteValidationServiceImpl validationService;

	@Mock
	private VoteHashService voteHashService;

	@Mock
	private VoteValidationRepository voteValidationRepository;

	private String tenantIdMock = "123";
	private String ballotIdMock = "321";

	@Before
	public void setup() {
		final Ballot ballotMock = new Ballot();
		ballotMock.setId(100);
		ballotMock.setBallotId("100");
		ballotMock.setTenantId("100");
		final String jsonEscapedBallotMock = "{\"id\":\"100\", \"elections\":[{\"id\":\"100\", \"type\":\"election\"}]}";
		ballotMock.setJson(jsonEscapedBallotMock);

		final BallotElection ballotElection = new BallotElection();
		final HashSet<Election> electionsSet = new HashSet<>();
		final Election election = new Election();
		final String electionIdMock = "1";
		election.setId(electionIdMock);
		electionsSet.add(election);
		ballotElection.setContests(electionsSet);
	}

	@Test
	public void validateVoteWithVoteNullThrowApplicationException() throws ApplicationException, ResourceNotFoundException {
		voteMock = null;
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage(ApplicationExceptionMessages.EXCEPTION_MESSAGE_VOTE_IS_NULL);

		validationService.validate(voteMock, tenantIdMock, electionEventIdMock, ballotIdMock);
	}

	@Test
	public void validateVoteWithTenantIdNullThrowApplicationException() throws ApplicationException, ResourceNotFoundException {
		tenantIdMock = null;
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);

		validationService.validate(voteMock, tenantIdMock, electionEventIdMock, ballotIdMock);
	}

	@Test
	public void validateVoteWithElectionIdNullThrowApplicationException() throws ApplicationException, ResourceNotFoundException {
		ballotIdMock = null;
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage(ApplicationExceptionMessages.EXCEPTION_MESSAGE_BALLOT_ID_IS_NULL);

		validationService.validate(voteMock, tenantIdMock, electionEventIdMock, ballotIdMock);
	}

	@Test
	public void validateVoteThrowRightApplicationException() throws ApplicationException, ResourceNotFoundException {
		final String exceptionMessage = "Test message";
		when(ruleExecutor.execute(any(), eq(voteMock))).thenThrow(new ApplicationException(exceptionMessage));
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage(exceptionMessage);

		validationService.validate(voteMock, tenantIdMock, electionEventIdMock, ballotIdMock);
	}

	@Test
	public void validateVoteFailedRulesListEmpty() throws ApplicationException, ResourceNotFoundException {
		final ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.SUCCESS);
		when(ruleExecutor.execute(any(), any(Vote.class))).thenReturn(validationError);

		final ValidationResult result = validationService.validate(voteMock, tenantIdMock, electionEventIdMock, ballotIdMock);
		assertTrue(result.isResult());
	}

}
