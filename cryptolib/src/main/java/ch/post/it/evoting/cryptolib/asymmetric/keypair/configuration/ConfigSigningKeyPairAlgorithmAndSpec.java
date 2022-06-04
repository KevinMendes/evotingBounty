/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import ch.post.it.evoting.cryptolib.asymmetric.keypair.constants.KeyPairConstants;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum which defines the signing key pair algorithms and their specification.
 *
 * <p>Each element of the enum contains the following attributes:
 *
 * <ol>
 *   <li>An algorithm.
 *   <li>A {@link RSAKeyGenParameterSpec}, which is composed of: (a) a key size; and (b) an
 *       exponent, which is set to 65537 by default.
 *   <li>A {@link Provider}.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigSigningKeyPairAlgorithmAndSpec {
	RSA_2048_F4_SUN_RSA_SIGN(KeyPairConstants.RSA_ALG, new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4), Provider.SUN_RSA_SIGN),

	RSA_3072_F4_SUN_RSA_SIGN(KeyPairConstants.RSA_ALG, new RSAKeyGenParameterSpec(3072, RSAKeyGenParameterSpec.F4), Provider.SUN_RSA_SIGN),

	RSA_4096_F4_SUN_RSA_SIGN(KeyPairConstants.RSA_ALG, new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4), Provider.SUN_RSA_SIGN),

	RSA_2048_F4_BC(KeyPairConstants.RSA_ALG, new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4), Provider.BOUNCY_CASTLE),

	RSA_3072_F4_BC(KeyPairConstants.RSA_ALG, new RSAKeyGenParameterSpec(3072, RSAKeyGenParameterSpec.F4), Provider.BOUNCY_CASTLE),

	RSA_4096_F4_BC(KeyPairConstants.RSA_ALG, new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4), Provider.BOUNCY_CASTLE);

	private final String algorithm;

	private final Provider provider;

	private final AlgorithmParameterSpec algorithmParameterSpec;

	ConfigSigningKeyPairAlgorithmAndSpec(final String name, final AlgorithmParameterSpec algorithmParameterSpec, final Provider provider) {

		algorithm = name;
		this.algorithmParameterSpec = algorithmParameterSpec;
		this.provider = provider;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public AlgorithmParameterSpec getSpec() {
		return algorithmParameterSpec;
	}

	public Provider getProvider() {
		return provider;
	}
}
