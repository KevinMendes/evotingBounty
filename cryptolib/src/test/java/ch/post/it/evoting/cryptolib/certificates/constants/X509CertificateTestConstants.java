/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.constants;

import java.io.File;

/**
 * Declaration of constants specific to certificate tests.
 */
public class X509CertificateTestConstants {

	public static final String ROOT_CERTIFICATE_PROPERTIES_FILE_PATH = "properties/rootX509Certificate.properties";

	public static final String INTERMEDIATE_CERTIFICATE_PROPERTIES_FILE_PATH = "properties/intermediateX509Certificate.properties";

	public static final String LEAF_CERTIFICATE_PROPERTIES_FILE_PATH = "properties/leafX509Certificate.properties";

	public static final String USER_CERTIFICATE_PROPERTIES_FILE_PATH = "properties/userX509Certificate.properties";

	public static final String OUTPUT_DATA_PATH = "target" + File.separator + "data";

	public static final String ROOT_CERTIFICATE_FILE_PATH = OUTPUT_DATA_PATH + File.separator + "rootCertificate.pem";

	public static final String USER_CERTIFICATE_FILE_PATH = OUTPUT_DATA_PATH + File.separator + "userCertificate.pem";

	public static final String ISSUER_COMMON_NAME_PROPERTY_NAME = "issuer.common.name";

	public static final String ISSUER_ORGANIZATIONAL_UNIT_PROPERTY_NAME = "issuer.organizational.unit";

	public static final String ISSUER_ORGANIZATION_PROPERTY_NAME = "issuer.organization";

	public static final String ISSUER_LOCALITY_PROPERTY_NAME = "issuer.locality";

	public static final String ISSUER_COUNTRY_PROPERTY_NAME = "issuer.country";

	public static final String SUBJECT_COMMON_NAME_PROPERTY_NAME = "subject.common.name";

	public static final String SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME = "subject.organizational.unit";

	public static final String SUBJECT_ORGANIZATION_PROPERTY_NAME = "subject.organization";

	public static final String SUBJECT_LOCALITY_PROPERTY_NAME = "subject.locality";

	public static final String SUBJECT_COUNTRY_PROPERTY_NAME = "subject.country";

	public static final String NUMBER_YEARS_VALIDITY_REMAINING_PROPERTY_NAME = "number.years.validity.remaining";

	public static final String AUTHORITY_INFORMATION_ACCESS_URL_PROPERTY_NAME = "authority.information.access.url";

	public static final String CRL_DISTRIBUTION_POINT_URL_PROPERTY_NAME = "crl.distribution.point.url";

	public static final String SUBJECT_ALTERNATIVE_NAME_PROPERTY_NAME = "subject.alternative.name";

	public static final String EXTENDED_KEY_USAGES_PROPERTY_NAME = "extended.key.usages";

	public static final String DIGITAL_SIGNATURE_PROPERTY_NAME = "digitalSignature";

	public static final String NON_REPUDIATION_PROPERTY_NAME = "nonRepudiation";

	public static final String KEY_ENCIPHERMENT_PROPERTY_NAME = "keyEncipherment";

	public static final String DATA_ENCIPHERMENT_PROPERTY_NAME = "dataEncipherment";

	public static final String KEY_AGREEMENT_PROPERTY_NAME = "keyAgreement";

	public static final String KEY_CERT_SIGN_PROPERTY_NAME = "keyCertSign";

	public static final String CRL_SIGN_PROPERTY_NAME = "crlSign";

	public static final String ENCIPHER_ONLY_PROPERTY_NAME = "encipherOnly";

	public static final String DECIPHER_ONLY_PROPERTY_NAME = "decipherOnly";

	public static final int EPOCH_TIME_LENGTH = 10;
}
