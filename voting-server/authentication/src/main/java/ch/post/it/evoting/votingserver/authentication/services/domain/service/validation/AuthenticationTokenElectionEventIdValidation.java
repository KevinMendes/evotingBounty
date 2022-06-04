/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.AuthenticationTokenValidation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;

/**
 * This class implements the election event id validation of the authentication token.
 */
public class AuthenticationTokenElectionEventIdValidation implements AuthenticationTokenValidation {

	/**
	 * This method implements the validation of token election event id. That is, The election event
	 * id inside the authentication token is the same that the ones received in the input parameter.
	 *
	 * @param tenantId            - the tenant identifier.
	 * @param electionEventId     - the election event identifier.
	 * @param votingCardId        - the voting card identifier.
	 * @param authenticationToken - the authentication token to be validated.
	 * @return validationResult, if successfully validated. Otherwise, a exception is thrown.
	 */
	@Override
	public ValidationResult execute(String tenantId, String electionEventId, String votingCardId, AuthenticationToken authenticationToken) {
		// validation result
		if (!authenticationToken.getVoterInformation().getElectionEventId().equals(electionEventId)) {
			throw new AuthTokenValidationException(ValidationErrorType.INVALID_EEID);
		}
		return new ValidationResult(true);
	}
}
