/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain.spring.batch;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link SensitiveJobParameter}.
 */
class SensitiveJobParameterTest {
	@Test
	void testToString() {
		final SensitiveJobParameter parameter = new SensitiveJobParameter("value", true);
		assertFalse(parameter.toString().contains("value"));
	}
}
