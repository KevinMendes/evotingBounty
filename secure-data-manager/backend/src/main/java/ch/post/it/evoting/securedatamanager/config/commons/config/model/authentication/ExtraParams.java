/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication;

import java.util.Optional;

/**
 * Entity representing an authentication key of a voter
 */
public class ExtraParams {

	private final Optional<String> value;

	private final Optional<String> alias;

	private ExtraParams(final Optional<String> value, final Optional<String> alias) {
		this.value = value;
		this.alias = alias;
	}

	public static ExtraParams ofChallenges(final Optional<String> value, final Optional<String> alias) {
		return new ExtraParams(value, alias);
	}

	public Optional<String> getValue() {
		return value;
	}

	public Optional<String> getAlias() {
		return alias;
	}
}
