/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.confirmation.ConfirmationMessageValidation;

/**
 * Service which validates the confirmation message sent by the client.
 */
public class ConfirmationMessageValidationServiceImpl implements ConfirmationMessageValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmationMessageValidationServiceImpl.class);
	private final Collection<ConfirmationMessageValidation> validations = new ArrayList<>();

	@Inject
	@Any
	void setValidations(final Instance<ConfirmationMessageValidation> instance) {
		for (final ConfirmationMessageValidation validation : instance) {
			validations.add(validation);
		}
	}

	@Override
	public ConfirmationInformationResult validateConfirmationMessage(final String tenantId, final String electionEventId, final String votingCardId,
			final ConfirmationInformation confirmationInformation, final AuthenticationToken authenticationToken) {

		LOGGER.info("Validating confirmation message.");

		// result of validation
		final ConfirmationInformationResult confirmationInformationResult = new ConfirmationInformationResult();
		confirmationInformationResult.setValid(true);
		confirmationInformationResult.setElectionEventId(electionEventId);
		confirmationInformationResult.setVotingCardId(votingCardId);

		// execute validations
		for (final ConfirmationMessageValidation validation : validations) {
			final ValidationError validationErrorResult = validation
					.execute(tenantId, electionEventId, votingCardId, confirmationInformation, authenticationToken);
			if (validationErrorResult.getValidationErrorType().equals(ValidationErrorType.SUCCESS)) {
				LOGGER.info("Success validation of: {}.", validation.getClass().getSimpleName());
			} else {
				LOGGER.info("Fail validation of: {}.", validation.getClass().getSimpleName());
				confirmationInformationResult.setValid(false);
				confirmationInformationResult.setValidationError(validationErrorResult);

				if (!(validation instanceof ConfirmationMessageMathematicalGroupValidation)) {
					break;
				}
			}
		}

		LOGGER.info("Validation confirmation message result is: {}.", confirmationInformationResult.isValid());

		return confirmationInformationResult;
	}
}
