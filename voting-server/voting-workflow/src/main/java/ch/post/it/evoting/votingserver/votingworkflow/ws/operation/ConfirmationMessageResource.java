/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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

import com.google.gson.Gson;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenService;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.CastVoteService;

/**
 * The endpoint for confirmation messages
 */
@Path("/confirmations")
@Stateless(name = "vw-ConfirmationMessageResource")
public class ConfirmationMessageResource {

	// The authentication token parameter
	private static final String AUTHENTICATION_TOKEN = "authenticationToken";

	// The name of the parameter value tenant id.
	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	// The name of the parameter value voting card id.
	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	// The name of the parameter value election event id.
	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private AuthenticationTokenService authenticationTokenService;

	@Inject
	private Logger logger;

	@Inject
	private CastVoteService castVoteService;

	/**
	 * Receives a confirmation message with additional information , and validates this information.
	 *
	 * @param tenantId                      - Tenant identifier
	 * @param electionEventId               - election event identifier
	 * @param votingCardId                  - voting card identifier
	 * @param confirmationInformation       - confirmation information to be validated
	 * @param authenticationTokenJsonString - the authentication token
	 * @param request                       - the http servlet request.
	 * @return the result of the validation
	 * @throws ApplicationException      if there are validations errors in input parameters.
	 * @throws ResourceNotFoundException if ballot if not found.
	 * @throws SemanticErrorException    if the authentication token or vote has semantic errors.
	 * @throws SyntaxErrorException      if the authentication token or vote has syntax errors.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	public Response validateConfirmationMessage(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId, ConfirmationInformation confirmationInformation,
			@HeaderParam(AUTHENTICATION_TOKEN)
					String authenticationTokenJsonString,
			@Context
					HttpServletRequest request) throws ResourceNotFoundException, ApplicationException {

		// Tracking id used for logging and tracking purposes
		trackIdInstance.setTrackId(trackingId);

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
		} catch (IOException | IllegalArgumentException | SyntaxErrorException | SemanticErrorException e) {
			logger.error("Invalid authentication token format.", e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		// no need to continue if auth token is invalid
		if (!authTokenValidationResult.isResult()) {
			return getResponseInvalidToken(authTokenValidationResult);
		}

		// result of process
		VoteCastResult voteCastResult = castVoteService
				.castVote(authenticationTokenJsonString, authenticationToken.getVoterInformation(), confirmationInformation);
		return Response.ok().entity(voteCastResult).build();
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
