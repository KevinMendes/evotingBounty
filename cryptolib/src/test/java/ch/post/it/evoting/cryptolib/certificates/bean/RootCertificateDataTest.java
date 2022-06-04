/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.Principal;
import java.security.PublicKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPublicKey;

class RootCertificateDataTest {

	private RootCertificateData target;

	@BeforeEach
	void setUp() {
		target = new RootCertificateData();
	}

	@Test
	void setPublicKeyTest() {
		final PublicKey publicKey = new TestPublicKey(new byte[1]);

		assertDoesNotThrow(() -> target.setSubjectPublicKey(publicKey));
	}

	@Test
	void setNullPublicKeyTest() {
		assertThrows(GeneralCryptoLibException.class, () -> target.setSubjectPublicKey(null));
	}

	@Test
	void setPublicKeyEmptyBytesTest() {
		final PublicKey publicKey = new TestPublicKey(null);

		assertThrows(GeneralCryptoLibException.class, () -> target.setSubjectPublicKey(publicKey));
	}

	@Test
	void setNullX509Test() {
		final X509DistinguishedName subjectDn = null;

		assertThrows(GeneralCryptoLibException.class, () -> target.setSubjectDn(subjectDn));
	}

	@Test
	void setNullPrincipalTest() {
		final Principal subjectDnPrincipal = null;

		assertThrows(GeneralCryptoLibException.class, () -> target.setSubjectDn(subjectDnPrincipal));
	}

	@Test
	void setNullValidityDaysTest() {
		assertThrows(GeneralCryptoLibException.class, () -> target.setValidityDates(null));
	}
}
