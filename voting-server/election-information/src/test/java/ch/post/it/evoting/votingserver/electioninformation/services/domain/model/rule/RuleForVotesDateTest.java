/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.ElectionValidationRequest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.election.ElectionService;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RuleForVotesDateTest {

	private static final String ballotBoxId = "73783638d2984205b821f38e82e265c5";
	private static final String votingCardId = "73553638d2984205b821f38e82e265c5";
	@InjectMocks
	private final ElectionDatesRule rule = new ElectionDatesRule();
	private final String authenticationToken = "{\"id\": \"lnniWSgf+XDd4dasaIX9rQ==\",\"voterInformation\": {\"electionEventId\": \"100\","
			+ "\"votingCardId\": \"100\",\"ballotId\": \"100\",\"verificationCardId\": \"100\",\"tenantId\":\"100\",\"ballotBoxId\": \"100\",\"votingCardSetId\": \"100\",\"credentialId\":\"100\","
			+ "\"verificationCardSetId\": \"100\"},\"timestamp\": \"1430759337499\",\"signature\": \"base64encodedSignature==\"}";
	@Mock
	private ElectionService electionService;
	private Vote vote;

	@Before
	public void setup() {
		vote = new Vote();
		vote.setBallotBoxId(ballotBoxId);
		vote.setAuthenticationToken(authenticationToken);
		vote.setVotingCardId(votingCardId);
	}

	@Test
	public void electionInDatesTest() throws ResourceNotFoundException {
		ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.SUCCESS);
		when(electionService.validateIfElectionIsOpen(any(ElectionValidationRequest.class))).thenReturn(validationError);
		assertEquals(rule.execute(vote).getValidationErrorType(), ValidationErrorType.SUCCESS);
	}

	@Test
	public void electionBeforeDateFrom() throws ResourceNotFoundException, GeneralCryptoLibException {
		ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.ELECTION_OVER_DATE);
		when(electionService.validateIfElectionIsOpen(any(ElectionValidationRequest.class))).thenReturn(validationError);
		assertEquals(rule.execute(vote).getValidationErrorType(), ValidationErrorType.ELECTION_OVER_DATE);
	}

	@Test
	public void electionAfterDateTo() throws ResourceNotFoundException {
		ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.ELECTION_OVER_DATE);
		when(electionService.validateIfElectionIsOpen(any(ElectionValidationRequest.class))).thenReturn(validationError);
		assertEquals(rule.execute(vote).getValidationErrorType(), ValidationErrorType.ELECTION_OVER_DATE);
	}

}
