/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.factory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;

class CryptoRandomStringTest {

	private CryptoRandomString cryptoStringRandom32;
	private CryptoRandomString cryptoStringRandom64;
	private CryptoRandomString cryptoStringRandom4;

	@BeforeEach
	void setup() {
		cryptoStringRandom32 = new SecureRandomFactory().createStringRandom(SecureRandomConstants.ALPHABET_BASE32);
		cryptoStringRandom64 = new SecureRandomFactory().createStringRandom(SecureRandomConstants.ALPHABET_BASE64);
		cryptoStringRandom4 = new SecureRandomFactory().createStringRandom("ABCD");
	}

	@Test
	void testThatGeneratesUpToTheSpecifiedLengthOfCharacters() throws GeneralCryptoLibException {
		String randomString32;
		String randomString64;

		for (int i = 1; i < 100; i++) {

			randomString32 = cryptoStringRandom32.nextRandom(i);

			randomString64 = cryptoStringRandom64.nextRandom(i);

			assertTrue(i >= randomString32.length());
			assertTrue(i >= randomString64.length());
		}
	}

	@Test
	void testThatGeneratesWithOtherAlphabetsPowerOfTwo() {
		assertDoesNotThrow(() -> cryptoStringRandom4.nextRandom(1));
	}

	@Test
	void whenGiveAnIncorrectLength() {
		final int aboveMaxStringLength = SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH + 1;

		final String errorMsg = "Length in characters must be less than or equal to maximum allowed value for secure random Strings: "
				+ SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH + "; Found " + aboveMaxStringLength;

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> cryptoStringRandom32.nextRandom(aboveMaxStringLength));
		assertEquals(errorMsg, exception.getMessage());
	}

	@Test
	void whenGiveAnAlphabetWithRepeatedCharacters() {
		final String errorMsg = "The given alphabet has repeated characters";

		final CryptoLibException exception = assertThrows(CryptoLibException.class, () -> new SecureRandomFactory().createStringRandom("AAAA"));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@Test
	void whenGiveAnEmptyAlphabet() {
		final String errorMsg = "The given alphabet cannot be an empty string";

		final CryptoLibException exception = assertThrows(CryptoLibException.class, () -> new SecureRandomFactory().createStringRandom(""));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@Test
	void whenGiveAnAlphabetLengthNotPowerOfTwo() {
		final String errorMsg = "The alphabet length should be a power of two.";

		final CryptoLibException exception = assertThrows(CryptoLibException.class, () -> new SecureRandomFactory().createStringRandom("ABC"));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@Test
	void whenGiveANullAlphabet() {
		final String errorMsg = "The given alphabet is null";

		final CryptoLibException exception = assertThrows(CryptoLibException.class, () -> new SecureRandomFactory().createStringRandom(null));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@Test
	void testThatNotGeneratesBytesWhenLengthIsZero() {
		final int lengthInBytes = 0;
		final String errorMsg = "Length in characters must be greater than or equal to : 1; Found " + lengthInBytes;

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> cryptoStringRandom32.nextRandom(lengthInBytes));
		assertEquals(errorMsg, exception.getMessage());
	}
}
