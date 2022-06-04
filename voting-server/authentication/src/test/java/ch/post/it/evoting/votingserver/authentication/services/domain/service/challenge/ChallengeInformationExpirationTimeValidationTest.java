/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ServerChallengeMessage;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeInformationExpirationTimeValidationTest {

	public static final long A_LONG_TIME_AGO_IN_MILLISECONDS = 1000000L;
	@InjectMocks
	ChallengeInformationExpirationTimeValidation validation = new ChallengeInformationExpirationTimeValidation();

	@Mock
	ChallengeInformation challengeInformationMock;

	@Mock
	AuthenticationContentService authenticationContentServiceMock;

	@Mock
	AuthenticationContent authenticationContentMock;

	@Mock
	ServerChallengeMessage serverChallengeMessageMock;

	@Mock
	Logger logger;

	@Before
	public void setup() {
		when(challengeInformationMock.getServerChallengeMessage()).thenReturn(serverChallengeMessageMock);
	}

	@Test
	public void givenChallengeInformationWhenValidTimestampThenValidationSuccess() throws ResourceNotFoundException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(challengeInformationMock.getServerChallengeMessage().getTimestamp()).thenReturn(String.valueOf(System.currentTimeMillis()));
		when(authenticationContentServiceMock.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContentMock);
		when(authenticationContentMock.getChallengeExpirationTime()).thenReturn(111110);

		assertTrue(validation.execute(tenantId, electionEventId, votingCardId, challengeInformationMock));
	}

	@Test
	public void givenChallengeInformationWhenNegativeTimestampThenValidationFail() throws ResourceNotFoundException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(challengeInformationMock.getServerChallengeMessage().getTimestamp()).thenReturn(String.valueOf(Long.MIN_VALUE));

		assertFalse(validation.execute(tenantId, electionEventId, votingCardId, challengeInformationMock));
	}

	@Test
	public void givenChallengeInformationWhenNegativeDifferenceTimestampThenValidationFail() throws ResourceNotFoundException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(challengeInformationMock.getServerChallengeMessage().getTimestamp()).thenReturn(String.valueOf(Long.MAX_VALUE));

		assertFalse(validation.execute(tenantId, electionEventId, votingCardId, challengeInformationMock));
	}

	@Test
	public void givenChallengeInformationWhenExpiredTimestampThenValidationFail() throws ResourceNotFoundException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(challengeInformationMock.getServerChallengeMessage().getTimestamp())
				.thenReturn(String.valueOf(System.currentTimeMillis() - A_LONG_TIME_AGO_IN_MILLISECONDS));
		when(authenticationContentServiceMock.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContentMock);
		when(authenticationContentMock.getChallengeExpirationTime()).thenReturn(0);

		assertFalse(validation.execute(tenantId, electionEventId, votingCardId, challengeInformationMock));
	}
}
