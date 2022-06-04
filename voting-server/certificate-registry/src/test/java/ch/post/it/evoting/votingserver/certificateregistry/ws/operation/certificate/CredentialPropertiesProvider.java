/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.operation.certificate;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.votingserver.commons.beans.CertificateConfigurationInput;
import ch.post.it.evoting.votingserver.commons.util.ConfigObjectMapper;

public class CredentialPropertiesProvider {
	private final CertificateConfigurationInput certificateConfigurationInput;

	public CredentialPropertiesProvider() {
		URL url = this.getClass().getResource("/keys_config.json");

		ConfigObjectMapper _configObjectMapper = new ConfigObjectMapper();
		try {
			certificateConfigurationInput = _configObjectMapper.fromJSONFileToJava(new File(url.toURI()), CertificateConfigurationInput.class);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("An error occurred while reading the internal configuration file");
		}
	}

	public CredentialProperties getPlatformRootCredentialPropertiesFromClassPath() {
		return certificateConfigurationInput.getPlatformRootCA();
	}

	public CredentialProperties getTenantCredentialPropertiesFromClassPath() {
		return certificateConfigurationInput.getTenantCA();
	}

	public CredentialProperties getServicesLoggingSigningCredentialPropertiesFromClassPath() {
		return certificateConfigurationInput.getLoggingServicesSignerCert();
	}

	public CredentialProperties getServicesLoggingEncryptionCredentialPropertiesFromClassPath() {
		return certificateConfigurationInput.getLoggingServicesEncryptionCert();
	}

}
