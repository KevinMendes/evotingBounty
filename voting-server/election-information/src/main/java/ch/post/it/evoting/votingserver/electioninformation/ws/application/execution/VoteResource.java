/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.execution;

import java.io.IOException;
import java.io.Reader;

import javax.ejb.Stateless;
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
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.Message;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote.VoteService;

/**
 * Web Service which receives a vote already validated and tries to store it in the ballot box.
 */
@Path("/votes")
@Stateless
public class VoteResource {

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String PARAMETER_AUTHENTICATION_TOKEN = "authenticationToken";
	private static final Logger LOGGER = LoggerFactory.getLogger(VoteResource.class);
	private final Gson gson = new Gson();
	@Inject
	private TrackIdInstance trackIdInstance;
	@Inject
	private VoteService voteService;
	@Inject
	private BallotBoxRepository ballotBoxRepository;

	/**
	 * Saves the content of a vote
	 *
	 * @param trackingId          - the track id to be used for logging purposes.
	 * @param electionEventId     - election event identifier
	 * @param tenantId            - tenant identifier
	 * @param voteInput           - the vote to be stored.
	 * @param authenticationToken - the authentication token.
	 * @param request             - the http servlet request.
	 * @return the result of saving the vote.
	 * @throws IOException         if there are errors during conversion of vote to json format.
	 * @throws ValidationException if vote is not valid.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}")
	public Response saveVote(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@NotNull
					Reader voteInput,
			@NotNull
			@HeaderParam(PARAMETER_AUTHENTICATION_TOKEN)
					String authenticationToken,
			@Context
					HttpServletRequest request) throws IOException, ValidationException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		// transform the json to object
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		final VoteAndComputeResults vote = objectMapper.readValue(voteInput, VoteAndComputeResults.class);

		try {

			voteService.saveVote(vote, authenticationToken);
			return Response.ok().build();

		} catch (DuplicateEntryException e) {
			LOGGER.error("Error storing the vote for tenant: {} and ballot: {} into ballotBox: {}.", vote.getVote().getTenantId(),
					vote.getVote().getBallotId(), vote.getVote().getBallotBoxId(), e);
			String errorCode = ErrorCodes.DUPLICATE_ENTRY;
			Message message = new Message();
			message.setText("Duplicated vote entry");
			message.addError("", "", errorCode);
			return Response.status(Status.CONFLICT).entity(message).build();
		}

	}

	/**
	 * Returns a Vote object which matches with the provided parameters.
	 *
	 * @param trackingId      - the track id to be used for logging purposes.
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardId    - the voting card identifier.
	 * @param request         - the http servlet request.
	 * @return If the operation is successfully performed, returns a response with HTTP status code
	 * 200 and the Vote in json format.
	 * @throws ApplicationException      if one of the input parameters is not valid.
	 * @throws ResourceNotFoundException if there is no vote found.
	 */
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVote(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Context
					HttpServletRequest request) throws ApplicationException, ResourceNotFoundException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, votingCardId);

		// return json
		VoteAndComputeResults voteWithComputeResults = voteService.retrieveVote(tenantId, electionEventId, votingCardId);

		// response
		return Response.ok(voteWithComputeResults).build();
	}

	/**
	 * Checks is a vote exists for the specified parameters
	 *
	 * @param trackingId      - the track id to be used for logging purposes.
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardId    - the voting card identifier.
	 * @param request         - the http servlet request.
	 * @return 200 if the votes exists, 404 if not
	 * @throws ApplicationException if one of the input parameters is not valid.
	 */
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/available")
	@GET
	public Response checkVote(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Context
					HttpServletRequest request) throws ApplicationException {
		try {
			getVote(trackingId, tenantId, electionEventId, votingCardId, request);
			return Response.ok().build();
		} catch (ResourceNotFoundException e) {
			LOGGER.info("Resource not found trying to check vote.", e);
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	private void validateInput(String tenantId, String electionEventId, String votingCardId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL);
		}
		if (votingCardId == null || votingCardId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_VOTING_CARD_ID_IS_NULL);
		}
	}

}
