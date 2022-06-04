/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import java.io.InputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.CertificateServiceClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.InputStreamTypedOutput;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

@Stateless(name = "ag-CertificatesDataResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(CertificatesDataResource.RESOURCE_PATH)
public class CertificatesDataResource {

	static final String SAVE_CERTIFICATE_FOR_ELECTION_EVENT = "tenant/{tenantId}/electionevent/{electionEventId}";

	static final String SAVE_CERTIFICATE_FOR_TENANT = "tenant/{tenantId}/";

	static final String GET_CERTIFICATE = "name/{certificateName}";

	static final String GET_CERTIFICATE_FOR_TENANT = "tenant/{tenantId}/name/{certificateName}";

	static final String CHECK_IF_CERTIFICATE_EXIST = "tenant/{tenantId}/name/{certificateName}/status";

	static final String RESOURCE_PATH = "certificates";

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_CERTIFICATE_NAME = "certificateName";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String CERTIFICATES_DATA_PATH = PROPERTIES.getPropertyValue("CERTIFICATES_DATA_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(CertificatesDataResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final CertificateServiceClient certificateServiceClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	CertificatesDataResource(CertificateServiceClient certificateServiceClient, TrackIdGenerator trackIdGenerator) {
		this.certificateServiceClient = certificateServiceClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	/**
	 * Store a certificate in the registry
	 *
	 * @param tenantId        - tenant identifier
	 * @param electionEventId - election event identifier
	 * @param certificate     - content of the certificate
	 * @return 200OK or exception in case of error
	 */
	@POST
	@Path(SAVE_CERTIFICATE_FOR_ELECTION_EVENT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response saveCertificate(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final InputStream certificate,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		String signature = request.getHeader(RestClientInterceptor.HEADER_SIGNATURE);
		String originator = request.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);

		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, certificate);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(certificateServiceClient
				.saveCertificate(CERTIFICATES_DATA_PATH, tenantId, electionEventId, xForwardedFor, trackingId, originator, signature, body))) {
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to save certificate for election event.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Store a certificate in the registry
	 *
	 * @param tenantId    - tenant identifier
	 * @param certificate - content of the certificate
	 * @return 200OK or exception in case of error
	 */
	@POST
	@Path(SAVE_CERTIFICATE_FOR_TENANT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response saveCertificate(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@NotNull
			final InputStream certificate,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		String signature = request.getHeader(RestClientInterceptor.HEADER_SIGNATURE);
		String originator = request.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);

		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, certificate);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(
				certificateServiceClient.saveCertificate(CERTIFICATES_DATA_PATH, tenantId, xForwardedFor, trackingId, originator, signature, body))) {
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to save certificate for Tenant.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Store a certificate in the registry
	 *
	 * @param certificate - content of the certificate
	 * @return 200OK or exception in case of error
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response saveCertificate(
			@NotNull
			final InputStream certificate,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		String signature = request.getHeader(RestClientInterceptor.HEADER_SIGNATURE);
		String originator = request.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);

		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, certificate);

		try {
			RetrofitConsumer.processResponse(
					certificateServiceClient.saveCertificate(CERTIFICATES_DATA_PATH, xForwardedFor, trackingId, originator, signature, body));
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to save certificate.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Search for a certificate with the given parameters
	 *
	 * @param certificateName - certificate name
	 * @return an object with the certificate data
	 */
	@GET
	@Path(GET_CERTIFICATE)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response getCertificate(
			@PathParam(QUERY_PARAMETER_CERTIFICATE_NAME)
			final String certificateName,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		try {
			JsonObject processResponse = RetrofitConsumer
					.processResponse(certificateServiceClient.getCertificate(CERTIFICATES_DATA_PATH, certificateName, xForwardedFor, trackingId));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get certificate.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Search for a certificate with the given parameters
	 *
	 * @param tenantId        - tenant identifier
	 * @param certificateName - certificate name
	 * @return an object with the certificate data
	 */

	@GET
	@Path(GET_CERTIFICATE_FOR_TENANT)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response getCertificate(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_CERTIFICATE_NAME)
			final String certificateName,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(
					certificateServiceClient.getCertificate(CERTIFICATES_DATA_PATH, tenantId, certificateName, xForwardedFor, trackingId));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get certificate for tenant.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Check if a certificate exists for the given parameters
	 *
	 * @param tenantId - the tenant identifier.
	 * @param name     - the certificate name
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	@Path(CHECK_IF_CERTIFICATE_EXIST)
	public Response checkIfCertificateExist(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_CERTIFICATE_NAME)
			final String name,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(
					certificateServiceClient.checkIfCertificateExists(CERTIFICATES_DATA_PATH, tenantId, name, xForwardedFor, trackingId));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to check if certificate exist.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
