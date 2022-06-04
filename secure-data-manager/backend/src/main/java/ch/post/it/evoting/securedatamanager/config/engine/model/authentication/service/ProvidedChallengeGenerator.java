/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ChallengeGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtraParams;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ProvidedChallenges;

public class ProvidedChallengeGenerator implements ChallengeGenerator {

	private final SequentialProvidedChallengeSource providedChallengeSource;

	public ProvidedChallengeGenerator(final SequentialProvidedChallengeSource providedChallengeSource) {
		this.providedChallengeSource = providedChallengeSource;
	}

	@Override
	public ExtraParams generateExtraParams() {

		final ProvidedChallenges providedChallenges = providedChallengeSource.next();

		final String alias = providedChallenges.getAlias();
		final List<String> challenges = providedChallenges.getChallenges();

		final String value = challenges.stream().collect(Collectors.joining());

		return ExtraParams.ofChallenges(Optional.of(value), Optional.of(alias));
	}
}
