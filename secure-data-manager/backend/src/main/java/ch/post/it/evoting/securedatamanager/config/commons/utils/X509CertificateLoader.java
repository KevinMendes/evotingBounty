/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.springframework.stereotype.Component;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;

/**
 * Reads a X.509 certificate from a file in a pem format.
 */
@Component
public class X509CertificateLoader {

	public CryptoAPIX509Certificate load(final String fileName) throws GeneralSecurityException, GeneralCryptoLibException, IOException {
		try (final InputStream is = Files.newInputStream(Paths.get(fileName))) {
			return load(is);
		}
	}

	public CryptoAPIX509Certificate load(final Path fileName) throws GeneralSecurityException, GeneralCryptoLibException, IOException {
		try (final InputStream is = Files.newInputStream(fileName)) {
			return load(is);
		}
	}

	public CryptoAPIX509Certificate load(final InputStream inputStream) throws GeneralSecurityException, GeneralCryptoLibException {

		final CertificateFactory cf = CertificateFactory.getInstance("X.509");
		final X509Certificate crt = (X509Certificate) cf.generateCertificate(inputStream);
		return new CryptoX509Certificate(crt);
	}
}
