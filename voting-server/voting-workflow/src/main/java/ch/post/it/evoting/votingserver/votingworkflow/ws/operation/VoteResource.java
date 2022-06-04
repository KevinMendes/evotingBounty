/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenService;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCard;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStatusValue;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.VoteService;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.VotingCardStateService;

/**
 * Web service which will handle the process of voting and storing a ballot. For now this class is implemented only for testing purposes.
 */
@Path("/votes")
@Stateless(name = "vw-VoteResource")
public class VoteResource {

	public static final String INACTIVE_VOTING_CARDS_FILENAME = "Inactive-Voting-Cards-Filename";

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String PARAMETER_AUTHENTICATION_TOKEN = "authenticationToken";

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@Inject
	private AuthenticationTokenService authenticationTokenService;

	@Inject
	private VoteService voteService;

	@EJB
	private VotingCardStateService votingCardStateService;

	@EJB
	private VoterInformationRepository voterInformationRepository;

	@Inject
	private Logger logger;

	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Receives a vote in json as parameter, tries to validate and store a vote.
	 *
	 * @param tenantId                      - the tenant identifier.
	 * @param electionEventId               - the election event identifier.
	 * @param votingCardId                  - the voting card identifier.
	 * @param authenticationTokenJsonString - the authentication token in json format received in the header.
	 * @param voteJsonString                The vote json input with all the necessary information.
	 * @param request                       - the http servlet request.
	 * @return Returns an HTTP 200 response if OK, and error code otherwise.
	 * @throws ApplicationException      if there are validation errors in input parameters.
	 * @throws IOException               if there are problems during convention to json format.
	 * @throws ResourceNotFoundException if ballot if not found.
	 * @throws DuplicateEntryException   if the voting card state can not be correctly saved.
	 */
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateVoteAndStore(
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
			@NotNull
					String voteJsonString,
			@Context
					HttpServletRequest request) throws ApplicationException, IOException, ResourceNotFoundException, DuplicateEntryException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, votingCardId);

		AuthenticationToken authenticationToken;
		ValidationResult authTokenValidationResult;
		try {
			// convert from json to object
			authenticationToken = objectMapper.readValue(authenticationTokenJsonString, AuthenticationToken.class);

			// validate auth token
			ValidationUtils.validate(authenticationToken);
			// returns the result of authentication token validation
			authTokenValidationResult = authenticationTokenService
					.validateAuthenticationToken(tenantId, electionEventId, votingCardId, authenticationTokenJsonString);
		} catch (IOException | IllegalArgumentException | ValidationException e) {
			final String error = "Invalid authentication token format.";
			logger.error(error, e);

			ValidationResult validationResult = new ValidationResult(false);
			ValidationError validationError = new ValidationError(ValidationErrorType.FAILED);
			validationError.setErrorArgs(new String[] { error });
			validationResult.setValidationError(validationError);
			return Response.status(Response.Status.UNAUTHORIZED).entity(validationError).build();
		}

		// no need to continue if auth token is invalid
		if (!authTokenValidationResult.isResult()) {
			return getResponseInvalidToken(authTokenValidationResult);
		}

		// we have validated the authentication token, so let's proceed with
		// validating the Vote

		Vote vote;
		try {
			// validate vote
			vote = obtainVote(authenticationTokenJsonString, voteJsonString);
			vote.setTenantId(authenticationToken.getVoterInformation().getTenantId());
			vote.setElectionEventId(authenticationToken.getVoterInformation().getElectionEventId());
			vote.setVotingCardId(votingCardId);
			vote.setBallotBoxId(authenticationToken.getVoterInformation().getBallotBoxId());
			vote.setBallotId(authenticationToken.getVoterInformation().getBallotId());
			// token signature used to validate the signature of vote
			vote.setAuthenticationTokenSignature(authenticationToken.getSignature());
			vote.setVerificationCardId(authenticationToken.getVoterInformation().getVerificationCardId());
			vote.setVerificationCardSetId(authenticationToken.getVoterInformation().getVerificationCardSetId());
			ValidationUtils.validate(vote);
		} catch (ValidationException | IOException e) {
			logger.error("Invalid vote format.", e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		ValidationVoteResult validationVoteResult = voteService
				.validateVoteAndStore(tenantId, electionEventId, votingCardId, authenticationToken.getVoterInformation().getVerificationCardId(),
						vote, authenticationTokenJsonString);

		return Response.ok().entity(objectMapper.writeValueAsString(validationVoteResult)).build();

	}

	/**
	 * Gets the status of voting cards.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCards     the voting cards
	 * @param request         the request
	 * @return the status of voting cards
	 * @throws ApplicationException the application exception
	 */
	@POST
	@Path("/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/states")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatusOfVotingCards(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final List<VotingCard> votingCards,
			@Context
			final HttpServletRequest request) throws ApplicationException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId);

		// Build the states response
		return Response.ok().entity(getAndCopyVotingCardStates(tenantId, electionEventId, votingCards)).build();
	}

	/**
	 * Gets the id and state of inactive voting cards.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param request         the request
	 * @return the id and state of inactive voting cards
	 * @throws ApplicationException the application exception
	 */
	@GET
	@Path("/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/states/inactive")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getIdAndStateOfInactiveVotingCards(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Context
			final HttpServletRequest request) throws ApplicationException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId);

		String timestampInit = new Timestamp(System.currentTimeMillis()).toString();
		String timestamp = timestampInit.substring(0, timestampInit.length() - 4).replace(" ", "_").replace(":", "-");
		String filename = "inactive_voting_cards_" + timestamp + ".csv";

		StreamingOutput entity = stream -> votingCardStateService.writeIdAndStateOfInactiveVotingCards(tenantId, electionEventId, stream);
		return Response.ok().header(INACTIVE_VOTING_CARDS_FILENAME, filename).entity(entity).build();
	}

	/**
	 * Block voting cards.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCards     the voting cards
	 * @param request         the request
	 * @return the status of voting cards
	 * @throws ApplicationException      the application exception
	 * @throws ResourceNotFoundException the resource not found exception
	 * @throws DuplicateEntryException   the duplicate entry exception
	 */
	@PUT
	@Path("/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/block")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response blockVotingCards(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final List<VotingCard> votingCards,
			@Context
			final HttpServletRequest request) throws ApplicationException, ResourceNotFoundException, DuplicateEntryException {

		logger.debug("VW - New request received: block voting cards.");

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId);

		// Build the states response
		final List<VotingCardStatusValue> result = blockVotingCards(tenantId, electionEventId, votingCards);
		return Response.ok().entity(result).build();
	}

	/**
	 * Obtain vote.
	 *
	 * @param authenticationTokenJsonString the authentication token json string
	 * @param voteJsonString                the vote json string
	 * @return the vote
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private Vote obtainVote(String authenticationTokenJsonString, String voteJsonString) throws IOException {
		Vote vote = objectMapper.readValue(voteJsonString, Vote.class);
		vote.setAuthenticationToken(authenticationTokenJsonString);
		return vote;
	}

	/**
	 * Block voting cards.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCards     the voting cards
	 * @return the list
	 * @throws ApplicationException      the application exception
	 * @throws ResourceNotFoundException the resource not found exception
	 * @throws DuplicateEntryException   the duplicate entry exception
	 */
	// Block every request voting card
	private List<VotingCardStatusValue> blockVotingCards(final String tenantId, final String electionEventId, final List<VotingCard> votingCards)
			throws ApplicationException, ResourceNotFoundException, DuplicateEntryException {

		for (VotingCard votingCard : votingCards) {
			votingCardStateService.blockVotingCardIgnoreUnable(tenantId, electionEventId, votingCard.getId());
		}

		// Return new states
		return getAndCopyVotingCardStates(tenantId, electionEventId, votingCards);
	}

	// Copy only the status and the voting card id to the response

	/**
	 * Gets the and copy voting card states.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCards     the voting cards
	 * @return the and copy voting card states
	 * @throws ApplicationException the application exception
	 */
	private List<VotingCardStatusValue> getAndCopyVotingCardStates(final String tenantId, final String electionEventId,
			final List<VotingCard> votingCards) throws ApplicationException {

		List<VotingCardStatusValue> result = new ArrayList<>();
		for (VotingCard votingCard : votingCards) {

			boolean votingCardNotFound = checkIfVotingCardsExistsAndCreateStatus(tenantId, electionEventId, votingCard.getId(), result);
			if (votingCardNotFound) {
				continue;
			}

			// already returns the 'consistent' state
			VotingCardState votingCardState = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCard.getId());

			VotingCardStatusValue votingCardStatusValue = new VotingCardStatusValue();
			votingCardStatusValue.setVotingCardId(votingCardState.getVotingCardId());
			votingCardStatusValue.setStatus(votingCardState.getState().name());
			votingCardStatusValue.setConfirmationAttempts(votingCardState.getAttempts());

			result.add(votingCardStatusValue);
		}

		return result;
	}

	/**
	 * Check If Voting Cards Exists and create status NOT_FOUND if not found.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param vcId            the vc id
	 * @param result          the result
	 * @return true, if successful
	 */
	private boolean checkIfVotingCardsExistsAndCreateStatus(final String tenantId, final String electionEventId, String vcId,
			final List<VotingCardStatusValue> result) {

		boolean votingCardNotFound = false;

		try {
			VoterInformation voterInformation = voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, vcId);
			if (voterInformation == null) {
				VotingCardStatusValue votingCardStatusValue = createNotFoundVotingCard(vcId);
				result.add(votingCardStatusValue);
				votingCardNotFound = true;
			}
		} catch (EJBException | ResourceNotFoundException e) {
			logger.warn("Error trying to check if voting cards exists.", e);
			VotingCardStatusValue votingCardStatusValue = createNotFoundVotingCard(vcId);
			result.add(votingCardStatusValue);
			votingCardNotFound = true;
		}

		return votingCardNotFound;
	}

	/**
	 * Creates the not found voting card.
	 *
	 * @param id the id
	 * @return the voting card status value
	 */
	private VotingCardStatusValue createNotFoundVotingCard(final String id) {
		VotingCardStatusValue votingCardStatusValue = new VotingCardStatusValue();
		votingCardStatusValue.setVotingCardId(id);
		votingCardStatusValue.setStatus(VotingCardStates.NOT_FOUND.toString());
		votingCardStatusValue.setConfirmationAttempts(0L);
		return votingCardStatusValue;
	}

	// Does a basic validation of the input. In case something is wrong, just

	/**
	 * Validate input.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @throws ApplicationException the application exception
	 */
	// throws an exception.
	private void validateInput(final String tenantId, final String electionEventId) throws ApplicationException {
		if (tenantId == null || "".equals(tenantId)) {
			throw new ApplicationException("Invalid parameter: tenantId cannot be null or empty");
		}
		if (electionEventId == null || "".equals(electionEventId)) {
			throw new ApplicationException("Invalid parameter: electionEventId cannot be null or empty");
		}
	}

	// Does a basic validation of the input. In case something is wrong, just

	/**
	 * Validate input.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @throws ApplicationException the application exception
	 */
	// throws an exception.
	private void validateInput(String tenantId, String electionEventId, String votingCardId) throws ApplicationException {
		if (tenantId == null || "".equals(tenantId)) {
			throw new ApplicationException("Invalid parameter: tenantId cannot be null or empty");
		}
		if (electionEventId == null || "".equals(electionEventId)) {
			throw new ApplicationException("Invalid parameter: electionEventId cannot be null or empty");
		}
		if (votingCardId == null || "".equals(votingCardId)) {
			throw new ApplicationException("Invalid parameter: votingCardId cannot be null or empty");
		}
	}

	/**
	 * Constructs the response given a invalid validation result.
	 *
	 * @param validationResult the invalid validation result.
	 * @return a response with the status UNAUTHORIZED.
	 */
	private Response getResponseInvalidToken(ValidationResult validationResult) throws ApplicationException {
		try {
			String json = objectMapper.writeValueAsString(validationResult);
			return Response.status(Response.Status.UNAUTHORIZED).entity(json).build();
		} catch (IOException e) {
			throw new ApplicationException("Failed to convert object to json format.", e);
		}
	}
}
