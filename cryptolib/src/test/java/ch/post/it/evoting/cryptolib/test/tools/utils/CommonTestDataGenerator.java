/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.test.tools.utils;

import static java.util.Arrays.fill;

import java.security.SecureRandom;

/**
 * Utility to generate various types of common data needed by tests.
 */
public class CommonTestDataGenerator {
	private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	/**
	 * Non-public constructor
	 */
	private CommonTestDataGenerator() {
	}

	/**
	 * Randomly generates an integer within a specified range.
	 *
	 * @param min the minimum value of the integer.
	 * @param max the maximum value of the integer.
	 * @return the randomly generated integer.
	 */
	public static int getInt(final int min, final int max) {
		int value;
		do {
			value = SECURE_RANDOM.nextInt(max + 1);
		} while (value < min);
		return value;
	}

	/**
	 * Randomly generates a string of a specified length in characters, from a specified set of allowed characters.
	 *
	 * @param length       the length in characters of the string.
	 * @param allowedChars the set of allowed characters.
	 * @return the randomly generated string.
	 */
	public static String getString(final int length, final String allowedChars) {
		return new String(getCharArray(length, allowedChars));
	}

	/**
	 * Randomly generates an alphanumeric string of a specified length.
	 *
	 * @param length the length in characters of the string.
	 * @return the randomly generated string.
	 */
	public static String getAlphanumeric(final int length) {
		return getString(length, ALPHANUMERIC);
	}

	/**
	 * Randomly generates a character array of a specified length, from a specified set of allowed characters.
	 *
	 * @param length       the length of the character array.
	 * @param allowedChars the set of allowed characters.
	 * @return the randomly generated string.
	 */
	public static char[] getCharArray(final int length, final String allowedChars) {
		final char[] chars = new char[length];
		for (int i = 0; i < chars.length; i++) {
			final int index = SECURE_RANDOM.nextInt(allowedChars.length());
			chars[i] = allowedChars.charAt(index);
		}
		return chars;
	}

	/**
	 * Generates a string that consists only of white spaces.
	 *
	 * @param length the length of the string.
	 * @return the white space string.
	 */
	public static String getWhiteSpaceString(final int length) {
		final char[] whiteSpace = new char[length];
		fill(whiteSpace, ' ');
		return new String(whiteSpace);
	}
}
