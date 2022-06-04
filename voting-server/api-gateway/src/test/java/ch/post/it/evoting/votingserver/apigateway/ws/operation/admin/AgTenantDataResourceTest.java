/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;

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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.AuthenticationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoteVerificationAdminClient;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Test for AgTenantDataResource
 * <p>
 * This test uses Mockito and JEE/JerseyTest to make the test run. This test constructs an instance (on-demand) of the SUT using the bindings defined
 * in the configure() method. The bindings are required for all the dependencies because Mockito is not responsible for instantiating the SUT. When
 * the sut is instantiated, Mockito already initialized the mock dependencies and so it will inject them correctly.
 */
@RunWith(MockitoJUnitRunner.class)
public class AgTenantDataResourceTest extends JerseyTest {

	private static final String TRACK_ID = "trackId";
	private static final String URL_ACTIVATE_TENANT = AgTenantDataResource.RESOURCE_PATH + "/" + AgTenantDataResource.CHECK_TENANT_ACTIVATION;
	private static final String TENANT_ID = "100";
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
	@Mock
	VoteVerificationAdminClient voteVerificationAdminClient;
	@Mock
	AuthenticationAdminClient authenticationAdminClient;
	AgTenantDataResource sut;

	@Test
	public void checkTenantActivation() throws IOException {

		commonPreparation();

		JsonObject tenantConfigured = new JsonObject();
		tenantConfigured.addProperty(TENANT_ID, "configured");

		@SuppressWarnings("unchecked")
		Call<JsonObject> callMock = (Call<JsonObject>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(tenantConfigured));

		when(electionInformationAdminClient.checkTenantActivation(eq(AgTenantDataResource.PATH_TENANT_DATA), eq(TENANT_ID), eq(","), eq(TRACK_ID)))
				.thenReturn(callMock);
		when(voteVerificationAdminClient.checkTenantActivation(eq(AgTenantDataResource.PATH_TENANT_DATA), eq(TENANT_ID), eq(","), eq(TRACK_ID)))
				.thenReturn(callMock);
		when(authenticationAdminClient.checkTenantActivation(eq(AgTenantDataResource.PATH_TENANT_DATA), eq(TENANT_ID), eq(","), eq(TRACK_ID)))
				.thenReturn(callMock);
		javax.ws.rs.core.Response serviceResponse = target(URL_ACTIVATE_TENANT).resolveTemplate("tenantId", TENANT_ID).request()
				.accept("application/json").get();
		int status = serviceResponse.getStatus();

		Assert.assertEquals("UTF-8", serviceResponse.getMediaType().getParameters().get("charset"));
		Assert.assertEquals(200, status);
		JsonArray array = new JsonArray();
		array.add(tenantConfigured);
		array.add(tenantConfigured);
		array.add(tenantConfigured);
		Assert.assertEquals(array.toString(), serviceResponse.readEntity(String.class));
	}

	private void commonPreparation() {
		when(servletRequest.getHeader(anyString())).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(TRACK_ID);
	}

	@Override
	protected Application configure() {

		environmentVariables.set("ELECTION_INFORMATION_CONTEXT_URL", "localhost");
		environmentVariables.set("AUTHENTICATION_CONTEXT_URL", "localhost");
		environmentVariables.set("VERIFICATION_CONTEXT_URL", "localhost");

		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(trackIdGenerator).to(TrackIdGenerator.class);
				bind(servletRequest).to(HttpServletRequest.class);
				bind(authenticationAdminClient).to(AuthenticationAdminClient.class);
				bind(electionInformationAdminClient).to(ElectionInformationAdminClient.class);
				bind(voteVerificationAdminClient).to(VoteVerificationAdminClient.class);
			}
		};
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(AgTenantDataResource.class).register(binder);
	}
}
