/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.compute;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.controlcomponents.OrchestratorClient;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;

public class ComputeResourceTest extends JerseyTest {

	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TRACK_ID = "trackId";
	private static final String TENANT_ID = "100t";
	private static final String X_FORWARDER_VALUE = "localhost,";
	private static final String URL_COMPUTE_REQUEST = ComputeResource.RESOURCE_PATH + "/" + ComputeResource.COMPUTE_CHOICE_CODES_REQUEST;
	private static final String URL_COMPUTE_RETRIEVE = ComputeResource.RESOURCE_PATH + "/" + ComputeResource.COMPUTE_CHOICE_CODES_RETRIEVAL;
	private static final String VERIFICATION_CARD_SET_ID = "dfasifnasdfasf6jg893724dnfsifasf";
	private static final int CHUNK_ID = 0;
	private static final String VC_ID_1 = "79c6c49af4044b1abb5434413304748f";
	private static final String VC_ID_2 = "39c6c49af4044b1abb5434413304748f";
	private static final Map<String, String> VERIFICATION_CARD_IDS_TO_BCK = new HashMap<>();
	private static final String BALLOT_OPTIONS_LIST = "encryptedOptions";
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
	OrchestratorClient orchestratorClient;
	ComputeResource sut;

	@Test
	public void computePostHappyPath() throws IOException {

		int statusOK = 200;
		commonPreparation();
		BufferedSource source = new Buffer();
		ResponseBody result = new RealResponseBody("application/json", 0, source);

		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(result));

		VERIFICATION_CARD_IDS_TO_BCK.put(VC_ID_1, "2");
		VERIFICATION_CARD_IDS_TO_BCK.put(VC_ID_2, "3");

		ComputeInput computeInput = new ComputeInput();
		computeInput.setVerificationCardIdsToBallotCastingKeys(VERIFICATION_CARD_IDS_TO_BCK);
		computeInput.setEncryptedRepresentations(BALLOT_OPTIONS_LIST);

		when(orchestratorClient.requestComputeChoiceCodes(eq(ComputeResource.COMPUTE_PATH), eq(X_FORWARDER_VALUE), eq(TRACK_ID), any()))
				.thenReturn(callMock);

		int status = target(URL_COMPUTE_REQUEST)
				.resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("verificationCardSetId", VERIFICATION_CARD_SET_ID)
				.request()
				.post(Entity.entity(computeInput, MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(status, statusOK);

	}

	@Test
	public void computeGetHappyPath() throws IOException {

		commonPreparation();
		BufferedSource source = mock(BufferedSource.class);
		byte[] bytes = { 1, 2, 3 };
		when(source.inputStream()).thenReturn(new ByteArrayInputStream(bytes));

		ResponseBody result = mock(ResponseBody.class);
		when(result.source()).thenReturn(source);

		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(result));

		when(orchestratorClient
				.retrieveComputedChoiceCodes(eq(ComputeResource.COMPUTE_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(VERIFICATION_CARD_SET_ID),
						eq(CHUNK_ID), eq(X_FORWARDER_VALUE), eq(TRACK_ID))).thenReturn(callMock);

		Response response = target(URL_COMPUTE_RETRIEVE).resolveTemplate("tenantId", TENANT_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID)
				.resolveTemplate("verificationCardSetId", VERIFICATION_CARD_SET_ID).resolveTemplate("chunkId", CHUNK_ID).request().get();
		int status = response.getStatus();

		Assert.assertEquals(200, status);

	}

	private void commonPreparation() {
		when(servletRequest.getHeader(eq(XForwardedForFactoryImpl.HEADER))).thenReturn("localhost");
		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(TRACK_ID);
	}

	@Override
	protected Application configure() {

		environmentVariables.set("ORCHESTRATOR_CONTEXT_URL", "localhost");
		MockitoAnnotations.initMocks(this);

		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(servletRequest).to(HttpServletRequest.class);
			}
		};
		sut = new ComputeResource(orchestratorClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}

	private static class ComputeInput {

		private String encryptedRepresentations;
		private Map<String, String> verificationCardIdsToBallotCastingKeys;

		public String getEncryptedRepresentations() {
			return encryptedRepresentations;
		}

		public void setEncryptedRepresentations(String encryptedRepresentations) {
			this.encryptedRepresentations = encryptedRepresentations;
		}

		public Map<String, String> getVerificationCardIdsToBallotCastingKeys() {
			return verificationCardIdsToBallotCastingKeys;
		}

		public void setVerificationCardIdsToBallotCastingKeys(Map<String, String> verificationCardIdsToBallotCastingKeys) {
			this.verificationCardIdsToBallotCastingKeys = verificationCardIdsToBallotCastingKeys;
		}
	}
}
