/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.OptionGroup;
import org.junit.jupiter.api.Test;

class MutuallyExclusiveCommandTest {

	@Test
	void checkAllCommandsOptionGroupIsRight() {
		final OptionGroup options = MutuallyExclusiveCommand.getCommandsOptionGroup();
		final Collection<String> optionNames = options.getNames();
		assertEquals(4, optionNames.size());
		assertTrue(optionNames.contains(MutuallyExclusiveCommand.HELP.getCommandName()));
		assertTrue(optionNames.contains(MutuallyExclusiveCommand.GEN_ENCRYPTION_PARAM.getCommandName()));
		assertTrue(optionNames.contains(MutuallyExclusiveCommand.GEN_PRIME_GROUP_MEMBERS.getCommandName()));
		assertTrue(optionNames.contains(MutuallyExclusiveCommand.GEN_KEY_STORE.getCommandName()));
	}

	@Test
	void checkHelpCommandParametersAreRight() {
		final List<CommandParameter> parameters = MutuallyExclusiveCommand.HELP.getCommandParameters();
		assertEquals(0, parameters.size());
	}

	@Test
	void checkGenPreConfigCommandParametersAreRight() {
		final List<CommandParameter> parameters = MutuallyExclusiveCommand.GEN_ENCRYPTION_PARAM.getCommandParameters();
		assertEquals(5, parameters.size());
		assertTrue(parameters.contains(CommandParameter.P12_PATH));
		assertTrue(parameters.contains(CommandParameter.SEED_PATH));
		assertTrue(parameters.contains(CommandParameter.SEED_SIG_PATH));
		assertTrue(parameters.contains(CommandParameter.TRUSTED_CA_PATH));
		assertTrue(parameters.contains(CommandParameter.OUT));
	}

	@Test
	void checkGeneratePrimeGroupMembersCommandParametersAreRight() {
		final List<CommandParameter> parameters = MutuallyExclusiveCommand.GEN_PRIME_GROUP_MEMBERS.getCommandParameters();
		assertEquals(4, parameters.size());
		assertTrue(parameters.contains(CommandParameter.ENCRYPTION_PARAMS));
		assertTrue(parameters.contains(CommandParameter.OUT));
		assertTrue(parameters.contains(CommandParameter.TRUSTED_CA_PATH));
		assertTrue(parameters.contains(CommandParameter.P12_PATH));
	}
}
