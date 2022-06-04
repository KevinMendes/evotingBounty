/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Test for BallotBoxResource.  This is another way to instantiate the SUT. In this case the mock dependency will be already initialized when we
 * manually construct the SUT because of the call to MockitoAnnotations.initMocks (in the configure() method). In this case we must not have
 *
 * @RunWith(MockitoJrunner) because otherwise the "configured" mocks (in the test methods) won't be the same as the ones initialized in the call to
 * initMocks. Using @InjectMocks on the SUT is not necessary, but can be used in case we need it for other dependencies.
 */
public class BallotBoxResourceTest extends JerseyTest {

	private static final String X_FORWARDER_VALUE = ",";
	private static final String SIGNATURE = "signature";
	private static final String ORIGINATOR = "originator";
	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TRACK_ID = "trackId";
	private static final String BALLOT_BOX_ID = "100b";
	private static final String TENANT_ID = "100t";
	private static final String URL_ENCRYPTED_BB = BallotBoxResource.RESOURCE_PATH + "/" + BallotBoxResource.GET_ENCRYPTED_BALLOT_BOX_CSV;
	private static final String URL_CHECK_BB_IS_EMPTY = BallotBoxResource.RESOURCE_PATH + "/" + BallotBoxResource.CHECK_IF_BALLOT_BOX_IS_EMPTY;
	private static final String URL_CHECK_BB_IS_AVAILABLE =
			BallotBoxResource.RESOURCE_PATH + "/" + BallotBoxResource.CHECK_IF_BALLOT_BOX_IS_AVAILABLE;
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

	@InjectMocks
	BallotBoxResource sut;

	@Test
	public void getEncryptedBallotBox() throws IOException {
		int mockedInvocationStatus = 200;

		commonPreparation();
		String responseString = "response";
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(
				ResponseBody.create(okhttp3.MediaType.parse("application/octet-stream"), responseString.getBytes(StandardCharsets.UTF_8))));

		when(electionInformationAdminClient
				.getEncryptedBallotBox(eq(BallotBoxResource.BALLOT_BOX_STATUS_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(BALLOT_BOX_ID),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID), eq(ORIGINATOR), eq(SIGNATURE))).thenReturn(callMock);

		javax.ws.rs.core.Response serviceResponse = target(URL_ENCRYPTED_BB).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("ballotBoxId", BALLOT_BOX_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE)
				.accept("application/octet-stream").get();
		int status = serviceResponse.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
		Assert.assertEquals(responseString.length(), serviceResponse.getLength());
	}

	@Test
	public void getEmptyEncryptedBallotBox() throws IOException {
		int mockedInvocationStatus = 200;

		commonPreparation();

		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationAdminClient
				.getEncryptedBallotBox(eq(BallotBoxResource.BALLOT_BOX_STATUS_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(BALLOT_BOX_ID),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID), eq(ORIGINATOR), eq(SIGNATURE))).thenReturn(callMock);
		javax.ws.rs.core.Response serviceResponse = target(URL_ENCRYPTED_BB).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("ballotBoxId", BALLOT_BOX_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE)
				.accept("application/octet-stream").get();
		int status = serviceResponse.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
		Assert.assertEquals(0, serviceResponse.getLength());
	}

	@Test
	public void checkIfBallotBoxIsEmpty() throws IOException {
		int mockedInvocationStatus = 200;

		commonPreparation();

		JsonObject bbIsEmpty = new JsonObject();
		bbIsEmpty.addProperty("100", "empty");
		@SuppressWarnings("unchecked")
		Call<JsonObject> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(bbIsEmpty));
		when(electionInformationAdminClient
				.checkIfBallotBoxIsEmpty(eq(BallotBoxResource.BALLOT_BOX_STATUS_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(BALLOT_BOX_ID),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID))).thenReturn(callMock);
		javax.ws.rs.core.Response serviceResponse = target(URL_CHECK_BB_IS_EMPTY).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("ballotBoxId", BALLOT_BOX_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID).request().get();
		int status = serviceResponse.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
		Assert.assertEquals(bbIsEmpty.toString().length(), serviceResponse.getLength());
	}

	@Test
	public void checkIfBallotBoxIsAvailable() throws IOException {
		int mockedInvocationStatus = 200;

		commonPreparation();

		JsonObject bbIsAvailable = new JsonObject();
		bbIsAvailable.addProperty("100", "available");
		@SuppressWarnings("unchecked")
		Call<JsonObject> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(bbIsAvailable));

		when(electionInformationAdminClient
				.checkIfBallotBoxIsAvailable(eq(BallotBoxResource.BALLOT_BOX_STATUS_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(BALLOT_BOX_ID),
						eq(X_FORWARDER_VALUE), eq(TRACK_ID))).thenReturn(callMock);

		javax.ws.rs.core.Response serviceResponse = target(URL_CHECK_BB_IS_AVAILABLE).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("ballotBoxId", BALLOT_BOX_ID).resolveTemplate("electionEventId", ELECTION_EVENT_ID).request().get();
		int status = serviceResponse.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
		Assert.assertEquals(bbIsAvailable.toString().length(), serviceResponse.getLength());
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

		environmentVariables.set("ELECTION_INFORMATION_CONTEXT_URL", "localhost");

		MockitoAnnotations.initMocks(this);

		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(servletRequest).to(HttpServletRequest.class);
			}
		};
		sut = new BallotBoxResource(electionInformationAdminClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}
}
