/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Node keys container.
 */
class NodeKeys {
	private final PrivateKey caPrivateKey;

	private final X509Certificate[] caCertificateChain;

	private final PrivateKey encryptionPrivateKey;

	private final X509Certificate[] encryptionCertificateChain;

	private final PrivateKey logSigningPrivateKey;

	private final X509Certificate[] logSigningCertificateChain;

	private final PrivateKey logEncryptionPrivateKey;

	private final X509Certificate[] logEncryptionCertificateChain;

	private NodeKeys(PrivateKey caPrivateKey, X509Certificate[] caCertificateChain, PrivateKey encryptionPrivateKey,
			X509Certificate[] encryptionCertificateChain, PrivateKey logSigningPrivateKey, X509Certificate[] logSigningCertificateChain,
			PrivateKey logEncryptionPrivateKey, X509Certificate[] logEncryptionCertificateChain) {
		this.caPrivateKey = caPrivateKey;
		this.caCertificateChain = caCertificateChain;
		this.encryptionPrivateKey = encryptionPrivateKey;
		this.encryptionCertificateChain = encryptionCertificateChain;
		this.logSigningPrivateKey = logSigningPrivateKey;
		this.logSigningCertificateChain = logSigningCertificateChain;
		this.logEncryptionPrivateKey = logEncryptionPrivateKey;
		this.logEncryptionCertificateChain = logEncryptionCertificateChain;
	}

	/**
	 * Returns the CCN CA certificate.
	 *
	 * @return the CCN CA certificate.
	 */
	public X509Certificate caCertificate() {
		return caCertificateChain[0];
	}

	/**
	 * Returns the CCN CA certificate chain.
	 *
	 * @return the CCN CA certificate chain.
	 */
	public X509Certificate[] caCertificateChain() {
		return caCertificateChain;
	}

	/**
	 * Returns the CCN CA private key.
	 *
	 * @return the CCN CA private key.
	 */
	public PrivateKey caPrivateKey() {
		return caPrivateKey;
	}

	/**
	 * Returns the CCN CA public key.
	 *
	 * @return the CCN CA public key.
	 */
	public PublicKey caPublicKey() {
		return caCertificate().getPublicKey();
	}

	/**
	 * Returns the encryption certificate.
	 *
	 * @return the encryption certificate.
	 */
	public X509Certificate encryptionCertificate() {
		return encryptionCertificateChain[0];
	}

	/**
	 * Returns the encryption certificate chain.
	 *
	 * @return the encryption certificate chain.
	 */
	public X509Certificate[] encryptionCertificateChain() {
		return encryptionCertificateChain;
	}

	/**
	 * Returns the encryption private key.
	 *
	 * @return the encryption private key.
	 */
	public PrivateKey encryptionPrivateKey() {
		return encryptionPrivateKey;
	}

	/**
	 * Returns the encryption public key.
	 *
	 * @return the encryption public key.
	 */
	public PublicKey encryptionPublicKey() {
		return encryptionCertificate().getPublicKey();
	}

	/**
	 * Returns the log encryption certificate.
	 *
	 * @return the log encryption certificate.
	 */
	public X509Certificate logEncryptionCertificate() {
		return logEncryptionCertificateChain[0];
	}

	/**
	 * Returns the log encryption certificate chain.
	 *
	 * @return the log encryption certificate chain.
	 */
	public X509Certificate[] logEncryptionCertificateChain() {
		return logEncryptionCertificateChain;
	}

	/**
	 * Returns the log encryption private key.
	 *
	 * @return the log encryption private key.
	 */
	public PrivateKey logEncryptionPrivateKey() {
		return logEncryptionPrivateKey;
	}

	/**
	 * Returns the log encryption public key.
	 *
	 * @return the log encryption public key.
	 */
	public PublicKey logEncryptionPublicKey() {
		return logEncryptionCertificate().getPublicKey();
	}

	/**
	 * Returns the log signing certificate.
	 *
	 * @return the log signing certificate.
	 */
	public X509Certificate logSigningCertificate() {
		return logSigningCertificateChain[0];
	}

	/**
	 * Returns the log signing certificate chain.
	 *
	 * @return the log signing certificate chain.
	 */
	public X509Certificate[] logSigningCertificateChain() {
		return logSigningCertificateChain;
	}

