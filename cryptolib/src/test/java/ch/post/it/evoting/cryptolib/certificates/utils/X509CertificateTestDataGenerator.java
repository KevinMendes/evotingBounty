/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import java.security.KeyPair;
import java.security.PublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Properties;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.BasicConstraintsExtension;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationData;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationType;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateTestConstants;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGeneratorFactory;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

/**
 * Utility to generate various types of certificate data needed by tests.
 */
public class X509CertificateTestDataGenerator {

	/**
	 * Generates a root certificate authority X509 certificate and encapsulates it in a {@code CryptoAPIX509Certificate} object.
	 *
	 * @param rootKeyPair the key pair of the root authority certificate.
	 * @return the generated certificate.
	 * @throws GeneralCryptoLibException if the certificate generation process fails.
	 */
	public static CryptoAPIX509Certificate getRootAuthorityX509Certificate(final KeyPair rootKeyPair) throws GeneralCryptoLibException {

		final RootCertificateData rootCertificateData = getRootCertificateData(rootKeyPair);

		return new CertificatesService().createRootAuthorityX509Certificate(rootCertificateData, rootKeyPair.getPrivate());
	}

	/**
	 * Generates a intermediate certificate authority X509 certificate and encapsulates it in a {@code CryptoAPIX509Certificate} object.
	 *
	 * @param keyPair       the key pair of the certificate.
	 * @param issuerKeyPair the issuer key pair of the certificate.
	 * @return the generated certificate.
	 * @throws GeneralCryptoLibException if the certificate generation process fails.
	 */
	public static CryptoAPIX509Certificate getIntermediateAuthorityX509Certificate(final KeyPair keyPair, final KeyPair issuerKeyPair)
			throws GeneralCryptoLibException {

		final CertificateData certificateData = getIntermediateCertificateData(keyPair);

		return new CertificatesService().createIntermediateAuthorityX509Certificate(certificateData, issuerKeyPair.getPrivate());
	}

	/**
	 * Generates a leaf X509 certificate and encapsulates it in a {@code CryptoAPIX509Certificate} object.
	 *
	 * @param keyPair       the key pair of the certificate.
	 * @param issuerKeyPair the issuer key pair of the certificate.
	 * @return the generated certificate.
	 * @throws GeneralCryptoLibException if the certificate generation process fails.
	 */
	public static CryptoAPIX509Certificate getLeafX509Certificate(final KeyPair keyPair, final KeyPair issuerKeyPair)
			throws GeneralCryptoLibException {

		final CertificateData certificateData = getLeafCertificateData(keyPair);

		return new CertificatesService().createSignX509Certificate(certificateData, issuerKeyPair.getPrivate());
	}

	/**
	 * Generates a signing X509 certificate and encapsulates it in a {@code CryptoAPIX509Certificate} object.
	 *
	 * @param keyPair       the key pair of the certificate.
	 * @param issuerKeyPair the issuer key pair of the certificate.
	 * @return the generated certificate.
	 * @throws GeneralCryptoLibException if the certificate generation process fails.
	 */
	public static CryptoAPIX509Certificate getSignX509Certificate(final KeyPair keyPair, final KeyPair issuerKeyPair)
			throws GeneralCryptoLibException {

		final CertificateData certificateData = getCertificateData(keyPair);

		return new CertificatesService().createSignX509Certificate(certificateData, issuerKeyPair.getPrivate());
	}

	/**
	 * Generates an encryption X509 certificate and encapsulates it in a {@code CryptoAPIX509Certificate} object.
	 *
	 * @param keyPair       the key pair of the certificate.
	 * @param issuerKeyPair the issuer key pair of the certificate.
	 * @return the generated certificate.
	 * @throws GeneralCryptoLibException if the certificate generation process fails.
	 */
	public static CryptoAPIX509Certificate getEncryptionX509Certificate(final KeyPair keyPair, final KeyPair issuerKeyPair)
			throws GeneralCryptoLibException {

		final CertificateData certificateData = getCertificateData(keyPair);

		return new CertificatesService().createEncryptionX509Certificate(certificateData, issuerKeyPair.getPrivate());
	}

