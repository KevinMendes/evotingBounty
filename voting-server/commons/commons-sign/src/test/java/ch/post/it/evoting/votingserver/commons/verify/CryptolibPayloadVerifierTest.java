/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.CertificateChainValidationException;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.domain.election.model.messaging.Payload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.domain.election.payload.verify.CryptolibPayloadVerifier;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationResult;

/**
 * Test for CryptolibPayloadVerifier
 */
public class CryptolibPayloadVerifierTest {

	private X509Certificate platformCACertificate;

	private X509Certificate verificationCertificate;

	private CertificateValidationResult certificateValidationResult;

	private AsymmetricServiceAPI asymmetricService;

	private Payload payload;

	private CryptoPrimitivesPayloadSignature payloadSignature;

	private PayloadSigningCertificateValidator certificateChainValidator;

	@Before
	public void setUp() throws IOException {
		asymmetricService = mock(AsymmetricServiceAPI.class);
		certificateChainValidator = mock(PayloadSigningCertificateValidator.class);

		platformCACertificate = mock(X509Certificate.class);
		verificationCertificate = mock(X509Certificate.class);
		X509Certificate[] certificateChain = new X509Certificate[] { platformCACertificate, verificationCertificate };

		payloadSignature = mock(CryptoPrimitivesPayloadSignature.class);
		when(payloadSignature.getCertificateChain()).thenReturn(certificateChain);

		payload = mock(Payload.class);
		when(payload.getSignature()).thenReturn(payloadSignature);
		when(payload.getSignableContent()).thenReturn(new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * @throws PayloadVerificationException
	 * @throws GeneralCryptoLibException
	 * @throws CryptographicOperationException
	 * @throws CertificateChainValidationException
	 */
	@Test
	public void validateWithCorrectCertificateChainAndSignature()
			throws PayloadVerificationException, GeneralCryptoLibException, CryptographicOperationException, CertificateChainValidationException {
		// Set up the verifier.
		when(certificateChainValidator.isValid(any(), any())).thenReturn(true);
		when(asymmetricService.verifySignature(any(), any(), any(InputStream.class))).thenReturn(true);

		CryptolibPayloadVerifier sut = new CryptolibPayloadVerifier(asymmetricService, certificateChainValidator);

		assertTrue(sut.isValid(payload, platformCACertificate));
	}

	/**
	 * @throws PayloadVerificationException
	 * @throws GeneralCryptoLibException
	 * @throws CryptographicOperationException
	 * @throws CertificateChainValidationException
	 */
	@Test
	public void notValidateWithWrongCertificateChainAndSignature()
			throws PayloadVerificationException, GeneralCryptoLibException, CryptographicOperationException, CertificateChainValidationException {
		// Set up the verifier.
		when(certificateChainValidator.isValid(any(), any())).thenReturn(false);

		CryptolibPayloadVerifier sut = new CryptolibPayloadVerifier(asymmetricService, certificateChainValidator);

		assertFalse(sut.isValid(payload, platformCACertificate));
	}

	/**
	 * @throws PayloadVerificationException
	 * @throws GeneralCryptoLibException
	 * @throws CryptographicOperationException
	 * @throws CertificateChainValidationException
	 */
	@Test
	public void notValidateWithCorrectCertificateChainAndWrongSignature()
			throws PayloadVerificationException, GeneralCryptoLibException, CryptographicOperationException, CertificateChainValidationException {
		// Set up the verifier.
		when(certificateChainValidator.isValid(any(), any())).thenReturn(true);
		when(asymmetricService.verifySignature(any(), any(), any(InputStream.class))).thenReturn(false);

		CryptolibPayloadVerifier sut = new CryptolibPayloadVerifier(asymmetricService, certificateChainValidator);

		assertFalse(sut.isValid(payload, platformCACertificate));
	}
}
