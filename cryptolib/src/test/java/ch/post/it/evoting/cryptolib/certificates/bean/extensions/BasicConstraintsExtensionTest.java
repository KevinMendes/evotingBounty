/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;

class BasicConstraintsExtensionTest {

	private static final boolean ROOT_BASIC_CONSTRAINTS_EXTENSION_IS_CRITICAL = true;
	private static final boolean USER_BASIS_CONSTRAINTS_EXTENSION_IS_CRITICAL = false;
	private static final boolean ROOT_IS_CERTIFICATE_AUTHORITY = true;
	private static final boolean USER_IS_CERTIFICATE_AUTHORITY = false;

	private static BasicConstraintsExtension rootBasicConstraintsExtension;
	private static BasicConstraintsExtension userBasicConstraintsExtension;

	@BeforeAll
	static void setUp() {
		rootBasicConstraintsExtension = new BasicConstraintsExtension(ROOT_BASIC_CONSTRAINTS_EXTENSION_IS_CRITICAL, ROOT_IS_CERTIFICATE_AUTHORITY);
		userBasicConstraintsExtension = new BasicConstraintsExtension(USER_BASIS_CONSTRAINTS_EXTENSION_IS_CRITICAL, USER_IS_CERTIFICATE_AUTHORITY);
	}

	@Test
	void retrieveExtensionType() {
		final boolean rootExtensionTypeRetrieved = rootBasicConstraintsExtension.getExtensionType() == ExtensionType.BASIC_CONSTRAINTS;
		final boolean userExtensionTypeRetrieved = userBasicConstraintsExtension.getExtensionType() == ExtensionType.BASIC_CONSTRAINTS;

		assertTrue(rootExtensionTypeRetrieved && userExtensionTypeRetrieved);
	}

	@Test
	void retrieveIsCriticalFlag() {
		final boolean rootIsCriticalFlagRetrieved = rootBasicConstraintsExtension.isCritical() == ROOT_BASIC_CONSTRAINTS_EXTENSION_IS_CRITICAL;
		final boolean userIsCriticalFlagRetrieved = userBasicConstraintsExtension.isCritical() == USER_BASIS_CONSTRAINTS_EXTENSION_IS_CRITICAL;

		assertTrue(rootIsCriticalFlagRetrieved && userIsCriticalFlagRetrieved);
	}

	@Test
	void retrieveInternallySetIsCriticalFlag() {
		final BasicConstraintsExtension rootBasicConstraintsExtension = new BasicConstraintsExtension(ROOT_IS_CERTIFICATE_AUTHORITY);
		final boolean rootIsCriticalFlagRetrieved =
				rootBasicConstraintsExtension.isCritical() == X509CertificateConstants.CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT;

		final BasicConstraintsExtension userBasicConstraintsExtension = new BasicConstraintsExtension(USER_IS_CERTIFICATE_AUTHORITY);
		final boolean userIsCriticalFlagRetrieved =
				userBasicConstraintsExtension.isCritical() == X509CertificateConstants.CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT;

		assertTrue(rootIsCriticalFlagRetrieved && userIsCriticalFlagRetrieved);
	}

	@Test
	void retrieveIsCertificateAuthorityFlag() {
		final boolean rootIsCertificateAuthorityFlagRetrieved = rootBasicConstraintsExtension.isCertificateAuthority() == ROOT_IS_CERTIFICATE_AUTHORITY;
		final boolean userIsCertificateAuthorityFlagRetrieved = userBasicConstraintsExtension.isCertificateAuthority() == USER_IS_CERTIFICATE_AUTHORITY;

		assertTrue(rootIsCertificateAuthorityFlagRetrieved && userIsCertificateAuthorityFlagRetrieved);
	}
}
