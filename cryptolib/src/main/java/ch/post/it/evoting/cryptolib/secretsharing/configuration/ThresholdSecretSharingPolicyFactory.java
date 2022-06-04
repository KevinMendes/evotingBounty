/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.secretsharing.configuration;

import java.util.Properties;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.api.secretsharing.ThresholdSecretSharingScheme;

/**
 * A secret sharing policy object configured through properties.
 */
public final class ThresholdSecretSharingPolicyFactory extends CryptolibFactory {
	private static final String SECRET_SHARING_SCHEME_KEY = "secretsharing.scheme";

	private ThresholdSecretSharingPolicyFactory() {
	}

	/**
	 * @return the default threshold secret sharing policy.
	 */
	public static ThresholdSecretSharingPolicy fromDefaults() {
		return () -> ThresholdSecretSharingScheme.SHAMIR;
	}

	/**
	 * Returns a policy specified as properties.
	 *
	 * @param properties the policy represented as properties
	 * @return the policy from the properties
	 */
	public static ThresholdSecretSharingPolicy fromProperties(final Properties properties) {
		return () -> ThresholdSecretSharingScheme
				.valueOf(properties.getProperty(SECRET_SHARING_SCHEME_KEY, ThresholdSecretSharingScheme.SHAMIR.name()));
	}
}
