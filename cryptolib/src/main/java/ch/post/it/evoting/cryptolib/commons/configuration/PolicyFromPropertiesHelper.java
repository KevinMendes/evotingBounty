/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.configuration;

import static java.nio.file.Files.exists;
import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;

/**
 * This class helps {@code Policy} from properties builders to retrieve not blank properties. While the builders will take care of setting the default
 * values, the PolicyFromPropertiesHelper will load the overwritten properties from the cryptolibPolicy.properties file in the properties directory.
 * Example: the default value for stores.keystore is SUN. If another value, e.g. BC, should be used instead, it can be defined in
 * properties/cryptolibPolicy.properties by putting stores.keystore=BC
 */
public class PolicyFromPropertiesHelper {
	public static final String CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH = "properties/cryptolibPolicy.properties";
	private static final String OS_DEPENDENCY_PROPERTY_FORMAT = "%s.%s";
	private final Properties properties;

	/**
	 * Creates a helper that will get properties from the given properties file.
	 *
	 * @throws CryptoLibException if the file loading failed.
	 */
	public PolicyFromPropertiesHelper() {
		this.properties = loadCryptolibProperties();
	}

	/**
	 * Creates a helper that will get properties from the given properties file.
	 *
	 * @param properties The path of the properties file.
	 * @throws CryptoLibException if the file loading failed.
	 */
	public PolicyFromPropertiesHelper(final Properties properties) {
		this.properties = properties;
	}

	/*
	 * Finds the URL of the policy properties. To preserve the backward
	 * compatibility the implementation repeats the following steps from
	 * Apache Commons Configuration:
	 *
	 * 1. Try to use the path as a valid URL.
	 *
	 * 2. Try to treat the path as an absolute file. If the file exists,
	 * then transform it to URL.
	 *
	 * 3. Try to resolve the path against the user home folder. If the file
	 * exists, then transform it to URL.
	 *
	 * 4. Try to find the resource in the context class loader.
	 *
	 * 5. Try to find the resource in the system class loader.
	 */
	private static URL findProperties(final String path) {
		if (path == null) {
			throw new CryptoLibException("Path is null.");
		}

		try {
			return new URL(path);
		} catch (final MalformedURLException e) {
			// nothing to do
		}

		Path file = Paths.get(path);
		if (file.isAbsolute() && exists(file)) {
			try {
				return file.toUri().toURL();
			} catch (final MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}

		file = Paths.get(System.getProperty("user.home"), path);
		if (exists(file)) {
			try {
				return file.toUri().toURL();
			} catch (final MalformedURLException e) {
				throw new IllegalStateException(e);
			}
		}

		URL url = PolicyFromPropertiesHelper.class.getClassLoader().getResource(path);
		if (url != null) {
			return url;
		}

		url = ClassLoader.getSystemResource(path);
		if (url != null) {
			return url;
		}

		throw new CryptoLibException(format("Failed to find properties at ''{0}''.", path));
	}

	public static Properties loadProperties(final String path) {
		final URL url = findProperties(path);
		final Properties properties = new Properties();
		try (final InputStream stream = url.openStream()) {
			properties.load(stream);
		} catch (final IOException e) {
			throw new CryptoLibException(format("Failed to load properties from ''{0}''.", url), e);
		}
		return properties;
	}

	public static Properties loadCryptolibProperties() {
		try {
			return loadProperties(CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH);
		} catch (final CryptoLibException e) {
			return new Properties();
		}
	}

	private static void validateNotBlank(final String propertyName, final String propertyValue) {
		if (propertyValue == null || propertyValue.isEmpty()) {
			throw new CryptoLibException(String.format("property:'%s' cannot be blank.", propertyName));
		}
		for (int i = 0; i < propertyValue.length(); i++) {
			if (!Character.isWhitespace(propertyValue.charAt(i))) {
				return;
			}
		}
		throw new CryptoLibException(String.format("property:'%s' cannot be blank.", propertyName));
	}

	private static String getOSName() {
		return OperatingSystem.current().name().toLowerCase();
	}

	private static String getOsDependentPropertyValue(final String unixValue, final String windowsValue) {
		if (OperatingSystem.WINDOWS.isCurrent()) {
			return windowsValue;
		}
		return unixValue;
	}

	private String getNotBlankOrDefaultProperty(final String key, final String defaultValue) {
		final String value = properties.getProperty(key, defaultValue);
		validateNotBlank(key, value);
		return value;
	}

	/**
	 * Retrieves the value of the given key. The method also checks if the returned value is blank. If the key is not set, the default value is
	 * returned.
	 *
	 * @param key          The requested key.
	 * @param defaultValue The default value to be used if the key is not defined.
	 * @return The value assigned to the key.
	 */
	public String getNotBlankOrDefaultPropertyValue(final String key, final String defaultValue) {

		return getNotBlankOrDefaultProperty(key, defaultValue);
	}

	/**
	 * Retrieves the value of the given key adapted to the operating system. The method also checks if the returned value is blank.
	 *
	 * @param key The requested key.
	 * @return The value assigned to the key.
	 * @throws CryptoLibException in case of returning a blank value.
	 */
	public String getNotBlankOrDefaultOSDependentPropertyValue(final String key, final String unixDefault, final String windowsDefault) {

		return getNotBlankOrDefaultProperty(String.format(OS_DEPENDENCY_PROPERTY_FORMAT, key, getOSName()),
				getOsDependentPropertyValue(unixDefault, windowsDefault));
	}

	/**
	 * Retrieves the value of the given key.
	 *
	 * @param key the key
	 * @return the value or {@code null} if the key is not defined.
	 */
	public String getPropertyValue(final String key) {
		return properties.getProperty(key);
	}
}
