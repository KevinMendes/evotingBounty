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
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenElectionEventIdValidationTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	AuthenticationTokenElectionEventIdValidation authenticationTokenElectionEventIdValidation = new AuthenticationTokenElectionEventIdValidation();
	@Mock
	private AuthenticationToken authenticationTokenMock;

	@Mock
	private VoterInformation voterInformationMock;

	@Mock
	private AuthenticationContent authenticationContentMock;

	@Test
	public void givenAuthenticationTokenWhenElectionEventIdsAreEqualThenValidationSuccess() {
		final String tenantId = "1";
		final String electionEventId = "1";
		final String votingCardId = "1";
		when(authenticationTokenMock.getVoterInformation()).thenReturn(voterInformationMock);
		when(voterInformationMock.getElectionEventId()).thenReturn(electionEventId);

		assertTrue(authenticationTokenElectionEventIdValidation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock).isResult());

	}

	@Test
	public void givenAuthenticationTokenWhenElectionEventIdsAreNotEqualThenValidationFail() {
		final String tenantId = "1";
		final String electionEventId = "1";
		final String votingCardId = "1";
		when(authenticationTokenMock.getVoterInformation()).thenReturn(voterInformationMock);
		final String tokenElectionEventId = "2";
		when(voterInformationMock.getElectionEventId()).thenReturn(tokenElectionEventId);
		expectedException.expect(AuthTokenValidationException.class);

		authenticationTokenElectionEventIdValidation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock);

	}
}
