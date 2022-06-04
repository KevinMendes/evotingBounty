/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.secretsharing;

import java.math.BigInteger;
import java.util.Set;

/**
 * Generic interface for secret sharing algorithms, i.e. Shamir's and Blakley's
 */
public interface ThresholdSecretSharingSchemeAPI {

	/**
	 * Splits a list of secrets into a set of shares. The number of shares created and the threshold (the number of shares needed to recover the
	 * secret) are provided as parameters.
	 * <i>Postconditions:</i> - <code>result.size() = number</code> - <code>recover(result) = secrets</code> .
	 * (where <code>result</code> refers to this function's return value).
	 *
	 * <p>The secrets parameters will be zero filled after splitting making sure it will be disposed
	 *
	 * @param secrets   the secrets to be split. Will be disposed after splitting
	 * @param number    the number of shares that will be generated, i.e., the length of the returned list.
	 * @param threshold the number of shares needed to recover secret. If <code>threshold &gt; number
	 *                  </code>, the threshold's value is reset to <code>number</code>.
	 * @param modulus   the modulus (size of the finite field) of the Shamir secret sharing scheme.
	 * @return the list of shares that the secrets have been split into.
	 * @throws IllegalArgumentException If all the secrets do not have the same byte length.
	 */
	Set<Share> split(final byte[][] secrets, final int number, final int threshold, BigInteger modulus);

	/**
	 * Recovers all secrets from a set of shares. If it cannot be recovered, an empty secret is returned.
	 *
	 * @param shares          set, possibly partial, of shares for the secret.
	 * @param expectedSecrets number of secrets expected inside this set of shares.
	 * @return the recovered secret.
	 */
	byte[][] recover(final Set<Share> shares, final int expectedSecrets);
}
