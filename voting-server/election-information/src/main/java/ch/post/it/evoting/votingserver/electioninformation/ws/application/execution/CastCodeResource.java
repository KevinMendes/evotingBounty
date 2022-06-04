/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.execution;

import java.io.IOException;
import java.util.Base64;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode.VoteCastCode;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode.VoteCastCodeRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote.VoteCastCodeService;

/**
 * The endpoint for cast codes.
 */
@Path(CastCodeResource.RESOURCE_PATH)
@Stateless(name = "ei-CastCodeResource")
public class CastCodeResource {

	static final String RESOURCE_PATH = "/castcodes";

	static final String STORE_CAST_CODE_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}";

	static final String GET_CAST_CODE_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}";

	static final String CHECK_CAST_CODE_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/available";

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";
	private static final Logger LOGGER = LoggerFactory.getLogger(CastCodeResource.class);
	private final Gson gson = new Gson();
	@Inject
	private VoteCastCodeRepository voteCastCodeRepository;
	@Inject
	private TrackIdInstance trackIdInstance;
	@Inject
	private VoteCastCodeService voteCastCodeService;

	/**
	 * Stores the vote cast codes in the repository.
	 *
	 * @param trackingId      - the track id to be used for logging purposes.
	 * @param tenantId        - Tenant identifier
	 * @param electionEventId - election event identifier
	 * @param votingCardId    - voting card identifier
	 * @param voteCastCode    - the vote cast code.
	 * @param request         - the http servlet request.
	 * @return the result of the validation
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(STORE_CAST_CODE_PATH)
	public Response storeCastCode(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@NotNull
					ShortVoteCastReturnCodeAndComputeResults voteCastCode,
			@Context
					HttpServletRequest request) {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		ValidationResult result = new ValidationResult();
		result.setResult(true);

		try {
			voteCastCodeService.save(tenantId, electionEventId, votingCardId, voteCastCode);
		} catch (ApplicationException | IOException e) {
			LOGGER.error("Error saving vote cast code", e);
			result.setResult(false);
		}

		// convert to string
		String json = gson.toJson(result);

		return Response.ok(json).build();
	}

	/**
	 * Retrieve the vote cast code.
	 *
	 * @param tenantId        - Tenant identifier
	 * @param electionEventId - election event identifier
	 * @param votingCardId    - voting card identifier
	 * @param trackingId      - the track id to be used for logging purposes.
	 * @param request         - the http servlet request.
	 * @return the result of the validation
	 * @throws ResourceNotFoundException if the vote cast code data can not be found.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(GET_CAST_CODE_PATH)
	public Response getVoteCastCode(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Context
					HttpServletRequest request) throws ResourceNotFoundException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		// get the cast code from the repository.
		final VoteCastCode voteCastCode = voteCastCodeRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);

		final ShortVoteCastReturnCodeAndComputeResults shortVoteCastReturnCodeAndComputeResults = new ShortVoteCastReturnCodeAndComputeResults();
		shortVoteCastReturnCodeAndComputeResults.setShortVoteCastReturnCode(voteCastCode.getVoteCastCode());
		shortVoteCastReturnCodeAndComputeResults.setComputationResults(Base64.getEncoder().encodeToString(voteCastCode.getComputationResults()));

		// returns the vote cast code
		return Response.ok(shortVoteCastReturnCodeAndComputeResults).build();
	}

	@GET
	@Path(CHECK_CAST_CODE_PATH)
	public Response checkVoteCastCode(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Context
					HttpServletRequest request) {
		try {
			getVoteCastCode(trackingId, tenantId, electionEventId, votingCardId, request);
			// no exception, we found it. ignore result.
			return Response.ok().build();
		} catch (ResourceNotFoundException e) {
			LOGGER.info("Resource not found checking the vote cast code availability [electionEventId: {}, votingCardId: {}].", electionEventId,
					votingCardId);
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

}
