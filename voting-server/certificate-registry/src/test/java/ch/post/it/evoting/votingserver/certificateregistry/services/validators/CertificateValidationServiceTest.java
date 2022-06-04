/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.services.validators;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationData;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationType;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateValidator;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.platform.CrCertificateValidationServiceImpl;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationResult;

public class CertificateValidationServiceTest {

	private static CrCertificateValidationServiceImpl target;

	private static KeyPair rootKeyPair;

	private static KeyPair userKeyPair;

	private static String rootSubjectCn;

	private static String rootSubjectOrgUnit;

	private static String rootSubjectOrg;

	private static String rootSubjectLocality;

	private static String rootSubjectCountry;

	private static X509DistinguishedName rootSubjectDn;

	private static int rootNumYearsValidity;

	private static ValidityDates rootValidityDates;

	private static String userSubjectCn;

	private static String userSubjectOrgUnit;

	private static String userSubjectOrg;

	private static String userSubjectLocality;

	private static String userSubjectCountry;

	private static X509DistinguishedName userSubjectDn;

	private static String userIssuerCn;

	private static String userIssuerOrgUnit;

	private static String userIssuerOrg;

	private static String userIssuerLocality;

	private static String userIssuerCountry;

	private static int userNumYearsValidity;

	private static X509DistinguishedName userIssuerDn;

	private static ValidityDates userValidityDates;

	private static AsymmetricService asymmetricService;

	private static CryptoX509Certificate caCryptoCert;

	private static CryptoX509Certificate signCryptoCert;

	private static CryptoX509Certificate encryptCryptoCert;

	@BeforeClass
	public static void setUp() throws GeneralCryptoLibException {

		target = new CrCertificateValidationServiceImpl();

		asymmetricService = new AsymmetricService();

		CertificatesService certificatesService = new CertificatesService();

		retrieveKeyPairs();

		retrieveInputParameters();

		// /////////////////////////////////////////////
		//
		// Root certificate data
		//
		// /////////////////////////////////////////////

		createRootDistinguishedNames();

		createRootValidityDates();

		RootCertificateData rootCertificateData = new RootCertificateData();
		rootCertificateData.setSubjectPublicKey(rootKeyPair.getPublic());
		rootCertificateData.setSubjectDn(rootSubjectDn);
		rootCertificateData.setValidityDates(rootValidityDates);

		caCryptoCert = (CryptoX509Certificate) certificatesService.createRootAuthorityX509Certificate(rootCertificateData, rootKeyPair.getPrivate());

		// /////////////////////////////////////////////
		//
		// User certificate data
		//
		// /////////////////////////////////////////////

		createUserDistinguishedNames();

		createUserValidityDates();

		CertificateData userCertificate = new CertificateData();
		userCertificate.setSubjectPublicKey(userKeyPair.getPublic());
		userCertificate.setIssuerDn(userIssuerDn);
		userCertificate.setSubjectDn(userSubjectDn);
		userCertificate.setValidityDates(userValidityDates);

		signCryptoCert = (CryptoX509Certificate) certificatesService.createSignX509Certificate(userCertificate, rootKeyPair.getPrivate());

		encryptCryptoCert = (CryptoX509Certificate) certificatesService.createEncryptionX509Certificate(userCertificate, rootKeyPair.getPrivate());
	}

	// /////////////////////////////////////////////
	//
	// Tests using new validator
	//
	// /////////////////////////////////////////////

	private static void retrieveKeyPairs() {

		rootKeyPair = asymmetricService.getKeyPairForSigning();
		userKeyPair = asymmetricService.getKeyPairForSigning();
	}

	private static void retrieveInputParameters() {

		// Retrieve root certificate properties.
		Properties properties = loadProperties(X509CertificateTestConstants.ROOT_CERTIFICATE_PROPERTIES_FILE_PATH);
		rootSubjectCn = properties.getProperty(X509CertificateTestConstants.SUBJECT_COMMON_NAME_PROPERTY_NAME);
		rootSubjectOrgUnit = properties.getProperty(X509CertificateTestConstants.SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME);
		rootSubjectOrg = properties.getProperty(X509CertificateTestConstants.SUBJECT_ORGANIZATION_PROPERTY_NAME);
		rootSubjectLocality = properties.getProperty(X509CertificateTestConstants.SUBJECT_LOCALITY_PROPERTY_NAME);
		rootSubjectCountry = properties.getProperty(X509CertificateTestConstants.SUBJECT_COUNTRY_PROPERTY_NAME);
		rootNumYearsValidity = Integer.parseInt(properties.getProperty(X509CertificateTestConstants.NUMBER_YEARS_VALIDITY_PROPERTY_NAME));

		// Retrieve user certificate properties.
		properties = loadProperties(X509CertificateTestConstants.USER_CERTIFICATE_PROPERTIES_FILE_PATH);
		userSubjectCn = properties.getProperty(X509CertificateTestConstants.SUBJECT_COMMON_NAME_PROPERTY_NAME);
		userSubjectOrgUnit = properties.getProperty(X509CertificateTestConstants.SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME);
		userSubjectOrg = properties.getProperty(X509CertificateTestConstants.SUBJECT_ORGANIZATION_PROPERTY_NAME);
		userSubjectLocality = properties.getProperty(X509CertificateTestConstants.SUBJECT_LOCALITY_PROPERTY_NAME);
		userSubjectCountry = properties.getProperty(X509CertificateTestConstants.SUBJECT_COUNTRY_PROPERTY_NAME);
		userIssuerCn = properties.getProperty(X509CertificateTestConstants.ISSUER_COMMON_NAME_PROPERTY_NAME);
		userIssuerOrgUnit = properties.getProperty(X509CertificateTestConstants.ISSUER_ORGANIZATIONAL_UNIT_PROPERTY_NAME);
		userIssuerOrg = properties.getProperty(X509CertificateTestConstants.ISSUER_ORGANIZATION_PROPERTY_NAME);
		userIssuerLocality = properties.getProperty(X509CertificateTestConstants.ISSUER_LOCALITY_PROPERTY_NAME);
		userIssuerCountry = properties.getProperty(X509CertificateTestConstants.ISSUER_COUNTRY_PROPERTY_NAME);
		userNumYearsValidity = Integer.parseInt(properties.getProperty(X509CertificateTestConstants.NUMBER_YEARS_VALIDITY_PROPERTY_NAME));
	}

