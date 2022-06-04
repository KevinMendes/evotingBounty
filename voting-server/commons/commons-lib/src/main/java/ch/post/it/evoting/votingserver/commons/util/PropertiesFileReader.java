/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for reading the application properties file.
 */
public enum PropertiesFileReader {

	INSTANCE;

	// The name of the properties file of the application.
	private static final String FILE_NAME_APPLICATION_PROPERTIES = "Application.properties";
	private final Properties properties;

	private final Logger logger = LoggerFactory.getLogger(PropertiesFileReader.class);

	PropertiesFileReader() {
		properties = new Properties();
		try {
			final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME_APPLICATION_PROPERTIES);
			properties.load(inputStream);
		} catch (final IOException ioE) {
			logger.error("Error trying to get resource " + FILE_NAME_APPLICATION_PROPERTIES, ioE);
		}
	}

	/**
	 * Obtains the instance of the propertiesFileReader.
	 *
	 * @return Class instance
	 */
	public static PropertiesFileReader getInstance() {
		return INSTANCE;
	}

	/**
	 * Given a property name returns its value.
	 *
	 * @param propertyName , name of the property
	 * @return the value of the property
	 */
	public String getPropertyValue(final String propertyName) {
		return properties.getProperty(propertyName);
	}
}
