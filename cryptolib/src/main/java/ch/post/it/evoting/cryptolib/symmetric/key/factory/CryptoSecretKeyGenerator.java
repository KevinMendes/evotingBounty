/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.factory;

import javax.crypto.SecretKey;

/**
 * Defines the methods supported by any secret key generators.
 */
public interface CryptoSecretKeyGenerator {

	/**
	 * Creates a {@link SecretKey} object.
	 *
	 * <p>The attributes of the returned {@link SecretKey} depend on the attributes of the generator
	 * that is used to create the key.
	 *
	 * @return A newly created secret key.
	 */
	SecretKey genSecretKey();
}
