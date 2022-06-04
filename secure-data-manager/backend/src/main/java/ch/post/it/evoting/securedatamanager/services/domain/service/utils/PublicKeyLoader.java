/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;

@Component
public class PublicKeyLoader {

	private static final Charset CHAR_SET = StandardCharsets.UTF_8;

	private static final Logger LOGGER = LoggerFactory.getLogger(PublicKeyLoader.class);

	public String getPublicKeyAsStringFromCertificate(final Path certificatePath) throws IOException {

		final String certificateFullPath = certificatePath.toAbsolutePath().toString();
		LOGGER.info("Attempting to obtain public key from certificate at: " + certificateFullPath);

		final PublicKey publicKey;

		try {

			publicKey = getPublicKeyFromCertificatePemFile(certificateFullPath);

			final String publickeyAsPem = PemUtils.publicKeyToPem(publicKey);

			LOGGER.info("Successfully obtained public key");
			return publickeyAsPem;

		} catch (final GeneralCryptoLibException | IOException e) {

			final String errorMsg1 = "Error while trying to obtain public key from certificate at: " + certificateFullPath;
			final String errorMsg2 = "Exception was " + e.getMessage();
			LOGGER.error(errorMsg1);
			LOGGER.error(errorMsg2);
			throw new IOException(errorMsg1, e);
		}
	}

	private PublicKey getPublicKeyFromCertificatePemFile(final String path) throws IOException, GeneralCryptoLibException {

		final InputStream inputStream = new FileInputStream(new File(path));

		final String certificateAsPemString = IOUtils.toString(inputStream, CHAR_SET);

		final Certificate cert = PemUtils.certificateFromPem(certificateAsPemString);

		return cert.getPublicKey();
	}
}
