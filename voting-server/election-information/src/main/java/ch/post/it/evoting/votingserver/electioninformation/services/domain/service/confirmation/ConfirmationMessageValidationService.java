/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;

/**
 * Interface with operations required to validate the confirmation message.
 */
public interface ConfirmationMessageValidationService {

	/**
	 * Validates confirmation message with a set of validations including: - check credential X509
	 * Certificate has the same credential id as the provided. - verify signature on [confirmation
	 * message, auth token, voting card id, election event id] - check confirmation message is member
	 * of mathematical group
	 *
	 * @param tenantId                - the tenant id.
	 * @param electionEventId         - the election event id.
	 * @param votingCardId            - the voting card id.
	 * @param confirmationInformation - the confirmation information to be validated.
	 * @param authenticationToken     - the authentication token.
	 * @return a Confirmation result of the validation.
	 */
	ConfirmationInformationResult validateConfirmationMessage(String tenantId, String electionEventId, String votingCardId,
			ConfirmationInformation confirmationInformation, AuthenticationToken authenticationToken);

}
