/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration;

/**
 * Interface which specifies the methods that must be implemented when creating encryption key pairs.
 */
public interface EncryptionKeyPairPolicy {

	/**
	 * Returns configuration for creating encryption key pairs.
	 *
	 * @return configuration.
	 */
	ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec();
}
