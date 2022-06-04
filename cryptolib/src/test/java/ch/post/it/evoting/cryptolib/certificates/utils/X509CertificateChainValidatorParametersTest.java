/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.cert.X509Certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;

@ExtendWith(MockitoExtension.class)
class X509CertificateChainValidatorParametersTest {

	@Mock
	private X509Certificate leafCertificate;

	@Mock
	private X509DistinguishedName subjectLeafCertificate;

	@Mock
	private X509Certificate trustedCertificate;

	@Mock
	private X509DistinguishedName subjectChain0;

	@Mock
	private X509DistinguishedName subjectChain1;

	@Test
	void validatorCreationOKyTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[1];
		subjects[0] = subjectChain0;
		final X509Certificate[] chain = new X509Certificate[1];
		chain[0] = leafCertificate;

		assertDoesNotThrow(
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, chain, subjects, trustedCertificate));
	}

	@Test
	void trustedCertificateNullTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[1];
		subjects[0] = subjectChain0;
		final X509Certificate[] chain = new X509Certificate[1];
		chain[0] = leafCertificate;

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, chain, subjects, null));
	}

	@Test
	void subjectLeafCertificateNullTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[1];
		subjects[0] = subjectChain0;
		final X509Certificate[] chain = new X509Certificate[1];
		chain[0] = leafCertificate;

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, null, chain, subjects, trustedCertificate));
	}

	@Test
	void subjectsAndChainLengthErrorTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[2];
		subjects[0] = subjectChain0;
		subjects[1] = subjectChain1;
		final X509Certificate[] chain = new X509Certificate[1];
		chain[0] = leafCertificate;

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, chain, subjects, trustedCertificate));
	}

	@Test
	void leafCertificateNullTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[2];
		subjects[0] = subjectChain0;
		subjects[1] = subjectChain1;
		final X509Certificate[] chain = new X509Certificate[1];
		chain[0] = leafCertificate;

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(null, leafKeyType, subjectLeafCertificate, chain, subjects, trustedCertificate));
	}

	@Test
	void chainEmptyTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[1];
		subjects[0] = subjectChain0;
		final X509Certificate[] chain = new X509Certificate[0];

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, chain, subjects, trustedCertificate));
	}

	@Test
	void chainNullTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[1];
		subjects[0] = subjectChain0;

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, null, subjects, trustedCertificate));
	}

	@Test
	void chainAndSubjectsNullTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;

		assertDoesNotThrow(
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, null, null, trustedCertificate));
	}

	@Test
	void chainAndSubjectsEmptyTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[0];
		final X509Certificate[] chain = new X509Certificate[0];

		assertDoesNotThrow(
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, chain, subjects, trustedCertificate));
	}

	@Test
	void subjectsEmptyTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509DistinguishedName[] subjects = new X509DistinguishedName[0];
		final X509Certificate[] chain = new X509Certificate[1];
		chain[0] = leafCertificate;

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, chain, subjects, trustedCertificate));
	}

	@Test
	void subjectsNullTest() {
		final X509CertificateType leafKeyType = X509CertificateType.ENCRYPT;
		final X509Certificate[] chain = new X509Certificate[1];
		chain[0] = leafCertificate;

		assertThrows(GeneralCryptoLibException.class,
				() -> new X509CertificateChainValidator(leafCertificate, leafKeyType, subjectLeafCertificate, chain, null, trustedCertificate));
	}
}
