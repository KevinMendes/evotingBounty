/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ConfigObjectMapperException;

/**
 * Adapter of jackson mapper to execute common transformations (from java to json, from json to java, from java to file and from file to java)
 */
public class ConfigObjectMapper {

	private final ObjectMapper mapper = new ObjectMapper();

	public void fromJavaToJSONFile(final Object obj, final File dest) throws IOException {

		createFile(dest);

		mapper.writerWithDefaultPrettyPrinter().writeValue(dest, obj);
	}

	public void fromJavaToJSONFileWithoutNull(final Object obj, final File dest) throws IOException {

		createFile(dest);

		mapper.setSerializationInclusion(Include.NON_NULL).writerWithDefaultPrettyPrinter().writeValue(dest, obj);
	}

	public <T> T fromJSONFileToJava(final File src, final Class<T> valueType) throws IOException {

		return mapper.readValue(src, valueType);
	}

	private void createFile(final File dest) {
		try {
			if (!dest.exists() && !dest.createNewFile()) {
				throw new ConfigObjectMapperException("An error occurred while creating the file  " + dest.toString());
			}
		} catch (IOException e) {
			throw new ConfigObjectMapperException("Error trying to create file.", e);
		}
	}
}