	private static Properties loadProperties(String resource) {
		Properties properties = new Properties();
		try (InputStream stream = CertificateValidationServiceTest.class.getClassLoader().getResourceAsStream(resource);
				Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			properties.load(reader);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load properties.", e);
		}
		return properties;
	}

	private static void createRootValidityDates() throws GeneralCryptoLibException {

		Date rootNotBefore = new Date(System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, rootNumYearsValidity);
		Date rootNotAfter = calendar.getTime();
		rootValidityDates = new ValidityDates(rootNotBefore, rootNotAfter);
	}

	private static void createRootDistinguishedNames() throws GeneralCryptoLibException {

		rootSubjectDn = new X509DistinguishedName.Builder(rootSubjectCn, rootSubjectCountry).addOrganizationalUnit(rootSubjectOrgUnit)
				.addOrganization(rootSubjectOrg).addLocality(rootSubjectLocality).build();
	}

	// /////////////////////////////////////////////
	//
	// Tests using cryptolib validator
	//
	// /////////////////////////////////////////////

	private static void createUserDistinguishedNames() throws GeneralCryptoLibException {

		userSubjectDn = new X509DistinguishedName.Builder(userSubjectCn, userSubjectCountry).addOrganizationalUnit(userSubjectOrgUnit)
				.addOrganization(userSubjectOrg).addLocality(userSubjectLocality).build();
		userIssuerDn = new X509DistinguishedName.Builder(userIssuerCn, userIssuerCountry).addOrganizationalUnit(userIssuerOrgUnit)
				.addOrganization(userIssuerOrg).addLocality(userIssuerLocality).build();
	}