	/**
	 * Generates an X509 certificate with no key usage extensions and encapsulates it in a {@code CryptoAPIX509Certificate} object.
	 *
	 * @param certificateData the certificate data, encapsulated in a {@code CertificateData} object.
	 * @param issuerKeyPair   the issuer key pair of the certificate. @return the generated certificate.
	 * @throws GeneralCryptoLibException if the certificate generation process fails.
	 */
	public static CryptoAPIX509Certificate getX509CertificateWithNoKeyUsage(final CertificateData certificateData, final KeyPair issuerKeyPair)
			throws GeneralCryptoLibException {

		final CryptoX509CertificateGenerator x509certificateGenerator = new X509CertificateGeneratorFactory().create();

		return x509certificateGenerator.generate(certificateData, new AbstractCertificateExtension[0], issuerKeyPair.getPrivate());
	}

	/**
	 * Generates a certificate authority X509 certificate with no key usage extensions and encapsulates it in a {@code CryptoAPIX509Certificate}
	 * object.
	 *
	 * @param certificateData the certificate data, encapsulated in a {@code CertificateData} object.
	 * @param issuerKeyPair   the issuer key pair of the certificate. @return the generated certificate.
	 * @throws GeneralCryptoLibException if the certificate generation process fails.
	 */
	public static CryptoAPIX509Certificate getCertificateAuthorityX509CertificateWithNoKeyUsage(final CertificateData certificateData,
			final KeyPair issuerKeyPair) throws GeneralCryptoLibException {

		final CryptoX509CertificateGenerator x509certificateGenerator = new X509CertificateGeneratorFactory().create();

		final AbstractCertificateExtension[] extensions = new AbstractCertificateExtension[] { new BasicConstraintsExtension(true) };

		return x509certificateGenerator.generate(certificateData, extensions, issuerKeyPair.getPrivate());
	}

	/**
	 * Generates the data needed to generate a root certificate authority X509 certificate and encapsulates it in a {@code RootCertificateData}
	 * object.
	 *
	 * @return the root certificate data.
	 * @throws GeneralCryptoLibException if the root certificate data generation process fails.
	 */
	public static RootCertificateData getRootCertificateData(final KeyPair keyPair) throws GeneralCryptoLibException {

		final RootCertificateData rootCertificateData = new RootCertificateData();

		rootCertificateData.setSubjectPublicKey(keyPair.getPublic());
		rootCertificateData.setSubjectDn(getRootDistinguishedName());
		rootCertificateData.setValidityDates(getRootValidityDates());

		return rootCertificateData;
	}

	/**
	 * Generates the data needed to generate an intermediate certificate authority X509 certificate and encapsulates it in a {@code CertificateData}
	 * object.
	 *
	 * @return the intermediate certificate data.
	 * @throws GeneralCryptoLibException if the intermediate certificate data generation process fails.
	 */
	public static CertificateData getIntermediateCertificateData(final KeyPair keyPair) throws GeneralCryptoLibException {

		final CertificateData certificateData = new CertificateData();

		certificateData.setSubjectPublicKey(keyPair.getPublic());
		certificateData.setSubjectDn(getIntermediateDistinguishedName());
		certificateData.setIssuerDn(getRootDistinguishedName());
		certificateData.setValidityDates(getIntermediateValidityDates());

		return certificateData;
	}

	/**
	 * Generates the data needed to generate a leaf X509 certificate and encapsulates it in a {@code CertificateData} object.
	 *
	 * @return the leaf certificate data.
	 * @throws GeneralCryptoLibException if the leaf certificate data generation process fails.
	 */
	public static CertificateData getLeafCertificateData(final KeyPair keyPair) throws GeneralCryptoLibException {

		final CertificateData certificateData = new CertificateData();

		certificateData.setSubjectPublicKey(keyPair.getPublic());
		certificateData.setSubjectDn(getLeafDistinguishedName());
		certificateData.setIssuerDn(getIntermediateDistinguishedName());
		certificateData.setValidityDates(getLeafValidityDates());

		return certificateData;
	}

