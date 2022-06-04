/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.voting;

import java.io.InputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.VotingWorkflowVotingClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.InputStreamTypedOutput;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

import okhttp3.RequestBody;

/**
 * Web service which will handle the process of voting and storing a ballot. For now this class is implemented only for testing purposes.
 */
@Stateless(name = "ag-VoteResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(VoteResource.RESOURCE_PATH)
public class VoteResource {

	static final String VALIDATE_VOTE_AND_STORE = "/vote";

	static final String RESOURCE_PATH = RestApplication.API_OV_VOTING_BASEURI;

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String VOTE_PATH = PROPERTIES.getPropertyValue("VOTE_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final VotingWorkflowVotingClient votingWorkflowVotingClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	VoteResource(VotingWorkflowVotingClient votingWorkflowVotingClient, TrackIdGenerator trackIdGenerator) {
		this.votingWorkflowVotingClient = votingWorkflowVotingClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@Path(VALIDATE_VOTE_AND_STORE)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response validateVoteAndStore(
			@PathParam(RestApplication.PARAMETER_VALUE_VERSION)
			final String version,
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(RestApplication.PARAMETER_VALUE_VOTING_CARD_ID)
			final String votingCardId,
			@NotNull
			@HeaderParam(RestApplication.PARAMETER_AUTHENTICATION_TOKEN)
			final String authenticationTokenJsonString,
			@NotNull
			final InputStream vote,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, vote);

		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(votingWorkflowVotingClient
					.validateVoteAndStore(VOTE_PATH, tenantId, electionEventId, votingCardId, authenticationTokenJsonString, xForwardedFor,
							trackingId, body));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to validate vote and store.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
