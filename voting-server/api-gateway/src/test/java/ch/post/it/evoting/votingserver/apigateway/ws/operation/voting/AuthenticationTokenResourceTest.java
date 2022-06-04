/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.voting;

import static org.mockito.ArgumentMatchers.any;
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

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.VotingWorkflowVotingClient;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import retrofit2.Call;

public class AuthenticationTokenResourceTest extends JerseyTest {

	public static final String URL_TOKEN_CONTEXT_DATA = AuthenticationTokenResource.RESOURCE_PATH;
	private static final String VERSION = "1v";
	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TRACK_ID = "trackId";
	private static final String TENANT_ID = "100t";
	private static final String CREDENTIAL_ID = "1c";
	private static final String X_FORWARDER_VALUE = "localhost,";
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
	VotingWorkflowVotingClient votingWorkflowVotingClient;

	AuthenticationTokenResource sut;

	@Test
	public void getStatusOfVotingCards() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();
		JsonObject reply = new JsonObject();

		@SuppressWarnings("unchecked")
		Call<JsonObject> callMock = (Call<JsonObject>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(reply));

		when(votingWorkflowVotingClient
				.getAuthenticationToken(eq(AuthenticationTokenResource.AUTHENTICATION_TOKEN_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID),
						eq(CREDENTIAL_ID), eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		int status = target(URL_TOKEN_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).resolveTemplate("credentialId", CREDENTIAL_ID).request()
				.post(Entity.entity(new ArrayList<Object>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	private void commonPreparation() {
		when(servletRequest.getHeader(eq(XForwardedForFactoryImpl.HEADER))).thenReturn("localhost");

		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(TRACK_ID);
	}

	@Override
	protected Application configure() {

		environmentVariables.set("VOTING_WORKFLOW_CONTEXT_URL", "localhost");
		MockitoAnnotations.initMocks(this);

		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(servletRequest).to(HttpServletRequest.class);
			}
		};
		sut = new AuthenticationTokenResource(votingWorkflowVotingClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}
}
