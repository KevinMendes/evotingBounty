/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenExpirationTimeValidationTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@InjectMocks
	AuthenticationTokenExpirationTimeValidation validation = new AuthenticationTokenExpirationTimeValidation();
	@Mock
	AuthenticationToken authenticationTokenMock;
	@Mock
	AuthenticationContentService authenticationContentServiceMock;
	@Mock
	private AuthenticationContent authenticationContentMock;

	@Test
	public void givenAuthenticationTokenWhenValidTimestampThenValidationSuccess() throws ResourceNotFoundException {
		final String tenantId = "1";
		final String electionEventId = "1";
		final String votingCardId = "1";

		final String now = String.valueOf(System.currentTimeMillis());
		when(authenticationTokenMock.getTimestamp()).thenReturn(now);
		when(authenticationContentServiceMock.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContentMock);
		when(authenticationContentMock.getTokenExpirationTime()).thenReturn(100000);

		assertTrue(validation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock).isResult());

	}

	@Test
	public void givenAuthenticationTokenWhenNegativeTimestampThenValidationFail() throws ResourceNotFoundException {
		final String tenantId = "1";
		final String electionEventId = "1";
		final String votingCardId = "1";

		when(authenticationTokenMock.getTimestamp()).thenReturn(String.valueOf(Long.MIN_VALUE));
		when(authenticationContentServiceMock.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContentMock);
		when(authenticationContentMock.getTokenExpirationTime()).thenReturn(1);
		expectedException.expect(AuthTokenValidationException.class);
		validation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock);
	}

	@Test
	public void givenAuthenticationTokenWhenNegativeDifferenceTimestampThenValidationFail() throws ResourceNotFoundException {
		final String tenantId = "1";
		final String electionEventId = "1";
		final String votingCardId = "1";

		when(authenticationTokenMock.getTimestamp()).thenReturn(String.valueOf(Long.MAX_VALUE));
		when(authenticationContentServiceMock.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContentMock);
		when(authenticationContentMock.getTokenExpirationTime()).thenReturn(100000);
		expectedException.expect(AuthTokenValidationException.class);
		validation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock);
	}

	@Test
	public void testAuthenticationTokenIsNotStillValid() throws ResourceNotFoundException {
		final String tenantId = "1";
		final String electionEventId = "1";
		final String votingCardId = "1";

		final String someTimeAgo = String.valueOf(System.currentTimeMillis() - 1000L);
		when(authenticationTokenMock.getTimestamp()).thenReturn(someTimeAgo);
		when(authenticationContentServiceMock.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContentMock);
		when(authenticationContentMock.getTokenExpirationTime()).thenReturn(0);
		expectedException.expect(AuthTokenValidationException.class);
		validation.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock);

	}

}
