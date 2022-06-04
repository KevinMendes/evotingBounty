/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomString;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGenerator;

/**
 * Service responsible of generating the start voting key
 */
@Service
@JobScope
public class StartVotingKeyService {

	protected static final int DESIRED_BASE = 2;

	/**
	 * The default charset for the SVK is 32 so 5 is the number of bits that cover this representation
	 *
	 * @see Constants#SVK_ALPHABET
	 */
	protected static final int NUMBER_OF_BITS_PER_SVK_CHARACTER = 5;

	private final AuthenticationKeyGenerator authenticationKeyGenerator;
	private final CryptoRandomString cryptoRandomString;
	private int startVotingKeyLength;

	public StartVotingKeyService(final AuthenticationKeyGenerator authenticationKeyGenerator) {
		this.authenticationKeyGenerator = authenticationKeyGenerator;
		final SecureRandomFactory secureRandomFactory = new SecureRandomFactory();
		cryptoRandomString = secureRandomFactory.createStringRandom(Constants.SVK_ALPHABET);
	}

	@PostConstruct
	public void init() {

		startVotingKeyLength = calculateStartVotingKeyLength();
	}

	public String generateStartVotingKey() throws GeneralCryptoLibException {

		return cryptoRandomString.nextRandom(getStartVotingKeyLength());

	}

	/**
	 * Calculates the necessary length for the start voting key to guarantee the same entropy as the authentication key
	 *
	 * @return length of start voting key
	 */
	private int calculateStartVotingKeyLength() {

		final int result;

		if (authenticationKeyGenerator.getAlphabet().length() == Constants.SVK_ALPHABET.length()) {
			// By default, the start voting key length equals the authentication key length.
			// We use a different start voting key length only
			// in case there are multiple extended authentication factors
			result = authenticationKeyGenerator.getSecretsLength();
		} else {

			final int alphabetLength = authenticationKeyGenerator.getAlphabet().length();
			final int secretsLength = authenticationKeyGenerator.getSecretsLength();

			final double computation = computeAuthenticationEntropy(alphabetLength, secretsLength);
			result = (int) computation;

		}

		return result;

	}

	private double computeAuthenticationEntropy(final int alphabetLength, final int secretsLength) {

		final double pow = Math.pow(alphabetLength, secretsLength);
		final double log = Math.log(pow) / Math.log(DESIRED_BASE);
		final double logValueRounded = Math.round(log);

		return Math.round(logValueRounded / NUMBER_OF_BITS_PER_SVK_CHARACTER);

	}

	/**
	 * Gets startVotingKeyLength.
	 *
	 * @return Value of startVotingKeyLength.
	 */
	public int getStartVotingKeyLength() {
		return startVotingKeyLength;
	}
}
