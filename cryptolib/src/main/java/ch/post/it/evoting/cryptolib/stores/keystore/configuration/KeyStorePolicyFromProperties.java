/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;

/**
 * Implementation of the {@link KeyStorePolicy} interface, which reads the key store cryptographic policy from a properties file. For a key that is
 * not set in the properties input, a default value will be used. The default values are:
 * <ul>
 *     <li>stores.keystore=SUN</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public class KeyStorePolicyFromProperties implements KeyStorePolicy {

	static final ConfigKeyStoreSpec DEFAULT_KEYSTORE = ConfigKeyStoreSpec.SUN;
	static final String STORES_KEYSTORE_KEY = "stores.keystore";

	private final ConfigKeyStoreSpec keyStoreSpec;

	/**
	 * Default constructor.
	 *
	 * @throws CryptoLibException if the properties cannot be retrieved.
	 */
	public KeyStorePolicyFromProperties() {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();
		try {
			keyStoreSpec = ConfigKeyStoreSpec.valueOf(helper.getNotBlankOrDefaultPropertyValue(STORES_KEYSTORE_KEY, DEFAULT_KEYSTORE.name()));
		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigKeyStoreSpec getKeyStoreSpec() {

		return keyStoreSpec;
	}
}
