/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.factory;

import static ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants.MAXIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH;
import static ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants.MINIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.security.SecureRandom;

import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomInteger;

/**
 * Class that implements {@link CryptoAPIRandomInteger}.
 *
 * <p>Note that the {@link SecureRandom} that is used to generate the random integers is stored in
 * this class and cannot be modified.
 *
 * <p>Instances of this class are immutable.
 */
public final class CryptoRandomInteger implements CryptoAPIRandomInteger {

	private final SecureRandom secureRandom;

	/**
	 * Instantiates a random generator as a wrapper of the specifier parameter.
	 *
	 * @param secureRandom The instance of SecureRandom that should be wrapped within this class.
	 */
	CryptoRandomInteger(final SecureRandom secureRandom) {
		this.secureRandom = secureRandom;
	}

	@Override
	public BigInteger genRandomIntegerByBits(final int lengthInBits) {
		checkArgument(lengthInBits > 0, "Length in bits must be positive. Provided length: %s.", lengthInBits);

		return new BigInteger(lengthInBits, secureRandom);
	}

	@Override
	public BigInteger genRandomIntegerByDigits(final int lengthInDigits) {
		checkArgument(lengthInDigits >= MINIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH,
				"Length in digits must be greater than or equal to the minimum allowed number of digits (%s). Provided length: %s.",
				MINIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH, lengthInDigits);
		checkArgument(lengthInDigits <= MAXIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH,
				"Length in digits must be smaller than or equal to the maximum allowed number of digits (%s). Provided length: %s.",
				MAXIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH, lengthInDigits);

		final BigInteger n = BigInteger.TEN.pow(lengthInDigits).subtract(BigInteger.ONE);

		final int bitLength = n.bitLength();

		BigInteger r;
		do {
			r = genRandomIntegerByBits(bitLength);
		} while (r.compareTo(n) > 0);

		return r;
	}

	@Override
	public BigInteger genRandomIntegerUpperBounded(final BigInteger upperBound) {
		checkNotNull(upperBound);
		checkArgument(upperBound.compareTo(BigInteger.ZERO) > 0);

		final int length = upperBound.bitLength();

		BigInteger random;
		do {
			random = genRandomIntegerByBits(length);
		} while (random.compareTo(upperBound) >= 0);

		return random;
	}

}
