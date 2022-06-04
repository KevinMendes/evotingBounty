/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication;

import java.io.IOException;

import javax.ejb.Local;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Repository for handling Authentication Tokens.
 */
@Local
public interface AuthenticationTokenRepository {

	/**
	 * Gets the authentication token message for the given parameters.
	 *
	 * @param tenantId             - identifier of the tenant.
	 * @param electionEventId      - identifier of the election event .
	 * @param credentialId         - identifier of the credential.
	 * @param challengeInformation - the challenge information including client challenge, server
	 *                             challenge, and server timestamp.
	 * @return an AuthenticationToken
	 * @throws ResourceNotFoundException if authentication token can not be successfully build.
	 */
	AuthenticationTokenMessage getAuthenticationToken(String tenantId, String electionEventId, String credentialId,
			ChallengeInformation challengeInformation) throws ResourceNotFoundException, ApplicationException;

	/**
	 * Validates an authentication token by applying the set of validation rules.
	 *
	 * @param tenantId            - the tenant identifier.
	 * @param electionEventId     - the electionEventIdentifier.
	 * @param votingCardId        - the voting card identifier.
	 * @param authenticationToken - the token to be validated.
	 * @return an AuthenticationTokenValidationResult object.
	 * @throws IOException               if there is a problem during conversion of authentication token to json
	 *                                   format.
	 * @throws ResourceNotFoundException if there are problems validating token.
	 */
	ValidationResult validateAuthenticationToken(String tenantId, String electionEventId, String votingCardId, String authenticationToken)
			throws IOException, ResourceNotFoundException, ApplicationException;
}
