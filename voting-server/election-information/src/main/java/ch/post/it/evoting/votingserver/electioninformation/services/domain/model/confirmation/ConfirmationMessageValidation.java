/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.confirmation;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;

/**
 * Interface to provide functionalities for confirmation message validations.
 */
public interface ConfirmationMessageValidation {

	/**
	 * Executes a specific validation.
	 *
	 * @param tenantId                - the tenant identifier.
	 * @param electionEventId         - the election event identifier.
	 * @param votingCardId            - the voting card identifier.
	 * @param confirmationInformation - the confirmation information to be validated.
	 * @param authenticationToken     - the authentication token.
	 * @return A ValidationError describing if the rule is satisfied or not.
	 */
	ValidationError execute(String tenantId, String electionEventId, String votingCardId, ConfirmationInformation confirmationInformation,
			AuthenticationToken authenticationToken);
}
