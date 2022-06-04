/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans;

import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;

public class CertificateConfigurationInput {

	private CredentialProperties platformRootCA;

	private CredentialProperties tenantCA;

	private CredentialProperties loggingServicesSignerCert;

	private CredentialProperties loggingServicesEncryptionCert;

	private CredentialProperties systemServicesEncryptionCert;

	/**
	 * Gets platformRootCA.
	 *
	 * @return Value of platformRootCA.
	 */
	public CredentialProperties getPlatformRootCA() {
		return platformRootCA;
	}

	/**
	 * Sets new platformRootCA.
	 *
	 * @param platformRootCA New value of platformRootCA.
	 */
	public void setPlatformRootCA(CredentialProperties platformRootCA) {
		this.platformRootCA = platformRootCA;
	}

	/**
	 * Gets tenantCA.
	 *
	 * @return Value of tenantCA.
	 */
	public CredentialProperties getTenantCA() {
		return tenantCA;
	}

	/**
	 * Sets new tenantCA.
	 *
	 * @param tenantCA New value of tenantCA.
	 */
	public void setTenantCA(CredentialProperties tenantCA) {
		this.tenantCA = tenantCA;
	}

	/**
	 * Gets systemServicesEncryptionCA.
	 *
	 * @return Value of systemServicesEncryptionCA.
	 */
	public CredentialProperties getSystemServicesEncryptionCert() {
		return systemServicesEncryptionCert;
	}

	/**
	 * Sets new systemServicesEncryptionCA.
	 *
	 * @param systemServicesEncryptionCert New value of systemServicesEncryptionCA.
	 */
	public void setSystemServicesEncryptionCert(CredentialProperties systemServicesEncryptionCert) {
		this.systemServicesEncryptionCert = systemServicesEncryptionCert;
	}

	/**
	 * Gets loggingServicesEncryptionCert.
	 *
	 * @return Value of loggingServicesEncryptionCert.
	 */
	public CredentialProperties getLoggingServicesEncryptionCert() {
		return loggingServicesEncryptionCert;
	}

	/**
	 * Sets new loggingServicesEncryptionCert.
	 *
	 * @param loggingServicesEncryptionCert New value of loggingServicesEncryptionCert.
	 */
	public void setLoggingServicesEncryptionCert(CredentialProperties loggingServicesEncryptionCert) {
		this.loggingServicesEncryptionCert = loggingServicesEncryptionCert;
	}

	/**
	 * Gets loggingServicesSignerCert.
	 *
	 * @return Value of loggingServicesSignerCert.
	 */
	public CredentialProperties getLoggingServicesSignerCert() {
		return loggingServicesSignerCert;
	}

	/**
	 * Sets new loggingServicesSignerCert.
	 *
	 * @param loggingServicesSignerCert New value of loggingServicesSignerCert.
	 */
	public void setLoggingServicesSignerCert(CredentialProperties loggingServicesSignerCert) {
		this.loggingServicesSignerCert = loggingServicesSignerCert;
	}
}
