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

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.AuthenticationAdminClient;
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
 * Web service for handling election event resources of the authentication context (AU)
 */
@Stateless(name = "ag-au-ElectionEventDataResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(ElectionEventDataAUResource.RESOURCE_PATH)
public class ElectionEventDataAUResource {

	static final String RESOURCE_PATH = "/au/electioneventdata";
	private static final String PATH_TO_ELECTION_EVENT = "tenant/{tenantId}/electionevent/{electionEventId}";
	static final String SAVE_ELECTION_EVENT_DATA = PATH_TO_ELECTION_EVENT + "/adminboard/{adminBoardId}";
	static final String CHECK_IF_ELECTION_EVENT_DATA_IS_EMPTY = PATH_TO_ELECTION_EVENT + "/status";
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String ELECION_EVENT_DATA_PATH = PROPERTIES.getPropertyValue("ELECION_EVENT_DATA_PATH");

	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventDataAUResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private AuthenticationAdminClient authenticationAdminClient;
	private TrackIdGenerator trackIdGenerator;

	@Inject
	ElectionEventDataAUResource(AuthenticationAdminClient authenticationAdminClient, TrackIdGenerator trackIdGenerator) {
		this.authenticationAdminClient = authenticationAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@POST
	@Path(SAVE_ELECTION_EVENT_DATA)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveElectionEventData(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream data,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, data);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(authenticationAdminClient
				.saveElectionEventData(ELECION_EVENT_DATA_PATH, tenantId, electionEventId, adminBoardId, xForwardedFor, trackingId, body))) {
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to save election event data.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Validates if the election event data for the Authentication context (AU) is empty.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	@Path(CHECK_IF_ELECTION_EVENT_DATA_IS_EMPTY)
	public Response checkIfElectionEventDataIsEmpty(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(authenticationAdminClient
					.checkIfElectionEventDataIsEmpty(ELECION_EVENT_DATA_PATH, tenantId, electionEventId, xForwardedFor, trackingId));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to check if election event data is empty.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
