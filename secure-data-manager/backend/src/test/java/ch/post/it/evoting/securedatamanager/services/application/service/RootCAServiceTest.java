/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.deleteIfExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;

/**
 * Tests of {@link PlatformRootCAService}.
 */
class RootCAServiceTest {

	private Path file;
	private FileRootCAService service;

	@BeforeEach
	void setUp() throws IOException {
		file = createTempFile("platformRootCA", ".pem");
		final PathResolver resolver = args -> file;
		service = new FileRootCAService(resolver, file.getFileName().toString());
	}

	@AfterEach
	void tearDown() throws IOException {
		deleteIfExists(file);
	}

	@Test
	void testSaveLoad() throws GeneralCryptoLibException, CertificateManagementException {
		final AsymmetricServiceAPI asymmetricService = new AsymmetricService();
		final KeyPair pair = asymmetricService.getKeyPairForSigning();
		final CertificatesServiceAPI certificateService = new CertificatesService();
		final RootCertificateData data = new RootCertificateData();
		final X509DistinguishedName name = new X509DistinguishedName.Builder("TEST", "ES").build();
		data.setSubjectPublicKey(pair.getPublic());
		data.setSubjectDn(name);
		final Date from = new Date();
		final Date to = new Date(from.getTime() + 1000);
		final ValidityDates dates = new ValidityDates(from, to);
		data.setValidityDates(dates);
		final CryptoAPIX509Certificate certificate = certificateService.createRootAuthorityX509Certificate(data, pair.getPrivate());

		final X509Certificate expected = certificate.getCertificate();
		service.save(expected);
		final X509Certificate actual = service.load();
		assertEquals(expected, actual);
	}
}
