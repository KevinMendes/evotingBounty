/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.factory;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration.AsymmetricCipherPolicy;

public class AsymmetricCipherFactory extends CryptolibFactory {

	private final AsymmetricCipherPolicy asymmetricCipherPolicy;

	/**
	 * Constructs a {@code AsymmetricCipherFactory} using the provided {@link AsymmetricCipherPolicy}.
	 *
	 * @param asymmetricCipherPolicy the {@code AsymmetricCipherPolicy} to be used to configure this AsymmetricCipherFactory.
	 *                               <p>NOTE: The received {@link AsymmetricCipherPolicy} should be an immutable object. If this
	 *                               is the case, then the entire {@code AsymmetricCipherFactory} class is thread safe.
	 */
	public AsymmetricCipherFactory(final AsymmetricCipherPolicy asymmetricCipherPolicy) {

		this.asymmetricCipherPolicy = asymmetricCipherPolicy;
	}

	/**
	 * Creates a {@link CryptoAsymmetricCipher} according to the given asymmetric cipher policy.
	 *
	 * @return a {@link CryptoAsymmetricCipher} object.
	 */
	public CryptoAsymmetricCipher create() {

		try {
			return new CryptoAsymmetricCipher(asymmetricCipherPolicy);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}
	}
}
