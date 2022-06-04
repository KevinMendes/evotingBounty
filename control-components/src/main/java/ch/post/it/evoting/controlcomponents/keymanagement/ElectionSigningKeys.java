/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Election signing keys container.
 */
public class ElectionSigningKeys {
	private final PrivateKey privateKey;

	private final X509Certificate[] certificateChain;

	/**
	 * Constructor.
	 *
	 * @param privateKey       the private key
	 * @param certificateChain the certificate chain.
	 */
	public ElectionSigningKeys(PrivateKey privateKey, X509Certificate[] certificateChain) {
		this.privateKey = privateKey;
		this.certificateChain = certificateChain;
	}

	/**
	 * Returns the certificate.
	 *
	 * @return the certificate.
	 */
	public X509Certificate certificate() {
		return certificateChain[0];
	}

	/**
	 * Returns the certificate chain.
	 *
	 * @return the certificate chain.
	 */
	public X509Certificate[] certificateChain() {
		return certificateChain;
	}

	/**
	 * Returns the private key.
	 *
	 * @return the private key.
	 */
	public PrivateKey privateKey() {
		return privateKey;
	}

	/**
	 * Returns the public key.
	 *
	 * @return the public key.
	 */
	public PublicKey publicKey() {
		return certificate().getPublicKey();
	}
}
