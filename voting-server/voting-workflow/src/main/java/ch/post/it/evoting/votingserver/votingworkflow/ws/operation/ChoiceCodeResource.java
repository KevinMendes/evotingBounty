/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.EJBException;
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

import com.google.gson.Gson;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteRepositoryException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenService;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.choicecode.ChoiceCodeRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.validation.ValidationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.VotingCardStateService;

/**
 * The end point for getting Choice Codes.
 */
@Path("/choicecodes")
@Stateless(name = "vw-ChoiceCodeResource")
public class ChoiceCodeResource {

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_AUTHENTICATION_TOKEN = "authenticationToken";

	@Inject
	private Logger logger;

	@EJB
	private ChoiceCodeRepository choiceCodeRepository;

	@EJB
	private VotingCardStateService votingCardStateService;

	@EJB
	private VoteRepository voteRepository;

	@EJB
	private ValidationRepository validationRepository;

	@Inject
	private AuthenticationTokenService authenticationTokenService;

	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Gets the generated choice codes which matches with the given parameters and voting card state in SENT_BUT_NOT_CAST.
	 *
	 * @param tenantId                      - the tenant identifier.
	 * @param electionEventId               - the election event identifier.
	 * @param votingCardId                  - the voting card Identifier.
	 * @param authenticationTokenJsonString the authentication token json in string format.
	 * @param request                       - the http servlet request.
	 * @return if the operation is successfully performed, the generated choice codes
	 * @throws ApplicationException      if parameter validation fails
	 * @throws ResourceNotFoundException if the voter information is not found
	 * @throws DuplicateEntryException   if there is an error updating voting card state
	 */
	@GET
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getChoiceCodes(
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
					HttpServletRequest request)
			throws ApplicationException, ResourceNotFoundException, DuplicateEntryException, SemanticErrorException, SyntaxErrorException,
			VoteRepositoryException {

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
			logger.error("Invalid authentication token format.", e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		// result of the operation
		ValidationVoteResult result = new ValidationVoteResult();

		// recover the voting card state
		VotingCardState votingCardState = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);

		// no need to continue if the authentication token is invalid
		if (!authTokenValidationResult.isResult()) {
			return getResponseInvalidToken(authTokenValidationResult);
		}

		// result of the operation
		ValidationResult validationResult = validationRepository
				.validateElectionDatesInEI(tenantId, electionEventId, authenticationToken.getVoterInformation().getBallotBoxId());
		result.setValidationError(validationResult.getValidationError());

		if (!validationResult.isResult()) {
			return Response.ok().entity(validationResult).build();
		}

		// check the voting card state
		if (VotingCardStates.SENT_BUT_NOT_CAST.equals(votingCardState.getState())) {

			// get the encrypted vote
			VoteAndComputeResults voteAndComputeResults = voteRepository
					.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);

			// validate vote
			ValidationUtils.validate(voteAndComputeResults);

			// generate choice codes
			try {
				ShortChoiceReturnCodeAndComputeResults generateChoiceCodes = choiceCodeRepository
						.generateChoiceCodes(tenantId, electionEventId, authenticationToken.getVoterInformation().getVerificationCardId(),
								voteAndComputeResults);
				result.setChoiceCodes(generateChoiceCodes.getShortChoiceReturnCodes());
				result.setValid(true);

			} catch (EJBException | ResourceNotFoundException e) {
				// change status of voting card to CHOICE CODES FAILED
				votingCardStateService.updateVotingCardState(tenantId, electionEventId, votingCardId, VotingCardStates.CHOICE_CODES_FAILED);
				result.setValid(Boolean.FALSE);
				result.setValidationError(new ValidationError(ValidationErrorType.FAILED));
			}
		}

		// Returns the authentication token
		return Response.ok().entity(result).build();
	}

	/**
	 * Validate the input.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @throws ApplicationException the application exception
	 */
	// Does a basic validation of the input. In case something is wrong, just
	// throws an exception.
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
	 * Constructs the response given an invalid validation result.
	 *
	 * @param validationResult the invalid validation result.
	 * @return a response with the status UNAUTHORIZED.
	 */
	private Response getResponseInvalidToken(ValidationResult validationResult) {

		Gson gson = new Gson();
		String json = gson.toJson(validationResult);
		return Response.status(Response.Status.UNAUTHORIZED).entity(json).build();

	}
}
