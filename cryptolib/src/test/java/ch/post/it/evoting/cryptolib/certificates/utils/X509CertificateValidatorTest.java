/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import java.security.KeyPair;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationData;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationType;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;

/**
 * Tests of X509 certificate validator.
 */
class X509CertificateValidatorTest {

	private static KeyPair rootKeyPair;

	private static KeyPair intermediateKeyPair;

	private static KeyPair keyPair;

	private static X509DistinguishedName rootSubjectDn;

	private static X509DistinguishedName intermediateSubjectDn;

	private static X509DistinguishedName subjectDn;

	private static X509CertificateValidationType[] validationTypes;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		rootKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		rootSubjectDn = X509CertificateTestDataGenerator.getRootDistinguishedName();

		intermediateKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		intermediateSubjectDn = X509CertificateTestDataGenerator.getIntermediateDistinguishedName();

		keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		subjectDn = X509CertificateTestDataGenerator.getDistinguishedName();

		validationTypes = new X509CertificateValidationType[5];
		validationTypes[0] = X509CertificateValidationType.DATE;
		validationTypes[1] = X509CertificateValidationType.SUBJECT;
		validationTypes[2] = X509CertificateValidationType.ISSUER;
		validationTypes[3] = X509CertificateValidationType.SIGNATURE;
		validationTypes[4] = X509CertificateValidationType.KEY_TYPE;
	}

	@Test
	void checkRootAuthorityCertificateValidityWithCorrectData() throws GeneralCryptoLibException {

		final CryptoX509Certificate rootCertificate = (CryptoX509Certificate) X509CertificateTestDataGenerator.getRootAuthorityX509Certificate(rootKeyPair);

		final Date date = new Date(System.currentTimeMillis());
		final X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(rootSubjectDn)
				.addIssuerDn(rootSubjectDn).addKeyType(X509CertificateType.CERTIFICATE_AUTHORITY).addCaPublicKey(rootKeyPair.getPublic()).build();

		final X509CertificateValidator validator = new X509CertificateValidator(rootCertificate, validationData, validationTypes);

		final X509CertificateValidationResult validationResult = validator.validate();

		Assertions.assertTrue(validationResult.isValidated(), "Unexpected validation result. expected:<success> but was:<failure>");
		Assertions.assertEquals(0, validationResult.getFailedValidationTypes().size(), "Unexpected number of failed validations");
	}

	@Test
	void checkIntermediateAuthorityCertificateValidityWithCorrectData() throws GeneralCryptoLibException {

		final CryptoX509Certificate intermediateCertificate = (CryptoX509Certificate) X509CertificateTestDataGenerator
				.getIntermediateAuthorityX509Certificate(intermediateKeyPair, rootKeyPair);

		final Date date = new Date(System.currentTimeMillis());
		final X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(intermediateSubjectDn)
				.addIssuerDn(rootSubjectDn).addKeyType(X509CertificateType.CERTIFICATE_AUTHORITY).addCaPublicKey(rootKeyPair.getPublic()).build();

		final X509CertificateValidator validator = new X509CertificateValidator(intermediateCertificate, validationData, validationTypes);

		final X509CertificateValidationResult validationResult = validator.validate();

		Assertions.assertTrue(validationResult.isValidated(), "Unexpected validation result. expected:<success> but was:<failure>");
		Assertions.assertEquals(0, validationResult.getFailedValidationTypes().size(), "Unexpected number of failed validations");
	}

	@Test
	void checkSignCertificateValidityWithCorrectData() throws GeneralCryptoLibException {

		final CryptoX509Certificate signCertificate = (CryptoX509Certificate) X509CertificateTestDataGenerator.getSignX509Certificate(keyPair, rootKeyPair);

		final Date date = new Date(System.currentTimeMillis());
		final X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(subjectDn)
				.addIssuerDn(rootSubjectDn).addKeyType(X509CertificateType.SIGN).addCaPublicKey(rootKeyPair.getPublic()).build();

		final X509CertificateValidator validator = new X509CertificateValidator(signCertificate, validationData, validationTypes);

		final X509CertificateValidationResult validationResult = validator.validate();

		Assertions.assertTrue(validationResult.isValidated(), "Unexpected validation result. expected:<success> but was:<failure>");
		Assertions.assertEquals(0, validationResult.getFailedValidationTypes().size(), "Unexpected number of failed validations");
	}

	@Test
	void checkEncryptionCertificateValidityWithCorrectData() throws GeneralCryptoLibException {

		final CryptoX509Certificate encryptionCertificate = (CryptoX509Certificate) X509CertificateTestDataGenerator
				.getEncryptionX509Certificate(keyPair, rootKeyPair);

		final Date date = new Date(System.currentTimeMillis());
		final X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(subjectDn)
				.addIssuerDn(rootSubjectDn).addKeyType(X509CertificateType.ENCRYPT).addCaPublicKey(rootKeyPair.getPublic()).build();

		final X509CertificateValidator validator = new X509CertificateValidator(encryptionCertificate, validationData, validationTypes);

		final X509CertificateValidationResult validationResult = validator.validate();

		Assertions.assertTrue(validationResult.isValidated(), "Unexpected validation result. expected:<success> but was:<failure>");
		Assertions.assertEquals(0, validationResult.getFailedValidationTypes().size(), "Unexpected number of failed validations");
	}
}
