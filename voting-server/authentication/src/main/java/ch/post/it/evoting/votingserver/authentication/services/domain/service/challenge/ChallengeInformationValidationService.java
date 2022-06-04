/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.ChallengeInformationValidation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * This service provides the functionality to validate a challenge information based on the predefined rules.
 */
@Stateless
public class ChallengeInformationValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeInformationValidationService.class);
	private final Map<Integer, ChallengeInformationValidation> validations = new TreeMap<>();

	@Inject
	@Any
	void setValidations(final Instance<ChallengeInformationValidation> instance) {
		for (final ChallengeInformationValidation validation : instance) {
			validations.put(validation.getOrder(), validation);
		}
	}

	/**
	 * This method validates an challenge information by applying the set of validation rules.
	 *
	 * @param tenantId             - the tenant identifier.
	 * @param electionEventId      - the election event identifier.
	 * @param votingCardId         - the voting card identifier.
	 * @param challengeInformation - the challenge information to be validated.
	 * @return an AuthenticationTokenValidationResult containing the result of the validation.
	 * @throws ResourceNotFoundException
	 * @throws CryptographicOperationException
	 */
	public ValidationResult validate(final String tenantId, final String electionEventId, final String votingCardId,
			final ChallengeInformation challengeInformation)
			throws ResourceNotFoundException, CryptographicOperationException {
		LOGGER.info("Starting validation of Challenge Information.");

		// result of validation
		final ValidationResult validationResult = new ValidationResult();
		validationResult.setResult(true);

		// execute validations
		for (final ChallengeInformationValidation validation : validations.values()) {
			if (validation.execute(tenantId, electionEventId, votingCardId, challengeInformation)) {
				LOGGER.info("Challenge Information validation successful. [class: {}]", validation.getClass().getSimpleName());
			} else {
				LOGGER.info("Challenge Information validation fails. [class: {}]", validation.getClass().getSimpleName());
				validationResult.setResult(false);
				break;
			}
		}

		LOGGER.info("Result of Challenge Information validation. [isResult: {}]", validationResult.isResult());

		return validationResult;
	}
}
