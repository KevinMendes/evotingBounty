/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Principal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

@DisplayName("A CertificateData")
class CertificateDataTest {

	@Test
	@DisplayName("throws exception if setting null X509DistinguishedName")
	void setNullX509Test() {
		final CertificateData target = new CertificateData();
		final X509DistinguishedName issuerDn = null;

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> target.setIssuerDn(issuerDn));
		assertTrue(exception.getMessage().contains("Issuer distinguished name"));
	}

	@Test
	@DisplayName("throws exception if setting null Principal")
	void setNullPrincipalTest() {
		final CertificateData target = new CertificateData();
		final Principal issuerDnPrincipal = null;

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> target.setIssuerDn(issuerDnPrincipal));
		assertTrue(exception.getMessage().contains("Issuer Principal"));
	}
}
