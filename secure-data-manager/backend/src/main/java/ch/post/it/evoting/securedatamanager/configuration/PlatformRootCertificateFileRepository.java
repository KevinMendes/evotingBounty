/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

/**
 * Allows performing operations with the platform root certificate. The certificate is persisted/retrieved to/from the file system of the SDM, in its
 * workspace.
 */
@Service
public class PlatformRootCertificateFileRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformRootCertificateFileRepository.class);

	private final PathResolver pathResolver;

	public PlatformRootCertificateFileRepository(final PathResolver pathResolver) {
		this.pathResolver = pathResolver;
	}

	/**
	 * Persists the platform root certificate to the file system.
	 *
	 * @param platformRootCertificate the certificate to persist.
	 * @return the path where the certificate was saved.
	 * @throws NullPointerException     if {@code platformRootCertificate} is null.
	 * @throws IllegalArgumentException if the conversion of the certificate in PEM format fails.
	 * @throws UncheckedIOException     if writing the certificate to the file system fails.
	 */
	public Path save(final X509Certificate platformRootCertificate) {
		checkNotNull(platformRootCertificate);

		final Path certificatePath = pathResolver.resolveConfigPath().resolve(Constants.CONFIG_FILE_NAME_PLATFORM_ROOT_CA);

		final String pem;
		try {
			pem = PemUtils.certificateToPem(platformRootCertificate);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalArgumentException(String.format("Failed to convert certificate to pem. [path: %s]", certificatePath), e);
		}

		final byte[] bytes = pem.getBytes(StandardCharsets.UTF_8);
		try {
			final Path writePath = Files.write(certificatePath, bytes);
			LOGGER.debug("Successfully persisted platform root certificate. [path: {}]", certificatePath);

			return writePath;
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Failed to save certificate. [path: %s]", certificatePath), e);
		}
	}

	/**
	 * Retrieves the platform root certificate from the file system.
	 *
	 * @return the platform root certificate as a {@link X509Certificate}.
	 * @throws UncheckedIOException if reading the certificate from file system fails
	 */
	public X509Certificate load() {
		final Path certificatePath = pathResolver.resolveConfigPath().resolve(Constants.CONFIG_FILE_NAME_PLATFORM_ROOT_CA);

		final byte[] pem;
		try {
			pem = Files.readAllBytes(certificatePath);
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Failed to read certificate file. [path: %s]", certificatePath), e);
		}

		try {
			return (X509Certificate) PemUtils.certificateFromPem(new String(pem, StandardCharsets.UTF_8));
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalArgumentException(String.format("Failed to parse certificate file. [path: %s]", certificatePath));
		}
	}

}
