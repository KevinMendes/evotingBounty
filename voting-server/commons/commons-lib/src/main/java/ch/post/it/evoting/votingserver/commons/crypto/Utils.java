/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

/**
 * Contains util methods for the cryptographic part.
 */
public final class Utils {

	// Avoid instatiation.
	private Utils() {
	}

	/**
	 * Returns the ciphertext from the given encrypted options.
	 *
	 * @param encryptedOptions The encrypted options that contains the ciphertext.
	 * @return an array of strings representing the ciphertext.
	 */
	public static String[] getCiphertextElementsFromEncryptedOptions(String encryptedOptions) {
		return encryptedOptions.split(Constants.SEPARATOR_ENCRYPTED_OPTIONS);
	}

	/**
	 * Returns the array of N elements representing the ciphertext from the given encrypted options.
	 *
	 * @param encryptedOptions The encrypted options that contains the ciphertext.
	 * @return an array of strings representing the ciphertext.
	 */
	public static String[] getNCiphertextElementsFromEncryptedOptions(String encryptedOptions, int n) {
		String[] ciphers = getCiphertextElementsFromEncryptedOptions(encryptedOptions);
		if (ciphers.length != n) {
			throw new IllegalArgumentException("Unexpected number of elements in encrypted options, found " + ciphers.length + " should be " + n);
		}
		return ciphers;
	}

	/**
	 * Returns the ciphertext C0 from the given encrypted options.
	 *
	 * @param encryptedOptions The encrypted options that contains the C0.
	 * @return a string representing the C0.
	 */
	public static String getC0FromEncryptedOptions(String encryptedOptions) {
		return encryptedOptions.split(Constants.SEPARATOR_ENCRYPTED_OPTIONS)[Constants.POSITION_C0];
	}

	/**
	 * Returns the ciphertext C1 from the given encrypted options.
	 *
	 * @param encryptedOptions The encrypted options that contains the C1.
	 * @return a string representing the C1.
	 */
	public static String getC1FromEncryptedOptions(String encryptedOptions) {
		return encryptedOptions.split(Constants.SEPARATOR_ENCRYPTED_OPTIONS)[Constants.POSITION_C1];
	}

}
