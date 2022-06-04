/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.configuration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Enum which defines the eligible providers.
 */
public enum Provider {
	BOUNCY_CASTLE(BouncyCastleProvider.PROVIDER_NAME),
	SUN("SUN"),
	SUN_JCE("SunJCE"),
	SUN_JSSE("SunJSSE"),
	SUN_RSA_SIGN("SunRsaSign"),
	SUN_MSCAPI("SunMSCAPI"),
	XML_DSIG("XMLDSig"),
	DEFAULT;

	private String providerName;

	Provider() {
	}

	Provider(final String providerName) {
		this.providerName = providerName;
	}

	public String getProviderName() {
		return providerName;
	}
}
