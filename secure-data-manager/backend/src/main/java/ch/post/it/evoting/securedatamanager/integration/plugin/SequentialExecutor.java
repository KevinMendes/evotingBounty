/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SequentialExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SequentialExecutor.class);

	public void execute(final List<String> commands, final Parameters parameters, final ExecutionListener listener) {

		for (final String command : commands) {
			String mockCommand = command;
			try {
				// Replace the parameters.
				final String[] partialCommands = replaceParameters(command, parameters);
				final String fullCommand = partialCommands[0];
				mockCommand = partialCommands[1];

				// Remove unwanted environment variable that will be inherited by the child process.
				final String[] envp = buildCommandEnv();

				// Execute the command.
				final Process proc = Runtime.getRuntime().exec(new StringTokenizer(fullCommand, " \t\n\r\f").getTokenArray(), envp);

				final InputStream isIn = proc.getInputStream();
				consumeProcessStream(isIn, listener);

				final StringBuilder stringBuilder = new StringBuilder();
				final InputStream isError = proc.getErrorStream();
				consumeProcessStream(isError, stringBuilder);

				final int exitValue = proc.waitFor();
				detectError(exitValue, stringBuilder, mockCommand, listener);

			} catch (final IllegalArgumentException e) {
				listener.onError(ResultCode.UNEXPECTED_ERROR.value());
				listener.onMessage(e.getMessage());
				LOGGER.error("Error '{}' when executing command: {} : {}", ResultCode.UNEXPECTED_ERROR.value(), mockCommand, e);
			} catch (final RuntimeException | IOException e) {
				listener.onError(ResultCode.GENERAL_ERROR.value());
				listener.onMessage(e.getMessage());
				LOGGER.error("Error '{}' when executing command: {} : {}", ResultCode.GENERAL_ERROR.value(), mockCommand, e);
			} catch (final InterruptedException e) {
				LOGGER.warn("Got interrupted when executing command: {}", mockCommand);
				Thread.currentThread().interrupt();
			}
		}
	}

	private String[] buildCommandEnv() {
		// Get the current environment variables into a modifiable map.
		final Map<String, String> env = System.getenv();
		final Map<String, String> modifiable = new HashMap<>(env);

		// Some variables logging is polluting the error stream of the child process.
		// JAVA_TOOL_OPTIONS must not be inherited otherwise it will be tried to attach two debuggers to the same jvm on the same port.
		modifiable.remove("JAVA_TOOL_OPTIONS");

		return modifiable.entrySet().stream().map(Object::toString).toArray(String[]::new);
	}

	private void consumeProcessStream(final InputStream is, final StringBuilder sb) throws IOException {
		final String error = IOUtils.toString(is, StandardCharsets.UTF_8);
		sb.append(error);
	}

	private void consumeProcessStream(final InputStream is, final ExecutionListener listener) {
		try {
			final InputStreamReader isr = new InputStreamReader(is);
			final BufferedReader br = new BufferedReader(isr);
			String line;
			LOGGER.debug("Plugin Output ----->>");
			while ((line = br.readLine()) != null) {
				if (line.contains("ERROR")) {
					LOGGER.warn("Plugin execution may have generated an error --->> {}", line);
					listener.onProgress(line);
					listener.onError(-1);
				} else {
					LOGGER.debug(line);
					listener.onProgress(line);
				}
			}
			LOGGER.debug("<<----- Plugin Output");
		} catch (final IOException e) {
			LOGGER.warn("Failed to read plugin execution output", e);
		}
	}

	private String[] replaceParameters(final String command, final Parameters parameters) {
		String partialCommand = command;
		String replacedCommand = command;

		for (final KeyParameter key : KeyParameter.values()) {
			if (replacedCommand.contains(key.toString())) {
				final String value = parameters.getParam(key.name());
				if (value == null || value.isEmpty()) {
					throw new IllegalArgumentException("Parameter #" + key.name() + "# is null or empty");
				} else {
					replacedCommand = replacedCommand.replaceAll("#" + key + "#", value);
					if (key == KeyParameter.PRIVATE_KEY) {
						partialCommand = partialCommand.replaceAll("#" + key + "#", "PRIVATE_KEY");
					} else {
						partialCommand = partialCommand.replaceAll("#" + key + "#", value);
					}
				}
			}
		}

		return new String[] { replacedCommand, partialCommand };
	}

	/**
	 * In our plugin the error code is always '0' even if it fails internally (unless it throws an uncaught exception). Therefore, we have to infer
	 * the real exit value from the process outputstream and return it in the 'listener' object
	 */
	private void detectError(final int exitValue, final StringBuilder stringBuilder, final String mockCommand, final ExecutionListener listener) {

		final String errorOutput = stringBuilder.toString().trim();

		if (exitValue != 0 || errorOutput.length() > 0) {
			if (exitValue != 0) {
				listener.onError(exitValue);
			} else {
				try {
					final int actualError = Integer.parseInt(errorOutput);
					listener.onError(actualError);
				} catch (final NumberFormatException e) {
					listener.onError(ResultCode.GENERAL_ERROR.value());
				}
			}
			listener.onMessage("Failed to execute: " + mockCommand);
			LOGGER.error("ExitCode '{}' when executing command {} : {}", exitValue, mockCommand, errorOutput);
		}
	}
}
