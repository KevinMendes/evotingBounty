/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.config.commands.encryptionparameters.EncryptionParametersCommandProcessor;
import ch.post.it.evoting.config.commands.keystore.KeyStoreCommandProcessor;
import ch.post.it.evoting.config.commands.primes.PrimeGroupCommandProcessor;

/**
 * Main class in charge of dispatching the main mutually exclusive commands to the relate command processor.
 */
@Service
public class ConfigurationCommandLine implements CommandLineRunner {

	private final KeyStoreCommandProcessor keyStoreCommandProcessor;
	private final EncryptionParametersCommandProcessor encryptionParametersCommandProcessor;
	private final PrimeGroupCommandProcessor primeGroupCommandProcessor;

	public ConfigurationCommandLine(final KeyStoreCommandProcessor keyStoreCommandProcessor,
			final EncryptionParametersCommandProcessor encryptionParametersCommandProcessor,
			final PrimeGroupCommandProcessor primeGroupCommandProcessor) {

		this.keyStoreCommandProcessor = keyStoreCommandProcessor;
		this.encryptionParametersCommandProcessor = encryptionParametersCommandProcessor;
		this.primeGroupCommandProcessor = primeGroupCommandProcessor;
	}

	@Override
	public void run(final String... args) {
		final Command processedCommand = MainParametersProcessor.process(args);

		if (processedCommand == null) {
			return;
		}

		final MutuallyExclusiveCommand action = processedCommand.getIdentifier();
		Parameters parameters = processedCommand.getParameters();

		switch (action) {
		case HELP:
			// Nothing to do, is done inside MainParametersProcessor.process
			break;
		case GEN_ENCRYPTION_PARAM:
			encryptionParametersCommandProcessor.accept(parameters);
			break;
		case GEN_PRIME_GROUP_MEMBERS:
			primeGroupCommandProcessor.accept(parameters);
			break;
		case GEN_KEY_STORE:
			keyStoreCommandProcessor.accept(parameters);
			break;
		default:
			throw new UnsupportedOperationException(String.format("The command '%s' has no implementation yet.", action.getCommandName()));
		}
	}
}
