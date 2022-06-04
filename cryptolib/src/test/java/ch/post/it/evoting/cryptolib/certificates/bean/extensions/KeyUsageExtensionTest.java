/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;

class KeyUsageExtensionTest {

	private static final int NUM_KEY_USAGES_IN_CA_CERT_EXTENSION = 2;
	private static final int NUM_KEY_USAGES_IN_SIGN_CERT_EXTENSION = 2;
	private static final int NUM_KEY_USAGES_IN_ENCRYPT_CERT_EXTENSION = 2;
	private static final boolean CA_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL = true;
	private static final boolean SIGN_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL = false;
	private static final boolean ENCRYPT_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL = false;

	private static EnumSet<CertificateKeyUsage> caCertKeyUsages;
	private static EnumSet<CertificateKeyUsage> signCertKeyUsages;
	private static EnumSet<CertificateKeyUsage> encryptCertKeyUsages;
	private static CertificateKeyUsageExtension caCertKeyUsageExtension;
	private static CertificateKeyUsageExtension signCertKeyUsageExtension;
	private static CertificateKeyUsageExtension encryptCertKeyUsageExtension;

	@BeforeAll
	static void setUp() {
		caCertKeyUsages = EnumSet.of(CertificateKeyUsage.KEY_CERT_SIGN, CertificateKeyUsage.CRL_SIGN);
		signCertKeyUsages = EnumSet.of(CertificateKeyUsage.DIGITAL_SIGNATURE, CertificateKeyUsage.NON_REPUDIATION);
		encryptCertKeyUsages = EnumSet.of(CertificateKeyUsage.KEY_ENCIPHERMENT, CertificateKeyUsage.DATA_ENCIPHERMENT);

		caCertKeyUsageExtension = new CertificateKeyUsageExtension(CA_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL, caCertKeyUsages);
		signCertKeyUsageExtension = new CertificateKeyUsageExtension(SIGN_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL, signCertKeyUsages);
		encryptCertKeyUsageExtension = new CertificateKeyUsageExtension(ENCRYPT_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL, encryptCertKeyUsages);
	}

	@Test
	void retrieveExtensionType() {
		final boolean caCertExtensionTypeRetrieved = caCertKeyUsageExtension.getExtensionType() == ExtensionType.KEY_USAGE;
		final boolean signCertExtensionTypeRetrieved = signCertKeyUsageExtension.getExtensionType() == ExtensionType.KEY_USAGE;
		final boolean encryptCertExtensionTypeRetrieved = encryptCertKeyUsageExtension.getExtensionType() == ExtensionType.KEY_USAGE;

		assertTrue(caCertExtensionTypeRetrieved && signCertExtensionTypeRetrieved && encryptCertExtensionTypeRetrieved);
	}

	@Test
	void retrieveIsCriticalFlag() {
		final boolean caCertIsCriticalFlagRetrieved = caCertKeyUsageExtension.isCritical() == CA_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL;
		final boolean signCertIsCriticalFlagRetrieved = signCertKeyUsageExtension.isCritical() == SIGN_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL;
		final boolean encryptCertIsCriticalFlagRetrieved = encryptCertKeyUsageExtension.isCritical() == ENCRYPT_CERT_KEY_USAGE_EXTENSION_IS_CRITICAL;

		assertTrue(caCertIsCriticalFlagRetrieved && signCertIsCriticalFlagRetrieved && encryptCertIsCriticalFlagRetrieved);
	}

	@Test
	void retrieveInternallySetIsCriticalFlag() {
		final CertificateKeyUsageExtension caCertKeyUsageExtension = new CertificateKeyUsageExtension(caCertKeyUsages);
		final boolean caCertIsCriticalFlagRetrieved =
				caCertKeyUsageExtension.isCritical() == X509CertificateConstants.CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT;

		final CertificateKeyUsageExtension signCertKeyUsageExtension = new CertificateKeyUsageExtension(signCertKeyUsages);
		final boolean signCertIsCriticalFlagRetrieved =
				signCertKeyUsageExtension.isCritical() == X509CertificateConstants.CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT;

		final CertificateKeyUsageExtension encryptCertKeyUsageExtension = new CertificateKeyUsageExtension(encryptCertKeyUsages);
		final boolean encryptCertIsCriticalFlagRetrieved =
				encryptCertKeyUsageExtension.isCritical() == X509CertificateConstants.CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT;

		assertTrue(caCertIsCriticalFlagRetrieved && signCertIsCriticalFlagRetrieved && encryptCertIsCriticalFlagRetrieved);
	}

