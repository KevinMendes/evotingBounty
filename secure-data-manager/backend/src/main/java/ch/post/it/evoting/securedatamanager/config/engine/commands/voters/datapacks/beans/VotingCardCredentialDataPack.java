/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans;

import java.security.KeyPair;

import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.SerializedCredentialDataPack;

public class VotingCardCredentialDataPack extends SerializedCredentialDataPack {

	private KeyPair keyPairAuth;

	private CryptoAPIX509Certificate certificateAuth;

	public KeyPair getKeyPairAuth() {
		return keyPairAuth;
	}

	public void setKeyPairAuth(final KeyPair keyPairAuth) {
		this.keyPairAuth = keyPairAuth;
	}

	public CryptoAPIX509Certificate getCertificateAuth() {
		return certificateAuth;
	}

	public void setCertificateAuth(final CryptoAPIX509Certificate certificateAuth) {
		this.certificateAuth = certificateAuth;
	}
}
