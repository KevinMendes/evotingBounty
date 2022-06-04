/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain.spring.batch;

import org.springframework.batch.core.JobParametersBuilder;

/**
 * This class overrides extends JobParametersBuilder to allow the addition of "sensitive value" job parameters
 */
public class SensitiveAwareJobParametersBuilder extends JobParametersBuilder {

	public JobParametersBuilder addSensitiveString(final String key, final String parameter) {
		final SensitiveJobParameter sensitiveJobParameter = new SensitiveJobParameter(parameter, true);
		super.addParameter(key, sensitiveJobParameter);
		return this;
	}

	public JobParametersBuilder addSensitiveString(final String key, final String parameter, final boolean identifying) {
		final SensitiveJobParameter sensitiveJobParameter = new SensitiveJobParameter(parameter, identifying);
		super.addParameter(key, sensitiveJobParameter);
		return this;
	}
}
