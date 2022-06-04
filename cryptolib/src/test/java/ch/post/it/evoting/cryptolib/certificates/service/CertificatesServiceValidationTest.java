/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateTestConstants;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataGenerator;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPrivateKey;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPublicKey;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestX509Certificate;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class CertificatesServiceValidationTest {

	private static CertificatesService certificatesServiceForDefaultPolicy;
	private static String whiteSpaceString;
	private static KeyPair rootKeyPair;
	private static PrivateKey rootPrivateKey;
	private static CertificateData certificateData;
	private static CryptoAPIX509Certificate certificate;
	private static TestPublicKey nullContentPublicKey;
	private static TestPublicKey emptyContentPublicKey;
	private static TestPrivateKey nullContentPrivateKey;
	private static TestPrivateKey emptyContentPrivateKey;
	private static TestX509Certificate nullContentX509Certificate;
	private static TestX509Certificate emptyContentX509Certificate;
	private static CertificateData certificateDataWithNullIssuerDn;
	private static CertificateData certificateDataWithNullSubjectDn;
	private static CertificateData certificateDataWithNullSubjectPublicKey;
	private static CertificateData certificateDataWithNullValidityDates;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		whiteSpaceString = CommonTestDataGenerator
				.getWhiteSpaceString(CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH));

		certificatesServiceForDefaultPolicy = new CertificatesService();

		rootKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		rootPrivateKey = rootKeyPair.getPrivate();

		final KeyPair keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();

		certificateData = X509CertificateTestDataGenerator.getCertificateData(keyPair);
		certificate = certificatesServiceForDefaultPolicy.createSignX509Certificate(certificateData, rootPrivateKey);

		final byte[] emptyByteArray = new byte[0];

		nullContentPublicKey = new TestPublicKey(null);
		emptyContentPublicKey = new TestPublicKey(emptyByteArray);

		nullContentPrivateKey = new TestPrivateKey(null);
		emptyContentPrivateKey = new TestPrivateKey(emptyByteArray);

		nullContentX509Certificate = new TestX509Certificate(nullContentPublicKey);
		emptyContentX509Certificate = new TestX509Certificate(emptyContentPublicKey);

		setCertificateDataWithUnsetData(keyPair.getPublic());
	}

	static Stream<Arguments> createX509DistinguishedNameBuilder() throws GeneralCryptoLibException {

		final Properties properties = getRootCertificateProperties();
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final String commonName = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COMMON_NAME_PROPERTY_NAME));
		final String country = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COUNTRY_PROPERTY_NAME));

		final int attributeMaxLength = X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE;
		final int attributeOutOfBoundsLength = attributeMaxLength + 1;
		final String aboveMaxLengthAttribute = PrimitivesTestDataGenerator.getString64(attributeOutOfBoundsLength);

		final String attributeWithIllegalChar = X509CertificateTestDataGenerator.getDnAttributeWithIllegalCharacter();
		final String countryAttributeWithIllegalChar = X509CertificateTestDataGenerator.getCountryDnAttributeWithIllegalCharacter();

		return Stream.of(arguments(null, country, "Common name is null."), arguments("", country, "Common name is blank."),
				arguments(whiteSpaceString, country, "Common name is blank."), arguments(aboveMaxLengthAttribute, country,
						"Common name length must be less than or equal to : " + attributeMaxLength + "; Found " + attributeOutOfBoundsLength),
				arguments(attributeWithIllegalChar, country, "Common name contains characters that are not ASCII printable."),
				arguments(commonName, null, "Country is null."), arguments(commonName, "", "Country is blank."),
				arguments(commonName, whiteSpaceString, "Country is blank."), arguments(commonName, aboveMaxLengthAttribute,
						"Country length must be less than or equal to : " + 2 + "; Found " + attributeOutOfBoundsLength),
				arguments(commonName, countryAttributeWithIllegalChar,
						"Country contains characters outside of allowed set " + SecureRandomConstants.ALPHABET_BASE64));
	}

	static Stream<Arguments> createX509DistinguishedNameOrganizationalUnit() throws GeneralCryptoLibException {

		final Properties properties = getRootCertificateProperties();
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final String commonName = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COMMON_NAME_PROPERTY_NAME));
		final String country = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COUNTRY_PROPERTY_NAME));

		final int attributeMaxLength = X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE;
		final int attributeOutOfBoundsLength = attributeMaxLength + 1;
		final String aboveMaxLengthAttribute = PrimitivesTestDataGenerator.getString64(attributeOutOfBoundsLength);

		final String attributeWithIllegalChar = X509CertificateTestDataGenerator.getDnAttributeWithIllegalCharacter();

		return Stream.of(arguments(commonName, country, null, "Organizational unit is null."), arguments(commonName, country, aboveMaxLengthAttribute,
						"Organizational unit length must be less than or equal to : " + attributeMaxLength + "; Found " + attributeOutOfBoundsLength),
				arguments(commonName, country, attributeWithIllegalChar, "Organizational unit contains characters that are not ASCII printable."));
	}

	static Stream<Arguments> createX509DistinguishedNameOrganization() throws GeneralCryptoLibException {

		final Properties properties = getRootCertificateProperties();
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final String commonName = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COMMON_NAME_PROPERTY_NAME));
		final String country = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COUNTRY_PROPERTY_NAME));

		final int attributeMaxLength = X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE;
		final int attributeOutOfBoundsLength = attributeMaxLength + 1;
		final String aboveMaxLengthAttribute = PrimitivesTestDataGenerator.getString64(attributeOutOfBoundsLength);

		final String attributeWithIllegalChar = X509CertificateTestDataGenerator.getDnAttributeWithIllegalCharacter();

		return Stream.of(arguments(commonName, country, null, "Organization is null."), arguments(commonName, country, aboveMaxLengthAttribute,
						"Organization length must be less than or equal to : " + attributeMaxLength + "; Found " + attributeOutOfBoundsLength),
				arguments(commonName, country, attributeWithIllegalChar, "Organization contains characters that are not ASCII printable" + "."));
	}

	static Stream<Arguments> createX509DistinguishedNameLocality() throws GeneralCryptoLibException {

		final Properties properties = getRootCertificateProperties();
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final String commonName = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COMMON_NAME_PROPERTY_NAME));
		final String country = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COUNTRY_PROPERTY_NAME));

		final int attributeMaxLength = X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE;
		final int attributeOutOfBoundsLength = attributeMaxLength + 1;
		final String aboveMaxLengthAttribute = PrimitivesTestDataGenerator.getString64(attributeOutOfBoundsLength);

		final String attributeWithIllegalChar = X509CertificateTestDataGenerator.getDnAttributeWithIllegalCharacter();

		return Stream.of(arguments(commonName, country, null, "Locality is null."), arguments(commonName, country, aboveMaxLengthAttribute,
						"Locality length must be less than or equal to : " + attributeMaxLength + "; Found " + attributeOutOfBoundsLength),
				arguments(commonName, country, attributeWithIllegalChar, "Locality contains characters that are not ASCII printable."));
	}

	static Stream<Arguments> createValidityDates() throws GeneralCryptoLibException {

		final Properties properties = getRootCertificateProperties();
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final int numYearsValidity = Integer.parseInt(config.getPropertyValue(X509CertificateTestConstants.NUMBER_YEARS_VALIDITY_REMAINING_PROPERTY_NAME));

		final Date notBefore = new Date(System.currentTimeMillis());
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, numYearsValidity);
		final Date notAfter = calendar.getTime();

		final Date maxDate = X509CertificateTestDataGenerator.getMaximumDateOfValidity();
		final Date aboveMaxNotBefore = X509CertificateTestDataGenerator.getDateAfterMaximum(1);
		final Date aboveMaxNotAfter = X509CertificateTestDataGenerator.getDateAfterMaximum(2);

		return Stream
				.of(arguments(null, notAfter, "Starting date of validity is null."), arguments(notBefore, null, "Ending date of validity is null."),
						arguments(notAfter, notBefore,
								"Starting date of validity " + notAfter + " is not before ending date of validity " + notBefore),
						arguments(aboveMaxNotBefore, aboveMaxNotAfter,
								"Starting date of validity " + aboveMaxNotBefore + " is not before maximum date of validity " + maxDate),
						arguments(notBefore, aboveMaxNotAfter,
								"Ending date of validity " + aboveMaxNotAfter + " is not before maximum date of validity " + maxDate));
	}

	static Stream<Arguments> setRootCertificateDataSubjectDn() {
		return Stream.of(arguments(null, "Subject distinguished name is null."));
	}

	static Stream<Arguments> setRootCertificateDataValidiatyDates() {
		return Stream.of(arguments(null, "Validity dates object is null."));
	}

	static Stream<Arguments> setRootCertificateDataSubjectPublicKey() {
		return Stream.of(arguments(null, "Subject public key is null."), arguments(nullContentPublicKey, "Subject public key content is null."),
				arguments(emptyContentPublicKey, "Subject public key content is empty."));
	}

	static Stream<Arguments> setCertificateDataSubjectDn() {
		return Stream.of(arguments(null, "Subject distinguished name is null."));
	}

	static Stream<Arguments> setCertificateDataIssuerDn() {
		return Stream.of(arguments(null, "Issuer distinguished name is null."));
	}

	static Stream<Arguments> setCertificateDataValidiatyDates() {
		return Stream.of(arguments(null, "Validity dates object is null."));
	}

	static Stream<Arguments> setCertificateDataSubjectPublicKey() {
		return Stream.of(arguments(null, "Subject public key is null."), arguments(nullContentPublicKey, "Subject public key content is null."),
				arguments(emptyContentPublicKey, "Subject public key content is empty."));
	}

	static Stream<Arguments> createRootAuthorityCertificate() throws GeneralCryptoLibException {

		final RootCertificateData rootCertificateData = X509CertificateTestDataGenerator.getRootCertificateData(rootKeyPair);

		return Stream.of(arguments(null, rootPrivateKey, "Certificate data is null."),
				arguments(rootCertificateData, null, "Issuer private key is null" + "."),
				arguments(rootCertificateData, nullContentPrivateKey, "Issuer private key content is null."),
				arguments(rootCertificateData, emptyContentPrivateKey, "Issuer private key content is empty."),
				arguments(certificateDataWithNullSubjectPublicKey, rootPrivateKey, "Subject public key is null."),
				arguments(certificateDataWithNullIssuerDn, rootPrivateKey, "Issuer distinguished name is null."),
				arguments(certificateDataWithNullSubjectDn, rootPrivateKey, "Subject distinguished name is null."),
				arguments(certificateDataWithNullValidityDates, rootPrivateKey, "Validity dates object is null."));
	}

	static Stream<Arguments> createIntermediateAuthorityCertificate() {

		return Stream
				.of(arguments(null, rootPrivateKey, "Certificate data is null."), arguments(certificateData, null, "Issuer private key is null."),
						arguments(certificateData, nullContentPrivateKey, "Issuer private key content is null."),
						arguments(certificateData, emptyContentPrivateKey, "Issuer private key content is empty."),
						arguments(certificateDataWithNullSubjectPublicKey, rootPrivateKey, "Subject public key is null."),
						arguments(certificateDataWithNullIssuerDn, rootPrivateKey, "Issuer distinguished name is null."),
						arguments(certificateDataWithNullSubjectDn, rootPrivateKey, "Subject distinguished name is null."),
						arguments(certificateDataWithNullValidityDates, rootPrivateKey, "Validity dates object is null."));
	}

	static Stream<Arguments> createSignCertificate() {

		return Stream
				.of(arguments(null, rootPrivateKey, "Certificate data is null."), arguments(certificateData, null, "Issuer private key is null."),
						arguments(certificateData, nullContentPrivateKey, "Issuer private key content is null."),
						arguments(certificateData, emptyContentPrivateKey, "Issuer private key content is empty."),
						arguments(certificateDataWithNullSubjectDn, rootPrivateKey, "Subject distinguished name is null."),
						arguments(certificateDataWithNullSubjectPublicKey, rootPrivateKey, "Subject public key is null."),
						arguments(certificateDataWithNullIssuerDn, rootPrivateKey, "Issuer distinguished name is null."),
						arguments(certificateDataWithNullSubjectDn, rootPrivateKey, "Subject distinguished name is null."),
						arguments(certificateDataWithNullValidityDates, rootPrivateKey, "Validity dates object is null."));
	}

	static Stream<Arguments> createEncryptionCertificate() {

		return Stream
				.of(arguments(null, rootPrivateKey, "Certificate data is null."), arguments(certificateData, null, "Issuer private key is null."),
						arguments(certificateData, nullContentPrivateKey, "Issuer private key content is null."),
						arguments(certificateDataWithNullSubjectPublicKey, rootPrivateKey, "Subject public key is null."),
						arguments(certificateDataWithNullIssuerDn, rootPrivateKey, "Issuer distinguished name is null."),
						arguments(certificateDataWithNullSubjectDn, rootPrivateKey, "Subject distinguished name is null."),
						arguments(certificateDataWithNullValidityDates, rootPrivateKey, "Validity dates object is null."));
	}

	static Stream<Arguments> createCertificateFromX509Certificate() {

		return Stream.of(arguments(null, "X509 certificate is null."), arguments(nullContentX509Certificate, "X509 certificate content is null."),
				arguments(emptyContentX509Certificate, "X509 certificate content is empty."));
	}

	static Stream<Arguments> verifyCertificate() {

		return Stream.of(arguments(null, "Issuer public key is null."), arguments(nullContentPublicKey, "Issuer public key content is null."),
				arguments(emptyContentPublicKey, "Issuer public key content is empty."));
	}

	static Stream<Arguments> checkCertificateDateValidity() {
		return Stream.of(arguments(null, "Date is null."));
	}

	private static Properties getRootCertificateProperties() {
		final Properties properties = new Properties();
		properties.setProperty(X509CertificateTestConstants.ISSUER_COMMON_NAME_PROPERTY_NAME, "Test Root Certificate");
		properties.setProperty(X509CertificateTestConstants.ISSUER_ORGANIZATIONAL_UNIT_PROPERTY_NAME, "Test Organizational Unit");
		properties.setProperty(X509CertificateTestConstants.ISSUER_ORGANIZATION_PROPERTY_NAME, "Test Organization");
		properties.setProperty(X509CertificateTestConstants.ISSUER_LOCALITY_PROPERTY_NAME, "Barcelona");
		properties.setProperty(X509CertificateTestConstants.ISSUER_COUNTRY_PROPERTY_NAME, "ES");
		properties.setProperty(X509CertificateTestConstants.NUMBER_YEARS_VALIDITY_REMAINING_PROPERTY_NAME, "5");
		return properties;
	}

	private static void setCertificateDataWithUnsetData(final PublicKey publicKey) throws GeneralCryptoLibException {
		final X509DistinguishedName issuerDn = X509CertificateTestDataGenerator.getRootDistinguishedName();
		final X509DistinguishedName subjectDn = X509CertificateTestDataGenerator.getDistinguishedName();
		final ValidityDates validityDates = X509CertificateTestDataGenerator.getRootValidityDates();

		certificateDataWithNullIssuerDn = new CertificateData();
		certificateDataWithNullIssuerDn.setSubjectPublicKey(publicKey);
		certificateDataWithNullIssuerDn.setSubjectDn(subjectDn);
		certificateDataWithNullIssuerDn.setValidityDates(validityDates);

		certificateDataWithNullSubjectDn = new CertificateData();
		certificateDataWithNullSubjectDn.setSubjectPublicKey(publicKey);
		certificateDataWithNullSubjectDn.setIssuerDn(issuerDn);
		certificateDataWithNullSubjectDn.setValidityDates(validityDates);

		certificateDataWithNullSubjectPublicKey = new CertificateData();
		certificateDataWithNullSubjectPublicKey.setIssuerDn(issuerDn);
		certificateDataWithNullSubjectPublicKey.setSubjectDn(subjectDn);
		certificateDataWithNullSubjectPublicKey.setValidityDates(validityDates);

		certificateDataWithNullValidityDates = new CertificateData();
		certificateDataWithNullValidityDates.setSubjectPublicKey(publicKey);
		certificateDataWithNullValidityDates.setIssuerDn(issuerDn);
		certificateDataWithNullValidityDates.setSubjectDn(subjectDn);
	}

	@ParameterizedTest
	@MethodSource("createX509DistinguishedNameBuilder")
	void testX509DistinguishedNameBuilderCreationValidation(final String commonName, final String country, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new X509DistinguishedName.Builder(commonName, country));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createX509DistinguishedNameOrganizationalUnit")
	void testX509DistinguishedNameOrganizationalUnitCreationValidation(
			final String commonName, final String country, final String orgUnit, final String errorMsg)
			throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganizationalUnit() and not builder
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganizationalUnit(orgUnit));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createX509DistinguishedNameOrganization")
	void testX509DistinguishedNameOrganizationCreationValidation(
			final String commonName, final String country, final String organization, final String errorMsg)
			throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganization() and not builder
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganization(organization));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createX509DistinguishedNameLocality")
	void testX509DistinguishedNameLocalityCreationValidation(final String commonName, final String country, final String locality, final String errorMsg)
			throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addLocality() and not builder
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> builder.addLocality(locality));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createValidityDates")
	void testValidityDatesCreationValidation(final Date notBefore, final Date notAfter, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new ValidityDates(notBefore, notAfter));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setRootCertificateDataSubjectDn")
	void testRootCertificateDataSubjectDnSettingValidation(final X509DistinguishedName subjectDn, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new RootCertificateData().setSubjectDn(subjectDn));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setRootCertificateDataValidiatyDates")
	void testRootCertificateDataValidityDatesSettingValidation(final ValidityDates validityDates, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new RootCertificateData().setValidityDates(validityDates));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setRootCertificateDataSubjectPublicKey")
	void testRootCertificateDataValidityDatesSettingValidation(final PublicKey subjectPublicKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new RootCertificateData().setSubjectPublicKey(subjectPublicKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setCertificateDataSubjectDn")
	void testCertificateDataSubjectDnSettingValidation(final X509DistinguishedName subjectDn, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new CertificateData().setSubjectDn(subjectDn));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setCertificateDataIssuerDn")
	void testCertificateDataIssuerDnSettingValidation(final X509DistinguishedName issuerDn, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new CertificateData().setIssuerDn(issuerDn));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setCertificateDataValidiatyDates")
	void testCertificateDataValidityDatesSettingValidation(final ValidityDates validityDates, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new CertificateData().setValidityDates(validityDates));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setCertificateDataSubjectPublicKey")
	void testCertificateDataValidityDatesSettingValidation(final PublicKey subjectPublicKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new CertificateData().setSubjectPublicKey(subjectPublicKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createRootAuthorityCertificate")
	void testRootAuthorityCertificateCreationValidation(final RootCertificateData rootCertificateData, final PrivateKey rootPrivateKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> certificatesServiceForDefaultPolicy.createRootAuthorityX509Certificate(rootCertificateData, rootPrivateKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createIntermediateAuthorityCertificate")
	void testIntermediateAuthorityCertificateCreationValidation(final CertificateData certificateData, final PrivateKey issuerPrivateKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> certificatesServiceForDefaultPolicy.createIntermediateAuthorityX509Certificate(certificateData, issuerPrivateKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createSignCertificate")
	void testSignCertificateCreationValidation(final CertificateData certificateData, final PrivateKey issuerPrivateKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> certificatesServiceForDefaultPolicy.createSignX509Certificate(certificateData, issuerPrivateKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createEncryptionCertificate")
	void testEncryptionCertificateCreationValidation(final CertificateData certificateData, final PrivateKey issuerPrivateKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> certificatesServiceForDefaultPolicy.createEncryptionX509Certificate(certificateData, issuerPrivateKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createCertificateFromX509Certificate")
	void testCertificateFromX509CertificateCreationValidation(final X509Certificate x509Certificate, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new CryptoX509Certificate(x509Certificate));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("verifyCertificate")
	void testCertificateVerificationValidation(final PublicKey issuerPublicKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> certificate.verify(issuerPublicKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("checkCertificateDateValidity")
	void testCertificateDateValidityCheckValidation(final Date date, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> certificate.checkValidity(date));
		assertEquals(errorMsg, exception.getMessage());
	}
}
