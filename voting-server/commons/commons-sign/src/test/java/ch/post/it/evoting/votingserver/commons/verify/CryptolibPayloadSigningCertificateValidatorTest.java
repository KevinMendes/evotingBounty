/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.naming.InvalidNameException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.utils.CertificateChainValidationException;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptolibPayloadSigningCertificateValidator;
import ch.post.it.evoting.domain.election.exceptions.LambdaException;
import ch.post.it.evoting.votingserver.commons.sign.TestCertificateGenerator;

public class CryptolibPayloadSigningCertificateValidatorTest {

	private static final Logger logger = LoggerFactory.getLogger(CryptolibPayloadSigningCertificateValidatorTest.class);

	private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final CryptolibPayloadSigningCertificateValidator sut = new CryptolibPayloadSigningCertificateValidator();

	private static TestCertificateGenerator testCertificateGenerator;

	@BeforeClass
	public static void init() throws GeneralCryptoLibException {
		testCertificateGenerator = TestCertificateGenerator.createDefault();
	}

	@Test
	public void failOnEmptyCertificateChain() throws CertificateChainValidationException {
		X509Certificate[] emptyCertificateChain = new X509Certificate[0];
		X509Certificate trustedCertificate = mock(X509Certificate.class);

		boolean result = sut.isValid(emptyCertificateChain, trustedCertificate);

		assertFalse(result);
		assertFalse("There should be an error", sut.getErrors().isEmpty());

		logger.info(String.join("\n", sut.getErrors()));
	}

	@Test
	public void testEncryptionCertificate() throws CertificateChainValidationException, GeneralCryptoLibException, InvalidNameException {
		X509Certificate[] certificateChain = createCertificateChain(0, X509CertificateType.ENCRYPT);
		X509Certificate rootCertificate = certificateChain[certificateChain.length - 1];
		boolean result = sut.isValid(certificateChain, rootCertificate);

		assertFalse(result);
		assertFalse(sut.getErrors().isEmpty());
	}

	@Test
	public void testCertificateChainWithNoIntermediateCAs()
			throws CertificateChainValidationException, GeneralCryptoLibException, InvalidNameException {
		testValidCertificateChain(createCertificateChain(0));
	}

	@Test
	public void testCertificateChainWithOneIntermediateCAs()
			throws CertificateChainValidationException, GeneralCryptoLibException, InvalidNameException {
		testValidCertificateChain(createCertificateChain(1));
	}

	@Test
	public void testCertificateChainWithManyIntermediateCAs()
			throws CertificateChainValidationException, GeneralCryptoLibException, InvalidNameException {
		testValidCertificateChain(createCertificateChain(4));
	}

	@Test
	public void testConcurrentValidations() throws ExecutionException, InterruptedException {
		final int rounds = 10;

		// Create certificate chains concurrently.
		List<X509Certificate[]> certificateChains = new ArrayList<>(rounds);
		List<Future<X509Certificate[]>> certificateChainBuilders = new ArrayList<>(rounds);
		for (int i = 0; i < rounds; i++) {
			certificateChainBuilders.add(executorService.submit(() -> {
				logger.info("Creating certificate chain in thread {}...\n", Thread.currentThread().getName());
				try {
					return createCertificateChain(4);
				} catch (GeneralCryptoLibException | InvalidNameException e) {
					throw new LambdaException(e);
				}
			}));
		}
		for (Future<X509Certificate[]> certificateChainBuilder : certificateChainBuilders) {
			certificateChains.add(certificateChainBuilder.get());
		}

		assertEquals("Not all expected certificate chains were created", rounds, certificateChains.size());

		// Validate certificate chains them concurrently.
		List<Future<Boolean>> validations = new ArrayList<>(rounds);
		for (final X509Certificate[] certificateChain : certificateChains) {
			validations.add(executorService.submit(() -> {
				logger.info("Validating certificate chain {} in thread {}...\n", certificateChain, Thread.currentThread().getName());
				X509Certificate rootCertificate = certificateChain[certificateChain.length - 1];
				return sut.isValid(certificateChain, rootCertificate);
			}));
		}

		logger.info("Shutting down executor...");
		executorService.shutdown();
		logger.info("Executor is shut down");

		// Check that the validations succeeded.
		for (Future<Boolean> validation : validations) {
			assertTrue(validation.get());
		}

		assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
	}

