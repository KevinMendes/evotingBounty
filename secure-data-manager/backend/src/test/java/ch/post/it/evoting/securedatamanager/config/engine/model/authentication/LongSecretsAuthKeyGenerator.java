/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKey;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;

/**
 * Auth generator for testing purposes. Uses the maximum length greater than the allowed by the secure random String class
 */
public class LongSecretsAuthKeyGenerator implements AuthenticationKeyGenerator {

	public static final int SECRETS_LENGTH = 100;

	@Override
	public AuthenticationKey generateAuthKey(StartVotingKey startVotingKey) {
		final Optional<List<String>> secrets = Optional.of(Arrays.asList(startVotingKey.getValue()));

		return AuthenticationKey.ofSecrets(startVotingKey.getValue(), secrets);
	}

	@Override
	public int getSecretsLength() {
		return SECRETS_LENGTH;
	}
}
