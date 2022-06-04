/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.specific.ConfigObjectMapperException;

/**
 * Adapter of jackson mapper to execute common transformations (from java to json, from json to java, from java to file and from file to java)
 */
@Component
public class ConfigObjectMapper {

	private final ObjectMapper mapper = new ObjectMapper();

	public String fromJavaToJSON(final Object obj) throws JsonProcessingException {

		return mapper.writeValueAsString(obj);
	}

	public void fromJavaToJSONFile(final Object obj, final File dest) throws IOException {

		createFile(dest);

		mapper.writerWithDefaultPrettyPrinter().writeValue(dest, obj);
	}

	public void fromJavaToJSONFileWithoutNull(final Object obj, final File dest) throws IOException {

		createFile(dest);

		mapper.setSerializationInclusion(Include.NON_NULL).writerWithDefaultPrettyPrinter().writeValue(dest, obj);
	}

	public <T> T fromJSONToJava(final String json, final Class<T> valueType) throws IOException {

		return mapper.readValue(json, valueType);
	}

	public <T> T fromJSONFileToJava(final File src, final Class<T> valueType) throws IOException {
		return mapper.readValue(src, valueType);
	}

	public <T> T fromJSONStreamToJava(final InputStream src, final Class<T> valueType) throws IOException {
		return mapper.readValue(src, valueType);
	}

	private void createFile(final File dest) throws IOException {
		if (!dest.exists() && !dest.createNewFile()) {
			throw new ConfigObjectMapperException("An error occurred while creating the file  " + dest.toString());
		}
	}
}
