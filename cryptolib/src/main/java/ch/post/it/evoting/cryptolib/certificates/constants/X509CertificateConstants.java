/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.constants;

/**
 * Declaration of constants specific to X509 certificates.
 */
public final class X509CertificateConstants {

	/**
	 * Common prefix of the properties of this module.
	 */
	public static final String CERTIFICATE_ALGORITHM_PROVIDER_PROPERTY_NAME = "certificates.x509certificate";

	/**
	 * Maximum bit length of the certificate serial number.
	 */
	public static final int CERTIFICATE_SERIAL_NUMBER_MAX_BIT_LENGTH = 160;

	/**
	 * Flag which defines if a certificate extension is critical.
	 */
	public static final boolean CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT = true;

	/**
	 * Integer value constraint for certificate authorities.
	 */
	public static final int IS_CERTIFICATE_AUTHORITY_BASIC_CONSTRAINTS_INTEGER_VALUE = Integer.MAX_VALUE;

	/**
	 * Integer value constraint for not certificate authorities.
	 */
	public static final int IS_NOT_CERTIFICATE_AUTHORITY_BASIC_CONSTRAINTS_INTEGER_VALUE = -1;

	/**
	 * Certificate attribute which defines the owner's common name.
	 */
	public static final String COMMON_NAME_ATTRIBUTE_NAME = "CN";

	/**
	 * Certificate attribute which defines the certificate owner’s country of residence.
	 */
	public static final String COUNTRY_ATTRIBUTE_NAME = "C";

	/**
	 * Certificate attribute which defines the name of the organizational unit to which the certificate owner belongs.
	 */
	public static final String ORGANIZATIONAL_UNIT_ATTRIBUTE_NAME = "OU";

	/**
	 * Certificate attribute which defines the organization to which the certificate owner belongs.
	 */
	public static final String ORGANIZATION_ATTRIBUTE_NAME = "O";

	/**
	 * Certificate attribute which defines the locality.
	 */
	public static final String LOCALITY_ATTRIBUTE_NAME = "L";

	/**
	 * The maximum number of characters allowed in an X509 certificate distinguished name attribute.
	 */
	public static final int X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE = 64;

	/**
	 * Maximum year for the validity date of an X509 certificate.
	 */
	public static final int X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR = 9999;

	private X509CertificateConstants() {
	}
}
