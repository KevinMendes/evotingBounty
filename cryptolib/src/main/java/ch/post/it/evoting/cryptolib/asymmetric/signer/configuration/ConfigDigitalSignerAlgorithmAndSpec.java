/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum representing a set of parameters that can be used when requesting a digital signer. These are:
 *
 * <p>
 *
 * <ol>
 *   <li>An algorithm/padding
 *   <li>A padding message digest algorithm
 *   <li>Padding info
 *   <li>A provider
 * </ol>
 * <p>
 * Instances of this enum are immutable.
 */
@SuppressWarnings("squid:S1192") // Ignore 'String literals should not be duplicated' Sonar's rule for this enum definition.
public enum ConfigDigitalSignerAlgorithmAndSpec {
	SHA256_WITH_RSA_SHA256_BC("SHA256withRSA", "SHA-256", PaddingInfo.NO_PADDING_INFO, Provider.BOUNCY_CASTLE),

	SHA256_WITH_RSA_SHA256_DEFAULT("SHA256withRSA", "SHA-256", PaddingInfo.NO_PADDING_INFO, Provider.DEFAULT),

	SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC("SHA256withRSA/PSS", "SHA-256", PaddingInfo.PSS_PADDING_INFO, Provider.BOUNCY_CASTLE),

	SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_DEFAULT("SHA256withRSA/PSS", "SHA-256", PaddingInfo.PSS_PADDING_INFO, Provider.DEFAULT);

	private final String algorithmAndPadding;

	private final String paddingMessageDigestAlgorithm;

	private final PaddingInfo paddingInfo;

	private final Provider provider;

	ConfigDigitalSignerAlgorithmAndSpec(final String algorithmAndPadding, final String paddingMessageDigest, final PaddingInfo paddingInfo,
			final Provider provider) {

		this.algorithmAndPadding = algorithmAndPadding;
		paddingMessageDigestAlgorithm = paddingMessageDigest;
		this.paddingInfo = paddingInfo;
		this.provider = provider;
	}

	/**
	 * @return The algorithm and padding scheme of this {@link ConfigDigitalSignerAlgorithmAndSpec}.
	 */
	public String getAlgorithmAndPadding() {

		return algorithmAndPadding;
	}

	/**
	 * @return The provider of this {@link ConfigDigitalSignerAlgorithmAndSpec}.
	 */
	public String getPaddingMessageDigestAlgorithm() {

		return paddingMessageDigestAlgorithm;
	}

	/**
	 * @return Additional padding information, this depends on the particular algorithm and spec. {@link ConfigDigitalSignerAlgorithmAndSpec}.
	 */
	public PaddingInfo getPaddingInfo() {

		return paddingInfo;
	}

	/**
	 * @return The provider of this {@link ConfigDigitalSignerAlgorithmAndSpec}.
	 */
	public Provider getProvider() {

		return provider;
	}
}
