/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.secretsharing;

import java.math.BigInteger;

/**
 * A part of a secret.
 */
public interface Share {

	/**
	 * Get the number of parts of the set of shares this share belongs.
	 *
	 * @return the number of parts.
	 */
	int getNumberOfParts();

	/**
	 * Get the threshold of the set of shares this share belongs.
	 *
	 * @return the threshold.
	 */
	int getThreshold();

	/**
	 * Get the number of secrets of the set of shares this share belongs
	 *
	 * @return the number of secrets.
	 */
	int getNumberOfSecrets();

	/**
	 * Get the modulus of this set of shares.
	 *
	 * @return the modulus.
	 */
	BigInteger getModulus();

	/**
	 * Get secret length in bytes.
	 *
	 * @return The length of the secret.
	 */
	int getSecretLength();

	/**
	 * The Share implementations should use a serialization service, as an implementation of {@link ShareSerializer}. But the MultipleSharesContainer
	 * class for example uses its own serialization method, which can not be easily moved out.
	 *
	 * @return The serialized share as a byte array.
	 */
	default byte[] serialize() {
		throw new UnsupportedOperationException("The class must use an external serializer class.");
	}
}
