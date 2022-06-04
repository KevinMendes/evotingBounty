/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import java.util.Base64;
import java.util.Map;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Utils class.
 */
public final class CryptoExtendedKeyStoreWithPBKDFHelper {
	/**
	 * This is a helper class. It cannot be instantiated.
	 */
	private CryptoExtendedKeyStoreWithPBKDFHelper() {
	}

	/**
	 * Formats key store to JSON.
	 *
	 * @param salt       the salt.
	 * @param secretKeys the secret key.
	 * @param keyStoreJS the sey store JS.
	 * @return the key store formatted to JSON.
	 * @throws GeneralCryptoLibException
	 */
	public static String toJSON(final byte[] salt, final Map<String, byte[]> secretKeys, final Map<String, byte[]> elGamalPrivateKeys,
			final byte[] keyStoreJS) throws GeneralCryptoLibException {

		Validate.notNullOrEmpty(salt, "Key store salt");
		Validate.notNull(secretKeys, "Secret key map");
		Validate.notNull(elGamalPrivateKeys, "ElGamal private key map");

		final StringBuilder sb = new StringBuilder("{");
		sb.append(String.format("\"salt\":\"%s\"", Base64.getEncoder().encodeToString(salt)));
		if (!secretKeys.isEmpty()) {
			final StringBuilder nsb = new StringBuilder();

			for (final Map.Entry<String, byte[]> entry : secretKeys.entrySet()) {
				nsb.append(String.format("\"%s\":\"%s\",", entry.getKey(), Base64.getEncoder().encodeToString(entry.getValue())));
			}

			sb.append(String.format(",\"secrets\":{%s}", nsb.substring(0, nsb.length() - 1)));
		}
		if (!elGamalPrivateKeys.isEmpty()) {
			final StringBuilder nsb = new StringBuilder();

			for (final Map.Entry<String, byte[]> entry : elGamalPrivateKeys.entrySet()) {
				nsb.append(String.format("\"%s\":\"%s\",", entry.getKey(), Base64.getEncoder().encodeToString(entry.getValue())));
			}

			sb.append(String.format(",\"egPrivKeys\":{%s}", nsb.substring(0, nsb.length() - 1)));
		}
		if (keyStoreJS != null && keyStoreJS.length != 0) {
			sb.append(String.format(",\"store\":\"%s\"", Base64.getEncoder().encodeToString(keyStoreJS)));
		}
		sb.append('}');

		return sb.toString();
	}
}
