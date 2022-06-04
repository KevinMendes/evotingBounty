/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;

/**
 * Decorator of the confirmation message validation service.
 */
@Decorator
public class ConfirmationMessageValidationServiceDecorator implements ConfirmationMessageValidationService {

	@Inject
	@Delegate
	private ConfirmationMessageValidationService confirmationMessageValidationService;

	/**
	 * @see ConfirmationMessageValidationService#validateConfirmationMessage(String, String, String, ConfirmationInformation, AuthenticationToken)
	 */
	@Override
	public ConfirmationInformationResult validateConfirmationMessage(final String tenantId, final String electionEventId, final String votingCardId,
			final ConfirmationInformation confirmationInformation, final AuthenticationToken authenticationToken) {
		return confirmationMessageValidationService
				.validateConfirmationMessage(tenantId, electionEventId, votingCardId, confirmationInformation, authenticationToken);
	}

}
