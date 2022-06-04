/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Representation of a list of ElGamal key pairs. Each key pair is composed of a private key and a public key.
 */
public final class ElGamalKeyPair {

	private final ElGamalPrivateKey privateKey;

	private final ElGamalPublicKey publicKey;

	/**
	 * Creates a list of ElGamalKeyPair from the specified list of private and public keys.
	 *
	 * <ul>
	 *   <li>The lists of private and public keys contain the same number of elements.
	 *   <li>The public and private keys belong to the same mathematical group.
	 * </ul>
	 *
	 * @param privateKey the private key to be set in the key pair.
	 * @param publicKey  the public key to be set in the key pair.
	 * @throws GeneralCryptoLibException if private or public key is invalid.
	 */
	public ElGamalKeyPair(final ElGamalPrivateKey privateKey, final ElGamalPublicKey publicKey) throws GeneralCryptoLibException {

		Validate.notNull(privateKey, "ElGamal private key");
		Validate.notNull(publicKey, "ElGamal public key");
		Validate.isEqual(privateKey.getKeys().size(), publicKey.getKeys().size(), "ElGamal private key length", "ElGamal public key length");
		if (!privateKey.getGroup().equals(publicKey.getGroup())) {
			throw new GeneralCryptoLibException("ElGamal public and private keys must belong to same mathematical group.");
		}
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	/**
	 * Retrieves the {@link ElGamalPrivateKey}.
	 *
	 * @return the {@link ElGamalPrivateKey}.
	 */
	public ElGamalPrivateKey getPrivateKeys() {

		return privateKey;
	}

	/**
	 * Retrieves the {@link ElGamalPublicKey}.
	 *
	 * @return the {@link ElGamalPublicKey}.
	 */
	public ElGamalPublicKey getPublicKeys() {

		return publicKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((privateKey == null) ? 0 : privateKey.hashCode());
		result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ElGamalKeyPair other = (ElGamalKeyPair) obj;
		if (privateKey == null) {
			if (other.privateKey != null) {
				return false;
			}
		} else if (!privateKey.equals(other.privateKey)) {
			return false;
		}
		if (publicKey == null) {
			return other.publicKey == null;
		} else {
			return publicKey.equals(other.publicKey);
		}
	}

}
