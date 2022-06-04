/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKey;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.SingleSecretAuthenticationKeyGenerator;

class SingleSecretStrategyTest {

	private final AuthenticationKeyGenerator singleSecretStrategy = new SingleSecretAuthenticationKeyGenerator();
	StartVotingKey ignored = null;
	Set<String> randomStrings = new HashSet<>();

	@Test
	void generateNotNull() {

		final AuthenticationKey authenticationKey = singleSecretStrategy.generateAuthKey(ignored);
		assertNotNull(authenticationKey);

	}

	@Test
	void generatesRandomString() {
		for (int i = 0; i < 1000; i++) {
			final AuthenticationKey authenticationKey = singleSecretStrategy.generateAuthKey(ignored);
			final String authKeyValue = authenticationKey.getValue();
			final boolean added = randomStrings.add(authKeyValue);
			// make sure each time is a different string
			assertTrue(added);
		}
	}

	@Test
	void havePresentSecret() {

		final AuthenticationKey authenticationKey = singleSecretStrategy.generateAuthKey(ignored);
		final Optional<List<String>> optionalSecrets = authenticationKey.getSecrets();
		assertTrue(optionalSecrets.isPresent());
	}

	@Test
	void haveSingleSecret() {

		final AuthenticationKey authenticationKey = singleSecretStrategy.generateAuthKey(ignored);
		final Optional<List<String>> optionalSecrets = authenticationKey.getSecrets();
		final List<String> secrets = optionalSecrets.get();
		assertEquals(1, secrets.size());
	}

	@Test
	void generatesRandomStringWithCorrectSize() {
		final AuthenticationKey authenticationKey = singleSecretStrategy.generateAuthKey(ignored);
		final String authKeyValue = authenticationKey.getValue();
		assertEquals(Constants.SVK_LENGTH, authKeyValue.length());

	}
}
