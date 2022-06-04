/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.monitoring;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

import com.google.gson.JsonArray;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.VotingWorkflowVotingClient;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class VotingCardsResourceTest extends JerseyTest {

	private static final String VERSION = "1v";
	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TRACK_ID = "trackId";
	private static final String TENANT_ID = "100t";
	private static final String X_FORWARDER_VALUE = "localhost,";
	private static final String SIGNATURE = "signature";
	private static final String ORIGINATOR = "originator";
	private static final String URL_VC_STATUS_CONTEXT_DATA = VotingCardsResource.RESOURCE_PATH + VotingCardsResource.GET_STATUS_OF_VOTING_CARDS;
	private static final String URL_VC_BLOCK_CONTEXT_DATA = VotingCardsResource.RESOURCE_PATH + VotingCardsResource.BLOCK_VOTING_CARDS;
	private static final String URL_VC_STATUS_LIST_CONTEXT_DATA =
			VotingCardsResource.RESOURCE_PATH + VotingCardsResource.GET_ID_AND_STATE_OF_INACTIVE_VOTING_CARDS;
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
	VotingWorkflowVotingClient votinWorkflowVotingClient;

	VotingCardsResource sut;

	@Test
	public void getStatusOfVotingCards() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();
		JsonArray reply = new JsonArray();

		@SuppressWarnings("unchecked")
		Call<JsonArray> callMock = (Call<JsonArray>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(reply));

		when(votinWorkflowVotingClient
				.getStatusOfVotingCards(eq(VotingCardsResource.VOTE_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(X_FORWARDER_VALUE), eq(TRACK_ID),
						eq(ORIGINATOR), eq(SIGNATURE), any())).thenReturn(callMock);

		int status = target(URL_VC_STATUS_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR)
				.header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE)
				.post(Entity.entity(new ArrayList<Object>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void blockVotingCards() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();
		JsonArray reply = new JsonArray();

		@SuppressWarnings("unchecked")
		Call<JsonArray> callMock = (Call<JsonArray>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(reply));

		when(votinWorkflowVotingClient
				.blockVotingCards(eq(VotingCardsResource.VOTE_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(X_FORWARDER_VALUE), eq(TRACK_ID),
						eq(ORIGINATOR), eq(SIGNATURE), any())).thenReturn(callMock);

		int status = target(URL_VC_BLOCK_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR)
				.header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE)
				.put(Entity.entity(new ArrayList<Object>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void getIdAndStateOfInactiveVotingCards() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();

		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		Headers headers = new Headers.Builder().add("Inactive-Voting-Cards-Filename", "test.csv").build();
		Response<ResponseBody> mockedResponse = Response
				.success(ResponseBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM), "ok".getBytes(StandardCharsets.UTF_8)),
						headers);
		when(callMock.execute()).thenReturn(mockedResponse);

		when(votinWorkflowVotingClient
				.getIdAndStateOfInactiveVotingCards(eq(VotingCardsResource.VOTE_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(TRACK_ID),
						eq(X_FORWARDER_VALUE), eq(ORIGINATOR), eq(SIGNATURE))).thenReturn(callMock);

		int status = target(URL_VC_STATUS_LIST_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("electionEventId", ELECTION_EVENT_ID).resolveTemplate("version", VERSION).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE).get()
				.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void getIdAndStateOfInactiveVotingCardsWithoutResponse() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();

		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		Headers headers = new Headers.Builder().add("Inactive-Voting-Cards-Filename", "test.csv").build();
		Response<ResponseBody> mockedResponse = Response
				.success(ResponseBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM), "ok".getBytes(StandardCharsets.UTF_8)),
						headers);
		when(callMock.execute()).thenReturn(mockedResponse);

		when(votinWorkflowVotingClient
				.getIdAndStateOfInactiveVotingCards(eq(VotingCardsResource.VOTE_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(TRACK_ID),
						eq(X_FORWARDER_VALUE), eq(ORIGINATOR), eq(SIGNATURE))).thenReturn(callMock);

		int status = target(URL_VC_STATUS_LIST_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("electionEventId", ELECTION_EVENT_ID).resolveTemplate("version", VERSION).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE).get()
				.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void getIdAndStateOfInactiveVotingCardsWithoutCorrectHeader() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();

		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		Headers headers = new Headers.Builder().add("test", "test").build();
		Response<ResponseBody> mockedResponse = Response
				.success(ResponseBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM), "ok".getBytes(StandardCharsets.UTF_8)),
						headers);
		when(callMock.execute()).thenReturn(mockedResponse);

		when(votinWorkflowVotingClient
				.getIdAndStateOfInactiveVotingCards(eq(VotingCardsResource.VOTE_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(TRACK_ID),
						eq(X_FORWARDER_VALUE), eq(ORIGINATOR), eq(SIGNATURE))).thenReturn(callMock);

		int status = target(URL_VC_STATUS_LIST_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("electionEventId", ELECTION_EVENT_ID).resolveTemplate("version", VERSION).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE).get()
				.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void getIdAndStateOfInactiveVotingCardsWithoutHeader() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();

		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		Headers headers = new Headers.Builder().build();
		Response<ResponseBody> mockedResponse = Response
				.success(ResponseBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_OCTET_STREAM), "ok".getBytes(StandardCharsets.UTF_8)),
						headers);
		when(callMock.execute()).thenReturn(mockedResponse);

		when(votinWorkflowVotingClient
				.getIdAndStateOfInactiveVotingCards(eq(VotingCardsResource.VOTE_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(TRACK_ID),
						eq(X_FORWARDER_VALUE), eq(ORIGINATOR), eq(SIGNATURE))).thenReturn(callMock);

		int status = target(URL_VC_STATUS_LIST_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("electionEventId", ELECTION_EVENT_ID).resolveTemplate("version", VERSION).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE).get()
				.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	private void commonPreparation() {
		when(servletRequest.getHeader(eq(RestClientInterceptor.HEADER_ORIGINATOR))).thenReturn(ORIGINATOR);
		when(servletRequest.getHeader(eq(RestClientInterceptor.HEADER_SIGNATURE))).thenReturn(SIGNATURE);
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
		sut = new VotingCardsResource(votinWorkflowVotingClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}
}
