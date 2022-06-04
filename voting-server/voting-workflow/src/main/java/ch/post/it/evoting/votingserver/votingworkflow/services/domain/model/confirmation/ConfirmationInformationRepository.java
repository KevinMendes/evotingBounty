/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;

/**
 * Repository which validates the confirmation message.
 */
@Local
public interface ConfirmationInformationRepository {

	/**
	 * Validates a confirmation message.
	 *
	 * @param tenantId                - the tenant identifier.
	 * @param electionEventId         - the electionEventIdentifier.
	 * @param votingCardId            - the voting card identifier.
	 * @param confirmationInformation - confirmation information to be validated *
	 * @param token                   - authentication token
	 * @return Confirmation Information Result, with the result of the validation
	 * @throws ResourceNotFoundException
	 */
	ConfirmationInformationResult validateConfirmationMessage(String tenantId, String electionEventId, String votingCardId,
			ConfirmationInformation confirmationInformation, String token) throws ResourceNotFoundException;

}
