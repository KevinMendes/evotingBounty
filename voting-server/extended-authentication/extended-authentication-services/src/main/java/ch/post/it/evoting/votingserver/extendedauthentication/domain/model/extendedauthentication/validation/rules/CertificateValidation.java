/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.rules;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;

/**
 * Perform a specific validation with the received certificate during the process of updating the Extended Authentication
 */
public interface CertificateValidation {

	/**
	 * Validates a certificate using the information provided in the Authentication token
	 *
	 * @param certificate
	 * @param token
	 * @return
	 * @throws ExtendedAuthValidationException when the validation over the certificate is not correct
	 */
	void validateCertificate(String certificate, AuthenticationToken token);
}
