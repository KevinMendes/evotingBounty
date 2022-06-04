/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.rules;

import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;

/**
 * The Class implementing the rule for validating the credential id in the certificate used for verify the signature.
 */
public class CredentialIdValidation implements CertificateValidation {

	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialIdValidation.class);

	@Override
	public void validateCertificate(final String certificate, final AuthenticationToken token) {

		try {

			// Obtain certificate
			final X509Certificate certificateX509 = (X509Certificate) PemUtils.certificateFromPem(certificate);
			final CryptoX509Certificate wrappedCertificate = new CryptoX509Certificate(certificateX509);
			final String credentialToVerify = wrappedCertificate.getSubjectDn().getCommonName();

			if (!credentialToVerify.contains(token.getVoterInformation().getCredentialId())) {
				final String errMsg = "The provided credential ID does not match with the one in the certificate";
				LOGGER.error(errMsg);
				throw new ExtendedAuthValidationException(ValidationErrorType.INVALID_CREDENTIAL_ID_IN_CERTIFICATE);
			}

		} catch (final GeneralCryptoLibException e) {
			LOGGER.error("Error in the provided certificate", e);
			throw new ExtendedAuthValidationException(ValidationErrorType.INVALID_CERTIFICATE, e);
		}
	}

}
