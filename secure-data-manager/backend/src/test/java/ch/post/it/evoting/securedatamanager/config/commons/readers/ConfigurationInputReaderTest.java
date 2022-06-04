/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.readers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import ch.post.it.evoting.securedatamanager.config.commons.domain.common.ConfigurationInput;

class ConfigurationInputReaderTest {

	private ConfigurationInputReader configurationInputReader;

	@BeforeEach
	void init() {
		configurationInputReader = new ConfigurationInputReader();
	}

	@Test
	void readGivenInputStreamCorrectly() {

		final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("commons_keys_config.json");
		final ConfigurationInput configurationInput = assertDoesNotThrow(() -> configurationInputReader.fromStreamToJava(inputStream));

		assertEquals("privatekey", configurationInput.getBallotBox().getAlias().get("privateKey"));
	}

	@Test
	void throwAnExceptionIfGivenFileIsNotConsistent() {

		final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("not_consistent.json");

		assertThrows(UnrecognizedPropertyException.class, () -> configurationInputReader.fromStreamToJava(inputStream));
	}

}
