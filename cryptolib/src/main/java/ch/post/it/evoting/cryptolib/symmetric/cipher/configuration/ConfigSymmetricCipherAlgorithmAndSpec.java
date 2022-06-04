/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.cipher.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum representing a set of parameters that can be used when requesting a symmetric cipher.
 *
 * <p>These parameters are:
 *
 * <ol>
 *   <li>An algorithm/mode/padding.
 *   <li>An initialization vector length in bits.
 *   <li>An authentication tag length in bits.
 *   <li>A provider.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigSymmetricCipherAlgorithmAndSpec {
	AES_WITH_GCM_AND_NOPADDING_96_128_BC("AES", EncryptionOperatingMode.GCM, "NoPadding", 96, 128, Provider.BOUNCY_CASTLE),

	AES_WITH_GCM_AND_NOPADDING_96_128_DEFAULT("AES", EncryptionOperatingMode.GCM, "NoPadding", 96, 128, Provider.DEFAULT);

	private final String algorithm;
	private final EncryptionOperatingMode mode;
	private final String padding;
	private final int initVectorBitLength;
	private final int authTagBitLength;
	private final Provider provider;

	ConfigSymmetricCipherAlgorithmAndSpec(final String algorithm, final EncryptionOperatingMode mode, final String padding,
			final int initVectorBitLength, final int authTagBitLength, final Provider provider) {

		this.algorithm = algorithm;
		this.mode = mode;
		this.padding = padding;
		this.initVectorBitLength = initVectorBitLength;
		this.authTagBitLength = authTagBitLength;
		this.provider = provider;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public EncryptionOperatingMode getMode() {
		return mode;
	}

	public String getPadding() {
		return padding;
	}

	public String getTransformation() {
		return String.format("%s/%s/%s", algorithm, mode, padding);
	}

	public int getInitVectorBitLength() {

		return initVectorBitLength;
	}

	public int getAuthTagBitLength() {

		return authTagBitLength;
	}

	public Provider getProvider() {

		return provider;
	}
}