	/**
	 * Returns the log signing private key.
	 *
	 * @return the log signing private key.
	 */
	public PrivateKey logSigningPrivateKey() {
		return logSigningPrivateKey;
	}

	/**
	 * Returns the log signing public key.
	 *
	 * @return the log signing public key.
	 */
	public PublicKey logSigningPublicKey() {
		return logSigningCertificate().getPublicKey();
	}

	/**
	 * Builder for creating {@link NodeKeys} instances.
	 */
	static class Builder {
		private PrivateKey caPrivateKey;

		private X509Certificate[] caCertificateChain;

		private PrivateKey encryptionPrivateKey;

		private X509Certificate[] encryptionCertificateChain;

		private PrivateKey logSigningPrivateKey;

		private X509Certificate[] logSigningCertificateChain;

		private PrivateKey logEncryptionPrivateKey;

		private X509Certificate[] logEncryptionCertificateChain;

		/**
		 * Builds a new {@link NodeKeys} instance.
		 *
		 * @return a new instance.
		 */
		public NodeKeys build() {
			return new NodeKeys(caPrivateKey, caCertificateChain, encryptionPrivateKey, encryptionCertificateChain, logSigningPrivateKey,
					logSigningCertificateChain, logEncryptionPrivateKey, logEncryptionCertificateChain);
		}

		/**
		 * Sets the CCN CA keys.
		 *
		 * @param privateKey       the private key
		 * @param certificateChain the certificate chain
		 * @return this instance.
		 */
		public Builder setCAKeys(PrivateKey privateKey, X509Certificate[] certificateChain) {
			caPrivateKey = privateKey;
			caCertificateChain = certificateChain;
			return this;
		}

		/**
		 * Sets the CCN CA keys.
		 *
		 * @param entry the private key and certificate chain.
		 * @return this instance.
		 */
		public Builder setCAKeys(PrivateKeyEntry entry) {
			return setCAKeys(entry.getPrivateKey(), (X509Certificate[]) entry.getCertificateChain());
		}

		/**
		 * Sets the encryption keys.
		 *
		 * @param privateKey       the private key
		 * @param certificateChain the certificate chain
		 * @return this instance.
		 */
		public Builder setEncryptionKeys(PrivateKey privateKey, X509Certificate[] certificateChain) {
			encryptionPrivateKey = privateKey;
			encryptionCertificateChain = certificateChain;
			return this;
		}

		/**
		 * Sets the encryption keys.
		 *
		 * @param entry the private key and certificate chain.
		 * @return this instance.
		 */
		public Builder setEncryptionKeys(PrivateKeyEntry entry) {
			return setEncryptionKeys(entry.getPrivateKey(), (X509Certificate[]) entry.getCertificateChain());
		}

		/**
		 * Sets the log encryption keys.
		 *
		 * @param privateKey       the private key
		 * @param certificateChain the certificate chain
		 * @return this instance.
		 */
		public Builder setLogEncryptionKeys(PrivateKey privateKey, X509Certificate[] certificateChain) {
			logEncryptionPrivateKey = privateKey;
			logEncryptionCertificateChain = certificateChain;
			return this;
		}

		/**
		 * Sets the log encryption keys.
		 *
		 * @param entry the private key and certificate chain.
		 * @return this instance.
		 */
		public Builder setLogEncryptionKeys(PrivateKeyEntry entry) {
			return setLogEncryptionKeys(entry.getPrivateKey(), (X509Certificate[]) entry.getCertificateChain());
		}

		/**
		 * Sets the log signing keys.
		 *
		 * @param privateKey       the private key
		 * @param certificateChain the certificate chain
		 * @return this instance.
		 */
		public Builder setLogSigningKeys(PrivateKey privateKey, X509Certificate[] certificateChain) {
			logSigningPrivateKey = privateKey;
			logSigningCertificateChain = certificateChain;
			return this;
		}

		/**
		 * Sets the log signing keys.
		 *
		 * @param entry the private key and certificate chain.
		 * @return this instance.
		 */
		public Builder setLogSigningKeys(PrivateKeyEntry entry) {
			return setLogSigningKeys(entry.getPrivateKey(), (X509Certificate[]) entry.getCertificateChain());
		}
	}
}
