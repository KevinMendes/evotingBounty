/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.derivation;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Interface which provides methods to derive a {@link CryptoAPIDerivedKey}, using a password based key derivation function.
 */
public interface CryptoAPIPBKDFDeriver {

	/**
	 * Derives a {@link CryptoAPIDerivedKey} from a given password and a given salt. For security reasons, the password must contain a minimum of 16
	 * characters.
	 *
	 * @param password the password.
	 * @param salt     the salt.
	 * @return the derived {@link CryptoAPIDerivedKey}.
	 * @throws GeneralCryptoLibException if the password or the salt are invalid.
	 */
	CryptoAPIDerivedKey deriveKey(final char[] password, final byte[] salt) throws GeneralCryptoLibException;
}
