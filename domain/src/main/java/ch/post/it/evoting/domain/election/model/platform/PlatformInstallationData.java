/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.platform;

/**
 * Encapsulates the data that is need to be uploaded to each context to perform the installation of a platform.
 * <ul>
 * <li>The platform root issuer CA certificate (in PEM format)</li>
 * <li>The platform root CA certificate (in PEM format)</li>
 * <li>The logging encryption keystore (encoded in base64)</li>
 * <li>The logging signing keystore (encoded in base 64)</li>
 * </ul>
 * <p>
 * Note: the logging system requires two keypairs: one for logging and one for signing.
 */
public final class PlatformInstallationData {

	private String platformRootIssuerCaPEM;

	private String platformRootCaPEM;

	private String loggingEncryptionKeystoreBase64;

	private String loggingSigningKeystoreBase64;

	public String getPlatformRootIssuerCaPEM() {
		return platformRootIssuerCaPEM;
	}

	public void setPlatformRootIssuerCaPEM(String platformRootIssuerCaPEM) {
		this.platformRootIssuerCaPEM = platformRootIssuerCaPEM;
	}

	public String getPlatformRootCaPEM() {
		return platformRootCaPEM;
	}

	public void setPlatformRootCaPEM(final String platformRootCaPEM) {
		this.platformRootCaPEM = platformRootCaPEM;
	}

	public String getLoggingEncryptionKeystoreBase64() {
		return loggingEncryptionKeystoreBase64;
	}

	public void setLoggingEncryptionKeystoreBase64(final String loggingEncryptionKeystoreBase64) {
		this.loggingEncryptionKeystoreBase64 = loggingEncryptionKeystoreBase64;
	}

	public String getLoggingSigningKeystoreBase64() {
		return loggingSigningKeystoreBase64;
	}

	public void setLoggingSigningKeystoreBase64(final String loggingSigningKeystoreBase64) {
		this.loggingSigningKeystoreBase64 = loggingSigningKeystoreBase64;
	}
}
