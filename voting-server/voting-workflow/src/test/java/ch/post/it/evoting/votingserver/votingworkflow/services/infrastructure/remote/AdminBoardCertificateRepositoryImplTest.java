/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class AdminBoardCertificateRepositoryImplTest {

	private final String TENANT_ID = "100";
	private final String ELECTION_EVENT_ID = "1";
	private final String TRACK_ID = "1";

	// this value has to correspond with Application.properties:AUTHENTICATION_INFORMATION_PATH
	private final String AUTHENTICATION_CERTIFICATES_PATH_VALUE = "certificates";

	@Mock
	private TrackIdInstance trackId;

	@Mock
	private AuthenticationClient authenticationClient;

	@InjectMocks
	AdminBoardCertificateRepositoryImpl rut = new AdminBoardCertificateRepositoryImpl(authenticationClient);

	@Before
	public void init() {
		when(trackId.getTrackId()).thenReturn(TRACK_ID);
	}

	@Test
	public void testFindCredentialInfoSuccessful() throws ResourceNotFoundException, ApplicationException, IOException {
		AdminBoardCertificates adminMock = new AdminBoardCertificates("certs");

		@SuppressWarnings("unchecked")
		Call<AdminBoardCertificates> callMock = (Call<AdminBoardCertificates>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(adminMock));

		when(authenticationClient
				.findByTenantElectionEventCertificates(TRACK_ID, AUTHENTICATION_CERTIFICATES_PATH_VALUE, TENANT_ID, ELECTION_EVENT_ID))
				.thenReturn(callMock);

		AdminBoardCertificates adminBoardCertificates = rut.findByTenantElectionEventCertificates(TENANT_ID, ELECTION_EVENT_ID);
		assertEquals(adminMock, adminBoardCertificates);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testFindCredentialInfoResourceNotFoundException() throws ResourceNotFoundException, ApplicationException, IOException {

		when(authenticationClient
				.findByTenantElectionEventCertificates(TRACK_ID, AUTHENTICATION_CERTIFICATES_PATH_VALUE, TENANT_ID, ELECTION_EVENT_ID))
				.thenThrow(new ResourceNotFoundException("exception"));

		rut.findByTenantElectionEventCertificates(TENANT_ID, ELECTION_EVENT_ID);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testFindCredentialInfo404Error() throws ResourceNotFoundException, ApplicationException, IOException {
		@SuppressWarnings("unchecked")
		Call<AdminBoardCertificates> callMock = (Call<AdminBoardCertificates>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(authenticationClient
				.findByTenantElectionEventCertificates(TRACK_ID, AUTHENTICATION_CERTIFICATES_PATH_VALUE, TENANT_ID, ELECTION_EVENT_ID))
				.thenReturn(callMock);

		rut.findByTenantElectionEventCertificates(TENANT_ID, ELECTION_EVENT_ID);
	}

	@Test(expected = RetrofitException.class)
	public void testFindCredentialInfo500Error() throws ResourceNotFoundException, ApplicationException, IOException {
		@SuppressWarnings("unchecked")
		Call<AdminBoardCertificates> callMock = (Call<AdminBoardCertificates>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(500, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(authenticationClient
				.findByTenantElectionEventCertificates(TRACK_ID, AUTHENTICATION_CERTIFICATES_PATH_VALUE, TENANT_ID, ELECTION_EVENT_ID))
				.thenReturn(callMock);

		rut.findByTenantElectionEventCertificates(TENANT_ID, ELECTION_EVENT_ID);
	}

}

