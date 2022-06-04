/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.google.gson.JsonArray;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoteVerificationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import retrofit2.Call;
import retrofit2.Response;

public class ReturnCodesMappingTableResourceTest extends JerseyTest {

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
	VoteVerificationAdminClient voteVerificationAdminClient;

	@InjectMocks
	ReturnCodesMappingTableResource returnCodesMappingTableResource;

	@Test
	public void saveReturnCodesMappingTableWithValidParameters() throws IOException {
		final String electionEventId = "e3e3c2fd8a16489291c5c24e7b74b26e";
		final String verificationCardSetId = "e3e3c2fd8a16489291c5c24e7b74b26e";
		final String trackId = "trackId";
		final String X_FORWARDER_VALUE = "localhost,";
		final JsonArray response = new JsonArray();
		response.add(electionEventId);
		final Call<JsonArray> call = (Call<JsonArray>) Mockito.mock(Call.class);
		when(call.execute()).thenReturn(Response.success(response));

		when(servletRequest.getHeader(XForwardedForFactoryImpl.HEADER)).thenReturn("localhost");
		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(trackId);
		when(voteVerificationAdminClient
				.saveReturnCodesMappingTable(eq(electionEventId), eq(verificationCardSetId), any(), any(), any(), eq(X_FORWARDER_VALUE), eq(trackId)))
				.thenReturn(call);

		final int status = target(
				"/vv/api/v1/configuration/returncodesmappingtable/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}")
				.resolveTemplate("electionEventId", electionEventId)
				.resolveTemplate("verificationCardSetId", verificationCardSetId)
				.request()
				.post(Entity.entity(new ArrayList<>(), MediaType.APPLICATION_JSON_TYPE))
				.getStatus();

		assertEquals(200, status);
	}

	@Override
	protected Application configure() {
		environmentVariables.set("VERIFICATION_CONTEXT_URL", "localhost");
		MockitoAnnotations.openMocks(this);

		final AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(trackIdGenerator).to(TrackIdGenerator.class);
				bind(servletRequest).to(HttpServletRequest.class);
				bind(voteVerificationAdminClient).to(VoteVerificationAdminClient.class);
			}
		};
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(returnCodesMappingTableResource).register(binder);
	}
}