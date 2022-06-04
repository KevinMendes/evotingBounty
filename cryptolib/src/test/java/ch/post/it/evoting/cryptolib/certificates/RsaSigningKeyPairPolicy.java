/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates;

import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigEncryptionKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigSigningKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.EncryptionKeyPairPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.SigningKeyPairPolicy;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;

/**
 * An implementation of the KeyPairPolicy interface which specifies RSA as the key pair algorithm and cryptographic service provider.
 */
public class RsaSigningKeyPairPolicy implements KeyPairPolicy {

	/**
	 * @see SecureRandomPolicy#getSecureRandomAlgorithmAndProvider()
	 */
	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
		return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	}

	/**
	 * @see SigningKeyPairPolicy#getSigningKeyPairAlgorithmAndSpec()
	 */
	@Override
	public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
		return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
	}

	/**
	 * @see EncryptionKeyPairPolicy#getEncryptingKeyPairAlgorithmAndSpec()
	 */
	@Override
	public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
		return ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
	}
}
