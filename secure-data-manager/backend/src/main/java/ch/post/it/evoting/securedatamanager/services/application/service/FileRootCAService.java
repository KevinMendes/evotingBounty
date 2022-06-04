/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;

/**
 * Service responsible for the loading and storing root CA certificates, such as the platform CA or the tenant CA, in files.
 */
public class FileRootCAService {
	private final PathResolver pathResolver;

	private final String certificateFileName;

	public FileRootCAService(final PathResolver pathResolver, final String certificateFileName) {
		this.pathResolver = pathResolver;
		this.certificateFileName = certificateFileName;
	}

	/**
	 * Stores a given certificate.
	 *
	 * @param certificate the certificate
	 */
	public void save(final X509Certificate certificate) throws CertificateManagementException {
		final String pem;
		try {
			pem = PemUtils.certificateToPem(certificate);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Invalid certificate", e);
		}
		final byte[] bytes = pem.getBytes(StandardCharsets.UTF_8);

		final Path file = getCertificatePath();
		try {
			Files.createDirectories(file.getParent());
			Files.write(file, bytes);
		} catch (final IOException e) {
			throw new CertificateManagementException(e);
		}
	}

	/**
	 * Retrieves a certificate.
	 *
	 * @return the certificate
	 */
	public X509Certificate load() throws CertificateManagementException {
		try {
			final byte[] pem = Files.readAllBytes(getCertificatePath());
			return (X509Certificate) PemUtils.certificateFromPem(new String(pem, StandardCharsets.UTF_8));
		} catch (final IOException e) {
			throw new CertificateManagementException(e);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalStateException("Invalid certificate file", e);
		}
	}

	/**
	 * Returns the configuration PEM file storing the PlatformRoot CA certificate. It is not guaranteed that the returned file really exists.
	 *
	 * @return the certificate file.
	 */
	private Path getCertificatePath() {
		return pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, certificateFileName);
	}
}
