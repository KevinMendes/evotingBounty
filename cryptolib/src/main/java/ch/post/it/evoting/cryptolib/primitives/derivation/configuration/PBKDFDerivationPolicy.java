/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

/**
 * Interface which specifies the methods that should be implemented by a specific {@link PBKDFDerivationPolicy}.
 */
public interface PBKDFDerivationPolicy {

	/**
	 * Returns the password derivation algorithm and its parameters.
	 *
	 * @return The password derivation algorithm and its parameters.
	 */
	ConfigPBKDFDerivationParameters getPBKDFDerivationParameters();

	/**
	 * Returns the minimum length of the password to derive
	 *
	 * @return The minimum length of the password to derive
	 */
	int getPBKDFDerivationMinPasswordLength();

	/**
	 * Returns the maximum length of the password to derive
	 *
	 * @return The maximum length of the password to derive
	 */
	int getPBKDFDerivationMaxPasswordLength();
}
