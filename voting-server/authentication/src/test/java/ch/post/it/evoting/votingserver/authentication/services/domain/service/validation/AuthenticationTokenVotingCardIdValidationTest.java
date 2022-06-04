/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenVotingCardIdValidationTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	AuthenticationTokenVotingCardIdValidation authenticationTokenVotingCardIdValidation = new AuthenticationTokenVotingCardIdValidation();
	@Mock
	private AuthenticationToken authenticationTokenMock;
	@Mock
	private VoterInformation voterInformationMock;
	@Mock
	private AuthenticationContent authenticationContentMock;

	@Test
	public void givenAuthenticationTokenWhenVotingCardIdsAreEqualThenValidationSuccess() {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";
		when(authenticationTokenMock.getVoterInformation()).thenReturn(voterInformationMock);
		when(voterInformationMock.getVotingCardId()).thenReturn(votingCardId);

		ValidationResult result = authenticationTokenVotingCardIdValidation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock);

		assertTrue(result.isResult());
	}

	@Test
	public void givenAuthenticationTokenWhenVotingCardIdsAreNotEqualThenValidationFail() {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";
		when(authenticationTokenMock.getVoterInformation()).thenReturn(voterInformationMock);
		String tokenVotingCardId = "2";
		when(voterInformationMock.getVotingCardId()).thenReturn(tokenVotingCardId);

		expectedException.expect(AuthTokenValidationException.class);
		authenticationTokenVotingCardIdValidation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock);
	}
}