	private static void createUserValidityDates() throws GeneralCryptoLibException {

		Calendar calendar;
		Date userNotBefore = new Date(System.currentTimeMillis());
		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, userNumYearsValidity);
		Date userNotAfter = calendar.getTime();
		userValidityDates = new ValidityDates(userNotBefore, userNotAfter);
	}

	@Test
	public void whenValidateUserSignCertificateThenOK() throws GeneralCryptoLibException, CryptographicOperationException {

		CertificateValidationResult validationResult = target.validateCertificate(signCryptoCert.getCertificate(), caCryptoCert.getCertificate());

		assertTrue(validationResult.isValid() && validationResult.getValidationErrorMessages().isEmpty());
	}

	@Test
	public void whenValidateUserEncryptionCertificateThenOK() throws GeneralCryptoLibException, CryptographicOperationException {

		CertificateValidationResult validationResult = target.validateCertificate(encryptCryptoCert.getCertificate(), caCryptoCert.getCertificate());

		assertTrue(validationResult.isValid() && validationResult.getValidationErrorMessages().isEmpty());
	}

	@Test
	public void whenValidateRootCertificateUsingUserCertificateMethodThenExpectedFailures()
			throws GeneralCryptoLibException, CryptographicOperationException {

		CertificateValidationResult validationResult = target.validateCertificate(caCryptoCert.getCertificate(), caCryptoCert.getCertificate());

		assertTrue(!validationResult.isValid() && validationResult.getValidationErrorMessages().size() == 1);
	}

	@Test
	public void whenValidateRootCertificateThenOK() throws CryptographicOperationException {

		X509CertificateValidationResult validationResult = target.validateRootCertificate(caCryptoCert.getCertificate());

		assertTrue(validationResult.isValidated() && validationResult.getFailedValidationTypes().isEmpty());
	}

	@Test
	public void whenValidateSignCertificateAsRootCertificateThenExpected() throws CryptographicOperationException {

		X509CertificateValidationResult validationResult = target.validateRootCertificate(signCryptoCert.getCertificate());

		assertTrue(!validationResult.isValidated() && validationResult.getFailedValidationTypes().size() == 2);
		assertTrue(validationResult.getFailedValidationTypes().contains(X509CertificateValidationType.SIGNATURE));
		assertTrue(validationResult.getFailedValidationTypes().contains(X509CertificateValidationType.KEY_TYPE));
	}

	@Test
	public void checkCertAuthCertificateValidityWithCorrectData() throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(rootSubjectDn)
				.addIssuerDn(rootSubjectDn).addKeyType(X509CertificateType.CERTIFICATE_AUTHORITY).addCaPublicKey(rootKeyPair.getPublic()).build();

		X509CertificateValidator validator = new X509CertificateValidator(caCryptoCert, validationData, X509CertificateValidationType.SUBJECT,
				X509CertificateValidationType.ISSUER, X509CertificateValidationType.KEY_TYPE, X509CertificateValidationType.SIGNATURE);

		X509CertificateValidationResult validationResult = validator.validate();

		assert (validationResult.isValidated() && validationResult.getFailedValidationTypes().isEmpty());
	}

	@Test
	public void checkSignCertificateValidityWithCorrectData() throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(userSubjectDn)
				.addIssuerDn(userIssuerDn).addKeyType(X509CertificateType.SIGN).addCaPublicKey(rootKeyPair.getPublic()).build();

		X509CertificateValidator validator = new X509CertificateValidator(signCryptoCert, validationData, X509CertificateValidationType.SUBJECT,
				X509CertificateValidationType.ISSUER, X509CertificateValidationType.KEY_TYPE, X509CertificateValidationType.SIGNATURE);

		X509CertificateValidationResult validationResult = validator.validate();

		assert (validationResult.isValidated() && validationResult.getFailedValidationTypes().isEmpty());
	}

	@Test
	public void checkEncryptCertificateValidityWithCorrectData() throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(userSubjectDn)
				.addIssuerDn(userIssuerDn).addKeyType(X509CertificateType.ENCRYPT).addCaPublicKey(rootKeyPair.getPublic()).build();

		X509CertificateValidator validator = new X509CertificateValidator(encryptCryptoCert, validationData, X509CertificateValidationType.SUBJECT,
				X509CertificateValidationType.ISSUER, X509CertificateValidationType.KEY_TYPE, X509CertificateValidationType.SIGNATURE);

		X509CertificateValidationResult validationResult = validator.validate();

		assert (validationResult.isValidated() && validationResult.getFailedValidationTypes().isEmpty());
	}

	@Test
	public void checkCertAuthCertificateValidityWithIncorrectData() throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(rootSubjectDn)
				.addIssuerDn(rootSubjectDn).addKeyType(X509CertificateType.ENCRYPT).addCaPublicKey(rootKeyPair.getPublic()).build();

		X509CertificateValidator validator = new X509CertificateValidator(caCryptoCert, validationData, X509CertificateValidationType.SUBJECT,
				X509CertificateValidationType.ISSUER, X509CertificateValidationType.KEY_TYPE, X509CertificateValidationType.SIGNATURE);

		X509CertificateValidationResult validationResult = validator.validate();

		assert (!validationResult.isValidated() && validationResult.getFailedValidationTypes().size() == 1 && validationResult
				.getFailedValidationTypes().get(0).equals(X509CertificateValidationType.KEY_TYPE));
	}

	@Test
	public void checkSignCertificateValidityWithIncorrectData() throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(userSubjectDn)
				.addIssuerDn(userIssuerDn).addKeyType(X509CertificateType.SIGN).addCaPublicKey(userKeyPair.getPublic()).build();

		X509CertificateValidator validator = new X509CertificateValidator(signCryptoCert, validationData, X509CertificateValidationType.SUBJECT,
				X509CertificateValidationType.ISSUER, X509CertificateValidationType.KEY_TYPE, X509CertificateValidationType.SIGNATURE);

		X509CertificateValidationResult validationResult = validator.validate();

		assert (!validationResult.isValidated() && validationResult.getFailedValidationTypes().size() == 1 && validationResult
				.getFailedValidationTypes().get(0).equals(X509CertificateValidationType.SIGNATURE));
	}

	@Test
	public void checkEncryptCertificateValidityWithIncorrectData() throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(userSubjectDn)
				.addIssuerDn(userSubjectDn).addKeyType(X509CertificateType.ENCRYPT).addCaPublicKey(rootKeyPair.getPublic()).build();

		X509CertificateValidator validator = new X509CertificateValidator(encryptCryptoCert, validationData, X509CertificateValidationType.SUBJECT,
				X509CertificateValidationType.ISSUER, X509CertificateValidationType.KEY_TYPE, X509CertificateValidationType.SIGNATURE);

		X509CertificateValidationResult validationResult = validator.validate();

		assert (!validationResult.isValidated() && validationResult.getFailedValidationTypes().size() == 1 && validationResult
				.getFailedValidationTypes().get(0).equals(X509CertificateValidationType.ISSUER));
	}
}
