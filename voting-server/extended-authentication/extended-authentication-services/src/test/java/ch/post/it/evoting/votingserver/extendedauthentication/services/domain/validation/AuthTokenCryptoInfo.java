/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.domain.validation;

import java.security.PrivateKey;

import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;

public class AuthTokenCryptoInfo {

	private CryptoAPIX509Certificate certificate;

	private PrivateKey privateKey;

	/**
	 * Gets certificate.
	 *
	 * @return Value of certificate.
	 */
	public CryptoAPIX509Certificate getCertificate() {
		return certificate;
	}

	/**
	 * Sets new certificate.
	 *
	 * @param certificate New value of certificate.
	 */
	public void setCertificate(final CryptoAPIX509Certificate certificate) {
		this.certificate = certificate;
	}

	/**
	 * Gets privateKey.
	 *
	 * @return Value of privateKey.
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * Sets new privateKey.
	 *
	 * @param privateKey New value of privateKey.
	 */
	public void setPrivateKey(final PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
}