	/**
	 * Generates the data needed to generate a signing or encryption X509 certificate and encapsulates it in a {@code CertificateData} object.
	 *
	 * @return the certificate data.
	 * @throws GeneralCryptoLibException if the certificate data generation process fails.
	 */
	public static CertificateData getCertificateData(final KeyPair keyPair) throws GeneralCryptoLibException {

		final CertificateData certificateData = new CertificateData();

		certificateData.setSubjectPublicKey(keyPair.getPublic());
		certificateData.setSubjectDn(getDistinguishedName());
		certificateData.setIssuerDn(getRootDistinguishedName());
		certificateData.setValidityDates(getValidityDates());

		return certificateData;
	}

	/**
	 * Generates the data needed to generate a certificate that has expired validity dates and encapsulates it in a {@code CertificateData} object
	 * (for negative testing purposes).
	 *
	 * @return the expired certificate data.
	 * @throws GeneralCryptoLibException if the certificate data generation process fails.
	 */
	public static CertificateData getExpiredCertificateData(final KeyPair keyPair) throws GeneralCryptoLibException {

		final CertificateData certificateData = new CertificateData();

		certificateData.setSubjectPublicKey(keyPair.getPublic());
		certificateData.setSubjectDn(getDistinguishedName());
		certificateData.setIssuerDn(getRootDistinguishedName());
		certificateData.setValidityDates(getExpiredValidityDates());

		return certificateData;
	}

