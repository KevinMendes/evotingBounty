/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.configuration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Defines the providers that are supported for creating secret keys.
 */
public enum SecretKeyProvider {
	SUNJCE("SunJCE"),
	BOUNCY_CASTLE(BouncyCastleProvider.PROVIDER_NAME);

	private final String provider;

	SecretKeyProvider(final String name) {
		provider = name;
	}

	public String getProvider() {
		return provider;
	}
}
