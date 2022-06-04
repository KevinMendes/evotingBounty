/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomString;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.SingleSecretAuthenticationKeyGeneratorException;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKey;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;

public class SingleSecretAuthenticationKeyGenerator implements AuthenticationKeyGenerator {

	private final CryptoRandomString cryptoRandomString;

	public SingleSecretAuthenticationKeyGenerator() {

		SecureRandomFactory secureRandomFactory = new SecureRandomFactory();
		cryptoRandomString = secureRandomFactory.createStringRandom(Constants.SVK_ALPHABET);
	}

	/**
	 * @see AuthenticationKeyGenerator#generateAuthKey(StartVotingKey)
	 */
	@Override
	public AuthenticationKey generateAuthKey(final StartVotingKey startVotingKey) {

		String svk2;
		try {
			svk2 = cryptoRandomString.nextRandom(Constants.SVK_LENGTH);
		} catch (GeneralCryptoLibException e) {
			throw new SingleSecretAuthenticationKeyGeneratorException("Error trying to generate SVK2.", e);
		}

		final Optional<List<String>> secrets = Optional.of(Collections.singletonList(svk2));

		return AuthenticationKey.ofSecrets(svk2, secrets);
	}
}
