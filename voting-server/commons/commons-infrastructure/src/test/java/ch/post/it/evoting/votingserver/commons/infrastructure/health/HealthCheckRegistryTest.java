/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthCheckRegistryTest {

	private final HealthCheckRegistry sut = new HealthCheckRegistry();

	@Test
	void whenRegisterHealthCheckThenItShouldRunHealthCheck() {

		// given
		HealthCheck mockHealthCheck = mock(HealthCheck.class);

		// when
		sut.register(HealthCheckValidationType.DATABASE, mockHealthCheck);
		sut.runAllChecks();

		// then
		verify(mockHealthCheck, times(1)).execute();
	}

	@Test
	void whenUnregisterHealthCheckThenItShouldNotRunHealthCheck() {
		// given
		HealthCheck mockHealthCheck = mock(HealthCheck.class);

		sut.register(HealthCheckValidationType.LOGGING_INITIALIZED, mockHealthCheck);
		// when

		sut.unregister(HealthCheckValidationType.LOGGING_INITIALIZED);
		sut.runAllChecks();

		// then
		verify(mockHealthCheck, times(0)).execute();
	}

	@Test
	void testRunOnlySomeChecks() {

		// given
		HealthCheck mockHealthCheck = mock(HealthCheck.class);

		sut.register(HealthCheckValidationType.LOGGING_INITIALIZED, mockHealthCheck);
		sut.register(HealthCheckValidationType.DATABASE, mockHealthCheck);

		sut.runChecksDifferentFrom(Collections.singletonList(HealthCheckValidationType.LOGGING_INITIALIZED));

		// then
		verify(mockHealthCheck, times(1)).execute();
	}

}
