/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.configuration;

/**
 * Implementation of the {@link ExtendedKeyStorePolicy} interface, which reads values from a properties file. Extends the {@link
 * ExtendedKeyStorePolicyFromProperties} and uses p12 as key for retrieving the properties.
 *
 * <p>Instances of this class are immutable.
 */
public final class ExtendedKeyStoreP12PolicyFromProperties extends ExtendedKeyStorePolicyFromProperties implements ExtendedKeyStorePolicy {

	/**
	 * Creates an instance of the class with p12 from the specified properties class.
	 */
	public ExtendedKeyStoreP12PolicyFromProperties() {
		super(Key.P12.name());
	}
}
