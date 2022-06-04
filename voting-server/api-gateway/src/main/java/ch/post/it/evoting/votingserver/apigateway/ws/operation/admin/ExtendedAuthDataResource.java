/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import java.io.IOException;
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
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.ExtendedAuthenticationVotingClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.InputStreamTypedOutput;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Resource to upload extended authentication information to the system
 */
@Stateless(name = "ag-UploadExtAuthResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(ExtendedAuthDataResource.RESOURCE_PATH)
public class ExtendedAuthDataResource {

	static final String PUT_EXTENDED_AUTH = "/tenant/{tenantId}/electionevent/{electionEventId}/adminboard/{adminBoardId}";

	static final String RESOURCE_PATH = "/extendedauthentication";

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String PARAMETER_VALUE_ADMIN_BOARD_ID = "adminBoardId";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String EXT_AUTH_PATH = PROPERTIES.getPropertyValue("EXTENDED_AUTHENTICATION_SINGLE_PATH");

	private static final String BLOCKED_EXTENDED_AUTHENTICATION_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/status/blocked";

	private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthDataResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final ExtendedAuthenticationVotingClient client;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	ExtendedAuthDataResource(ExtendedAuthenticationVotingClient extendedAuthenticationVotingClient, TrackIdGenerator trackIdGenerator) {
		this.client = extendedAuthenticationVotingClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@Path(PUT_EXTENDED_AUTH)
	@POST
	@Consumes({ "text/csv" })
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response putExtendedAuth(
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PARAMETER_VALUE_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream auth,
			@Context
			final HttpServletRequest request) {

		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		RequestBody body = new InputStreamTypedOutput("text/csv", auth);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(
				client.saveExtendedAuthentication(EXT_AUTH_PATH, tenantId, electionEventId, adminBoardId, xForwardedFor, trackingId, body))) {
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to put extended authentication.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	@GET
	@Path(BLOCKED_EXTENDED_AUTHENTICATION_PATH)
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN + RestApplication.WITH_CHARSET_UTF_8 })
	public Response downloadBlockedExtendedAuthentications(
			@PathParam(RestApplication.PARAMETER_VALUE_VERSION)
			final String version,
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Context
			final HttpServletRequest request) throws IOException {

		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		try {
			retrofit2.Response<ResponseBody> processResponse = RetrofitConsumer
					.executeCall(client.getBlockedExtendedAuthentications(EXT_AUTH_PATH, tenantId, electionEventId, trackingId, xForwardedFor));
			return handleFileResponse(processResponse);
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to download blocked extended authentication.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	private Response handleFileResponse(retrofit2.Response<ResponseBody> response) {
		ResponseBody body = response.body();
		if (body == null || body.contentLength() == 0) {
			return Response.noContent().build();
		}
		InputStream byteStream = body.byteStream();

		// use the original status of the retrofit Response
		ResponseBuilder builder = Response.status(response.code());

		okhttp3.Headers headers = response.headers();

		for (String header : headers.names()) {
			if (header.equalsIgnoreCase(HEADER_CONTENT_DISPOSITION)) {
				builder.header(header, headers.get(header));
			}
		}

		return builder.entity(byteStream).build();
	}
}
