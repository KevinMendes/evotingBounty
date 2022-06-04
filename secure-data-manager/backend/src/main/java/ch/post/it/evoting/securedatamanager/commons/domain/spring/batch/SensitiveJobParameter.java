/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain.spring.batch;

import org.springframework.batch.core.JobParameter;

/**
 * This class overrides toString of JobParameter so that when Spring Batch prints this parameter, it
 * will not reveal its value
 */
public class SensitiveJobParameter extends JobParameter {

	public SensitiveJobParameter(String parameter, boolean identifying) {
		super(parameter, identifying);
	}

	@Override
	public String toString() {
		return "******";
	}
}
