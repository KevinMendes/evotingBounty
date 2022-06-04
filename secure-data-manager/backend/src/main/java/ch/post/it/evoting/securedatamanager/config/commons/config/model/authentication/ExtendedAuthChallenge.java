/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication;

import java.util.Optional;

/**
 * Challenge information that may be used for the extended authentication phase
 */
public class ExtendedAuthChallenge {

	private final AuthenticationDerivedElement derivedChallenges;

	private final Optional<String> alias;

	private final byte[] salt;

	public ExtendedAuthChallenge(final AuthenticationDerivedElement derivedChallenges, final Optional<String> alias, final byte[] salt) {
		super();
		this.derivedChallenges = derivedChallenges;
		this.alias = alias;
		this.salt = salt;
	}

	public static ExtendedAuthChallenge of(final AuthenticationDerivedElement derivedChallenges, final Optional<String> alias, final byte[] salt) {
		return new ExtendedAuthChallenge(derivedChallenges, alias, salt);
	}

	public AuthenticationDerivedElement getDerivedChallenges() {
		return derivedChallenges;
	}

	public byte[] getSalt() {
		return salt;
	}

	public Optional<String> getAlias() {
		return alias;
	}

}
