/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

class MainParametersProcessorTest {

	@Test
	void processEmptyCommand() throws IOException {
		final Command process;
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {

			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.toString()));
			process = MainParametersProcessor.process(new String[0]);
		}
		assertThat(process == null, CoreMatchers.is(true));
	}

	@Test
	void parseWrongCommand() throws IOException {
		final Command process;
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {

			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.toString()));
			final String[] params = new String[] { "-genUUID", "-primeGroupMembers", "-p", };
			process = MainParametersProcessor.process(params);
		}
		assertThat(process == null, CoreMatchers.is(true));
	}

	@Test
	void parseCommandWithParameterWithoutRequiredValue() throws IOException {
		final Command process;
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {

			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.toString()));
			final String[] params = new String[] { "-genUUID", "-num", };
			process = MainParametersProcessor.process(params);
		}
		assertThat(process == null, CoreMatchers.is(true));
	}

	@Test
	void parseCommandWithoutRequiredParameter() {
		final String[] params = new String[] { "-primeGroupMembers", "-params_path", "", "-p12_path", "", "-out", "", };

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> MainParametersProcessor.process(params));
		assertEquals("primeGroupMembers command requires p12_path parameter.", exception.getMessage());
	}

	@Test
	void parseHelpCommand() throws IOException {
		final Command process;
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {

			System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.toString()));
			final String[] params = new String[] { "-h", };
			process = MainParametersProcessor.process(params);
		}

		assertEquals(MutuallyExclusiveCommand.HELP, process.getIdentifier());
	}

	@Test
	void parseGenEncryptionParametersCommand() {
		final String seedPath = "some/path/seed.txt";
		final String seedSinaturePath = "some/path/seed.txt.p7";
		final String p12Path = "some/int.p12";
		final String trustedCA = "tCA";
		final String outPath = "out";
		final String[] params = new String[] { "-genEncryptionParameters", "-seed_path", seedPath, "-seed_sig_path", seedSinaturePath, "-p12_path",
				p12Path, "-out", outPath, "-trusted_ca_path", trustedCA };

		final Command result = MainParametersProcessor.process(params);

		assertEquals(MutuallyExclusiveCommand.GEN_ENCRYPTION_PARAM, result.getIdentifier());
		assertEquals(seedPath, result.getParameters().getParam(CommandParameter.SEED_PATH.getParameterName()));
		assertEquals(seedSinaturePath, result.getParameters().getParam(CommandParameter.SEED_SIG_PATH.getParameterName()));
		assertEquals(p12Path, result.getParameters().getParam(CommandParameter.P12_PATH.getParameterName()));
		assertEquals(outPath, result.getParameters().getParam(CommandParameter.OUT.getParameterName()));
		assertEquals(trustedCA, result.getParameters().getParam(CommandParameter.TRUSTED_CA_PATH.getParameterName()));
	}

	@Test
	void parseGeneratePrimeGroupMembersCommand() {
		final String paramsPath = "some/path";
		final String outPath = "outpath";
		final String p12Path = "path";
		final String trustedCA = "tCA";
		final String[] params = new String[] { "-primeGroupMembers", "-params_path", paramsPath, "-out", outPath, "-p12_path", p12Path,
				"-trusted_ca_path", trustedCA };

		final Command result = MainParametersProcessor.process(params);
		assertEquals(MutuallyExclusiveCommand.GEN_PRIME_GROUP_MEMBERS, result.getIdentifier());
		assertEquals(paramsPath, result.getParameters().getParam(CommandParameter.ENCRYPTION_PARAMS.getParameterName()));
		assertEquals(outPath, result.getParameters().getParam(CommandParameter.OUT.getParameterName()));
		assertEquals(p12Path, result.getParameters().getParam(CommandParameter.P12_PATH.getParameterName()));
		assertEquals(trustedCA, result.getParameters().getParam(CommandParameter.TRUSTED_CA_PATH.getParameterName()));
	}
}
