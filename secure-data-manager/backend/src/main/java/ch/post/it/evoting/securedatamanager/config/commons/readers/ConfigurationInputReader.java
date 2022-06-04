/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.readers;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.config.commons.domain.common.ConfigurationInput;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;

/**
 * A ConfigObjectMapper wrapper to get from a file ConfigurationInput objects.
 */
@Component
public class ConfigurationInputReader {

	private final ConfigObjectMapper configObjectMapper = new ConfigObjectMapper();

	public ConfigurationInput fromStreamToJava(final InputStream src) throws IOException {
		return configObjectMapper.fromJSONStreamToJava(src, ConfigurationInput.class);
	}

}
