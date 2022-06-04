/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

import static ch.post.it.evoting.config.CommandParameter.P12_PATH;
import static ch.post.it.evoting.config.CommandParameter.SEED_PATH;
import static ch.post.it.evoting.config.CommandParameter.SEED_SIG_PATH;
import static ch.post.it.evoting.config.CommandParameter.TRUSTED_CA_PATH;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.config.Parameters;

@DisplayName("A ConfigEncryptionParametersAdapter")
class ConfigEncryptionParametersAdapterTest {

	private static final ConfigEncryptionParametersAdapter adapter = new ConfigEncryptionParametersAdapter();

	@Test
	@DisplayName("calling adapt with invalid Parameters throws IllegalArgumentException")
	void adaptWithInvalidParameters() {
		final Parameters invalidParameters = new Parameters();
		invalidParameters.addParam(P12_PATH.getParameterName(), "");
		invalidParameters.addParam(SEED_PATH.getParameterName(), "");
		invalidParameters.addParam(TRUSTED_CA_PATH.getParameterName(), "");
		invalidParameters.addParam(SEED_SIG_PATH.getParameterName(), "");

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> adapter.adapt(invalidParameters));
		assertEquals("The p12_path must be informed.", exception.getMessage());
	}

	@Test
	@DisplayName("calling adapt with valid Parameters gives expected holder")
	void adaptWithValidParameters() {
		final Parameters validParameters = new Parameters();
		validParameters.addParam(P12_PATH.getParameterName(), "p12path");
		validParameters.addParam(SEED_PATH.getParameterName(), "seedPath");
		validParameters.addParam(TRUSTED_CA_PATH.getParameterName(), "trustedCAPath");
		validParameters.addParam(SEED_SIG_PATH.getParameterName(), "seedSignaturePath");

		final ConfigEncryptionParametersContainer holder = adapter.adapt(validParameters);

		assertAll(() -> assertEquals(Paths.get("p12path"), holder.getP12Path()), () -> assertEquals(Paths.get("seedPath"), holder.getSeedPath()),
				() -> assertEquals(Paths.get("trustedCAPath"), holder.getTrustedCAPath()),
				() -> assertEquals(Paths.get("seedSignaturePath"), holder.getSeedSignaturePath()));
	}
}
