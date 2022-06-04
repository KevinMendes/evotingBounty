/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.factory;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;

/**
 * Class to encapsulate a derived key.
 */
class CryptoDerivedKey implements CryptoAPIDerivedKey {

	private final byte[] key;

	/**
	 * Creates an instance of derived key initialized by provided {@code key}.
	 *
	 * @param key The derived key to encapsulate, as a byte[].
	 */
	public CryptoDerivedKey(final byte[] key) {
		this.key = key.clone();
	}

	@Override
	public byte[] getEncoded() {
		return key.clone();
	}
}
