/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.confirmation.ConfirmationMessageValidation;

/**
 * Decorator for confirmation message validation.
 */
@Decorator
public class ConfirmationMessageElectionDatesValidationDecorator implements ConfirmationMessageValidation {

	@Inject
	@Delegate
	private ConfirmationMessageElectionDatesValidation confirmationMessageElectionDatesValidation;

	/**
	 * @see ConfirmationMessageValidation#execute(String, String, String, ConfirmationInformation, AuthenticationToken)
	 */
	@Override
	public ValidationError execute(final String tenantId, final String electionEventId, final String votingCardId, final ConfirmationInformation confirmationInformation,
			final AuthenticationToken authenticationToken) {
		return confirmationMessageElectionDatesValidation
				.execute(tenantId, electionEventId, votingCardId, confirmationInformation, authenticationToken);
	}

}
