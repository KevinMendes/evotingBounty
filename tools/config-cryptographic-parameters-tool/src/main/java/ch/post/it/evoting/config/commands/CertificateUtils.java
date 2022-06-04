/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CertificateUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(CertificateUtils.class);

	private CertificateFactory certificateFactory;

	public CertificateUtils() {

		Security.addProvider(new BouncyCastleProvider());
		try {
			certificateFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
		} catch (CertificateException e) {
			LOGGER.error("Failed to instantiate Certificate Factory.", e);
		} catch (NoSuchProviderException e) {
			LOGGER.error("Failed to get BC provider.", e);
		}
	}

	public Certificate readTrustedCA(final Path trustedCAPath) throws IOException {
		try (InputStream s = Files.newInputStream(trustedCAPath)) {
			return certificateFactory.generateCertificate(s);
		} catch (CertificateException e) {
			throw new IllegalArgumentException("Failed reading the Trusted CA PEM.", e);
		}
	}
}
