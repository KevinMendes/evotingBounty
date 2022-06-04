/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

import static ch.post.it.evoting.config.CommandParameter.OUT;
import static ch.post.it.evoting.config.CommandParameter.P12_PATH;
import static ch.post.it.evoting.config.CommandParameter.SEED_PATH;
import static ch.post.it.evoting.config.CommandParameter.SEED_SIG_PATH;
import static ch.post.it.evoting.config.CommandParameter.TRUSTED_CA_PATH;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.config.Parameters;

/**
 * Provides functionality for extracting and transforming parameters for the pre-configuration process.
 */
@Service
public final class ConfigEncryptionParametersAdapter {

	/**
	 * Processes {@code receivedParameters} returns an adapted version of the parameters, encapsulated within a {@link
	 * ConfigEncryptionParametersContainer}.
	 *
	 * @param receivedParameters the parameters to adapt.
	 * @return the adapted parameters.
	 */
	public ConfigEncryptionParametersContainer adapt(final Parameters receivedParameters) {
		final Path p12Path = Paths.get(receivedParameters.getParam(P12_PATH.getParameterName()));
		final Path seedPath = Paths.get(receivedParameters.getParam(SEED_PATH.getParameterName()));
		final Path seedSignaturePath = Paths.get(receivedParameters.getParam(SEED_SIG_PATH.getParameterName()));
		final Path trustedCAPath = Paths.get(receivedParameters.getParam(TRUSTED_CA_PATH.getParameterName()));

		Path outputPath = null;
		if (receivedParameters.contains(OUT.getParameterName())) {
			outputPath = Paths.get(receivedParameters.getParam(OUT.getParameterName()));
		}
		validateReceivedParameters(P12_PATH.getParameterName(), p12Path);
		validateReceivedParameters(SEED_PATH.getParameterName(), seedPath);
		validateReceivedParameters(SEED_SIG_PATH.getParameterName(), seedSignaturePath);
		validateReceivedParameters(TRUSTED_CA_PATH.getParameterName(), trustedCAPath);
		return new ConfigEncryptionParametersContainer(p12Path, seedPath, seedSignaturePath, outputPath, trustedCAPath);
	}

	private void validateReceivedParameters(final String nameParameterToValidate, final Path parameterValue) {
		if ((parameterValue == null) || parameterValue.toString().isEmpty()) {
			throw new IllegalArgumentException(String.format("The %s must be informed.", nameParameterToValidate));
		}
	}
}