	@Test
	void retrieveKeyUsages() {
		final EnumSet<CertificateKeyUsage> caCertKeyUsages = caCertKeyUsageExtension.getKeyUsages();
		final boolean caCertKeyUsagesRetrieved = (caCertKeyUsages.size() == NUM_KEY_USAGES_IN_CA_CERT_EXTENSION) && (caCertKeyUsages
				.containsAll(KeyUsageExtensionTest.caCertKeyUsages));

		final EnumSet<CertificateKeyUsage> signCertKeyUsages = signCertKeyUsageExtension.getKeyUsages();
		final boolean signCertKeyUsagesRetrieved = (signCertKeyUsages.size() == NUM_KEY_USAGES_IN_SIGN_CERT_EXTENSION) && (signCertKeyUsages
				.containsAll(KeyUsageExtensionTest.signCertKeyUsages));

		final EnumSet<CertificateKeyUsage> encryptCertKeyUsages = encryptCertKeyUsageExtension.getKeyUsages();
		final boolean encryptCertKeyUsagesRetrieved = (encryptCertKeyUsages.size() == NUM_KEY_USAGES_IN_ENCRYPT_CERT_EXTENSION) && (encryptCertKeyUsages
				.containsAll(KeyUsageExtensionTest.encryptCertKeyUsages));

		assertTrue(caCertKeyUsagesRetrieved && signCertKeyUsagesRetrieved && encryptCertKeyUsagesRetrieved);
	}

	@Test
	void addExcessKeyUsages() {
		final EnumSet<CertificateKeyUsage> caCertKeyUsages = EnumSet
				.of(CertificateKeyUsage.KEY_CERT_SIGN, CertificateKeyUsage.CRL_SIGN, CertificateKeyUsage.KEY_CERT_SIGN, CertificateKeyUsage.CRL_SIGN);
		final boolean caCertKeyUsagesNotDuplicated = (caCertKeyUsages.size() == NUM_KEY_USAGES_IN_CA_CERT_EXTENSION) && (caCertKeyUsages
				.containsAll(KeyUsageExtensionTest.caCertKeyUsages));

		final EnumSet<CertificateKeyUsage> signCertKeyUsages = EnumSet
				.of(CertificateKeyUsage.DIGITAL_SIGNATURE, CertificateKeyUsage.NON_REPUDIATION, CertificateKeyUsage.DIGITAL_SIGNATURE,
						CertificateKeyUsage.NON_REPUDIATION, CertificateKeyUsage.NON_REPUDIATION);
		final boolean signCertKeyUsagesNotDuplicated = (signCertKeyUsages.size() == NUM_KEY_USAGES_IN_SIGN_CERT_EXTENSION) && (signCertKeyUsages
				.containsAll(KeyUsageExtensionTest.signCertKeyUsages));

		final EnumSet<CertificateKeyUsage> encryptCertKeyUsages = EnumSet
				.of(CertificateKeyUsage.KEY_ENCIPHERMENT, CertificateKeyUsage.DATA_ENCIPHERMENT, CertificateKeyUsage.DATA_ENCIPHERMENT,
						CertificateKeyUsage.KEY_ENCIPHERMENT, CertificateKeyUsage.KEY_ENCIPHERMENT);
		final boolean encryptCertKeyUsagesNotDuplicated = (encryptCertKeyUsages.size() == NUM_KEY_USAGES_IN_ENCRYPT_CERT_EXTENSION) && (encryptCertKeyUsages
				.containsAll(KeyUsageExtensionTest.encryptCertKeyUsages));

		assertTrue(caCertKeyUsagesNotDuplicated && signCertKeyUsagesNotDuplicated && encryptCertKeyUsagesNotDuplicated);
	}

}
