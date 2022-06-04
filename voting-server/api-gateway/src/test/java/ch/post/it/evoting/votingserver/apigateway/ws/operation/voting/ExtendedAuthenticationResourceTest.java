/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.voting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.apigateway.model.ExtendedAuthResponse;
import ch.post.it.evoting.votingserver.apigateway.model.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.apigateway.model.NumberOfRemainingAttempts;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.ExtendedAuthenticationVotingClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ExtendedAuthenticationResourceTest extends JerseyTest {

	public static final String URL_EA_CONTEXT_DATA = ExtendedAuthenticationResource.RESOURCE_PATH;
	private static final String VERSION = "1v";
	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TRACK_ID = "trackId";
	private static final String AUTH_TOKEN = "authToken";
	private static final String TENANT_ID = "100t";
	private static final String X_FORWARDER_VALUE = "localhost,";
	@ClassRule
	public static EnvironmentVariables environmentVariables = new EnvironmentVariables();
	@Rule
	public TestRule restoreSystemProperties = new RestoreSystemProperties();

	@Mock
	TrackIdGenerator trackIdGenerator;

	@Mock
	HttpServletRequest servletRequest;

	@Mock
	ExtendedAuthenticationVotingClient extendedAuthClient;

	ExtendedAuthenticationResource sut;

	@Test
	public void getEncryptedStartVotingKey() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();
		ExtendedAuthResponse reply = new ExtendedAuthResponse();
		reply.setEncryptedSVK("encryptedSVK");
		reply.setNumberOfRemainingAttempts(1);
		reply.setResponseCode("OK");

		@SuppressWarnings("unchecked")
		Call<ExtendedAuthResponse> callMock = (Call<ExtendedAuthResponse>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(reply));

		when(extendedAuthClient.getEncryptedStartVotingKey(eq(ExtendedAuthenticationResource.EXT_AUTH_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID),
				eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		ExtendedAuthentication extendedAuthenticationInfo = new ExtendedAuthentication();
		extendedAuthenticationInfo.setAuthId("authId");
		extendedAuthenticationInfo.setExtraParam("extraParam");
		Response response = target(URL_EA_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestApplication.PARAMETER_AUTHENTICATION_TOKEN, AUTH_TOKEN)
				.post(Entity.entity(extendedAuthenticationInfo, MediaType.APPLICATION_JSON_TYPE));
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
		ExtendedAuthResponse acualResponse = response.readEntity(ExtendedAuthResponse.class);
		Assert.assertEquals(reply.getEncryptedSVK(), acualResponse.getEncryptedSVK());
		Assert.assertNull(acualResponse.getNumberOfRemainingAttempts());
		Assert.assertNull(acualResponse.getResponseCode());
	}

	@Test
	public void getEncryptedStartVotingKeyUnauthorized() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();
		ExtendedAuthResponse reply = new ExtendedAuthResponse();
		reply.setEncryptedSVK("encryptedSVK");
		reply.setNumberOfRemainingAttempts(1);
		reply.setResponseCode("UNAUTHORIZED");

		@SuppressWarnings("unchecked")
		Call<ExtendedAuthResponse> callMock = (Call<ExtendedAuthResponse>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(reply));

		when(extendedAuthClient.getEncryptedStartVotingKey(eq(ExtendedAuthenticationResource.EXT_AUTH_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID),
				eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		ExtendedAuthentication extendedAuthenticationInfo = new ExtendedAuthentication();
		extendedAuthenticationInfo.setAuthId("authId");
		extendedAuthenticationInfo.setExtraParam("extraParam");
		Response response = target(URL_EA_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestApplication.PARAMETER_AUTHENTICATION_TOKEN, AUTH_TOKEN)
				.post(Entity.entity(extendedAuthenticationInfo, MediaType.APPLICATION_JSON_TYPE));
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
		NumberOfRemainingAttempts acualResponse = response.readEntity(NumberOfRemainingAttempts.class);
		Assert.assertEquals(reply.getNumberOfRemainingAttempts(), acualResponse.getNumberOfRemainingAttempts());
	}

	@Test
	public void getEncryptedStartVotingKeyBadRequest() throws IOException {

		int mockedInvocationStatus = 400;
		commonPreparation();
		ExtendedAuthResponse reply = new ExtendedAuthResponse();
		reply.setEncryptedSVK("encryptedSVK");
		reply.setNumberOfRemainingAttempts(1);
		reply.setResponseCode("BAD_REQUEST");

		@SuppressWarnings("unchecked")
		Call<ExtendedAuthResponse> callMock = (Call<ExtendedAuthResponse>) Mockito.mock(Call.class);
		StringWriter writer = new StringWriter();
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		objectMapper.writeValue(writer, reply);
		when(callMock.execute()).thenReturn(retrofit2.Response.error(mockedInvocationStatus,
				ResponseBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), writer.toString().getBytes(StandardCharsets.UTF_8))));

		when(extendedAuthClient.getEncryptedStartVotingKey(eq(ExtendedAuthenticationResource.EXT_AUTH_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID),
				eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		ExtendedAuthentication extendedAuthenticationInfo = new ExtendedAuthentication();
		extendedAuthenticationInfo.setAuthId("authId");
		extendedAuthenticationInfo.setExtraParam("extraParam");
		Response response = target(URL_EA_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestApplication.PARAMETER_AUTHENTICATION_TOKEN, AUTH_TOKEN)
				.post(Entity.entity(extendedAuthenticationInfo, MediaType.APPLICATION_JSON_TYPE));
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void getEncryptedStartVotingKeyServiceUnavailable() throws IOException {

		int mockedInvocationStatus = 400;
		commonPreparation();

		@SuppressWarnings("unchecked")
		Call<ExtendedAuthResponse> callMock = (Call<ExtendedAuthResponse>) Mockito.mock(Call.class);
		when(callMock.execute())
				.thenReturn(retrofit2.Response.error(mockedInvocationStatus, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(extendedAuthClient.getEncryptedStartVotingKey(eq(ExtendedAuthenticationResource.EXT_AUTH_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID),
				eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		ExtendedAuthentication extendedAuthenticationInfo = new ExtendedAuthentication();
		extendedAuthenticationInfo.setAuthId("authId");
		extendedAuthenticationInfo.setExtraParam("extraParam");
		Response response = target(URL_EA_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestApplication.PARAMETER_AUTHENTICATION_TOKEN, AUTH_TOKEN)
				.post(Entity.entity(extendedAuthenticationInfo, MediaType.APPLICATION_JSON_TYPE));
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void updateExtendedAuthData() throws IOException {

		int mockedInvocationStatus = 200;
		commonPreparation();
		ExtendedAuthResponse reply = new ExtendedAuthResponse();
		reply.setResponseCode("OK");

		@SuppressWarnings("unchecked")
		Call<ExtendedAuthResponse> callMock = (Call<ExtendedAuthResponse>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(reply));

		when(extendedAuthClient
				.updateExtendedAuthData(eq(ExtendedAuthenticationResource.EXT_AUTH_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(AUTH_TOKEN),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		extendedAuthenticationUpdateRequest.setCertificate("certificate");
		extendedAuthenticationUpdateRequest.setSignature("signature");
		Response response = target(URL_EA_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestApplication.PARAMETER_AUTHENTICATION_TOKEN, AUTH_TOKEN)
				.put(Entity.entity(extendedAuthenticationUpdateRequest, MediaType.APPLICATION_JSON_TYPE));
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void updateExtendedAuthDataServiceUnavailable() throws IOException {

		int mockedInvocationStatus = 400;
		commonPreparation();

		@SuppressWarnings("unchecked")
		Call<ExtendedAuthResponse> callMock = (Call<ExtendedAuthResponse>) Mockito.mock(Call.class);
		when(callMock.execute())
				.thenReturn(retrofit2.Response.error(mockedInvocationStatus, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(extendedAuthClient
				.updateExtendedAuthData(eq(ExtendedAuthenticationResource.EXT_AUTH_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(AUTH_TOKEN),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID), any())).thenReturn(callMock);

		ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		extendedAuthenticationUpdateRequest.setCertificate("certificate");
		extendedAuthenticationUpdateRequest.setSignature("signature");
		Response response = target(URL_EA_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("version", VERSION).request().header(RestApplication.PARAMETER_AUTHENTICATION_TOKEN, AUTH_TOKEN)
				.put(Entity.entity(extendedAuthenticationUpdateRequest, MediaType.APPLICATION_JSON_TYPE));
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	private void commonPreparation() {
		when(servletRequest.getHeader(eq(XForwardedForFactoryImpl.HEADER))).thenReturn("localhost");
		when(servletRequest.getHeader(eq(RestApplication.PARAMETER_AUTHENTICATION_TOKEN))).thenReturn(AUTH_TOKEN);

		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(TRACK_ID);
	}

	@Override
	protected Application configure() {

		environmentVariables.set("EXTENDED_AUTHENTICATION_CONTEXT_URL", "localhost");
		MockitoAnnotations.initMocks(this);

		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(servletRequest).to(HttpServletRequest.class);
			}
		};
		sut = new ExtendedAuthenticationResource(extendedAuthClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}
}
