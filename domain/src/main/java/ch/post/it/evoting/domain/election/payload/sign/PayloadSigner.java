/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.payload.sign;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.domain.election.model.messaging.Payload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;

/**
 * A service that signs payloads.
 */
public interface PayloadSigner {

	/**
	 * Generates the signature of a set of votes, and attaches to it a certificate chain with the public key for further validation.
	 *
	 * @param payload          the payload to sign
	 * @param signingKey       the key used to sign
	 * @param certificateChain the certificate chain containing the public key used to verify the signature
	 * @return the signature for the supplied set of votes
	 * @throws PayloadSignatureException if the signature generation could not be performed
	 */
	CryptoPrimitivesPayloadSignature sign(Payload payload, PrivateKey signingKey, X509Certificate[] certificateChain)
			throws PayloadSignatureException;

}
