/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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

import com.google.gson.Gson;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenService;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastCodeRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.validation.ValidationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.VoteCastCodeService;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.VotingCardStateService;

/**
 * The end point for getting Cast Codes.
 */
@Path("/castcodes")
@Stateless(name = "vw-CastCodeResource")
public class CastCodeResource {

	// The name of the parameter value tenant id.
	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	// The name of the parameter value election event id.
	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	// The name of the parameter value voting card id.
	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	// The name of the parameter value authentication token.
	private static final String PARAMETER_AUTHENTICATION_TOKEN = "authenticationToken";

	private static final Logger LOGGER = LoggerFactory.getLogger(CastCodeResource.class);

	// The cast code repository
	@EJB
	private VoteCastCodeRepository voteCastCodeRepository;

	// The voting card state service
	@EJB
	private VotingCardStateService votingCardStateService;

	// The validation repository
	@EJB
	private ValidationRepository validationRepository;

	@Inject
	private VoteCastCodeService voteCastCodeService;

	// Tracking id used for logging and tracking purposes
	@Inject
	private TrackIdInstance trackIdInstance;

	// The authentication token repository
	@Inject
	private AuthenticationTokenService authenticationTokenService;

	/**
	 * Gets the generate cast codes which matches with the given parameters and voting card state in
	 * CAST.
	 *
	 * @param tenantId                      - the tenant identifier.
	 * @param electionEventId               - the election event identifier.
	 * @param votingCardId                  - the voting card Identifier.
	 * @param authenticationTokenJsonString - the authentication token in json format.
	 * @param request                       - the http servlet request.
	 * @return if the operation is successfully performed, the generated choice codes
	 * @throws ApplicationException      if parameter validation fails
	 * @throws ResourceNotFoundException if the voter information is not found
	 */
	@GET
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCastCodeMessage(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@NotNull
			@HeaderParam(PARAMETER_AUTHENTICATION_TOKEN)
					String authenticationTokenJsonString,
			@Context
					HttpServletRequest request) throws ApplicationException, ResourceNotFoundException, IOException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, votingCardId);

		AuthenticationToken authenticationToken;
		ValidationResult authTokenValidationResult;
		try {
			// convert from json to object
			authenticationToken = DomainObjectMapper.getNewInstance().readValue(authenticationTokenJsonString, AuthenticationToken.class);

			// validate auth token
			ValidationUtils.validate(authenticationToken);
			// returns the result of authentication token validation
			authTokenValidationResult = authenticationTokenService
					.validateAuthenticationToken(tenantId, electionEventId, votingCardId, authenticationTokenJsonString);
		} catch (IOException | IllegalArgumentException | ValidationException e) {
			LOGGER.error("Invalid authentication token format.", e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		// result of operation
		ValidationVoteResult result = new ValidationVoteResult();

		VotingCardState votingCardState = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);

		// don't allow progressing if the auth token is invalid with an error code that IS NOT "election
		// over"
		if (!authTokenValidationResult.isResult() && !ValidationErrorType.ELECTION_OVER_DATE
				.equals(authTokenValidationResult.getValidationError().getValidationErrorType())) {
			// no need to continue if auth token is invalid
			return getResponseInvalidToken(authTokenValidationResult);
		}

		// Check that the voting card was used to already confirm a vote
		if (VotingCardStates.CAST.equals(votingCardState.getState())) {

			// validate
			ValidationResult validationResult = validationRepository
					.validateElectionDatesInEI(tenantId, electionEventId, authenticationToken.getVoterInformation().getBallotBoxId());

			if (!validationResult.isResult()) {
				ValidationErrorType validationErrorType = validationResult.getValidationError().getValidationErrorType();

				if (ValidationErrorType.ELECTION_NOT_STARTED.equals(validationErrorType)) {
					return Response.ok().entity(validationResult).build();
				} else if (ValidationErrorType.ELECTION_OVER_DATE.equals(validationErrorType)) {
					return Response.ok().entity(validationResult).build();
				} else {
					LOGGER.error("Check of voting card states failed with unknown error type: {}", validationErrorType);
					return Response.ok().entity(validationResult).build();
				}
			}

			ShortVoteCastReturnCodeAndComputeResults voteCastMessage = voteCastCodeRepository.getCastCode(tenantId, electionEventId, votingCardId);

			VoteCastResult voteCastResult = voteCastCodeService
					.generateVoteCastResult(electionEventId, votingCardId, authenticationToken.getVoterInformation().getVerificationCardId(),
							voteCastMessage);

			return Response.ok().entity(voteCastResult).build();
		}

		// Returns authentication token
		return Response.ok().entity(result).build();
	}

	// Does a basic validation of the input. In case something is wrong, just throws an exception.
	private void validateInput(String tenantId, String electionEventId, String votingCardId) throws ApplicationException {
		if (tenantId == null || "".equals(tenantId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (electionEventId == null || "".equals(electionEventId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL);
		}
		if (votingCardId == null || "".equals(votingCardId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_VOTING_CARD_ID_IS_NULL);
		}
	}

	/**
	 * Constructs the response given a invalid validation result
	 *
	 * @param validationResult
	 * @return
	 */
	private Response getResponseInvalidToken(ValidationResult validationResult) {

		Gson gson = new Gson();
		String json = gson.toJson(validationResult);
		return Response.status(Response.Status.UNAUTHORIZED).entity(json).build();

	}

}
