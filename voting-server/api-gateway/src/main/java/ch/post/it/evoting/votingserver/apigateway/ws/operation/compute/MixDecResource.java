/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.compute;

import java.io.IOException;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.controlcomponents.OrchestratorClient;
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
 * Web service to handle the mixing and decrypting in the control components.
 */
@Stateless(name = "ag-MixDecResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(MixDecResource.RESOURCE_PATH)
public class MixDecResource {

	static final String RESOURCE_PATH = "/or/mixdec";
	static final String BASE_PATH = "tenant/{tenantId}/electionevent/{electionEventId}";
	static final String PATH_BALLOT_BOXES = BASE_PATH + "/ballotboxes";
	static final String PATH_BALLOT_BOX = BASE_PATH + "/ballotbox/{ballotBoxId}";
	static final String PATH_STATUS = PATH_BALLOT_BOX + "/status";
	static final String PATH_MIXNET_INITIAL_PAYLOADS = PATH_BALLOT_BOX + "/mixnetInitialPayloads";
	static final String PATH_MIXNET_SHUFFLE_PAYLOADS = PATH_BALLOT_BOX + "/mixnetShufflePayloads";
	static final String PATH_PARAMETER_TENANT_ID = "tenantId";
	static final String PATH_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	static final String PATH_PARAMETER_BALLOT_BOX_ID = "ballotBoxId";
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();
	static final String MIX_DEC_PATH = PROPERTIES.getPropertyValue("MIX_DEC_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	@Inject
	private OrchestratorClient orchestratorClient;

	@Inject
	private TrackIdGenerator trackIdGenerator;

	@POST
	@Path(PATH_BALLOT_BOXES)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response processBallotBoxes(
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final List<String> ballotBoxIds,
			@HeaderParam(RestClientInterceptor.HEADER_ORIGINATOR)
			final String originator,
			@HeaderParam(RestClientInterceptor.HEADER_SIGNATURE)
			final String signature,
			@Context
			final HttpServletRequest request) {

		LOGGER.info("Received ballot boxes to mix: {}.", ballotBoxIds);

		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		ResponseBody response;
		try {
			response = RetrofitConsumer.processResponse(orchestratorClient
					.processBallotBoxes(MIX_DEC_PATH, tenantId, electionEventId, ballotBoxIds, originator, signature, xForwardedFor, trackingId));

			return Response.ok().entity(response.byteStream()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to process ballot boxes.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	@GET
	@Path(PATH_STATUS)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response getBallotBoxStatus(
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PATH_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@HeaderParam(RestClientInterceptor.HEADER_ORIGINATOR)
			final String originator,
			@HeaderParam(RestClientInterceptor.HEADER_SIGNATURE)
			final String signature,
			@Context
			final HttpServletRequest request) {

		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		JsonObject response = null;
		try {
			response = RetrofitConsumer.processResponse(orchestratorClient
					.getBallotBoxStatus(MIX_DEC_PATH, tenantId, electionEventId, ballotBoxId, originator, signature, xForwardedFor, trackingId));

			return Response.ok().entity(response.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get ballot box status.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Gets the payloads of the mixing control component nodes for a particular ballot box.
	 *
	 * @param tenantId        the tenant that owns the ballot box
	 * @param electionEventId the election event the ballot box belongs to
	 * @param ballotBoxId     the identifier of the ballot box
	 * @param originator
	 * @param signature
	 * @param request
	 * @return a JSON with the payloads
	 * @throws IOException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(PATH_MIXNET_INITIAL_PAYLOADS)
	public Response getMixnetInitialPayloads(
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PATH_PARAMETER_BALLOT_BOX_ID)
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
			ResponseBody response = RetrofitConsumer.processResponse(orchestratorClient
					.getMixnetInitialPayloads(MIX_DEC_PATH, tenantId, electionEventId, ballotBoxId, originator, signature, xForwardedFor,
							trackingId));

			return Response.ok().entity(response.byteStream()).build();

		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get mixnet inputs.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	/**
	 * Gets the payloads of the mixing control component nodes for a particular ballot box.
	 *
	 * @param tenantId        the tenant that owns the ballot box
	 * @param electionEventId the election event the ballot box belongs to
	 * @param ballotBoxId     the identifier of the ballot box
	 * @param originator
	 * @param signature
	 * @param request
	 * @return a JSON with the payloads
	 * @throws IOException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(PATH_MIXNET_SHUFFLE_PAYLOADS)
	public Response getMixnetShufflePayloads(
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PATH_PARAMETER_BALLOT_BOX_ID)
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
			ResponseBody response = RetrofitConsumer.processResponse(orchestratorClient
					.getMixnetShufflePayloads(MIX_DEC_PATH, tenantId, electionEventId, ballotBoxId, originator, signature, xForwardedFor,
							trackingId));

			return Response.ok().entity(response.byteStream()).build();

		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get node outputs.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
