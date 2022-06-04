/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.config.commands.keystore;

import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.getMapWithAllParameter;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.mapToParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.config.Application;
import ch.post.it.evoting.config.CommandParameter;
import ch.post.it.evoting.config.Parameters;

@SpringJUnitConfig(classes = Application.class)
class KeyStoreCommandProcessorTest {

	@Autowired
	KeyStoreCommandProcessor processor;

	@TempDir
	Path tempDir;

	@Test
	void provideOutputDirWhichAlreadyExist_creationIsSkippedWithoutException() throws IOException {
		// given
		final Path outputPath = tempDir.resolve("output");
		Files.createDirectory(outputPath);

		final Map<String, String> parametersMap = getMapWithAllParameter();
		parametersMap.put(CommandParameter.OUT.getParameterName(), outputPath.toString());
		final Parameters parameters = mapToParameters(parametersMap);

		// when
		processor.accept(parameters);

		// / then
		assertThat(Files.list(outputPath)).hasSize(3);
	}

	@ParameterizedTest
	@ValueSource(strings = { "signing_keystore_alias_value.jks", "signing_pw_alias_value.txt", "signing_certificate_alias_value.crt" })
	void provideOutputDirWhichAlreadyExist_exceptionIsThrown(String fileName) throws IOException {
		// given
		System.out.println("use directory " + tempDir.toString());
		final Map<String, String> parametersMap = getMapWithAllParameter();
		final Path outputPath = tempDir.resolve("output");
		Files.createDirectory(outputPath);
		parametersMap.put(CommandParameter.OUT.getParameterName(), outputPath.toString());

		Files.createFile(outputPath.resolve(fileName));

		final Parameters parameters = mapToParameters(parametersMap);

		// when / then
		assertThatThrownBy(() -> processor.accept(parameters))
				.isInstanceOf(UncheckedIOException.class);
	}
}