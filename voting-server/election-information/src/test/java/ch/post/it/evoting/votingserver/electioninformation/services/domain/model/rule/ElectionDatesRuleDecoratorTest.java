/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ElectionDatesRuleDecoratorTest {

	private static final String ballotBoxId = "73783638d2984205b821f38e82e265c5";

	private static final String votingCardId = "73553638d2984205b821f38e82e265c5";

	private static final Long days_4 = 4L;

	private static final Long days_8 = 8L;

	private static LocalDateTime now;
	private final String authenticationToken = "{\"id\": \"lnniWSgf+XDd4dasaIX9rQ==\",\"voterInformation\": {\"electionEventId\": \"100\","
			+ "\"votingCardId\": \"100\",\"ballotId\": \"100\",\"verificationCardId\": \"100\",\"tenantId\":\"100\",\"ballotBoxId\": \"100\",\"votingCardSetId\": \"100\",\"credentialId\":\"100\","
			+ "\"verificationCardSetId\": \"100\"},\"timestamp\": \"1430759337499\",\"signature\": \"base64encodedSignature==\"}";
	@InjectMocks
	private final ElectionDatesRuleDecorator rule = new ElectionDatesRuleDecorator();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private Vote vote;
	private DateTimeFormatter formatter;
	@Mock
	private ElectionDatesRule electionDatesRule;

	@Before
	public void setup() {
		vote = new Vote();
		vote.setBallotBoxId(ballotBoxId);
		vote.setAuthenticationToken(authenticationToken);
		vote.setVotingCardId(votingCardId);

		formatter = DateTimeFormatter.ISO_DATE_TIME;

		now = LocalDateTime.now();
	}

	@Test
	public void electionInDatesTest() {
		ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.SUCCESS);
		when(electionDatesRule.execute(vote)).thenReturn(validationError);
		assertEquals(rule.execute(vote).getValidationErrorType(), ValidationErrorType.SUCCESS);
	}

	@Test
	public void electionBeforeDateFrom() {
		ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.ELECTION_OVER_DATE);
		when(electionDatesRule.execute(vote)).thenReturn(validationError);
		assertEquals(rule.execute(vote).getValidationErrorType(), ValidationErrorType.ELECTION_OVER_DATE);
	}

	@Test
	public void electionAfterDateTo() {
		ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.ELECTION_OVER_DATE);
		when(electionDatesRule.execute(vote)).thenReturn(validationError);
		assertEquals(rule.execute(vote).getValidationErrorType(), ValidationErrorType.ELECTION_OVER_DATE);
	}

}
