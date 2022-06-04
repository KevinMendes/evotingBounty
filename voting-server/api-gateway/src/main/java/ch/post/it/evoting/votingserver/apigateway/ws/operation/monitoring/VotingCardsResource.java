/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.monitoring;

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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.VotingWorkflowVotingClient;
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

/**
 * Web service which will handle the process of voting and storing a ballot. For now this class is implemented only for testing purposes.
 */
@Stateless(name = "ag-VotingCardsResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(VotingCardsResource.RESOURCE_PATH)
public class VotingCardsResource {

	static final String GET_STATUS_OF_VOTING_CARDS = "/states";

	static final String GET_ID_AND_STATE_OF_INACTIVE_VOTING_CARDS = "/states/inactive";

	static final String BLOCK_VOTING_CARDS = "/block";

	static final String RESOURCE_PATH = RestApplication.API_OV_MONITORING_BASEURI + "/votingcards";

	static final String INACTIVE_VOTING_CARDS_FILENAME = "Inactive-Voting-Cards-Filename";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String VOTE_PATH = PROPERTIES.getPropertyValue("VOTE_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardsResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final VotingWorkflowVotingClient votingWorkflowVotingClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	VotingCardsResource(VotingWorkflowVotingClient votingWorkflowVotingClient, TrackIdGenerator trackIdGenerator) {
		this.votingWorkflowVotingClient = votingWorkflowVotingClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@Path(GET_STATUS_OF_VOTING_CARDS)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response getStatusOfVotingCards(
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final InputStream cards,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		String signature = request.getHeader(RestClientInterceptor.HEADER_SIGNATURE);
		String originator = request.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);
		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, cards);

		try {
			JsonArray processResponse = RetrofitConsumer.processResponse(votingWorkflowVotingClient
					.getStatusOfVotingCards(VOTE_PATH, tenantId, electionEventId, xForwardedFor, trackingId, originator, signature, body));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get status of voting cards.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}

	@Path(GET_ID_AND_STATE_OF_INACTIVE_VOTING_CARDS)
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getIdAndStateOfInactiveVotingCards(
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Context
			final HttpServletRequest request) throws IOException {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String signature = request.getHeader(RestClientInterceptor.HEADER_SIGNATURE);
		String originator = request.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);
		String trackingId = trackIdGenerator.generate();
		try {
			retrofit2.Response<ResponseBody> processResponse = RetrofitConsumer.executeCall(votingWorkflowVotingClient
					.getIdAndStateOfInactiveVotingCards(VOTE_PATH, tenantId, electionEventId, trackingId, xForwardedFor, originator, signature));
			return handleFileResponse(processResponse);
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get Id and state of inactive voting cards.", rfE);
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
			if (INACTIVE_VOTING_CARDS_FILENAME.equals(header)) {
				builder.header(header, headers.get(header));
			}
		}

		return builder.entity(byteStream).build();
	}

	@Path(BLOCK_VOTING_CARDS)
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response blockVotingCards(
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final InputStream cards,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		String signature = request.getHeader(RestClientInterceptor.HEADER_SIGNATURE);
		String originator = request.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);
		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, cards);

		try {
			JsonArray processResponse = RetrofitConsumer.processResponse(votingWorkflowVotingClient
					.blockVotingCards(VOTE_PATH, tenantId, electionEventId, xForwardedFor, trackingId, originator, signature, body));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to block voting cards.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
