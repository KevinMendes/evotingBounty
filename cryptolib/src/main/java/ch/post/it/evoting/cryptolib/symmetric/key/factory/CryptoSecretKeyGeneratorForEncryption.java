/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.factory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A generator of secret keys to be used for encryption.
 *
 * <p>Instances of this class are immutable.
 */
public final class CryptoSecretKeyGeneratorForEncryption implements CryptoSecretKeyGenerator {

	private final KeyGenerator secretKeyGenerator;

	CryptoSecretKeyGeneratorForEncryption(final KeyGenerator secretKeyGenerator) {
		this.secretKeyGenerator = secretKeyGenerator;
	}

	@Override
	public SecretKey genSecretKey() {
		return secretKeyGenerator.generateKey();
	}
}
