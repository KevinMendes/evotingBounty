/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.secretsharing;

import java.math.BigInteger;
import java.util.Set;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Generic interface for secret sharing algorithms, i.e. Shamir's and Blakley's
 */
public interface ThresholdSecretSharingServiceAPI {

	/**
	 * Splits a given secret into a set of shares. The number of shares created and the threshold (the number of shares needed to recover the secret)
	 * are provided as parameters.
	 * <i>Postconditions:</i> - <code>result.size() = number</code> - <code>recover(result) = s</code>
	 * . (where <code>result</code> refers to this function's return value).
	 *
	 * <p>The secret parameter will be zero filled after splitting making sure it will be disposed
	 *
	 * @param secret    the secret to be split. Will be disposed after splitting.
	 * @param number    the number of shares that will be generated, i.e., the length of the returned list.
	 * @param threshold the number of shares needed to recover secret. If <code>threshold &gt; number
	 *                  </code>, the threshold's value is reset to <code>number</code>.
	 * @param modulus   the modulus (size of the finite field) of the Shamir secret sharing scheme.
	 * @return the list of shares that the secret has been split into.
	 * @throws GeneralCryptoLibException if there is any problem splitting the {@link Share}s
	 */
	Set<Share> split(final byte[] secret, final int number, final int threshold, BigInteger modulus) throws GeneralCryptoLibException;

	/**
	 * Splits a list of secrets into a set of shares. The number of shares created and the threshold (the number of shares needed to recover the
	 * secret) are provided as parameters.
	 * <i>Postconditions:</i> - <code>result.size() = no</code> - <code>recover(result) = s</code> .
	 * (where <code>result</code> refers to this function's return value).
	 *
	 * <p>The secrets parameter will be zero filled after splitting making sure it will be disposed
	 *
	 * @param secrets   the secrets to be split. Will be disposed after splitting.
	 * @param number    the number of shares that will be generated, i.e., the length of the returned list.
	 * @param threshold the number of shares needed to recover secret. If <code>threshold &gt; no
	 *                  </code>, the threshold's value is reset to <code>no</code>.
	 * @param modulus   the modulus (size of the finite field) of the Shamir secret sharing scheme.
	 * @return the list of shares that the secrets have been split into.
	 * @throws GeneralCryptoLibException if there is any problem splitting the {@link Share}s
	 */
	Set<Share> split(final byte[][] secrets, final int number, final int threshold, BigInteger modulus) throws GeneralCryptoLibException;

	/**
	 * Recovers a secret from a set of shares. If it cannot be recovered, an empty secret is returned.
	 *
	 * @param shares set, possibly partial, of shares for the secret.
	 * @return the recovered secret.
	 * @throws GeneralCryptoLibException if an error occurs during the recovery.
	 */
	byte[] recover(final Set<Share> shares) throws GeneralCryptoLibException;

	/**
	 * Recovers several secrets from a set of shares. If it cannot be recovered, an empty secret is returned.
	 *
	 * @param shares          set, possibly partial, of shares for the secret.
	 * @param expectedSecrets number of secrets expected inside this set of shares.
	 * @return the recovered secret.
	 * @throws GeneralCryptoLibException if an error occurs during the recovery.
	 */
	byte[][] recover(final Set<Share> shares, int expectedSecrets) throws GeneralCryptoLibException;

	/**
	 * Convert a given share to a byte array representation of this {@link Share} in a byte[].
	 *
	 * @param share The {@link Share} you want to serialize
	 * @return the byte[] representation of the {@link Share}
	 */
	byte[] serialize(Share share);

	/**
	 * Build a {@link Share} from its serialized form.
	 *
	 * @param shareBytes The bytes the {@link Share} is read from.
	 * @return an instance of an Object {@link Share} created from its byte array representation.
	 * @throws GeneralCryptoLibException If there are too many or too little bytes.
	 */
	Share deserialize(byte[] shareBytes) throws GeneralCryptoLibException;
}
