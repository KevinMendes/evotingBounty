/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain;

import java.util.Map;
import java.util.Properties;

/**
 * Contains the properties needed to create the certificates that are created when creating an election event.
 * <p>
 * The framework that actually creates the certificates (inside the config generator) expects most of properties to be included in a map. However, the
 * Authentication Token Signer certificate (which is also created during the creation of the election event) is handled separately, and therefore its
 * properties are not included in the map. Although, the properties needed for the creation of the Authentication Token Signer certificate are exactly
 * the same type as those needed for the other certificates.
 */
public class CreateElectionEventCertificatePropertiesContainer {

	/**
	 * Contains properties needed for the following certificates:
	 * <ul>
	 * <li>Election Event CA</li>
	 * <li>Services CA</li>
	 * <li>Credentails CA</li>
	 * <li>Authorities CA</li>
	 * </ul>
	 */
	private Map<String, Properties> nameToCertificateProperties;

	/**
	 * Properties needed for the creation of the Authentication Token Signer Certificate.
	 */
	private Properties authTokenSignerCertificateProperties;

	public Map<String, Properties> getNameToCertificateProperties() {
		return this.nameToCertificateProperties;
	}

	public void setNameToCertificateProperties(final Map<String, Properties> mapNameToCertificateParameters) {
		this.nameToCertificateProperties = mapNameToCertificateParameters;
	}

	public Properties getAuthTokenSignerCertificateProperties() {
		return authTokenSignerCertificateProperties;
	}

	public void setAuthTokenSignerCertificateProperties(final Properties authTokenSignerX509CertificateProperties) {
		this.authTokenSignerCertificateProperties = authTokenSignerX509CertificateProperties;
	}
}
