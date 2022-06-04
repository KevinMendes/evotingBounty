/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

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

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoterMaterialAdminClient;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import retrofit2.Call;

public class VoterInformationResourceTest extends JerseyTest {

	private static final String ADMIN_BOARD_ID = "1a";
	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TERM_VOTING_CARD_ID = "searchterm";
	private static final String OFFSET = "offset";
	private static final String LIMIT = "limit";
	private static final String TRACK_ID = "trackId";
	private static final String TENANT_ID = "100t";
	private static final String SIGNATURE = "signature";
	private static final String ORIGINATOR = "originator";
	private static final String X_FORWARDER_VALUE = ",";
	private static final String URL_VI_QUERY_CONTEXT_DATA =
			VoterInformationResource.RESOURCE_PATH + "/" + VoterInformationResource.GET_VOTING_CARDS_BY_QUERY;
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
	VoterMaterialAdminClient voterMaterialAdminClient;

	VoterInformationResource sut;

	@Test
	public void getVotingCardsByQuery() throws IOException {

		int mockedInvocationStatus = 200;

		commonPreparation();

		JsonObject json = new JsonObject();
		json.addProperty("search", "result");
		@SuppressWarnings("unchecked")
		Call<JsonObject> callMock = (Call<JsonObject>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(json));

		when(voterMaterialAdminClient
				.getVotingCardsByQuery(eq(VoterInformationResource.CREDENTIAL_INFORMATION_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID),
						eq(TERM_VOTING_CARD_ID), eq(OFFSET), eq(LIMIT), eq(X_FORWARDER_VALUE), eq(TRACK_ID), eq(ORIGINATOR), eq(SIGNATURE)))
				.thenReturn(callMock);

		Response response = target(URL_VI_QUERY_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("electionEventId", ELECTION_EVENT_ID).resolveTemplate("adminBoardId", ADMIN_BOARD_ID)
				.queryParam("id", TERM_VOTING_CARD_ID).queryParam("offset", OFFSET).queryParam("size", LIMIT).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE).get();
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
		Assert.assertEquals(response.readEntity(String.class), json.toString());
	}

	private void commonPreparation() {
		when(servletRequest.getHeader(eq(RestClientInterceptor.HEADER_ORIGINATOR))).thenReturn(ORIGINATOR);
		when(servletRequest.getHeader(eq(RestClientInterceptor.HEADER_SIGNATURE))).thenReturn(SIGNATURE);
		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(TRACK_ID);
	}

	@Override
	protected Application configure() {

		environmentVariables.set("VOTER_MATERIAL_CONTEXT_URL", "localhost");
		MockitoAnnotations.initMocks(this);

		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(servletRequest).to(HttpServletRequest.class);
			}
		};
		sut = new VoterInformationResource(voterMaterialAdminClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}
}
