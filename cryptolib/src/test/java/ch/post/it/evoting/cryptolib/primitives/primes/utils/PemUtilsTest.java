/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.primes.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataGenerator;

class PemUtilsTest {

	private static final String RSA_PRIVATE_KEY_PATH = "/pem/rsaPrivateKey.pem";
	private static final String RSA_PUBLIC_KEY_PATH = "/pem/rsaPublicKey.pem";
	private static final String X509_CERTIFICATE_PATH = "/pem/x509Certificate.pem";
	private static final String RSA_PRIVATE_KEY_WITH_NO_NEWLINES_PATH = "/pem/rsaPrivateKeyWithNoNewlines.pem";
	private static final String RSA_PUBLIC_KEY_WITH_NO_NEWLINES_PATH = "/pem/rsaPublicKeyWithNoNewlines.pem";
	private static final String X509_CERTIFICATE_WITH_NO_NEWLINES_PATH = "/pem/x509CertificateWithNoNewlines.pem";
	private static final String CERTIFICATE_SIGNING_REQUEST_PATH = "/pem/certificateSigningRequest.pem";

	@Test
	void testReadingRsaPublicKeyFromAndWritingToPem() throws IOException, GeneralCryptoLibException {
		final String pemStrIn = removeNewLines(loadPem(RSA_PUBLIC_KEY_PATH));

		final PublicKey key = PemUtils.publicKeyFromPem(pemStrIn);
		final String pemStrOut = removeNewLines(PemUtils.publicKeyToPem(key));

		assertEquals(pemStrIn, pemStrOut);
	}

	@Test
	void testReadingRsaPrivateKeyFromAndWritingToPem() throws IOException, GeneralCryptoLibException {
		final String pemStrIn = removeNewLines(loadPem(RSA_PRIVATE_KEY_PATH));

		final PrivateKey key = PemUtils.privateKeyFromPem(pemStrIn);
		final String pemStrOut = removeNewLines(PemUtils.privateKeyToPem(key));

		assertEquals(pemStrIn, pemStrOut);
	}

	@Test
	void testReadingX509CertificateFromAndWritingToPem() throws IOException, GeneralCryptoLibException {
		final String pemStrIn = removeNewLines(loadPem(X509_CERTIFICATE_PATH));

		final Certificate cert = PemUtils.certificateFromPem(pemStrIn);
		final String pemStrOut = removeNewLines(PemUtils.certificateToPem(cert));

		assertEquals(pemStrIn, pemStrOut);
	}

	@Test
	void testReadingRsaPublicKeyWithNoNewlinesFromAndWritingToPem() throws IOException, GeneralCryptoLibException {
		final String pemStrIn = removeNewLines(loadPem(RSA_PUBLIC_KEY_WITH_NO_NEWLINES_PATH));

		final PublicKey key = PemUtils.publicKeyFromPem(pemStrIn);
		final String pemStrOut = removeNewLines(PemUtils.publicKeyToPem(key));

		assertEquals(pemStrIn, pemStrOut);
	}

	@Test
	void testReadingRsaPrivateKeyWithNoNewlinesFromAndWritingToPem() throws IOException, GeneralCryptoLibException {
		final String pemStrIn = removeNewLines(loadPem(RSA_PRIVATE_KEY_WITH_NO_NEWLINES_PATH));

		final PrivateKey key = PemUtils.privateKeyFromPem(pemStrIn);
		final String pemStrOut = removeNewLines(PemUtils.privateKeyToPem(key));

		assertEquals(pemStrIn, pemStrOut);
	}

	@Test
	void testReadingX509CertificateWithNoNewlinesFromAndWritingToPem() throws IOException, GeneralCryptoLibException {
		final String pemStrIn = removeNewLines(loadPem(X509_CERTIFICATE_WITH_NO_NEWLINES_PATH));

		final Certificate certificate = PemUtils.certificateFromPem(pemStrIn);
		final String pemStrOut = removeNewLines(PemUtils.certificateToPem(certificate));

		assertEquals(pemStrIn, pemStrOut);
	}

	@Test
	void testReadingCertificateSigningRequestFromAndWritingToPem() throws IOException, GeneralCryptoLibException {
		final String pemStrIn = removeNewLines(loadPem(CERTIFICATE_SIGNING_REQUEST_PATH));

		final PKCS10CertificationRequest csr = PemUtils.certificateSigningRequestFromPem(pemStrIn);
		final String pemStrOut = removeNewLines(PemUtils.certificateSigningRequestToPem(csr));

		assertEquals(pemStrIn, pemStrOut);
	}

	@Test
	void testSaveCertificateToFileOk(
			@TempDir
			final
			Path tempPath) throws Exception {
		final CryptoAPIX509Certificate cert = getCryptoAPIX509Certificate();

		final Path certPath = tempPath.resolve("cryptoAPIX509Certificate.pem");

		PemUtils.saveCertificateToFile(cert, certPath);

		final byte[] bytes = Files.readAllBytes(certPath);
		assertNotEquals(bytes.length, 0);
	}

	@Test
	void testSaveCertificateToFileCertValidation() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> PemUtils.saveCertificateToFile(null, Paths.get("test")));
		assertEquals("certificate is null.", exception.getMessage());
	}

	@Test
	void testSaveCertificateToFilePathValidation() throws GeneralCryptoLibException {
		final CryptoAPIX509Certificate cert = getCryptoAPIX509Certificate();

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> PemUtils.saveCertificateToFile(cert, null));
		assertEquals("path is null.", exception.getMessage());
	}

	private CryptoAPIX509Certificate getCryptoAPIX509Certificate() throws GeneralCryptoLibException {
		final KeyPair keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		final CertificatesService certificatesService = new CertificatesService();
		final CertificateData certificateData = X509CertificateTestDataGenerator
				.getCertificateData(AsymmetricTestDataGenerator.getKeyPairForSigning());
		return certificatesService.createEncryptionX509Certificate(certificateData, keyPair.getPrivate());
	}

	private String removeNewLines(final String str) {
		return str.replace("\n", "").replace("\r", "");
	}

	private String loadPem(final String resource) throws IOException {
		final StringBuilder pem = new StringBuilder();
		try (final InputStream stream = getClass().getResourceAsStream(resource); final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			int c;
			while ((c = reader.read()) != -1) {
				pem.append((char) c);
			}
		}
		return pem.toString();
	}
}
