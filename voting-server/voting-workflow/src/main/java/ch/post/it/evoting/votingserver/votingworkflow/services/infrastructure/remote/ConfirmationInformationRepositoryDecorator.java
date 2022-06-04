/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.ConfirmationInformationRepository;

/**
 * Decorator for confirmation information repository.
 */
@Decorator
public class ConfirmationInformationRepositoryDecorator implements ConfirmationInformationRepository {

	@Inject
	@Delegate
	private ConfirmationInformationRepository confirmationInformationRepository;

	/**
	 * @throws ResourceNotFoundException
	 * @see ConfirmationInformationRepository#validateConfirmationMessage(String,
	 * String, String,
	 * ConfirmationInformation,
	 * String)
	 */
	@Override
	public ConfirmationInformationResult validateConfirmationMessage(String tenantId, String electionEventId, String votingCardId,
			ConfirmationInformation confirmationInformation, String token) throws ResourceNotFoundException {

		return confirmationInformationRepository.validateConfirmationMessage(tenantId, electionEventId, votingCardId, confirmationInformation, token);

	}

}
