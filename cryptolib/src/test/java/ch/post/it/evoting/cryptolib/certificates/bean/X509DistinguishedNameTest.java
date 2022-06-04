/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;

class X509DistinguishedNameTest {

	private static String commonName;
	private static String country;
	private static String organizationalUnit;
	private static String organization;
	private static String locality;
	private static String maxSizeAttribute;
	private static String tooLargeAttribute;
	private static String illegalAttribute;
	private static String illegalCountryAttribute;
	private static X509DistinguishedName x509DistinguishedName;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {
		commonName = "Test certificate";
		country = "XX";
		organizationalUnit = "Test";
		organization = "Organization";
		locality = "Locality";

		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		builder.addOrganization(organization);
		builder.addOrganizationalUnit(organizationalUnit);
		builder.addLocality(locality);

		x509DistinguishedName = builder.build();

		maxSizeAttribute = new String(new char[X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE]).replace("\0", "X");
		tooLargeAttribute = new String(new char[X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE + 1]).replace("\0", "X");

		final String ILLEGAL_CHARACTER = "ç";
		illegalAttribute = "Organization" + ILLEGAL_CHARACTER;
		illegalCountryAttribute = "X" + ILLEGAL_CHARACTER;
	}

	@Test
	void whenGetCommonNameAttributeThenOK() {
		final String errorMsg = "The returned common name was not what was expected";

		assertEquals(commonName, x509DistinguishedName.getCommonName(), errorMsg);
	}

	@Test
	void whenGetOrganizationalUnitAttributeThenOK() {
		final String errorMsg = "The returned organizational unit was not what was expected";

		assertEquals(organizationalUnit, x509DistinguishedName.getOrganizationalUnit(), errorMsg);
	}

	@Test
	void whenGetOrganizationAttributeThenOK() {
		final String errorMsg = "The returned organization was not what was expected";

		assertEquals(organization, x509DistinguishedName.getOrganization(), errorMsg);
	}

	@Test
	void whenGetLocalityAttributeThenOK() {
		final String errorMsg = "The returned locality was not what was expected";

		assertEquals(locality, x509DistinguishedName.getLocality(), errorMsg);
	}

	@Test
	void whenGetCountryAttributeThenOK() {
		final String errorMsg = "The returned country was not what was expected";

		assertEquals(country, x509DistinguishedName.getCountry(), errorMsg);
	}

	@Test
	void givenMaxSizeCommonNameAttributeThenOk() throws GeneralCryptoLibException {
		assertNotNull(new X509DistinguishedName.Builder(maxSizeAttribute, country).build());
	}

	@Test
	void givenMaxSizeOrganizationalUnitAttributeThenOk() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country).addOrganizationalUnit(maxSizeAttribute);

		assertNotNull(builder.build());
	}

	@Test
	void givenMaxSizeOrganizationAttributeThenOk() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country).addOrganization(maxSizeAttribute);

		assertNotNull(builder.build());
	}

	@Test
	void givenMaxSizeLocalityAttributeThenOk() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country).addLocality(maxSizeAttribute);

		assertNotNull(builder.build());
	}

	@Test
	void givenSameDistinguishedNamesThenEquality() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		builder.addOrganization(organization);
		builder.addOrganizationalUnit(organizationalUnit);
		builder.addLocality(locality);

		final X509DistinguishedName x509DistinguishedName1 = builder.build();
		final X509DistinguishedName x509DistinguishedName2 = builder.build();

		assertEquals(x509DistinguishedName1, x509DistinguishedName2);
	}

	@Test
	void givenDifferentDistinguishedNamesThenInEquality() throws GeneralCryptoLibException {
		X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		builder.addOrganization(organization);
		builder.addOrganizationalUnit(organizationalUnit);
		builder.addLocality(locality);

		final X509DistinguishedName x509DistinguishedName1 = builder.build();

		builder = new X509DistinguishedName.Builder(commonName, country);
		builder.addOrganization(organization);
		builder.addOrganizationalUnit(organizationalUnit);
		builder.addLocality(locality + "x");

		final X509DistinguishedName x509DistinguishedName2 = builder.build();

		assertNotEquals(x509DistinguishedName1, x509DistinguishedName2);
	}

	@Test
	void givenNullCommonNameAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder(null, country));
	}

	@Test
	void givenNullCountryAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder(commonName, null));
	}

	@Test
	void givenNullOrganizationalUnitAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganizationalUnit() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganizationalUnit(null));
	}

	@Test
	void givenNullOrganizationAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganization() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganization(null));
	}

	@Test
	void givenNullLocalityAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addLocality() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addLocality(null));
	}

	@Test
	void givenEmptyCommonNameAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder("", country));
	}

	@Test
	void givenEmptyCountryAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder(commonName, ""));
	}

	@Test
	void givenTooLargeCommonNameAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder(tooLargeAttribute, country));
	}

	@Test
	void givenTooLargeCountryAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder(commonName, tooLargeAttribute));
	}

	@Test
	void givenTooLargeOrganizationalUnitAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganizationalUnit() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganizationalUnit(tooLargeAttribute));
	}

	@Test
	void givenTooLargeOrganizationAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganization() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganization(tooLargeAttribute));
	}

	@Test
	void givenTooLargeLocalityAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addLocality() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addLocality(tooLargeAttribute));
	}

	@Test
	void givenIllegalCommonNameAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder(illegalAttribute, country));
	}

	@Test
	void givenIllegalCountryAttributeThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new X509DistinguishedName.Builder(commonName, illegalCountryAttribute));
	}

	@Test
	void givenIllegalOrganizationalUnitAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganizationalUnit() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganizationalUnit(illegalAttribute));
	}

	@Test
	void givenIllegalOrganizationAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addOrganization() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addOrganization(illegalAttribute));
	}

	@Test
	void givenIllegalLocalityAttributeThenException() throws GeneralCryptoLibException {
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		// We explicitly expect an exception from addLocality() and not builder
		assertThrows(GeneralCryptoLibException.class, () -> builder.addLocality(illegalAttribute));
	}
}
