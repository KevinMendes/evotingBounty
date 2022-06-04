/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.securerandom;

import java.math.BigInteger;

/**
 * Interface that provides the functionality to obtain random integers, for which the class {@link BigInteger} is used.
 */
public interface CryptoAPIRandomInteger {

	/**
	 * Creates a random positive {@link BigInteger} according to the specified bit length.
	 *
	 * @param lengthInBits the bit length of the random integer.
	 * @return a random {@link BigInteger} within the range [0, 2^{@code lengthInBits}-1].
	 */
	BigInteger genRandomIntegerByBits(int lengthInBits);

	/**
	 * Creates a random positive {@link BigInteger} according to the specified {@code lengthInDigits}.
	 *
	 * @param lengthInDigits The lengthInDigits that specifies the maximum number of digits of the {@link BigInteger} to be generated.
	 * @return a random {@link BigInteger} up to {@code lengthInDigits} digits.
	 * @see java.security.SecureRandom#nextBytes(byte[] bytes)
	 */
	BigInteger genRandomIntegerByDigits(int lengthInDigits);

	/**
	 * Generate a random positive {@link BigInteger} smaller than {@code upperBound}.
	 *
	 * @param upperBound m, The exclusive positive non-zero upper bound of the generated random number.
	 * @return a random {@link BigInteger} smaller than {@code upperBound}.
	 * @see java.security.SecureRandom#nextBytes(byte[] bytes)
	 */
	BigInteger genRandomIntegerUpperBounded(BigInteger upperBound);
}
