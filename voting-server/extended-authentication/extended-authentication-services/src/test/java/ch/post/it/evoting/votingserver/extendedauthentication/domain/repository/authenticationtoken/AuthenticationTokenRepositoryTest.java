/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.repository.authenticationtoken;

import static ch.post.it.evoting.votingserver.extendedauthentication.domain.utils.BeanUtils.createAuthenticationToken;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenRepositoryException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.AuthenticationClient;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Test class for the Authentication token repository
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenRepositoryTest {

	private static final String TENANT_ID = "100";

	private static final String ELECTION_EVENT_ID = "100";

	private static final String TRACK_ID = "trackId";
	@Rule
	public TestRule restoreSystemProperties = new RestoreSystemProperties();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private AuthenticationClient client;
	@InjectMocks
	private final AuthenticationTokenRepository authenticationTokenRepository = new AuthenticationTokenRepository(client);
	@Mock
	private TrackIdInstance trackIdInstance;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void getTokenOK() throws IOException, AuthTokenRepositoryException {

		when(trackIdInstance.getTrackId()).thenReturn(TRACK_ID);

		final AuthenticationToken token = createAuthenticationToken();
		final ValidationResult result = new ValidationResult();
		result.setResult(true);

		final Call<ValidationResult> callMock = (Call<ValidationResult>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(new ValidationResult(true)));

		when(client.validateAuthenticationToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(callMock);
		final ValidationResult validationResult = authenticationTokenRepository.validateToken(TENANT_ID, ELECTION_EVENT_ID, token);
		assertTrue(validationResult.isResult());

	}
}
