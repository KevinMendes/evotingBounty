/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.payload.verify;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.CertificateChainValidationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.domain.election.model.messaging.Payload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;

/**
 * A service that verifies payload signatures based on the cryptolib.
 */
public class CryptolibPayloadVerifier implements PayloadVerifier {

	private static final Logger logger = LoggerFactory.getLogger(CryptolibPayloadVerifier.class);

	private final AsymmetricServiceAPI asymmetricService;

	private final PayloadSigningCertificateValidator certificateChainValidator;

	/**
	 * Initialises the service with the signing key.
	 *
	 * @param asymmetricService         an instance of the service used for signing
	 * @param certificateChainValidator a service that can validate a certificate chain
	 */
	public CryptolibPayloadVerifier(final AsymmetricServiceAPI asymmetricService, final PayloadSigningCertificateValidator certificateChainValidator) {
		this.asymmetricService = asymmetricService;
		this.certificateChainValidator = certificateChainValidator;
	}

	@Override
	public boolean isValid(final Payload payload, final X509Certificate trustedCertificate) throws PayloadVerificationException {
		logger.info("Verifying signature for payload {}...", payload);

		// Get the signature and the certificate chain.
		final CryptoPrimitivesPayloadSignature signature = payload.getSignature();
		final X509Certificate[] certificateChain = signature.getCertificateChain();

		boolean isSignatureValid = false;
		try {
			// Validate the certificate chain.
			if (isCertificateChainValid(certificateChain, trustedCertificate)) {
				// Extract the public key from the now-validated first
				// certificate.
				final PublicKey verificationKey = certificateChain[0].getPublicKey();

				// Verify the signature.
				isSignatureValid = asymmetricService.verifySignature(signature.getSignatureContents(), verificationKey, payload.getSignableContent());

				logger.info("Payload {} signature is {}", payload, isSignatureValid ? "valid" : "not valid");
			} else {
				logger.warn("Cannot verify the payload signature because the certificate chain is not valid");
			}
		} catch (final GeneralCryptoLibException | IOException | CertificateChainValidationException e) {
			logger.error("Payload {}'s signature could not be verified", payload, e);
			throw new PayloadVerificationException(e);
		}

		return isSignatureValid;
	}

	/**
	 * Ascertain whether a certificate chain can be traced back to a trusted certificate.
	 *
	 * @param certificateChain   the certificate chain to test
	 * @param trustedCertificate a trusted certificate
	 * @return whether the certificate chain can be trusted
	 * @throws CertificateChainValidationException
	 * @throws ValidationException                 if the validation could not be performed
	 */
	private boolean isCertificateChainValid(final X509Certificate[] certificateChain, final X509Certificate trustedCertificate)
			throws CertificateChainValidationException {
		final boolean isValid = certificateChainValidator.isValid(certificateChain, trustedCertificate);
		if (!isValid) {
			logger.error("Invalid payload certificate chain: ", certificateChainValidator.getErrors().stream().collect(Collectors.joining(" | ")));
		}

		return isValid;
	}
}
