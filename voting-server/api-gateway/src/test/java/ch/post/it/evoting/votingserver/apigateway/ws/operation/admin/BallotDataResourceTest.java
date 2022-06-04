/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class BallotDataResourceTest extends JerseyTest {

	private static final String ADMIN_BOARD_ID = "1a";
	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TRACK_ID = "trackId";
	private static final String BALLOT_ID = "100b";
	private static final String TENANT_ID = "100t";
	private static final String X_FORWARDER_VALUE = ",";
	private static final String URL_BB_CONTEXT_DATA = BallotDataResource.RESOURCE_PATH + "/" + BallotDataResource.SAVE_BALLOT_DATA;
	@ClassRule
	public static EnvironmentVariables environmentVariables = new EnvironmentVariables();
	@Rule
	public TestRule restoreSystemProperties = new RestoreSystemProperties();

	@Mock
	Logger logger;

	@Mock
	TrackIdGenerator trackIdGenerator;

	@Mock
	HttpServletRequest servletRequest;

	@Mock
	ElectionInformationAdminClient electionInformationAdminClient;

	BallotDataResource sut;

	@Test
	public void addBallotBoxInformation() throws IOException {

		final int mockedInvocationStatus = 200;

		commonPreparation();
		@SuppressWarnings("unchecked")
		final Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationAdminClient
				.saveBallotData(eq(BallotDataResource.BALLOTDATA_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(BALLOT_ID), eq(ADMIN_BOARD_ID),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		final int status = target(URL_BB_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("ballotId", BALLOT_ID).resolveTemplate("adminBoardId", ADMIN_BOARD_ID).request()
				.post(Entity.entity(new ArrayList<>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void addBallotBoxInformationUnsuccessful() throws IOException {

		final int mockedInvocationStatus = 400;

		commonPreparation();
		@SuppressWarnings("unchecked")
		final Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		when(callMock.execute())
				.thenReturn(Response.error(mockedInvocationStatus, ResponseBody.create(okhttp3.MediaType.parse("test/html"), new byte[0])));

		when(electionInformationAdminClient
				.saveBallotData(eq(BallotDataResource.BALLOTDATA_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(BALLOT_ID), eq(ADMIN_BOARD_ID),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		final int status = target(URL_BB_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("ballotId", BALLOT_ID).resolveTemplate("adminBoardId", ADMIN_BOARD_ID).request()
				.post(Entity.entity(new ArrayList<>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	private void commonPreparation() {
		when(servletRequest.getHeader(anyString())).thenReturn("");
		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(TRACK_ID);
	}

	@Override
	protected Application configure() {

		environmentVariables.set("ELECTION_INFORMATION_CONTEXT_URL", "localhost");

		MockitoAnnotations.openMocks(this);

		final AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(servletRequest).to(HttpServletRequest.class);
			}
		};
		sut = new BallotDataResource(electionInformationAdminClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}
}
