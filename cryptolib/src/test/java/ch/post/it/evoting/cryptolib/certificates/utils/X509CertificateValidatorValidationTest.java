/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationData;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationType;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPublicKey;

class X509CertificateValidatorValidationTest {

	private static KeyPair rootKeyPair;
	private static PublicKey rootPublicKey;
	private static KeyPair keyPair;
	private static PublicKey publicKey;
	private static X509DistinguishedName rootSubjectDn;
	private static X509DistinguishedName subjectDn;
	private static Date validDate;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		rootKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		rootPublicKey = rootKeyPair.getPublic();
		rootSubjectDn = X509CertificateTestDataGenerator.getRootDistinguishedName();

		keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		publicKey = keyPair.getPublic();
		subjectDn = X509CertificateTestDataGenerator.getDistinguishedName();

		validDate = X509CertificateTestDataGenerator.getDateWithinValidityPeriod();
	}

	static Stream<Arguments> createX509CertificateIssuerPublicKeyValidation() {
		return Stream.of(arguments(null, "Issuer public key is null."), arguments(new TestPublicKey(null), "Issuer public key content is null."),
				arguments(new TestPublicKey(new byte[0]), "Issuer public key content is empty."));
	}

	static Stream<Arguments> createX509CertificateValidator() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate certificate = X509CertificateTestDataGenerator.getSignX509Certificate(keyPair, rootKeyPair);

		final X509CertificateValidationData validationData = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.SIGN, rootPublicKey);

		final X509CertificateValidationType[] validationTypes = X509CertificateTestDataGenerator.getCertificateValidationTypes();

		final X509CertificateValidationType[] validationTypesWithNullValue = X509CertificateTestDataGenerator.getCertificateValidationTypes();
		validationTypesWithNullValue[0] = null;

		return Stream.of(arguments(null, validationData, validationTypes, "Certificate to validate is null."),
				arguments(certificate, null, validationTypes, "Validation data is null."),
				arguments(certificate, validationData, null, "Validation type array is null."),
				arguments(certificate, validationData, new X509CertificateValidationType[0], "Validation type array is empty."),
				arguments(certificate, validationData, validationTypesWithNullValue, "A validation type is null."));
	}

	static Stream<Arguments> validateRootAuthorityCertificate() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate rootCertificate = X509CertificateTestDataGenerator.getRootAuthorityX509Certificate(rootKeyPair);

		final Date invalidDate = X509CertificateTestDataGenerator.getDateOutsideValidityPeriod();
		final X509CertificateValidationData validationDataWithInvalidDate = X509CertificateTestDataGenerator
				.getCertificateValidationData(invalidDate, rootSubjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidSubject = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidIssuer = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, rootSubjectDn, subjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidKeyUsage = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, rootSubjectDn, rootSubjectDn, X509CertificateType.SIGN, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidPublicKey = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, rootSubjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, publicKey);

		return Stream.of(arguments(rootCertificate, validationDataWithInvalidDate, X509CertificateValidationType.DATE,
						"Validation failed for " + X509CertificateValidationType.DATE.name()),
				arguments(rootCertificate, validationDataWithInvalidSubject, X509CertificateValidationType.SUBJECT,
						"Validation failed for " + X509CertificateValidationType.SUBJECT.name()),
				arguments(rootCertificate, validationDataWithInvalidIssuer, X509CertificateValidationType.ISSUER,
						"Validation failed for " + X509CertificateValidationType.ISSUER.name()),
				arguments(rootCertificate, validationDataWithInvalidKeyUsage, X509CertificateValidationType.KEY_TYPE,
						"Validation failed for " + X509CertificateValidationType.KEY_TYPE.name()),
				arguments(rootCertificate, validationDataWithInvalidPublicKey, X509CertificateValidationType.SIGNATURE,
						"Validation failed for " + X509CertificateValidationType.SIGNATURE.name()));
	}

	static Stream<Arguments> validateIntermediateAuthorityCertificate() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate intermediateCertificate = X509CertificateTestDataGenerator
				.getIntermediateAuthorityX509Certificate(keyPair, rootKeyPair);

		final Date invalidDate = X509CertificateTestDataGenerator.getDateOutsideValidityPeriod();
		final X509CertificateValidationData validationDataWithInvalidDate = X509CertificateTestDataGenerator
				.getCertificateValidationData(invalidDate, subjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidSubject = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, rootSubjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidIssuer = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, subjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidKeyUsage = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.SIGN, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidPublicKey = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, publicKey);

		return Stream.of(arguments(intermediateCertificate, validationDataWithInvalidDate, X509CertificateValidationType.DATE,
						"Validation failed for " + X509CertificateValidationType.DATE.name()),
				arguments(intermediateCertificate, validationDataWithInvalidSubject, X509CertificateValidationType.SUBJECT,
						"Validation failed for " + X509CertificateValidationType.SUBJECT.name()),
				arguments(intermediateCertificate, validationDataWithInvalidIssuer, X509CertificateValidationType.ISSUER,
						"Validation failed for " + X509CertificateValidationType.ISSUER.name()),
				arguments(intermediateCertificate, validationDataWithInvalidKeyUsage, X509CertificateValidationType.KEY_TYPE,
						"Validation failed for " + X509CertificateValidationType.KEY_TYPE.name()),
				arguments(intermediateCertificate, validationDataWithInvalidPublicKey, X509CertificateValidationType.SIGNATURE,
						"Validation failed for " + X509CertificateValidationType.SIGNATURE.name()));
	}

	static Stream<Arguments> validateSignCertificate() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate signCertificate = X509CertificateTestDataGenerator.getSignX509Certificate(keyPair, rootKeyPair);

		final Date invalidDate = X509CertificateTestDataGenerator.getDateOutsideValidityPeriod();
		final X509CertificateValidationData validationDataWithInvalidDate = X509CertificateTestDataGenerator
				.getCertificateValidationData(invalidDate, subjectDn, rootSubjectDn, X509CertificateType.SIGN, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidSubject = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, rootSubjectDn, rootSubjectDn, X509CertificateType.SIGN, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidIssuer = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, subjectDn, X509CertificateType.SIGN, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidKeyUsage = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidPublicKey = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.SIGN, publicKey);

		return Stream.of(arguments(signCertificate, validationDataWithInvalidDate, X509CertificateValidationType.DATE,
						"Validation failed for " + X509CertificateValidationType.DATE.name()),
				arguments(signCertificate, validationDataWithInvalidSubject, X509CertificateValidationType.SUBJECT,
						"Validation failed for " + X509CertificateValidationType.SUBJECT.name()),
				arguments(signCertificate, validationDataWithInvalidIssuer, X509CertificateValidationType.ISSUER,
						"Validation failed for " + X509CertificateValidationType.ISSUER.name()),
				arguments(signCertificate, validationDataWithInvalidKeyUsage, X509CertificateValidationType.KEY_TYPE,
						"Validation failed for " + X509CertificateValidationType.KEY_TYPE.name()),
				arguments(signCertificate, validationDataWithInvalidPublicKey, X509CertificateValidationType.SIGNATURE,
						"Validation failed for " + X509CertificateValidationType.SIGNATURE.name()));
	}

	static Stream<Arguments> validateEncryptionCertificate() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate encryptionCertificate = X509CertificateTestDataGenerator.getEncryptionX509Certificate(keyPair, rootKeyPair);

		final Date invalidDate = X509CertificateTestDataGenerator.getDateOutsideValidityPeriod();
		final X509CertificateValidationData validationDataWithInvalidDate = X509CertificateTestDataGenerator
				.getCertificateValidationData(invalidDate, subjectDn, rootSubjectDn, X509CertificateType.ENCRYPT, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidSubject = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, rootSubjectDn, rootSubjectDn, X509CertificateType.ENCRYPT, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidIssuer = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, subjectDn, X509CertificateType.SIGN, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidKeyUsage = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);

		final X509CertificateValidationData validationDataWithInvalidPublicKey = X509CertificateTestDataGenerator
				.getCertificateValidationData(validDate, subjectDn, rootSubjectDn, X509CertificateType.ENCRYPT, publicKey);

		return Stream.of(arguments(encryptionCertificate, validationDataWithInvalidDate, X509CertificateValidationType.DATE,
						"Validation failed for " + X509CertificateValidationType.DATE.name()),
				arguments(encryptionCertificate, validationDataWithInvalidSubject, X509CertificateValidationType.SUBJECT,
						"Validation failed for " + X509CertificateValidationType.SUBJECT.name()),
				arguments(encryptionCertificate, validationDataWithInvalidIssuer, X509CertificateValidationType.ISSUER,
						"Validation failed for " + X509CertificateValidationType.ISSUER.name()),
				arguments(encryptionCertificate, validationDataWithInvalidKeyUsage, X509CertificateValidationType.KEY_TYPE,
						"Validation failed for " + X509CertificateValidationType.KEY_TYPE.name()),
				arguments(encryptionCertificate, validationDataWithInvalidPublicKey, X509CertificateValidationType.SIGNATURE,
						"Validation failed for " + X509CertificateValidationType.SIGNATURE.name()));
	}

	private static void validateCertificate(final CryptoX509Certificate certificate, final X509CertificateValidationData validationData,
			final X509CertificateValidationType failedValidationType) throws GeneralCryptoLibException {

		final X509CertificateValidator validator = new X509CertificateValidator(certificate, validationData, failedValidationType);

		final X509CertificateValidationResult validationResult = validator.validate();

		if (!validationResult.isValidated() && validationResult.getFailedValidationTypes().size() == 1 && (
				validationResult.getFailedValidationTypes().get(0) == failedValidationType)) {
			throw new GeneralCryptoLibException("Validation failed for " + failedValidationType.name());
		}
	}

	@Test
	void testX509CertificateValidationDateCreationValidation() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateValidationData.Builder().addDate(null));
		assertEquals("Date is null.", exception.getMessage());
	}

	@Test
	void testX509CertificateValidationSubjectCreationValidation() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateValidationData.Builder().addSubjectDn(null));
		assertEquals("Subject distinguished name is null.", exception.getMessage());
	}

	@Test
	void testX509CertificateValidationIssuerCreationValidation() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateValidationData.Builder().addIssuerDn(null));
		assertEquals("Issuer distinguished name is null.", exception.getMessage());
	}

	@Test
	void testX509CertificateValidationCertificateTypeCreationValidation() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateValidationData.Builder().addKeyType(null));
		assertEquals("Certificate type is null.", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createX509CertificateIssuerPublicKeyValidation")
	void testX509CertificateValidationDataCreationValidation(final PublicKey issuerPublicKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateValidationData.Builder().addCaPublicKey(issuerPublicKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createX509CertificateValidator")
	void testX509CertificateValidatorCreationValidation(final CryptoX509Certificate cryptoX509Cert,
			final X509CertificateValidationData validationData, final X509CertificateValidationType[] validationTypes, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateValidator(cryptoX509Cert, validationData, validationTypes));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("validateRootAuthorityCertificate")
	void testRootAuthorityCertificateValidation(final CryptoX509Certificate certificate, final X509CertificateValidationData validationData,
			final X509CertificateValidationType failedValidationType, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> validateCertificate(certificate, validationData, failedValidationType));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("validateIntermediateAuthorityCertificate")
	void testIntermediateAuthorityCertificateValidation(final CryptoX509Certificate certificate, final X509CertificateValidationData validationData,
			final X509CertificateValidationType failedValidationType, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> validateCertificate(certificate, validationData, failedValidationType));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("validateSignCertificate")
	void testSignCertificateValidation(final CryptoX509Certificate certificate, final X509CertificateValidationData validationData,
			final X509CertificateValidationType failedValidationType, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> validateCertificate(certificate, validationData, failedValidationType));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("validateEncryptionCertificate")
	void testEncryptionCertificateValidation(final CryptoX509Certificate certificate, final X509CertificateValidationData validationData,
			final X509CertificateValidationType failedValidationType, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> validateCertificate(certificate, validationData, failedValidationType));
		assertEquals(errorMsg, exception.getMessage());
	}
}
