/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.primes.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPrivateKey;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPublicKey;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class PemUtilsValidationTest {

	private static final String RSA_PRIVATE_KEY_WITH_NO_HEADER_PATH = "/pem/rsaPrivateKeyWithNoHeader.pem";
	private static final String RSA_PUBLIC_KEY_WITH_NO_HEADER_PATH = "/pem/rsaPublicKeyWithNoHeader.pem";
	private static final String X509_CERTIFICATE_WITH_NO_HEADER_PATH = "/pem/x509CertificateWithNoHeader.pem";
	private static final String CERTIFICATE_SIGNING_REQUEST_WITH_NO_HEADER_PATH = "/pem/certificateSigningRequestWithNoHeader.pem";
	private static final String RSA_PRIVATE_KEY_WITH_NO_FOOTER_PATH = "/pem/rsaPrivateKeyWithNoFooter.pem";
	private static final String RSA_PUBLIC_KEY_WITH_NO_FOOTER_PATH = "/pem/rsaPublicKeyWithNoFooter.pem";
	private static final String X509_CERTIFICATE_WITH_NO_FOOTER_PATH = "/pem/x509CertificateWithNoFooter.pem";
	private static final String CERTIFICATE_SIGNING_REQUEST_WITH_NO_FOOTER_PATH = "/pem/certificateSigningRequestWithNoFooter.pem";
	private static final String RSA_PRIVATE_KEY_WITH_BAD_HEADER_PATH = "/pem/rsaPrivateKeyWithBadHeader.pem";
	private static final String RSA_PUBLIC_KEY_WITH_BAD_HEADER_PATH = "/pem/rsaPublicKeyWithBadHeader.pem";
	private static final String X509_CERTIFICATE_WITH_BAD_HEADER_PATH = "/pem/x509CertificateWithBadHeader.pem";
	private static final String CERTIFICATE_SIGNING_REQUEST_WITH_BAD_HEADER_PATH = "/pem/certificateSigningRequestWithBadHeader.pem";
	private static final String RSA_PRIVATE_KEY_WITH_BAD_FOOTER_PATH = "/pem/rsaPrivateKeyWithBadFooter.pem";
	private static final String RSA_PUBLIC_KEY_WITH_BAD_FOOTER_PATH = "/pem/rsaPublicKeyWithBadFooter.pem";
	private static final String X509_CERTIFICATE_WITH_BAD_FOOTER_PATH = "/pem/x509CertificateWithBadFooter.pem";
	private static final String CERTIFICATE_SIGNING_REQUEST_WITH_BAD_FOOTER_PATH = "/pem/certificateSigningRequestWithBadFooter.pem";
	private static final String CORRUPT_RSA_PRIVATE_KEY_PATH = "/pem/corruptRsaPrivateKey.pem";
	private static final String CORRUPT_RSA_PUBLIC_KEY_PATH = "/pem/corruptRsaPublicKey.pem";
	private static final String CORRUPT_X509_CERTIFICATE_PATH = "/pem/corruptX509Certificate.pem";
	private static final String CORRUPT_CERTIFICATE_SIGNING_REQUEST_PATH = "/pem/corruptCertificateSigningRequest.pem";

	private static String whiteSpaceString;

	@BeforeAll
	static void setUp() {

		whiteSpaceString = CommonTestDataGenerator
				.getWhiteSpaceString(CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH));
	}

	static Stream<Arguments> convertPublicKeyToPem() {

		return Stream.of(arguments(null, "Public key is null."), arguments(new TestPublicKey(null), "Public key content is null."),
				arguments(new TestPublicKey(new byte[0]), "Public key content is empty."));
	}

	static Stream<Arguments> convertPrivateKeyToPem() {

		return Stream.of(arguments(null, "Private key is null."), arguments(new TestPrivateKey(null), "Private key content is null."),
				arguments(new TestPrivateKey(new byte[0]), "Private key content is empty."));
	}

	static Stream<Arguments> convertCertificateToPem() {
		return Stream.of(arguments(null, "Certificate is null."));
	}

	static Stream<Arguments> getPublicKeyFromPem() throws IOException {

		final String noHeaderPem = loadPem(RSA_PUBLIC_KEY_WITH_NO_HEADER_PATH);

		final String noFooterPem = loadPem(RSA_PUBLIC_KEY_WITH_NO_FOOTER_PATH);

		final String badHeaderPem = loadPem(RSA_PUBLIC_KEY_WITH_BAD_HEADER_PATH);

		final String badFooterPem = loadPem(RSA_PUBLIC_KEY_WITH_BAD_FOOTER_PATH);

		final String corruptPem = loadPem(CORRUPT_RSA_PUBLIC_KEY_PATH);

		return Stream.of(arguments(null, "Public key PEM string is null."), arguments("", "Public key PEM string is blank."),
				arguments(whiteSpaceString, "Public key PEM string is blank."),
				arguments(noHeaderPem, "Public key PEM string does not contain valid header."),
				arguments(noFooterPem, "Public key PEM string does not contain valid footer."),
				arguments(badHeaderPem, "Public key PEM string does not contain valid header."),
				arguments(badFooterPem, "Public key PEM string does not contain valid footer."),
				arguments(corruptPem, "Could not convert PEM string "));
	}

	static Stream<Arguments> getPrivateKeyFromPem() throws IOException {

		final String noHeaderPem = loadPem(RSA_PRIVATE_KEY_WITH_NO_HEADER_PATH);

		final String noFooterPem = loadPem(RSA_PRIVATE_KEY_WITH_NO_FOOTER_PATH);

		final String badHeaderPem = loadPem(RSA_PRIVATE_KEY_WITH_BAD_HEADER_PATH);

		final String badFooterPem = loadPem(RSA_PRIVATE_KEY_WITH_BAD_FOOTER_PATH);

		final String corruptPem = loadPem(CORRUPT_RSA_PRIVATE_KEY_PATH);

		return Stream.of(arguments(null, "Private key PEM string is null."), arguments("", "Private key PEM string is blank."),
				arguments(whiteSpaceString, "Private key PEM string is blank."),
				arguments(noHeaderPem, "Private key PEM string does not contain valid header."),
				arguments(noFooterPem, "Private key PEM string does not contain valid footer."),
				arguments(badHeaderPem, "Private key PEM string does not contain valid header."),
				arguments(badFooterPem, "Private key PEM string does not contain valid footer."),
				arguments(corruptPem, "Could not convert PEM string "));
	}

	static Stream<Arguments> getCertificateFromPem() throws IOException {

		final String noHeaderPem = loadPem(X509_CERTIFICATE_WITH_NO_HEADER_PATH);

		final String noFooterPem = loadPem(X509_CERTIFICATE_WITH_NO_FOOTER_PATH);

		final String badHeaderPem = loadPem(X509_CERTIFICATE_WITH_BAD_HEADER_PATH);

		final String badFooterPem = loadPem(X509_CERTIFICATE_WITH_BAD_FOOTER_PATH);

		final String corruptPem = loadPem(CORRUPT_X509_CERTIFICATE_PATH);

		return Stream.of(arguments(null, "Certificate PEM string is null."), arguments("", "Certificate PEM string is blank."),
				arguments(whiteSpaceString, "Certificate PEM string is blank."),
				arguments(noHeaderPem, "Certificate PEM string does not contain valid header."),
				arguments(noFooterPem, "Certificate PEM string does not contain valid footer."),
				arguments(badHeaderPem, "Certificate PEM string does not contain valid header."),
				arguments(badFooterPem, "Certificate PEM string does not contain valid footer."),
				arguments(corruptPem, "Could not convert PEM string "));
	}

	static Stream<Arguments> getCertificateSigningRequestFromPem() throws IOException {

		final String noHeaderPem = loadPem(CERTIFICATE_SIGNING_REQUEST_WITH_NO_HEADER_PATH);

		final String noFooterPem = loadPem(CERTIFICATE_SIGNING_REQUEST_WITH_NO_FOOTER_PATH);

		final String badHeaderPem = loadPem(CERTIFICATE_SIGNING_REQUEST_WITH_BAD_HEADER_PATH);

		final String badFooterPem = loadPem(CERTIFICATE_SIGNING_REQUEST_WITH_BAD_FOOTER_PATH);

		final String corruptPem = loadPem(CORRUPT_CERTIFICATE_SIGNING_REQUEST_PATH);

		return Stream.of(arguments(null, "Certificate signing request PEM string is null."),
				arguments("", "Certificate signing request PEM string is blank."),
				arguments(whiteSpaceString, "Certificate signing request PEM string is blank."),
				arguments(noHeaderPem, "Certificate signing request PEM string does not contain valid header."),
				arguments(noFooterPem, "Certificate signing request PEM string does not contain valid footer."),
				arguments(badHeaderPem, "Certificate signing request PEM string does not contain valid header."),
				arguments(badFooterPem, "Certificate signing request PEM string does not contain valid footer."),
				arguments(corruptPem, "Could not convert PEM string "));
	}

	private static String loadPem(final String resource) throws IOException {
		final StringBuilder pem = new StringBuilder();
		try (final InputStream stream = PemUtilsValidationTest.class.getResourceAsStream(resource);
				final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			int c;
			while ((c = reader.read()) != -1) {
				pem.append((char) c);
			}
		}
		return pem.toString();
	}

	@ParameterizedTest
	@MethodSource("convertPublicKeyToPem")
	void testPublicKeyToPemConversionValidation(final PublicKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> PemUtils.publicKeyToPem(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("convertPrivateKeyToPem")
	void testPrivateKeyToPemConversionValidation(final PrivateKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> PemUtils.privateKeyToPem(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("convertCertificateToPem")
	void testCertificateToPemConversionValidation(final Certificate cert, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> PemUtils.certificateToPem(cert));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getPublicKeyFromPem")
	void testPublicKeyFromPemRetrievalValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> PemUtils.publicKeyFromPem(pemStr));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("getPrivateKeyFromPem")
	void testPrivateKeyFromPemRetrievalValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> PemUtils.privateKeyFromPem(pemStr));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("getCertificateFromPem")
	void testCertificateFromPemRetrievalValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> PemUtils.certificateFromPem(pemStr));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("getCertificateSigningRequestFromPem")
	void testCertificateSigningRequestFromPemRetrievalValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> PemUtils.certificateSigningRequestFromPem(pemStr));
		assertTrue(exception.getMessage().contains(errorMsg));
	}
}