	/**
	 * Generates an X509 distinguished name for a root certificate authority and encapsulates it in a {@code X509DistinguishedName} object.
	 *
	 * @return the X509 distinguished name.
	 * @throws GeneralCryptoLibException if the certificate authority properties cannot be retrieved.
	 */
	public static X509DistinguishedName getRootDistinguishedName() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.ROOT_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateDistinguishedName(config);
	}

	/**
	 * Generates an X509 distinguished name for an intermediate certificate authority and encapsulates it in a {@code X509DistinguishedName} object.
	 *
	 * @return the X509 distinguished name.
	 * @throws GeneralCryptoLibException if the intermediate certificate properties cannot be retrieved.
	 */
	public static X509DistinguishedName getIntermediateDistinguishedName() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.INTERMEDIATE_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateDistinguishedName(config);
	}

	/**
	 * Generates an X509 distinguished name for a leaf certificate and encapsulates it in a {@code X509DistinguishedName} object.
	 *
	 * @return the X509 distinguished name.
	 * @throws GeneralCryptoLibException if the leaf certificate properties cannot be retrieved.
	 */
	public static X509DistinguishedName getLeafDistinguishedName() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.LEAF_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateDistinguishedName(config);
	}

	/**
	 * Generates an X509 distinguished name for a signing or encryption certificate and encapsulates it in a {@code X509DistinguishedName} object.
	 *
	 * @return the X509 distinguished name.
	 * @throws GeneralCryptoLibException if the certificate properties cannot be retrieved.
	 */
	public static X509DistinguishedName getDistinguishedName() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.USER_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateDistinguishedName(config);
	}

	/**
	 * Generates validity dates for a root certificate authority and encapsulates them in a {@code ValidityDates} object.
	 *
	 * @return the validity dates.
	 * @throws GeneralCryptoLibException if the certificate properties cannot be retrieved.
	 */
	public static ValidityDates getRootValidityDates() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.ROOT_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateValidityDates(config);
	}

	/**
	 * Generates validity dates for an intermediate certificate authority and encapsulates them in a {@code ValidityDates} object.
	 *
	 * @return the validity dates.
	 * @throws GeneralCryptoLibException if the intermediate certificate properties cannot be retrieved.
	 */
	public static ValidityDates getIntermediateValidityDates() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.INTERMEDIATE_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateValidityDates(config);
	}

	/**
	 * Generates validity dates for a leaf certificate and encapsulates them in a {@code ValidityDates} object.
	 *
	 * @return the validity dates.
	 * @throws GeneralCryptoLibException if the leaf certificate properties cannot be retrieved.
	 */
	public static ValidityDates getLeafValidityDates() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.LEAF_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateValidityDates(config);
	}

	/**
	 * Generates validity dates for a signing or encryption certificate and encapsulates them in a {@code ValidityDates} object.
	 *
	 * @return the validity dates.
	 * @throws GeneralCryptoLibException if the certificate properties cannot be retrieved.
	 */
	public static ValidityDates getValidityDates() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.USER_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		return generateValidityDates(config);
	}

	/**
	 * Generates expired validity dates and encapsulates them in a {@code ValidityDates} object (for negative testing purposes).
	 *
	 * @return the expired validity dates.
	 * @throws GeneralCryptoLibException if the certificate properties cannot be retrieved.
	 */
	public static ValidityDates getExpiredValidityDates() throws GeneralCryptoLibException {

		final ZonedDateTime dateTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime startDateTime = dateTimeNow.minusMonths(2);
		final ZonedDateTime endDateTime = dateTimeNow.minusMonths(1);

		final Date notBefore = Date.from(startDateTime.toInstant());
		final Date notAfter = Date.from(endDateTime.toInstant());

		return new ValidityDates(notBefore, notAfter);
	}

	/**
	 * Generates the epoch time string of a specified date.
	 *
	 * @param date the date from which to retrieve the epoch time string.
	 * @return the epoch time string.
	 */
	public static String getEpochTimeString(final Date date) {

		final Long dateAsLong = date.getTime();

		return dateAsLong.toString().substring(0, X509CertificateTestConstants.EPOCH_TIME_LENGTH);
	}

	/**
	 * Generates a date which is within the validity period of all generated test certificates.
	 *
	 * @return the valid date.
	 * @throws GeneralCryptoLibException if the certificate properties cannot be loaded.
	 */
	public static Date getDateWithinValidityPeriod() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.LEAF_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final int numYearsValidity = Integer.parseInt(config.getPropertyValue(X509CertificateTestConstants.NUMBER_YEARS_VALIDITY_REMAINING_PROPERTY_NAME));

		final ZonedDateTime dateTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime endDateTime = dateTimeNow.plusYears(numYearsValidity);
		final ZonedDateTime validDateTime = endDateTime.minusMonths(1);

		return Date.from(validDateTime.toInstant());
	}

	/**
	 * Generates a date which is outside the validity period of all generated test certificates (for negative testing purposes).
	 *
	 * @return the invalid date.
	 * @throws GeneralCryptoLibException if the certificate properties cannot be loaded.
	 */
	public static Date getDateOutsideValidityPeriod() throws GeneralCryptoLibException {

		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.ROOT_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final int numYearsValidity = Integer.parseInt(config.getPropertyValue(X509CertificateTestConstants.NUMBER_YEARS_VALIDITY_REMAINING_PROPERTY_NAME));

		final ZonedDateTime dateTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime endDateTime = dateTimeNow.plusYears(numYearsValidity);
		final ZonedDateTime validDateTime = endDateTime.plusMonths(1);

		return Date.from(validDateTime.toInstant());
	}

	/**
	 * Generates an X509 distinguished name attribute that contains an illegal character (for negative testing purposes). Note: A separate method must
	 * be used for country attributes.
	 *
	 * @return the generated attribute.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static String getDnAttributeWithIllegalCharacter() throws GeneralCryptoLibException {

		final String attribute = PrimitivesTestDataGenerator
				.getString64(CommonTestDataGenerator.getInt(2, X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE));

		final StringBuilder builder = new StringBuilder(attribute);
		builder.setCharAt(0, 'Ç');

		return builder.toString();
	}

	/**
	 * Generates an X509 distinguished name country attribute that contains an illegal character (for negative testing purposes).
	 *
	 * @return the generated country attribute.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static String getCountryDnAttributeWithIllegalCharacter() throws GeneralCryptoLibException {

		final String attribute = PrimitivesTestDataGenerator.getString32(2);

		final StringBuilder builder = new StringBuilder(attribute);
		builder.setCharAt(0, 'Ç');

		return builder.toString();
	}

	/**
	 * Retrieves the maximum allowed date of validity for X509 certificates.
	 *
	 * @return the maximum date of validity.
	 * @throws GeneralCryptoLibException if a date formatting error occurs.
	 */
	public static Date getMaximumDateOfValidity() throws GeneralCryptoLibException {

		final SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		final String maxDateAsString = "31-12-" + X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR + " 23:59:59";

		try {
			return formatter.parse(maxDateAsString);
		} catch (final ParseException e) {
			throw new GeneralCryptoLibException("Could not convert to Date object from String format", e);
		}
	}

	/**
	 * Generates a date with a specified number of years after the maximum allowed year for certificate validity (for negative testing purposes).
	 *
	 * @param numYearsAfterMax the number of years after the maximum allowed year.
	 * @return the generated date.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static Date getDateAfterMaximum(final int numYearsAfterMax) throws GeneralCryptoLibException {

		final int year = X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR + numYearsAfterMax;
		final String dateStr = year + "/01/01";
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

		try {
			return formatter.parse(dateStr);
		} catch (final ParseException e) {
			throw new GeneralCryptoLibException("Could not parse date string " + dateStr, e);
		}
	}

	/**
	 * Generates an array containing all of the X509 certificate validation types.
	 *
	 * @return the array of X509 certificate validation types.
	 */
	public static X509CertificateValidationType[] getCertificateValidationTypes() {

		final X509CertificateValidationType[] validationTypes = new X509CertificateValidationType[5];

		validationTypes[0] = X509CertificateValidationType.DATE;
		validationTypes[1] = X509CertificateValidationType.SUBJECT;
		validationTypes[2] = X509CertificateValidationType.ISSUER;
		validationTypes[3] = X509CertificateValidationType.KEY_TYPE;
		validationTypes[4] = X509CertificateValidationType.SIGNATURE;

		return validationTypes;
	}

	/**
	 * Generates a {@link X509CertificateValidationData} object for a specified set of certificate data to be validated.
	 *
	 * @param date            the date of validity.
	 * @param subjectDn       the subject distinguished name.
	 * @param issuerDn        the issuer distinguished name.
	 * @param certificateType the certificate type.
	 * @param issuerPublicKey the issuer public key.
	 * @return the certificate validation data.
	 * @throws GeneralCryptoLibException if the {@link X509CertificateValidationData} object cannot be created.
	 */
	public static X509CertificateValidationData getCertificateValidationData(final Date date, final X509DistinguishedName subjectDn,
			final X509DistinguishedName issuerDn, final X509CertificateType certificateType, final PublicKey issuerPublicKey)
			throws GeneralCryptoLibException {

		return new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(subjectDn).addIssuerDn(issuerDn).addKeyType(certificateType)
				.addCaPublicKey(issuerPublicKey).build();
	}

	private static X509DistinguishedName generateDistinguishedName(final PolicyFromPropertiesHelper config) throws GeneralCryptoLibException {

		final String commonName = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.SUBJECT_COMMON_NAME_PROPERTY_NAME));
		final String orgUnit = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME));
		final String organization = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.SUBJECT_ORGANIZATION_PROPERTY_NAME));
		final String locality = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.SUBJECT_LOCALITY_PROPERTY_NAME));
		final String country = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.SUBJECT_COUNTRY_PROPERTY_NAME));

		return new X509DistinguishedName.Builder(commonName, country).addOrganizationalUnit(orgUnit).addOrganization(organization)
				.addLocality(locality).build();
	}

	private static ValidityDates generateValidityDates(final PolicyFromPropertiesHelper config) throws GeneralCryptoLibException {

		final int numYearsValidityRemaining = Integer
				.parseInt(config.getPropertyValue(X509CertificateTestConstants.NUMBER_YEARS_VALIDITY_REMAINING_PROPERTY_NAME));

		final ZonedDateTime dateTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime startDateTime = dateTimeNow.minusYears(numYearsValidityRemaining);
		final ZonedDateTime endDateTime = dateTimeNow.plusYears(numYearsValidityRemaining);

		final Date notBefore = Date.from(startDateTime.toInstant());
		final Date notAfter = Date.from(endDateTime.toInstant());

		return new ValidityDates(notBefore, notAfter);
	}
}
