/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.config.commands.PasswordReaderUtils;

@SpringJUnitConfig(classes = Application.class)
class ConfigurationCommandLineTest {

	@TempDir
	Path tempDir;

	@Autowired
	private ConfigurationCommandLine commandLine;

	@Test
	void genEncryptionParameters() throws Exception {
		final Path p12Path = Paths.get(this.getClass().getResource("/integration.p12").toURI());
		final Path seedPath = Paths.get(this.getClass().getResource("/seed.txt").toURI());
		final Path seedSignaturePath = Paths.get(this.getClass().getResource("/seed.txt.p7").toURI());
		final Path trustedCAPath = Paths.get(this.getClass().getResource("/rootCA.pem").toURI());

		try (MockedStatic<PasswordReaderUtils> mockedPasswordReader = mockStatic(PasswordReaderUtils.class)) {
			mockedPasswordReader.when(PasswordReaderUtils::readPasswordFromConsole).thenReturn("TestPassword123".toCharArray());

			commandLine
					.run("-genEncryptionParameters", "-seed_path", seedPath.toString(), "-seed_sig_path", seedSignaturePath.toString(), "-p12_path",
							p12Path.toString(), "-trusted_ca_path", trustedCAPath.toString(), "-out", tempDir.toString());

			mockedPasswordReader.verify(PasswordReaderUtils::readPasswordFromConsole);
			assertEquals(2, Files.list(tempDir).count());
		}
	}

}
