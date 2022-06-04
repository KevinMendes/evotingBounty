/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.commons.beans.authentication.Credential;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

import okhttp3.ResponseBody;
import retrofit2.Call;

@RunWith(MockitoJUnitRunner.class)
public class CredentialRepositoryImplTest {
	public static final String TENANT_ID = "100";

	public static final String CREDENTIAL_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private TrackIdInstance trackIdMock;
	@Mock
	private VoterMaterialClient voterMaterialClient;
	@InjectMocks
	private final CredentialRepositoryImpl credentialRepository = new CredentialRepositoryImpl(voterMaterialClient);
	@Mock
	private Logger logger;
	@Mock
	private Credential credentialMock;

	@Test
	public void findByTenantIdElectionEventIdCredentialId() throws ResourceNotFoundException, IOException {

		@SuppressWarnings("unchecked")
		Call<Credential> callMock = (Call<Credential>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(credentialMock));

		when(voterMaterialClient.getCredential(any(), anyString(), anyString(), anyString(), anyString())).thenReturn(callMock);
		final Credential byTenantIdElectionEventIdCredentialId = credentialRepository
				.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
		assertNotNull(byTenantIdElectionEventIdCredentialId);
	}

	@Test
	public void findByTenantIdElectionEventIdCredentialIdThrowResourceNotFoundException() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		doThrow(ResourceNotFoundException.class).when(voterMaterialClient).getCredential(any(), anyString(), anyString(), anyString(), anyString());

		credentialRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
	}

	@Test
	public void findByTenantIdElectionEventIdCredentialIdThrowRetrofitError() throws ResourceNotFoundException, IOException {

		@SuppressWarnings("unchecked")
		Call<Credential> callMock = (Call<Credential>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		expectedException.expect(RetrofitException.class);
		when(voterMaterialClient.getCredential(any(), anyString(), anyString(), anyString(), anyString())).thenReturn(callMock);
		credentialRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
	}
}
