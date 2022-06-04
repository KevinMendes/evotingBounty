/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains methods to convert the inline commands into Java objects.
 */
public class MainParametersProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainParametersProcessor.class);

	private MainParametersProcessor() {
		// Intentionally left blank.
	}

	/**
	 * Gets a raw command line and process into a {@link Command}.
	 *
	 * @param args a raw command line.
	 * @return the {@link Command} that represents the raw command line, {@code null} if the raw command line cannot be processed.
	 */
	public static Command process(final String[] args) {

		final Options options = generateOptions();

		final DefaultParser parser = new DefaultParser();

		final CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (final ParseException e) {
			printHelp(options);
			return null;
		}

		MutuallyExclusiveCommand mutuallyExclusiveCommand = null;

		if (cmd.hasOption(MutuallyExclusiveCommand.HELP.getCommandName())) {
			mutuallyExclusiveCommand = MutuallyExclusiveCommand.HELP;
			printHelp(options);
		} else {
			final Option[] optionVector = cmd.getOptions();
			for (final MutuallyExclusiveCommand command : MutuallyExclusiveCommand.values()) {
				if (ArrayUtils.contains(optionVector, command.generateOption())) {
					mutuallyExclusiveCommand = command;
					break;
				}
			}
		}

		final Parameters commandParams = new Parameters();
		addValueToParameters(commandParams, cmd, mutuallyExclusiveCommand);

		return new Command(mutuallyExclusiveCommand, commandParams);
	}

	private static void addValueToParameters(final Parameters commandParams, final CommandLine cmd, final MutuallyExclusiveCommand command) {
		for (final CommandParameter commandParameter : command.getCommandParameters()) {
			// validate param required
			final String optionName = commandParameter.getParameterName();
			final String[] optionValues = cmd.getOptionValues(optionName);
			String optionValue = null;
			if (!ArrayUtils.isEmpty(optionValues)) {
				optionValue = String.join(",", optionValues);
			}
			if (StringUtils.isEmpty(optionValue)) {
				optionValue = commandParameter.getDefaultValue();
			}
			if (commandParameter.isRequired() && StringUtils.isEmpty(optionValue)) {
				throw new IllegalArgumentException(String.format("%s command requires %s parameter.", command.getCommandName(), optionName));
			}
			if (commandParameter.getNumberOfArgs() > 0 || commandParameter.getNumberOfArgs() == Option.UNLIMITED_VALUES) {
				commandParams.addParam(optionName, optionValue);
			} else {
				// parameters without value, like flags
				commandParams.addParam(optionName, String.valueOf(true));
			}
		}
	}

	private static Options generateOptions() {
		final Options options = new Options();
		final OptionGroup mainCommands = MutuallyExclusiveCommand.getCommandsOptionGroup();
		mainCommands.setRequired(true);

		options.addOptionGroup(mainCommands);

		for (final CommandParameter commandParameter : CommandParameter.values()) {
			options.addOption(commandParameter.generateOption());
		}
		return options;
	}

	private static void printHelp(final Options options) {

		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("program", options, true);

		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config-cmd.help.txt");
				PrintWriter pw = new PrintWriter(System.out)) {
			IOUtils.copy(is, pw, StandardCharsets.UTF_8);
		} catch (final IOException e) {
			LOGGER.warn("A error occurred when trying to print extend help.");
		}
	}
}
