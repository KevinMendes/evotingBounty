/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class CryptolibInitializer {
	private CryptolibInitializer() {
	}

	public static synchronized void initialize() {
		Security.addProvider(new BouncyCastleProvider());
	}
}
