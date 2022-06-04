/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;

/**
 * Entity that represents a cryptographic derived key
 */
public class AuthenticationDerivedElement {

	private final CryptoAPIDerivedKey derivedKey;

	private final String derivedKeyInEx;

	private AuthenticationDerivedElement(final CryptoAPIDerivedKey derivedKey, final String derivedKeyInEx) {
		this.derivedKey = derivedKey;
		this.derivedKeyInEx = derivedKeyInEx;
	}

	public static AuthenticationDerivedElement of(final CryptoAPIDerivedKey derivedKey, final String derivedKeyInEx) {
		return new AuthenticationDerivedElement(derivedKey, derivedKeyInEx);
	}

	public CryptoAPIDerivedKey getDerivedKey() {
		return derivedKey;
	}

	public String getDerivedKeyInEx() {
		return derivedKeyInEx;
	}
}
