/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.utils;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;

/**
 * Allows for the loading of public keys.
 */
@Component
public class SystemTenantPublicKeyLoader {

	private static final String FILENAME_PARTS_SEPARATOR = "_";

	@Autowired
	private PublicKeyLoader publicKeyLoader;

	@Autowired
	private PathResolver pathResolver;

	/**
	 * Loads a public key from a file, and returns it as a string in PEM format.
	 * <p>
	 * Note: this method assumes that a file with the following name will exist in the 'systemkeys' directory:
	 * <p>
	 * <tenantID>_<service name>_<type>.pem where "service name" is a two character string, and type
	 * is one of ENCRYPTION | SIGN.
	 *
	 * @param tenantID    the tenant identifier.
	 * @param serviceName a two character ID of the name of the service.
	 * @return the loaded public key as a string in PEM format.
	 * @throws IOException
	 */
	public String load(final String tenantID, final String serviceName, final CertificateParameters.Type type) throws IOException {

		final Path path = buildSystemTenantCertificatePath(tenantID, serviceName, type);
		return publicKeyLoader.getPublicKeyAsStringFromCertificate(path);
	}

	private Path buildSystemTenantCertificatePath(final String tenantID, final String serviceName, final CertificateParameters.Type type) {

		final String certificateFilename = tenantID + FILENAME_PARTS_SEPARATOR + serviceName + FILENAME_PARTS_SEPARATOR + type.name() + Constants.PEM;
		return pathResolver.resolve(Constants.SYSTEM_TENANT_KEYS).resolve(certificateFilename);
	}
}
