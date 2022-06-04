/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration;

import java.util.Properties;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;

/**
 * Implementation of the {@link MessageDigestPolicy} interface, which reads values from a properties input. For a key that is not set in the
 * properties input, a default value will be used. The default values are:
 * <ul>
 *     <li>primitives.messagedigest=SHA256_SUN</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public class MessageDigestPolicyFromProperties implements MessageDigestPolicy {

	static final ConfigMessageDigestAlgorithmAndProvider DEFAULT_MESSAGE_DIGEST = ConfigMessageDigestAlgorithmAndProvider.SHA256_SUN;
	static final String MESSAGE_DIGEST_KEY = "primitives.messagedigest";

	private final ConfigMessageDigestAlgorithmAndProvider messageDigestAlgorithmAndProvider;

	public MessageDigestPolicyFromProperties() {
		this(PolicyFromPropertiesHelper.loadCryptolibProperties());
	}

	/**
	 * Instantiates a provider of properties from the specified path.
	 *
	 * @param properties The properties that define the policy.
	 * @throws CryptoLibException if the helper or property key is invalid.
	 */
	public MessageDigestPolicyFromProperties(final Properties properties) {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper(properties);
		try {

			messageDigestAlgorithmAndProvider = ConfigMessageDigestAlgorithmAndProvider
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(MESSAGE_DIGEST_KEY, DEFAULT_MESSAGE_DIGEST.name()));

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigMessageDigestAlgorithmAndProvider getMessageDigestAlgorithmAndProvider() {
		return messageDigestAlgorithmAndProvider;
	}
}
