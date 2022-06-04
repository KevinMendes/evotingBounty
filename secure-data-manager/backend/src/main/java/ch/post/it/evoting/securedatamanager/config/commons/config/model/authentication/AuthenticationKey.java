/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication;

import java.util.List;
import java.util.Optional;

/**
 * Entity representing an authentication key of a voter
 */
public class AuthenticationKey {

	private final String value;

	private final Optional<List<String>> secrets;

	private AuthenticationKey(final String value, final Optional<List<String>> secrets) {
		this.value = value;
		this.secrets = secrets;
	}

	public static AuthenticationKey ofSecrets(final String value, final Optional<List<String>> secrets) {
		return new AuthenticationKey(value, secrets);
	}

	public String getValue() {
		return value;
	}

	public Optional<List<String>> getSecrets() {
		return secrets;
	}

}
