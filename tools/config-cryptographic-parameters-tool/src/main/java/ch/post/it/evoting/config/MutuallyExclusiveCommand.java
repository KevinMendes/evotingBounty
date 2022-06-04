/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

/**
 * Represents a command identifier. It contains the following parts:
 * <ul>
 * <li>A command name.</li>
 * <li>A list of {@link CommandParameter}.</li>
 * </ul>
 * Each MutuallyExclusiveCommand is capable to generate a {@link Option} to be used by the command
 * parser.
 */
public enum MutuallyExclusiveCommand {

	HELP("h") {
		@Override
		public List<CommandParameter> getCommandParameters() {
			return new ArrayList<>(0);
		}

		@Override
		public Option generateOption() {
			return Option.builder("h").longOpt("help").desc("Prints help.").build();
		}
	},
	GEN_ENCRYPTION_PARAM("genEncryptionParameters") {
		@Override
		public List<CommandParameter> getCommandParameters() {
			final List<CommandParameter> commandParams = new ArrayList<>();
			Collections.addAll(commandParams, CommandParameter.P12_PATH, CommandParameter.SEED_PATH, CommandParameter.SEED_SIG_PATH,
					CommandParameter.TRUSTED_CA_PATH, CommandParameter.OUT);
			return commandParams;
		}

		@Override
		public Option generateOption() {
			return Option.builder(getCommandName()).longOpt("generatePreConfiguration").desc("generates the pre-configuration.").build();
		}
	},
	GEN_PRIME_GROUP_MEMBERS("primeGroupMembers") {
		@Override
		public List<CommandParameter> getCommandParameters() {
			final List<CommandParameter> commandParams = new ArrayList<>();
			Collections.addAll(commandParams, CommandParameter.P12_PATH, CommandParameter.ENCRYPTION_PARAMS, CommandParameter.TRUSTED_CA_PATH,
					CommandParameter.OUT);
			return commandParams;
		}

		@Override
		public Option generateOption() {
			return Option.builder(getCommandName()).longOpt("primeGroupMembers").desc("generates the a list of prime group members.").build();
		}
	},
	GEN_KEY_STORE("keystore") {
		@Override
		public List<CommandParameter> getCommandParameters() {
			final List<CommandParameter> commandParams = new ArrayList<>();
			Collections.addAll(commandParams, CommandParameter.ALIAS, CommandParameter.VALID_FROM, CommandParameter.VALID_UNTIL,
					CommandParameter.CERTIFICATE_COMMON_NAME, CommandParameter.CERTIFICATE_COUNTRY, CommandParameter.CERTIFICATE_STATE,
					CommandParameter.CERTIFICATE_LOCALITY, CommandParameter.CERTIFICATE_ORGANISATION, CommandParameter.OUT,
					CommandParameter.PASSWORD_LENGTH);
			return commandParams;
		}

		@Override
		public Option generateOption() {
			return Option.builder(getCommandName()).longOpt("keystore")
					.desc("generates the key store containing a private key, the password file to open it and the certificate matching the key.")
					.build();
		}
	};

	private final String commandName;

	MutuallyExclusiveCommand(final String commandName) {
		this.commandName = commandName;
	}

	/**
	 * Gets commands option group composed by all {@link MutuallyExclusiveCommand} options.
	 *
	 * @return the commands option group composed by all {@link MutuallyExclusiveCommand} options.
	 */
	public static OptionGroup getCommandsOptionGroup() {
		final OptionGroup optionGroup = new OptionGroup();
		for (final MutuallyExclusiveCommand mutuallyExclusiveCommand : values()) {
			optionGroup.addOption(mutuallyExclusiveCommand.generateOption());
		}
		return optionGroup;
	}

	/**
	 * Gets command name.
	 *
	 * @return the command name
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * Gets the list of command parameters of this {@link MutuallyExclusiveCommand}.
	 *
	 * @return the list of command parameters of this {@link MutuallyExclusiveCommand}.
	 */
	public abstract List<CommandParameter> getCommandParameters();

	/**
	 * Generate {@link Option} to be used by the command parser.
	 *
	 * @return the option to be used by the command parser.
	 */
	public abstract Option generateOption();
}
