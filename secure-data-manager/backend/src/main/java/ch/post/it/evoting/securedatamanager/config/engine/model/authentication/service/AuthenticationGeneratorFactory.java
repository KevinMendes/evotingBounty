/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGeneratorStrategyType;

/**
 * Factory class to get the instance of the specific strategy to generate authentication data of the voter
 */
@Component
@JobScope
public class AuthenticationGeneratorFactory {

	public AuthenticationKeyGenerator createStrategy(final AuthenticationKeyGeneratorStrategyType authGeneratorStrategy) {

		switch (authGeneratorStrategy) {
		case SIMPLE:
			return new SimpleAuthenticationKeyGenerator();
		case SINGLESECRET:
			return new SingleSecretAuthenticationKeyGenerator();
		}
		throw new UnsupportedOperationException();
	}
}
