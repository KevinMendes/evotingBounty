/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.factory;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.MacPolicy;

/**
 * A factory class for creating a MAC generator and verifier.
 */
public class MacFactory extends CryptolibFactory {

	private final MacPolicy macPolicy;

	/**
	 * Constructs a MacFactory using the provided {@link MacPolicy}.
	 *
	 * @param macPolicy The MacPolicy to be used to configure this MacFactory.
	 *                  <p>NOTE: The received {@link MacPolicy} should be an immutable object. If this is the case,
	 *                  then the entire {@code MacFactory} class is thread safe.
	 */
	public MacFactory(final MacPolicy macPolicy) {

		this.macPolicy = macPolicy;
	}

	/**
	 * Creates a {@link CryptoMac} according to the given MAC policy.
	 *
	 * @return A {@link CryptoMac} object.
	 */
	public CryptoMac create() {

		return new CryptoMac(macPolicy);
	}
}
