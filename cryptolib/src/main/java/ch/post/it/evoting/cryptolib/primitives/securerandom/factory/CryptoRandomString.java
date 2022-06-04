/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.factory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;

/**
 * Class that implements {@link CryptoAPIRandomString}.
 *
 * <p>Note that the {@link SecureRandom} that is used to generate the random strings is stored in
 * this class and cannot be modified.
 *
 * <p>This class generates random strings from a given alphabet. The alphabet should not contain
 * repeated elements. At the moment only alphabets with length power of two are considered. The following two alphabets are provided in {@link
 * SecureRandomConstants}:
 *
 * <ul>
 *   <li>32 characters ( {@link
 *       SecureRandomConstants#ALPHABET_BASE32}
 *       ): ABCDEFGHIJKLMNOPQRSTUVWXYZ234567
 *   <li>64 characters ( {@link
 *       SecureRandomConstants#ALPHABET_BASE64}
 *       ): ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public final class CryptoRandomString implements CryptoAPIRandomString {

	private final SecureRandom secureRandom;

	private final String alphabet;

	private final int alphabetPowerOfTwo;

	/**
	 * Instantiates a random generator with specified parameters.
	 *
	 * @param secureRandom The instance of SecureRandom that should be wrapped within this class
	 * @param alphabet     The alphabet to be used to generate the random strings.
	 *                     <p>It should be noted that if the given alphabet is {@code null} or of a specific length
	 *                     which is not considered (see class description), a {@link CryptoLibException} is thrown.
	 */
	CryptoRandomString(final SecureRandom secureRandom, final String alphabet) {

		this.secureRandom = secureRandom;
		this.alphabet = alphabet;

		validateAlphabet();
		alphabetPowerOfTwo = getPowerOfTwo();
	}

	private void validateAlphabet() {

		if (alphabet == null) {
			throw new CryptoLibException(new IllegalArgumentException("The given alphabet is null"));
		} else if (alphabet.length() == 0) {
			throw new CryptoLibException(new IllegalArgumentException("The given alphabet cannot be an empty string"));
		} else if (alphabetHasRepeatedCharacters()) {
			throw new CryptoLibException(new IllegalArgumentException("The given alphabet has repeated characters"));
		} else if (!alphabetLengthIsPowerOfTwo()) {
			throw new CryptoLibException(new IllegalArgumentException("The alphabet length should be a power of two."));
		}
	}

	/**
	 * @return The power of two of the alphabet length.
	 */
	private int getPowerOfTwo() {
		return (int) (Math.log10(alphabet.length()) / Math.log10(2));
	}

	/**
	 * @return True if the alphabet length is a power of two. False otherwise.
	 */
	private boolean alphabetLengthIsPowerOfTwo() {
		return (alphabet.length() & (alphabet.length() - 1)) == 0;
	}

	private boolean alphabetHasRepeatedCharacters() {

		final Set<Character> setUniqueChars = new HashSet<>();

		for (final char c : alphabet.toCharArray()) {

			if (!setUniqueChars.add(c)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String nextRandom(final int lengthInChars) throws GeneralCryptoLibException {

		Validate.inRange(lengthInChars, 1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH, "Length in characters", "",
				"maximum allowed value for secure random Strings");

		final int numberOfBytes = (int) Math.ceil((alphabetPowerOfTwo * lengthInChars) / (float) Byte.SIZE);

		final byte[] bytes = new byte[numberOfBytes];
		secureRandom.nextBytes(bytes);
		String bitArray = new BigInteger(1, bytes).toString(2);

		final int bytesLength = numberOfBytes * Byte.SIZE;

		final StringBuilder stringBuilder = new StringBuilder(bitArray).reverse();

		while (stringBuilder.length() < bytesLength) {
			stringBuilder.append('0');
		}

		bitArray = stringBuilder.reverse().toString();

		String c;

		final StringBuilder stringBuilderGeneratedString = new StringBuilder();

		for (int i = 0; i < lengthInChars; i++) {

			c = bitArray.substring(i * alphabetPowerOfTwo, (i + 1) * alphabetPowerOfTwo);

			stringBuilderGeneratedString.append(alphabet.toCharArray()[Integer.parseInt(c, 2)]);
		}

		return stringBuilderGeneratedString.toString();
	}
}
