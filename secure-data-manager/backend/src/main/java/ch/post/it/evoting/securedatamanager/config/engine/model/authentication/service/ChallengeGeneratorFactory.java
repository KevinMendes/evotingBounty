/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ChallengeGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.ChallengeGeneratorStrategyType;

/**
 * Factory class to get the instance of the specific strategy to generate authentication challenge data of the voter
 */
@Component
@JobScope
public class ChallengeGeneratorFactory {

	@Autowired
	private SequentialProvidedChallengeSource providedChallengeSource;

	public ChallengeGenerator createStrategy(final ChallengeGeneratorStrategyType challengeGeneratorStrategy) {

		switch (challengeGeneratorStrategy) {
		case NONE:
			return new NoneChallengeGenerator();
		case PROVIDED:
			return new ProvidedChallengeGenerator(providedChallengeSource);
		}
		throw new UnsupportedOperationException();
	}
}
