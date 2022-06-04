/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKey;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.SimpleAuthenticationKeyGenerator;

class SimpleStrategyTest {

	private final SimpleAuthenticationKeyGenerator simpleStrategy = new SimpleAuthenticationKeyGenerator();
	private final StartVotingKey startVotingKey = StartVotingKey.ofValue("68n7vr7znmrdmq2hkpj7");

	@Test
	void generateNotNull() {

		final AuthenticationKey authenticationKey = simpleStrategy.generateAuthKey(startVotingKey);
		assertNotNull(authenticationKey);

	}

	@Test
	void haveValueEqualToSVK() {

		final AuthenticationKey authenticationKey = simpleStrategy.generateAuthKey(startVotingKey);
		final String authKeyValue = authenticationKey.getValue();
		assertEquals(startVotingKey.getValue(), authKeyValue);

	}

	@Test
	void havePresentSecret() {

		final AuthenticationKey authenticationKey = simpleStrategy.generateAuthKey(startVotingKey);
		final Optional<List<String>> optionalSecrets = authenticationKey.getSecrets();
		assertTrue(optionalSecrets.isPresent());
	}

	@Test
	void haveSingleSecret() {

		final AuthenticationKey authenticationKey = simpleStrategy.generateAuthKey(startVotingKey);
		final Optional<List<String>> optionalSecrets = authenticationKey.getSecrets();
		final List<String> secrets = optionalSecrets.get();
		assertEquals(1, secrets.size());

	}

	@Test
	void haveSecretEqualToSVK() {

		final AuthenticationKey authenticationKey = simpleStrategy.generateAuthKey(startVotingKey);
		final Optional<List<String>> optionalSecrets = authenticationKey.getSecrets();
		final List<String> secrets = optionalSecrets.get();
		assertEquals(startVotingKey.getValue(), secrets.get(0));

	}

}
