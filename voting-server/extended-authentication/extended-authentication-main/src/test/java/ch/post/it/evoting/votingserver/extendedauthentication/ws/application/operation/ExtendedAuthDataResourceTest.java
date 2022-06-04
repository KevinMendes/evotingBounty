/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.ExtendedAuthenticationServiceImpl;

public class ExtendedAuthDataResourceTest extends ExtendedAuthDataResource {

	public static final String TRACK_ID = "trackId";

	public static final String TENANT_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String ADMIN_BOARD_ID = "100";

	public static final int OK = 200;

	public static final int PRECONDITION_FAILED = 412;

	public ExtendedAuthDataResourceTest() {
		super();

		this.extendedAuthenticationService = mock(ExtendedAuthenticationServiceImpl.class);
		this.trackIdInstance = mock(TrackIdInstance.class);
	}

	@Test
	public void SuccessfulResponse() throws ApplicationException, IOException {
		when(extendedAuthenticationService.saveExtendedAuthenticationFromFile(any(), any(), any(), any())).thenReturn(true);
		InputStream inputStream = mock(InputStream.class);

		HttpServletRequest request = mock(HttpServletRequest.class);
		Response response = saveExtendedAuthenticationData(TRACK_ID, TENANT_ID, ELECTION_EVENT_ID, ADMIN_BOARD_ID, inputStream, request);
		assertThat(response.getStatus(), is(OK));

	}

	@Test
	public void signatureVerificationFails() throws ApplicationException, IOException {

		InputStream inputStream = mock(InputStream.class);

		HttpServletRequest request = mock(HttpServletRequest.class);
		Response response = saveExtendedAuthenticationData(TRACK_ID, TENANT_ID, ELECTION_EVENT_ID, ADMIN_BOARD_ID, inputStream, request);
		assertThat(response.getStatus(), is(PRECONDITION_FAILED));

	}

}
