/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.config.commons.domain.common.ConfigurationInput;
import ch.post.it.evoting.securedatamanager.config.commons.readers.ConfigurationInputReader;

@Component
public class CreateElectionEventHolderInitializer {

	private final ConfigurationInputReader configurationInputReader;

	public CreateElectionEventHolderInitializer(final ConfigurationInputReader configurationInputReader) {
		this.configurationInputReader = configurationInputReader;
	}

	public void init(final CreateElectionEventParametersHolder holder, final InputStream configurationInputStream) throws IOException {

		final ConfigurationInput configurationInput = configurationInputReader.fromStreamToJava(configurationInputStream);
		holder.setConfigurationInput(configurationInput);
	}
}
