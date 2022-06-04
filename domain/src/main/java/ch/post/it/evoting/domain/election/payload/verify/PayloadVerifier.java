/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.payload.verify;

import java.security.cert.X509Certificate;

import ch.post.it.evoting.domain.election.model.messaging.Payload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;

/**
 * A service that verifies signatures of payloads.
 */
public interface PayloadVerifier {

	/**
	 * Checks whether a signature is valid for the supplied public key and payload.
	 *
	 * @param payload         the payload to check the signature against
	 * @param rootCertificate the CA certificate that issued the chain of certificates leading to the key used to sign the payload
	 * @return whether the signature is valid or not
	 * @throws PayloadVerificationException if the verification of the payload could not be performed
	 */
	boolean isValid(Payload payload, X509Certificate rootCertificate) throws PayloadVerificationException;
}
