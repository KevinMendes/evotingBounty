/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.test.tools.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Properties;

/**
 * Utility to retrieve the properties of a pre-generated quadratic residue subgroup so that the unit tests that need this information do not have to
 * generate it themselves, which is a time consuming operation.
 */
public class GroupLoader {

	/**
	 * The relative path of the properties file that should be used for the quadratic residue subgroup configuration.
	 */
	private static final String PROPERTIES_FILE_PATH_PATTERN = "/properties/qr-subgroup-%d.properties";
	private static final String P_PARAMETER_PROPERTY_KEY = "p";
	private static final String Q_PARAMETER_PROPERTY_KEY = "q";
	private static final String GENERATOR_PROPERTY_KEY = "g";
	private final BigInteger p;
	private final BigInteger q;
	private final BigInteger g;

	/**
	 * Loads the pre-generated quadratic residue subgroup parameters from a file.
	 */
	public GroupLoader() {
		this(1);
	}

	public GroupLoader(final int index) {
		final String propertiesFilePath = String.format(PROPERTIES_FILE_PATH_PATTERN, index);

		final Properties config = new Properties();
		try (final InputStream stream = getClass().getResourceAsStream(propertiesFilePath)) {
			config.load(stream);
		} catch (final IOException e) {
			throw new IllegalStateException("Failed to load qr subgroup configuration.", e);
		}

		p = new BigInteger(config.getProperty(P_PARAMETER_PROPERTY_KEY));
		q = new BigInteger(config.getProperty(Q_PARAMETER_PROPERTY_KEY));
		g = new BigInteger(config.getProperty(GENERATOR_PROPERTY_KEY));
	}

	public BigInteger getP() {
		return p;
	}

	public BigInteger getQ() {
		return q;
	}

	public BigInteger getG() {
		return g;
	}

}
