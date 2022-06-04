/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.util.Map;

/**
 * A class representing all properties needed to generate a credential: name, alias, properties file, parent name (for the cert chain) and credential
 * type. Used as a part of ConfigurationInput bean.
 */
public class CredentialProperties {

	private CertificateParameters.Type credentialType;

	private Map<String, String> alias;

	private String name;
	private String propertiesFile;
	private String parentName;

	public CertificateParameters.Type getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(final CertificateParameters.Type credentialType) {
		this.credentialType = credentialType;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Map<String, String> getAlias() {
		return alias;
	}

	public void setAlias(final Map<String, String> alias) {
		this.alias = alias;
	}

	public String getPropertiesFile() {
		return propertiesFile;
	}

	public void setPropertiesFile(final String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(final String parentName) {
		this.parentName = parentName;
	}

	@Override
	public String toString() {
		return "CredentialProperties [name=" + name + ", alias=" + alias + ", propertiesFile=" + propertiesFile + ", parentName=" + parentName
				+ ", credentialType=" + credentialType + "]";
	}

	public String obtainPrivateKeyAlias() {
		return alias.get("privateKey");
	}

	public String obtainSecretKeyAlias() {
		return alias.get("secretKey");
	}
}
