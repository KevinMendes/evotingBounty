/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.filter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.PublicKey;
import java.security.cert.Certificate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;

class SignedRequestKeyManagerTest {

	@BeforeAll
	static void setUpAll() {
		// Make sure the SignedRequestKeyManager has not already been instantiated by another JUnit test.
		SignedRequestKeyManager.instance = null;
	}

	@Test
	void testGetPublicKeyFromCertificateString() throws GeneralCryptoLibException {
		String certificateContentStr = "-----BEGIN CERTIFICATE-----\n" + "MIIDjzCCAnegAwIBAgIVALxc4pAxqTzXp83vQTNQKwHaERTvMA0GCSqGSIb3DQEB\n"
				+ "CwUAMFwxFjAUBgNVBAMMDVRlbmFudCAxMDAgQ0ExFjAUBgNVBAsMDU9ubGluZSBW\n"
				+ "b3RpbmcxEjAQBgNVBAoMCVN3aXNzUG9zdDEJMAcGA1UEBwwAMQswCQYDVQQGEwJD\n"
				+ "SDAeFw0xNjA5MDcxMTE2MThaFw0xNjEwMTkyMjU5NTlaMIGDMT0wOwYDVQQDDDRB\n"
				+ "ZG1pbmlzdHJhdGlvbkJvYXJkIDNhM2M1MWNkZDE0NDRjOGJhYjA2NjQwZTM3ZTA0\n"
				+ "NzA4MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRIwEAYDVQQKDAlTd2lzc1Bvc3Qx\n"
				+ "CTAHBgNVBAcMADELMAkGA1UEBhMCQ0gwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw\n"
				+ "ggEKAoIBAQCLjRvSUWwAkNAvyGwnksccYJ0XMSa/LmYbE2caVaUTJgkhfkt7uMi2\n"
				+ "e+LjCqEVRbfvcqcuH2SF9dsYrfgCdm/FHQadeciu66BV6vBntc3dCw1GJa4LJQcp\n"
				+ "tTRJBL1ca7FVHl7u3onfIez/o9Jy07P8P2iv+ol8Xvvx4PBa6BvvJkIlukQy+Ayt\n"
				+ "/zggF9QFSzJ+jywse5MLEYwh4oT53uETYHP3pVDGa5crxSOuCfZEk73tyJmgH0ML\n"
				+ "enrbu2oR4yNVfm6qIhijiJ9on05uLXSuYjLBAvhNwTrgYJBVtS8RKzOb5oMqNMKa\n"
				+ "mib7MVuY57bELJe9hsUp2cWNKrF5D3IxAgMBAAGjIDAeMA4GA1UdDwEB/wQEAwIG\n"
				+ "wDAMBgNVHRMBAf8EAjAAMA0GCSqGSIb3DQEBCwUAA4IBAQB9vJEPCixxoDPZThUo\n"
				+ "b6t41iILWBkdAExvQlVyZ/VfQ6/eXSmyviuZLcO13SDa3zp8g1DEe5H9XIfVgyqF\n"
				+ "C7aZ4pV88aI8GNqxnkdO4GmXOZM5mQZ0c9eGPKr0RJ6jUIL6iJmBSSj/gt4I9XoD\n"
				+ "lKBL3xivNMJxw5BVhyqCB5vM1xO6j5fzyfwc7EiJZF4axMly8yg/Ip7tBnZvtZM3\n"
				+ "cFWi0kCM755AFfAMVWPqA7pak4HcTT6c2En4ok+1RB4mVwjirfJJOSPxUsPwFvw4\n"
				+ "vC5KMoy46gcZO/bx6e4wjSNWz9NqwbWxLYfESo55ZNREtPb9rTnlb4JN7GMmvd9y\n" + "QvCO\n" + "-----END CERTIFICATE-----\n";

		Certificate certificateToValidate = PemUtils.certificateFromPem(certificateContentStr);

		CryptoAPIX509Certificate cryptoAPIX509Certificate = SignedRequestKeyManager.getCryptoX509Certificate(certificateToValidate);

		PublicKey publicKey = cryptoAPIX509Certificate.getPublicKey();

		assertNotNull(publicKey);

	}

}
