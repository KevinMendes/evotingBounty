/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;

import java.security.PrivateKey;

import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;

/**
 * A base class to represent the basic information of a datapack: a keystore, a password and a parentPrivateKey representing the certificate parent on
 * the chain cert.
 */
public class CredentialDataPack {

	private CryptoAPIExtendedKeyStore keyStore;

	private char[] password;

	private String encryptedPassword;

	private PrivateKey parentPrivateKey;

	/**
	 * @return Returns the parentPrivateKey.
	 */
	public PrivateKey getParentPrivateKey() {
		return parentPrivateKey;
	}

	/**
	 * @param parentPrivateKey The parentPrivateKey to set.
	 */
	public void setParentPrivateKey(final PrivateKey parentPrivateKey) {
		this.parentPrivateKey = parentPrivateKey;
	}

	public CryptoAPIExtendedKeyStore getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(final CryptoAPIExtendedKeyStore keyStore) {
		this.keyStore = keyStore;
	}

	public void clearPassword() {
		fill(password, ' ');
	}

	public char[] getPassword() {
		return copyOf(password, password.length);
	}

	public void setPassword(final char[] password) {
		this.password = copyOf(password, password.length);
	}

	public String getEncryptedPassword() {
		return encryptedPassword;
	}

	public void setEncryptedPassword(final String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}
}
