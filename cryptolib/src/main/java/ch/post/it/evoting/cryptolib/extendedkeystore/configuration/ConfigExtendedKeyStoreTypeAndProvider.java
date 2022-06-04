/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.configuration;

import static ch.post.it.evoting.cryptolib.commons.configuration.Provider.BOUNCY_CASTLE;
import static ch.post.it.evoting.cryptolib.commons.configuration.Provider.DEFAULT;
import static ch.post.it.evoting.cryptolib.commons.configuration.Provider.SUN_JSSE;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum representing a set of parameters that can be used when requesting an instance of Store.
 *
 * <p>These parameters are:
 *
 * <ol>
 *   <li>A type.
 *   <li>A {@link Provider}.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
@SuppressWarnings("squid:S1192") // Ignore 'String literals should not be duplicated' Sonar's rule for this enum definition.
public enum ConfigExtendedKeyStoreTypeAndProvider {
	PKCS12_BC("PKCS12", BOUNCY_CASTLE),

	PKCS12_DEFAULT("PKCS12", DEFAULT),

	PKCS12_SUN_JSSE("PKCS12", SUN_JSSE);

	private final String type;

	private final Provider provider;

	ConfigExtendedKeyStoreTypeAndProvider(final String type, final Provider provider) {
		this.type = type;
		this.provider = provider;
	}

	public String getType() {
		return type;
	}

	public Provider getProvider() {
		return provider;
	}
}