	/**
	 * Test that a valid certificate chain is successfully validated.
	 *
	 * @param certificateChain the certificate chain to test.
	 * @throws CertificateChainValidationException
	 */
	private void testValidCertificateChain(X509Certificate[] certificateChain) throws CertificateChainValidationException {
		X509Certificate rootCertificate = certificateChain[certificateChain.length - 1];
		boolean result = sut.isValid(certificateChain, rootCertificate);

		assertTrue(result);
		assertTrue("Validation failed unexpectedly: " + String.join(", ", sut.getErrors()), sut.getErrors().isEmpty());
	}

	/**
	 * Create a certificate chain with a configurable number of intermediate CAs, and a signing key pair.
	 *
	 * @param intermediateCAs the number of intermediate CAs to build
	 * @return a certificate chain
	 * @throws GeneralCryptoLibException
	 */
	private X509Certificate[] createCertificateChain(int intermediateCAs) throws GeneralCryptoLibException, InvalidNameException {
		return createCertificateChain(intermediateCAs, X509CertificateType.SIGN);
	}

	/**
	 * Create a certificate chain with a configurable number of intermediate CAs, and a custom key pair.
	 *
	 * @param intermediateCAs     the number of intermediate CAs to build
	 * @param leafCertificateType the type of the leaf certificate to create
	 * @return a certificate chain
	 * @throws GeneralCryptoLibException
	 */
	private X509Certificate[] createCertificateChain(int intermediateCAs, X509CertificateType leafCertificateType)
			throws GeneralCryptoLibException, InvalidNameException {

		List<X509Certificate> certificateChainBuilder = new ArrayList<>();

		// Create the required intermediate certificates.
		PrivateKey caSigningKey = testCertificateGenerator.getRootKeyPair().getPrivate();

		X509Certificate caCertificate = testCertificateGenerator.getRootCertificate();
		certificateChainBuilder.add(caCertificate);

		for (int i = 0; i < intermediateCAs; i++) {
			KeyPair intermediateKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
			String commonName = "Intermediate CA #" + (i + 1);
			X509DistinguishedName intermediateDN = TestCertificateGenerator.buildDistinguishedName(commonName);

			// Build the intermediate CA.
			X509Certificate intermediateCertificate = testCertificateGenerator
					.createCACertificate(intermediateKeyPair, caSigningKey, caCertificate, intermediateDN);

			// Add the intermediate CA certificate to the certificate chain
			// builder.
			certificateChainBuilder.add(intermediateCertificate);

			// Keep this CA's private key, DN and certificate to sign the next
			// certificate.
			caCertificate = intermediateCertificate;
			caSigningKey = intermediateKeyPair.getPrivate();
		}

		// Create the last certificate.
		X509Certificate leafCertificate = (X509CertificateType.SIGN == leafCertificateType) ?
				testCertificateGenerator.createSigningLeafCertificate(AsymmetricTestDataGenerator.getKeyPairForSigning(), caSigningKey, caCertificate,
						"Signing leaf certificate") :
				testCertificateGenerator.createLeafCertificate(AsymmetricTestDataGenerator.getKeyPairForEncryption(), caSigningKey, caCertificate,
						"Leaf certificate");

		certificateChainBuilder.add(leafCertificate);

		// Invert the collection so that the root CA is first and the leaf
		// certificate is last.
		Collections.reverse(certificateChainBuilder);

		return certificateChainBuilder.toArray(new X509Certificate[certificateChainBuilder.size()]);
	}
}
