/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;

/**
 * Implementation of the {@link MacPolicy} interface, which reads values from a properties input. For a key that is not set in the properties input, a
 * default value will be used. The default values are:
 * <ul>
 *     <li>symmetric.mac=HMAC_WITH_SHA256_SUN</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public class MacPolicyFromProperties implements MacPolicy {

	static final ConfigMacAlgorithmAndProvider DEFAULT_MAC_ALGORITHM_PROVIDER = ConfigMacAlgorithmAndProvider.HMAC_WITH_SHA256_SUN;
	static final String SYMMETRIC_MAC_KEY = "symmetric.mac";

	private final ConfigMacAlgorithmAndProvider macAlgorithmAndProvider;

	/**
	 * Constructs an object that will provide the policy from the provided properties.
	 *
	 * @throws CryptoLibException if helper is invalid or properties are invalid.
	 */
	public MacPolicyFromProperties() {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();
		try {
			macAlgorithmAndProvider = ConfigMacAlgorithmAndProvider
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(SYMMETRIC_MAC_KEY, DEFAULT_MAC_ALGORITHM_PROVIDER.name()));

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigMacAlgorithmAndProvider getMacAlgorithmAndProvider() {

		return macAlgorithmAndProvider;
	}
}
