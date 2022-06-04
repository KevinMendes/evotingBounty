/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthCheckTest {

	private final HealthCheck sut = spy(HealthCheck.class);

	@Test
	void whenCheckThrowsResultShouldBeUnhealthy() {

		// given
		doThrow(new RuntimeException("exception")).when(sut).check();

		// when
		HealthCheck.HealthCheckResult result = sut.execute();

		// then
		assertFalse(result.getHealthy());
	}

	@Test
	void executeShouldReturnSameResultAsCheck() {
		HealthCheck.HealthCheckResult mockResult = mock(HealthCheck.HealthCheckResult.class);
		when(mockResult.getHealthy()).thenReturn(false);

		// given
		when(sut.check()).thenReturn(mockResult);

		// when
		HealthCheck.HealthCheckResult result = sut.execute();

		// then
		assertEquals(mockResult.getHealthy(), result.getHealthy());
	}
}
