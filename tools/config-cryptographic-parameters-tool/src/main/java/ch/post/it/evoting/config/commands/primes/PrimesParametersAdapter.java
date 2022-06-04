/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.primes;

import static ch.post.it.evoting.config.CommandParameter.OUT;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.config.CommandParameter;
import ch.post.it.evoting.config.Parameters;

@Service
public final class PrimesParametersAdapter {

	public PrimesParametersContainer adapt(final Parameters receivedParameters) {

		final String encryptionParamsPath = receivedParameters.getParam(CommandParameter.ENCRYPTION_PARAMS.getParameterName());
		final String p12Path = receivedParameters.getParam(CommandParameter.P12_PATH.getParameterName());
		final String trustedCAPath = receivedParameters.getParam(CommandParameter.TRUSTED_CA_PATH.getParameterName());

		String outputPath = null;
		if (receivedParameters.contains(OUT.getParameterName())) {
			outputPath = receivedParameters.getParam(OUT.getParameterName());
		}

		return new PrimesParametersContainer(p12Path, encryptionParamsPath, trustedCAPath, outputPath);
	}
}
