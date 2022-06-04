/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration;

/**
 * Interface which specifies the methods that must be implemented when creating signing key pairs.
 */
public interface SigningKeyPairPolicy {

	/**
	 * Returns the {@link ConfigSigningKeyPairAlgorithmAndSpec} (which encapsulates an algorithm, a provider and an {@link
	 * java.security.spec.AlgorithmParameterSpec}) that should be used to construct signing key pairs.
	 *
	 * @return {@code ch.post.it.evoting.cryptolib.asymetric.keypair.configuration.ConfigSigningKeyPairAlgorithmAndSpec}
	 */
	ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec();
}
