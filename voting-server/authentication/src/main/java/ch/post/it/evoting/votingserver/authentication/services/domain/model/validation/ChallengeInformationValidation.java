/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.validation;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface to provide functionalities for challenge information validations.
 */
public interface ChallengeInformationValidation {

	/**
	 * Executes a specific validation.
	 *
	 * @param tenantId             - the tenant identifier.
	 * @param electionEventId      - the election event identifier.
	 * @param credentialId         - the credential identifier.
	 * @param challengeInformation - the challenge information to be validated.
	 * @return true if the validation succeed. Otherwise, false.
	 * @throws ResourceNotFoundException
	 * @throws CryptographicOperationException
	 */
	boolean execute(String tenantId, String electionEventId, String credentialId, ChallengeInformation challengeInformation)
			throws ResourceNotFoundException, CryptographicOperationException;

	int getOrder();
}
