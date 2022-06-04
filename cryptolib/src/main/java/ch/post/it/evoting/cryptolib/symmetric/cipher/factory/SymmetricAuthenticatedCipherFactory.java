/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.cryptolib.symmetric.cipher.factory;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicy;

/**
 * A factory class for creating a symmetric authenticated cipher.
 */
public class SymmetricAuthenticatedCipherFactory extends CryptolibFactory {

	private final SymmetricCipherPolicy symmetricCipherPolicy;

	/**
	 * Constructs a {@code SymmetricAuthenticatedCipherFactory} using the provided {@link SymmetricCipherPolicy}.
	 *
	 * @param symmetricCipherPolicy the SymmetricCipherPolicy to be used to configure this SymmetricAuthenticatedCipherFactory.
	 *                              <p>NOTE: The received {@link SymmetricCipherPolicy} should be an immutable object. If this
	 *                              is the case, then the entire {@code SymmetricAuthenticatedCipherFactory} class is thread safe.
	 */
	public SymmetricAuthenticatedCipherFactory(final SymmetricCipherPolicy symmetricCipherPolicy) {

		this.symmetricCipherPolicy = symmetricCipherPolicy;
	}

	/**
	 * Creates a {@link SymmetricAuthenticatedCipher} according to the given symmetric cipher policy.
	 *
	 * @return a {@link SymmetricAuthenticatedCipher} object.
	 */
	public SymmetricAuthenticatedCipher create() {

		try {
			return new SymmetricAuthenticatedCipher(symmetricCipherPolicy);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}
	}
}
