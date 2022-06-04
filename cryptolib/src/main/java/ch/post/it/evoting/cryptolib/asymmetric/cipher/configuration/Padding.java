/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration;

/**
 * Enum which defines the paddings that may be used when asymmetrically operating with data.
 */
public enum Padding {
	PKCS1("PKCS1Padding"),
	PKCS5("PKCS5Padding"),
	PSS("PSS"),
	NONE("NoPadding");

	private final String paddingName;

	Padding(final String paddingName) {
		this.paddingName = paddingName;
	}

	public String getPaddingName() {
		return paddingName;
	}
}
