/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain.spring.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

@ExtendWith(MockitoExtension.class)
class SensitiveAwareJobParametersBuilderTest {

	@InjectMocks
	SensitiveAwareJobParametersBuilder sut;

	@Test
	void testAddSensitiveString() {
		final JobParameters parameters = new SensitiveAwareJobParametersBuilder().addSensitiveString("sensitive", "value").toJobParameters();
		final JobParameter parameter = parameters.getParameters().get("sensitive");
		assertEquals("value", parameter.getValue());
		assertFalse(parameter.toString().contains("value"));
	}

	@Test
	void returnHiddenValueForSensitiveParameter() {

		final String sensitiveParameterKey = "sensitiveParameterKey";
		final String sensitiveParameterValue = "sensitiveParameterValue";
		sut.addSensitiveString(sensitiveParameterKey, sensitiveParameterValue);

		final JobParameters jobParameters = sut.toJobParameters();

		// note: "jobParameters.getString" calls toString on the parameter. In production code we have
		// to be aware of this
		// otherwise we may get the "hidden" value instead of the real parameter value. For
		// sensitiveParameters we MUST
		// use the non-typed "get" and call getValue (or if using the
		// we may need to add a method to SensitiveParameter to get the underlying value
		assertNotEquals(sensitiveParameterValue, jobParameters.getString(sensitiveParameterKey));
		// this gets the underlying value without calling toString
		assertEquals(sensitiveParameterValue, jobParameters.getParameters().get(sensitiveParameterKey).getValue());
	}

	@Test
	void returnCorrectValueForIdentifyingParameter() {

		final String identifyingParamKey = "sensitiveParameterKey1";
		final String identifyingParamValue = "sensitiveParameterValue1";
		final String nonIdentifyingParamKey = "sensitiveParameterKey2";
		final String nonIdentifyingParamValue = "sensitiveParameterValue2";
		sut.addSensitiveString(identifyingParamKey, identifyingParamValue);
		sut.addSensitiveString(nonIdentifyingParamKey, nonIdentifyingParamValue, false);

		final JobParameters jobParameters = sut.toJobParameters();

		assertTrue(jobParameters.getParameters().get(identifyingParamKey).isIdentifying());
		assertFalse(jobParameters.getParameters().get(nonIdentifyingParamKey).isIdentifying());

	}
}
