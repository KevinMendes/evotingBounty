/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.ConfigDigitalSignerAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicy;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * Implementation of {@link DigitalSignerPolicyImpl} which retrieves {@link ConfigDigitalSignerAlgorithmAndSpec} from property {}
 */
class DigitalSignerPolicyImpl implements DigitalSignerPolicy {
	private static final String RESOURCE = '/' + PolicyFromPropertiesHelper.CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH;

	private static final String OS_DEPENDENCY_PROPERTY_FORMAT = "%s.%s";

	private static final String ASYMMETRIC_CMS_SIGNER_PROPERTY_NAME = "asymmetric.cms.signer";

	private static final String ASYMMETRIC_SIGNER_PROPERTY_NAME = "asymmetric.signer.securerandom";

	private final ConfigDigitalSignerAlgorithmAndSpec spec;

	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;

	private DigitalSignerPolicyImpl(final ConfigDigitalSignerAlgorithmAndSpec spec,
			ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider) {
		this.spec = spec;
		this.secureRandomAlgorithmAndProvider = secureRandomAlgorithmAndProvider;
	}

	/**
	 * Creates a new instance loading the signer specification from {@code cryptolibPolicy.properties} which must exist in the classpath as defined by
	 * {@link PolicyFromPropertiesHelper#CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH}.
	 *
	 * @return the instance.
	 */
	public static DigitalSignerPolicyImpl newInstance() {
		Properties properties = loadCryptolibPolicyProperties();
		ConfigDigitalSignerAlgorithmAndSpec spec = getConfigDigitalSignerAlgorithmAndSpec(properties);
		ConfigSecureRandomAlgorithmAndProvider prng = getConfigSecureRandomAlgorithmAndProvider(properties);
		return new DigitalSignerPolicyImpl(spec, prng);
	}

	private static String getOSName() {
		return OperatingSystem.current().name().toLowerCase();
	}

	private static String getOsDependentPropertyValue(final String key, Properties properties) {

		return properties.getProperty(String.format(OS_DEPENDENCY_PROPERTY_FORMAT, key, getOSName()));
	}

	private static ConfigSecureRandomAlgorithmAndProvider getConfigSecureRandomAlgorithmAndProvider(Properties properties) {

		String value = getOsDependentPropertyValue(ASYMMETRIC_SIGNER_PROPERTY_NAME, properties);
		value = value.trim();
		ConfigSecureRandomAlgorithmAndProvider prngSpec;
		try {
			prngSpec = ConfigSecureRandomAlgorithmAndProvider.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(format("Property ''{0}'' has invalid value ''{1}''.", ASYMMETRIC_CMS_SIGNER_PROPERTY_NAME, value), e);
		}
		return prngSpec;
	}

	private static ConfigDigitalSignerAlgorithmAndSpec getConfigDigitalSignerAlgorithmAndSpec(final Properties properties) {
		String value = properties.getProperty(ASYMMETRIC_CMS_SIGNER_PROPERTY_NAME);
		if (value == null) {
			throw new IllegalStateException(format("Property ''{0}'' does not exist.", ASYMMETRIC_CMS_SIGNER_PROPERTY_NAME));
		}
		value = value.trim();
		ConfigDigitalSignerAlgorithmAndSpec spec;
		try {
			spec = ConfigDigitalSignerAlgorithmAndSpec.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(format("Property ''{0}'' has invalid value ''{1}''.", ASYMMETRIC_CMS_SIGNER_PROPERTY_NAME, value), e);
		}
		return spec;
	}

	private static Properties loadCryptolibPolicyProperties() {
		Properties properties = new Properties();
		try (InputStream stream = DigitalSignerPolicyImpl.class.getResourceAsStream(RESOURCE)) {
			if (stream == null) {
				throw new IllegalStateException(format("Cryptolib policy ''{0}'' does not exists in the classpath.", RESOURCE));
			}
			properties.load(stream);
		} catch (IOException e) {
			throw new IllegalStateException(format("Failed to load cryptolib policy from ''{0}''.", RESOURCE), e);
		}
		return properties;
	}

	@Override
	public ConfigDigitalSignerAlgorithmAndSpec getDigitalSignerAlgorithmAndSpec() {
		return spec;
	}

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
		return secureRandomAlgorithmAndProvider;
	}
}
