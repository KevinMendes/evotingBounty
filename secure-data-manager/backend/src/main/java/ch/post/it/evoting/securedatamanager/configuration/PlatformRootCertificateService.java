/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import java.security.cert.X509Certificate;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Allows to load the platform root certificate.
 */
@Service
public class PlatformRootCertificateService {

	private final PlatformRootCertificateFileRepository platformRootCertificateFileRepository;

	public PlatformRootCertificateService(
			final PlatformRootCertificateFileRepository platformRootCertificateFileRepository) {
		this.platformRootCertificateFileRepository = platformRootCertificateFileRepository;
	}

	@Cacheable("platformRootCertificate")
	public X509Certificate load() {
		return platformRootCertificateFileRepository.load();
	}
}
