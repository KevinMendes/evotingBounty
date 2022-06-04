/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.securerandom;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Interface that provides the functionality to obtain random strings.
 */
public interface CryptoAPIRandomString {

	/**
	 * Creates a random string according to the specified {@code lengthInChars}.
	 *
	 * @param lengthInChars The lengthInChars that specifies the number of characters to be generated.
	 * @return A random string.
	 * @throws GeneralCryptoLibException if {@code lengthInChars} is out of the range for this generator.
	 * @see java.security.SecureRandom#nextBytes(byte[] bytes)
	 */
	String nextRandom(final int lengthInChars) throws GeneralCryptoLibException;
}
