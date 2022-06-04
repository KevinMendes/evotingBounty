/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation;

import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.confirmation.ConfirmationMessageValidation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.ElectionValidationRequest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.election.ElectionService;

/**
 * This class implements the confirmation message is member of a mathematical group.
 */
public class ConfirmationMessageElectionDatesValidation implements ConfirmationMessageValidation {

	@Inject
	private ElectionService electionService;

	/**
	 * This method validates whether the ballot box is still active before cast the vote.
	 *
	 * @param tenantId                - the tenant identifier.
	 * @param electionEventId         - the election event identifier.
	 * @param votingCardId            - the voting card identifier.
	 * @param confirmationInformation - the confirmation information to be validated.
	 * @param authenticationToken     - the authentication token.
	 * @return ValidationError with information about validation execution.
	 */
	@Override
	public ValidationError execute(String tenantId, String electionEventId, String votingCardId, ConfirmationInformation confirmationInformation,
			AuthenticationToken authenticationToken) {
		String ballotBoxId = authenticationToken.getVoterInformation().getBallotBoxId();
		// The validation will take into account the grace period
		ElectionValidationRequest electionValidationRequest = ElectionValidationRequest.create(tenantId, electionEventId, ballotBoxId, true);
		return electionService.validateIfElectionIsOpen(electionValidationRequest);
	}
}
