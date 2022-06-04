/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.configuration;

/**
 * Enum representing padding information to be used by a signing algorithm.
 *
 * <p>
 *
 * <ol>
 *   <li>A padding masking generation function algorithm
 *   <li>A padding masking generation function message digest algorithm
 *   <li>A padding salt bit length
 *   <li>A padding trailer field
 * </ol>
 * <p>
 * Instances of this enum are immutable.
 */
public enum PaddingInfo {
	NO_PADDING_INFO(),

	PSS_PADDING_INFO("MGF1", "SHA-256", 32, 1);

	private final String paddingMaskingGenerationFunctionAlgorithm;

	private final String paddingMaskingGenerationFunctionMessageDigestAlgorithm;

	private final int paddingSaltBitLength;

	private final int paddingTrailerField;

	PaddingInfo() {

		paddingMaskingGenerationFunctionAlgorithm = null;
		paddingMaskingGenerationFunctionMessageDigestAlgorithm = null;
		paddingSaltBitLength = 0;
		paddingTrailerField = 0;
	}

	PaddingInfo(final String paddingMaskingGenerationFunctionAlgorithm, final String paddingMaskingGenerationFunctionMessageDigest,
			final int paddingSaltBitLength, final int paddingTrailerField) {

		this.paddingMaskingGenerationFunctionAlgorithm = paddingMaskingGenerationFunctionAlgorithm;
		paddingMaskingGenerationFunctionMessageDigestAlgorithm = paddingMaskingGenerationFunctionMessageDigest;
		this.paddingSaltBitLength = paddingSaltBitLength;
		this.paddingTrailerField = paddingTrailerField;
	}

	/**
	 * @return The padding masking generation function algorithm of this {@link PaddingInfo}.
	 */
	public String getPaddingMaskingGenerationFunctionAlgorithm() {

		return paddingMaskingGenerationFunctionAlgorithm;
	}

	/**
	 * @return The padding masking generation function message digest algorithm of this {@link PaddingInfo}.
	 */
	public String getPaddingMaskingGenerationFunctionMessageDigestAlgorithm() {

		return paddingMaskingGenerationFunctionMessageDigestAlgorithm;
	}

	/**
	 * @return The padding salt bit length of this {@link PaddingInfo}.
	 */
	public int getPaddingSaltBitLength() {

		return paddingSaltBitLength;
	}

	/**
	 * @return The padding trailer field of this {@link PaddingInfo}.
	 */
	public int getPaddingTrailerField() {

		return paddingTrailerField;
	}
}
