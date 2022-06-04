/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.secretsharing.configuration;

import ch.post.it.evoting.cryptolib.api.secretsharing.ThresholdSecretSharingScheme;

/**
 * Configuration policy for secret sharing
 */
public interface ThresholdSecretSharingPolicy {

	/**
	 * Returns the configuration for secret sharing
	 *
	 * @return configuration
	 */
	ThresholdSecretSharingScheme getThresholdSecretSharingScheme();
}
