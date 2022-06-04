/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.factory;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicy;

/**
 * A factory class for creating a digital signer and a digital signature verifier.
 */
public class DigitalSignerFactory extends CryptolibFactory {

	private final DigitalSignerPolicy digitalSignerPolicy;

	/**
	 * Constructs a {@code DigitalSignerFactory} using the provided {@link DigitalSignerPolicy}.
	 *
	 * @param digitalSignerPolicy the digital signer policy to be used to configure this {@code DigitalSignerFactory}.
	 *                            <p>NOTE: The received {@link DigitalSignerPolicy} should be an immutable object. If this is
	 *                            the case, then the entire {@code DigitalSignerFactory} class is thread safe.
	 */
	public DigitalSignerFactory(final DigitalSignerPolicy digitalSignerPolicy) {

		this.digitalSignerPolicy = digitalSignerPolicy;
	}

	/**
	 * Creates a {@link CryptoDigitalSigner} according to the given digital signer policy.
	 *
	 * @return a {@link CryptoDigitalSigner} object.
	 */
	public CryptoDigitalSigner create() {

		return new CryptoDigitalSigner(digitalSignerPolicy);
	}
}
