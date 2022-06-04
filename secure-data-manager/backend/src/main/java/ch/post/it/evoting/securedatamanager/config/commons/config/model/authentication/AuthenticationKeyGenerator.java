/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication;

import ch.post.it.evoting.securedatamanager.commons.Constants;

/**
 * Interface for defining creation strategies of the authentication key
 */
public interface AuthenticationKeyGenerator {

	AuthenticationKey generateAuthKey(StartVotingKey startVotingKey);

	default int getSecretsLength() {
		return Constants.SVK_LENGTH;
	}

	default String getAlphabet() {
		return Constants.SVK_ALPHABET;
	}

}
