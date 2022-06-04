/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.utils;

import javax.crypto.SecretKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.symmetric.service.SymmetricService;

/**
 * Utility to generate various types of symmetric data needed by tests.
 */
public class SymmetricTestDataGenerator {

	/**
	 * Generates an secret key for encryption.
	 *
	 * @return the generated secret key.
	 * @throws GeneralCryptoLibException if the key generation process fails.
	 */
	public static SecretKey getSecretKeyForEncryption() throws GeneralCryptoLibException {

		final SymmetricService symmetricService = new SymmetricService();

		return symmetricService.getSecretKeyForEncryption();
	}

	/**
	 * Generates an secret key for HMAC generation.
	 *
	 * @return the generated secret key.
	 * @throws GeneralCryptoLibException if the key generation process fails.
	 */
	public static SecretKey getSecretKeyForHmac() throws GeneralCryptoLibException {

		final SymmetricService symmetricService = new SymmetricService();

		return symmetricService.getSecretKeyForHmac();
	}
}
