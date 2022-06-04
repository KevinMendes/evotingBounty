/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot.BallotRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot.BallotTextRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification.VerificationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.VotingCardStateService;

/**
 * Service for generating the authentication token.
 */
@Stateless
public class AuthenticationTokenService {

	private static final String VERIFICATION_CARD = "verificationCard";

	private static final String BALLOT_BOX = "ballotBox";

	private static final String BALLOT_TEXTS = "ballotTexts";

	private static final String BALLOT_TEXTS_SIGNATURE = "ballotTextsSignature";

	private static final String BALLOT = "ballot";

	private static final String AUTHENTICATION_TOKEN = "authenticationToken";

	private static final String VOTING_CARD_STATE = "votingCardState";

	private static final String VALIDATION_ERROR = "validationError";

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	// The EJB instance for retrieving authentication tokens
	@EJB
	private AuthenticationTokenRepository authenticationTokenRepository;

	// The EJB instance for retrieving ballot information
	@EJB
	private BallotRepository ballotRepository;

	// The EJB instance for retrieving ballot information
	@EJB
	private BallotBoxInformationRepository ballotBoxInformationRepository;

	// The EJB instance for retrieving ballot information
	@EJB
	private VerificationRepository verificationRepository;

	// The EJB instance for retrieving ballot text
	@EJB
	private BallotTextRepository ballotTextRepository;

	// The voting card state service
	@EJB
	private VotingCardStateService votingCardStateService;

	/**
	 * Gets the authentication token which matches with the given parameters.
	 *
	 * @param tenantId             - the tenant identifier.
	 * @param electionEventId      - the election event identifier.
	 * @param credentialId         - the credential Identifier.
	 * @param challengeInformation - the challenge information including client challenge, server challenge, and server timestamp.
	 * @return json object represeting the authentication token.
	 * @throws ApplicationException
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws DuplicateEntryException
	 * @throws EntryPersistenceException
	 */
	public JsonObject getAuthenticationToken(String tenantId, String electionEventId, String credentialId, ChallengeInformation challengeInformation)
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {

		// build the authentication token
		AuthenticationTokenMessage authTokenMessage = buildAuthenticationToken(tenantId, electionEventId, credentialId, challengeInformation);

		// assuming the authToken is always present even if it fails 'validation'
		// auth token
		AuthenticationToken authToken = authTokenMessage.getAuthenticationToken();
		JsonObject authTokenJsonObject = JsonUtils.getJsonObject(objectMapper.writeValueAsString(authToken));

		// get the voting card id from the voter information
		String votingCardId = authToken.getVoterInformation().getVotingCardId();

		// recover the voting card state
		VotingCardState votingCardState = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);

		// initialize the voting card state
		votingCardStateService.initializeVotingCardState(votingCardState);
		VotingCardStates votingCardStateValue = votingCardState.getState();
		// always include the validation result
		JsonValue validationJson = JsonUtils.getJsonObject(objectMapper.writeValueAsString(authTokenMessage.getValidationError()));

		// create object response
		JsonObjectBuilder authTokenObjectBuilder = Json.createObjectBuilder();
		authTokenObjectBuilder.add(AUTHENTICATION_TOKEN, authTokenJsonObject).add(VALIDATION_ERROR, validationJson)
				.add(VOTING_CARD_STATE, votingCardStateValue.name());

		// #9866 - always add the ballot information to the result so that the FE can either show the
		// ballot info
		addBallotAndVerificationDataToAuthToken(tenantId, electionEventId, authToken, authTokenObjectBuilder);

		return authTokenObjectBuilder.build();
	}

	public ValidationResult validateAuthenticationToken(final String tenantId, final String electionEventId, final String votingCardId,
			final String authenticationToken) throws IOException, ResourceNotFoundException, ApplicationException {

		return authenticationTokenRepository.validateAuthenticationToken(tenantId, electionEventId, votingCardId, authenticationToken);

	}

	// Generate authentication token.
	private AuthenticationTokenMessage buildAuthenticationToken(String tenantId, String electionEventId, String credentialId,
			ChallengeInformation challengeInformation) throws ResourceNotFoundException, ApplicationException {
		return authenticationTokenRepository.getAuthenticationToken(tenantId, electionEventId, credentialId, challengeInformation);
	}

	// Gets the verification card data json.
	private JsonObject getVerificationCardDataJson(String tenantId, String electionEventId, String verificationCardId)
			throws IOException, ResourceNotFoundException {
		Verification verificationCard = verificationRepository.findByTenantElectionEventVotingCard(tenantId, electionEventId, verificationCardId);
		return JsonUtils.getJsonObject(objectMapper.writeValueAsString(verificationCard));
	}

	// Gets the ballot box json.
	private JsonObject getBallotBoxJson(String tenantId, String electionEventId, String ballotBoxId) throws ResourceNotFoundException {
		String ballotBox = ballotBoxInformationRepository
				.getBallotBoxInfoByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId);
		return JsonUtils.getJsonObject(ballotBox);
	}

	// Gets the ballot json.
	private JsonObject getBallotJson(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException {
		String ballot = ballotRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId);
		return JsonUtils.getJsonObject(ballot);
	}

	// Gets the ballot text json.
	private String getBallotText(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException {
		return ballotTextRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId);
	}

	/**
	 * Add the ballot and verification information to the authentication token.
	 *
	 * @param tenantId               the tenant
	 * @param electionEventId        the election
	 * @param authToken              the original authentication token
	 * @param authTokenObjectBuilder the resulting authentication object
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 */
	private void addBallotAndVerificationDataToAuthToken(final String tenantId, final String electionEventId, final AuthenticationToken authToken,
			final JsonObjectBuilder authTokenObjectBuilder) throws ResourceNotFoundException, IOException {

		// search ballot representation extracting the info from the token
		String ballotId = authToken.getVoterInformation().getBallotId();
		JsonValue ballotJson = getBallotJson(tenantId, electionEventId, ballotId);

		// search ballot texts extracting the info from the token
		String ballotAndSignatureJson = getBallotText(tenantId, electionEventId, ballotId);
		JsonObject ballotAndSignatureJsonObject = JsonUtils.getJsonObject(ballotAndSignatureJson);
		JsonValue ballotTextJson = ballotAndSignatureJsonObject.getJsonArray(BALLOT_TEXTS);
		JsonValue ballotTextSignatureJson = ballotAndSignatureJsonObject.getJsonArray(BALLOT_TEXTS_SIGNATURE);

		// search ballot box representation extracting the info from the
		// token
		String ballotBoxId = authToken.getVoterInformation().getBallotBoxId();
		JsonValue ballotBoxJson = getBallotBoxJson(tenantId, electionEventId, ballotBoxId);

		// Obtains the verification card Data
		String verificationCardId = authToken.getVoterInformation().getVerificationCardId();
		JsonValue verificationCardJson = getVerificationCardDataJson(tenantId, electionEventId, verificationCardId);

		authTokenObjectBuilder.add(BALLOT, ballotJson).add(BALLOT_TEXTS, ballotTextJson).add(BALLOT_TEXTS_SIGNATURE, ballotTextSignatureJson)
				.add(BALLOT_BOX, ballotBoxJson).add(VERIFICATION_CARD, verificationCardJson);
	}

}
