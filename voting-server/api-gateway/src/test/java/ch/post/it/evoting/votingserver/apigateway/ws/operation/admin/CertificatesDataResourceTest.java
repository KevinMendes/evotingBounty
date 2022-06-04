/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

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

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.CertificateServiceClient;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class CertificatesDataResourceTest extends JerseyTest {

	private static final String ELECTION_EVENT_ID = "1e";
	private static final String TRACK_ID = "trackId";
	private static final String TENANT_ID = "100t";
	private static final String X_FORWARDER_VALUE = ",";
	private static final String SIGNATURE = "signature";
	private static final String ORIGINATOR = "originator";
	private static final String URL_SELF_CERT_CONTEXT_DATA = CertificatesDataResource.RESOURCE_PATH;
	private static final String BASE_PATH = CertificatesDataResource.RESOURCE_PATH + "/";
	private static final String URL_ELECTION_CERT_CONTEXT_DATA = BASE_PATH + CertificatesDataResource.SAVE_CERTIFICATE_FOR_ELECTION_EVENT;
	private static final String URL_TENANT_CERT_CONTEXT_DATA = BASE_PATH + CertificatesDataResource.SAVE_CERTIFICATE_FOR_TENANT;
	private static final String URL_GET_SELF_CERT_CONTEXT_DATA = BASE_PATH + CertificatesDataResource.GET_CERTIFICATE;
	private static final String URL_GET_TENANT_CERT_CONTEXT_DATA = BASE_PATH + CertificatesDataResource.GET_CERTIFICATE_FOR_TENANT;
	private static final String URL_CHECK_TENANT_CERT_CONTEXT_DATA = BASE_PATH + CertificatesDataResource.CHECK_IF_CERTIFICATE_EXIST;
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
	CertificateServiceClient certificateServiceClient;

	CertificatesDataResource sut;

	@Test
	public void saveCertificateForElection() throws IOException {

		final int mockedInvocationStatus = 200;

		commonPreparation();

		@SuppressWarnings("unchecked")
		final Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(certificateServiceClient
				.saveCertificate(eq(CertificatesDataResource.CERTIFICATES_DATA_PATH), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(X_FORWARDER_VALUE),
						eq(TRACK_ID), eq(ORIGINATOR), eq(SIGNATURE), any())).thenReturn(callMock);
		final int status = target(URL_ELECTION_CERT_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID)
				.resolveTemplate("electionEventId", ELECTION_EVENT_ID).request().header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR)
				.header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE)
				.post(Entity.entity(new ArrayList<>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void saveCertificateForTenant() throws IOException {

		final int mockedInvocationStatus = 200;

		commonPreparation();

		@SuppressWarnings("unchecked")
		final Call<ResponseBody> callMock = (Call<ResponseBody>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(certificateServiceClient
				.saveCertificate(eq(CertificatesDataResource.CERTIFICATES_DATA_PATH), eq(TENANT_ID), eq(X_FORWARDER_VALUE), eq(TRACK_ID),
						eq(ORIGINATOR), eq(SIGNATURE), any())).thenReturn(callMock);

		final int status = target(URL_TENANT_CERT_CONTEXT_DATA).resolveTemplate("tenantId", TENANT_ID).request()
				.header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR).header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE)
				.post(Entity.entity(new ArrayList<>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void saveCertificateForService() throws IOException {

		final int mockedInvocationStatus = 200;

		commonPreparation();

		@SuppressWarnings("unchecked")
		final Call<JsonObject> callMock = (Call<JsonObject>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(new JsonObject()));

		when(certificateServiceClient
				.saveCertificate(eq(CertificatesDataResource.CERTIFICATES_DATA_PATH), eq(X_FORWARDER_VALUE), eq(TRACK_ID), eq(ORIGINATOR),
						eq(SIGNATURE), any())).thenReturn(callMock);

		final int status = target(URL_SELF_CERT_CONTEXT_DATA).request().header(RestClientInterceptor.HEADER_ORIGINATOR, ORIGINATOR)
				.header(RestClientInterceptor.HEADER_SIGNATURE, SIGNATURE)
				.post(Entity.entity(new ArrayList<>(), MediaType.APPLICATION_JSON_TYPE)).getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void getCertificateForService() throws IOException {

		final int mockedInvocationStatus = 200;

		commonPreparation();

		@SuppressWarnings("unchecked")
		final Call<JsonObject> callMock = (Call<JsonObject>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(new JsonObject()));

		final String certificateName = "testCertificate";
		when(certificateServiceClient.getCertificate(CertificatesDataResource.CERTIFICATES_DATA_PATH, certificateName, X_FORWARDER_VALUE,
				TRACK_ID)).thenReturn(callMock);

		final int status = target(URL_GET_SELF_CERT_CONTEXT_DATA).resolveTemplate("certificateName", certificateName).request().get().getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void getCertificateForTenant() throws IOException {

		final int mockedInvocationStatus = 200;

		commonPreparation();

		@SuppressWarnings("unchecked")
		final Call<JsonObject> callMock = (Call<JsonObject>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(new JsonObject()));

		final String certificateName = "testCertificate";
		when(certificateServiceClient.getCertificate(CertificatesDataResource.CERTIFICATES_DATA_PATH, TENANT_ID, certificateName, X_FORWARDER_VALUE,
				TRACK_ID)).thenReturn(callMock);

		final int status = target(URL_GET_TENANT_CERT_CONTEXT_DATA).resolveTemplate("certificateName", certificateName)
				.resolveTemplate("tenantId", TENANT_ID).request().get().getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	@Test
	public void checkIfCertificateExist() throws IOException {

		final int mockedInvocationStatus = 200;

		commonPreparation();

		@SuppressWarnings("unchecked")
		final Call<JsonObject> callMock = (Call<JsonObject>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(new JsonObject()));

		final String certificateName = "testCertificate";
		when(certificateServiceClient.checkIfCertificateExists(CertificatesDataResource.CERTIFICATES_DATA_PATH, TENANT_ID, certificateName,
				X_FORWARDER_VALUE, TRACK_ID)).thenReturn(callMock);

		final int status = target(URL_CHECK_TENANT_CERT_CONTEXT_DATA).resolveTemplate("certificateName", certificateName)
				.resolveTemplate("tenantId", TENANT_ID).request().get().getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);
	}

	private void commonPreparation() {
		when(servletRequest.getHeader(RestClientInterceptor.HEADER_ORIGINATOR)).thenReturn(ORIGINATOR);
		when(servletRequest.getHeader(RestClientInterceptor.HEADER_SIGNATURE)).thenReturn(SIGNATURE);
		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
		when(trackIdGenerator.generate()).thenReturn(TRACK_ID);
	}

	@Override
	protected Application configure() {

		environmentVariables.set("CERTIFICATES_CONTEXT_URL", "localhost");
		// init the mocks before instantiating the SUT. We cannot have this and MockitoJrunner,
		// otherwise the mocks will
		// not be the ones that are used in the SUT constructor
		MockitoAnnotations.openMocks(this);

		final AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(servletRequest).to(HttpServletRequest.class);
			}
		};
		sut = new CertificatesDataResource(certificateServiceClient, trackIdGenerator);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut).register(binder);
	}
}
