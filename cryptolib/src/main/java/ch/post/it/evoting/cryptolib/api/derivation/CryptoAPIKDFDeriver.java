/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.derivation;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Interface which provides methods to derive a {@link CryptoAPIDerivedKey} with a key derivation function.
 */
public interface CryptoAPIKDFDeriver {

	/**
	 * Generates the given {@code lengthInBytes} number of bytes using the given seed and wraps them into {@link CryptoAPIDerivedKey}.
	 *
	 * @param seed          The seed from which to derive the {@link CryptoAPIDerivedKey}.
	 * @param lengthInBytes The desired byte length for the {@link CryptoAPIDerivedKey}.
	 * @return The derived {@link CryptoAPIDerivedKey}.
	 * @throws GeneralCryptoLibException if parameters are invalid.
	 */
	CryptoAPIDerivedKey deriveKey(byte[] seed, int lengthInBytes) throws GeneralCryptoLibException;
}
