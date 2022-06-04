/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.derivation;

/**
 * Interface for a class that encapsulates a derived key.
 */
public interface CryptoAPIDerivedKey {

	/**
	 * Returns the byte representation of the key.
	 *
	 * @return The byte[] representation of the key.
	 */
	byte[] getEncoded();
}
