/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.confirmation.ConfirmationMessageValidation;

/**
 * This class implements the credential id validation of the confirmation message.
 */
public class ConfirmationMessageCredentialIdValidation implements ConfirmationMessageValidation {

	/**
	 * This method implements the validation of confirmation message credential id.
	 *
	 * @param tenantId                - the tenant identifier.
	 * @param electionEventId         - the election event identifier.
	 * @param votingCardId            - the voting card identifier.
	 * @param confirmationInformation - the confirmation information to be validated.
	 * @param authenticationToken     - the authentication token.
	 * @return true, if successfully validated. Otherwise, false.
	 */
	@Override
	public ValidationError execute(final String tenantId, final String electionEventId, final String votingCardId,
			final ConfirmationInformation confirmationInformation,
			final AuthenticationToken authenticationToken) {

		final ValidationError result = new ValidationError();
		// #7213 verify credentialid from authtoken matches credentialid from confirmation info
		if (confirmationInformation.getCredentialId().equalsIgnoreCase(authenticationToken.getVoterInformation().getCredentialId())) {
			result.setValidationErrorType(ValidationErrorType.SUCCESS);
		}

		return result;
	}
}
