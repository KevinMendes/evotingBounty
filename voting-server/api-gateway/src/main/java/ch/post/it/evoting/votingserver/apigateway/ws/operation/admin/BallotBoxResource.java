/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

import okhttp3.ResponseBody;

/**
 * Web service for handling ballot box information.
 */
@Stateless(name = "ag-BallotBoxResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(BallotBoxResource.RESOURCE_PATH)
public class BallotBoxResource {

	static final String RESOURCE_PATH = "/ei/ballotboxes";
	private static final String BALLOT_BOX_URL = "tenant/{tenantId}/electionevent/{electionEventId}";
	static final String GET_ENCRYPTED_BALLOT_BOX_CSV = BALLOT_BOX_URL + "/ballotbox/{ballotBoxId}";
	static final String CHECK_IF_BALLOT_BOX_IS_EMPTY = BALLOT_BOX_URL + "/ballotbox/{ballotBoxId}/status";
	static final String CHECK_IF_BALLOT_BOX_IS_AVAILABLE = BALLOT_BOX_URL + "/ballotbox/{ballotBoxId}/available";
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_BALLOT_BOX_ID = "ballotBoxId";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String BALLOT_BOX_PATH = PROPERTIES.getPropertyValue("BALLOT_BOX_PATH");

	static final String BALLOT_BOX_STATUS_PATH = PROPERTIES.getPropertyValue("BALLOT_BOX_STATUS_PATH");

	private static final String BALLOT_BOX_AVAILABILITY_STATUS_PATH = PROPERTIES.getPropertyValue("BALLOT_BOX_AVAILABILITY_STATUS_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final ElectionInformationAdminClient electionInformationAdminClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	BallotBoxResource(ElectionInformationAdminClient electionInformationAdminClient, TrackIdGenerator trackIdGenerator) {
		this.electionInformationAdminClient = electionInformationAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path(GET_ENCRYPTED_BALLOT_BOX_CSV)
	public Response getEncryptedBallotBox(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@HeaderParam(RestClientInterceptor.HEADER_ORIGINATOR)
			final String originator,
			@HeaderParam(RestClientInterceptor.HEADER_SIGNATURE)
			final String signature,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		try {
			ResponseBody response = RetrofitConsumer.processResponse(electionInformationAdminClient
					.getEncryptedBallotBox(BALLOT_BOX_PATH, tenantId, electionEventId, ballotBoxId, xForwardedFor, trackingId, originator,
							signature));

			return Response.ok().entity(response.byteStream()).build();

		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get encrypted ballot box.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Validate if a ballot box is empty.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotBoxId     - the ballot box identifier.
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	@Path(CHECK_IF_BALLOT_BOX_IS_EMPTY)
	public Response checkIfBallotBoxIsEmpty(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(electionInformationAdminClient
					.checkIfBallotBoxIsEmpty(BALLOT_BOX_STATUS_PATH, tenantId, electionEventId, ballotBoxId, xForwardedFor, trackingId));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to check if ballot box is empty.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}

	}

	/**
	 * Validate if a ballot box is available.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotBoxId     - the ballot box identifier.
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	@Path(CHECK_IF_BALLOT_BOX_IS_AVAILABLE)
	public Response checkIfBallotBoxIsAvailable(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();
		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(electionInformationAdminClient
					.checkIfBallotBoxIsAvailable(BALLOT_BOX_AVAILABILITY_STATUS_PATH, tenantId, electionEventId, ballotBoxId, xForwardedFor,
							trackingId));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to check if ballot box is available.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
