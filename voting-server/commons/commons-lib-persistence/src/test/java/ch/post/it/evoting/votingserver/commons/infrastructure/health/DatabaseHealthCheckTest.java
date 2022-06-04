/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthCheckTest {

	@Mock
	private DataSource mockDataSource;

	@Test
	void whenDatabaseIsAvailableShouldReturnHealthyWithoutValidationQuery() {
		// given
		final DatabaseHealthCheck sut = new DatabaseHealthCheck(mockDataSource);

		// when
		final HealthCheck.HealthCheckResult result = sut.execute();

		// then
		assertTrue(result.getHealthy());
	}

	@Test
	void whenDatabaseIsAvailableShouldReturnHealthyWithValidationQuery() throws Exception {
		// given
		final DatabaseHealthCheck sut = new DatabaseHealthCheck(mockDataSource, null, null, "select 1");
		final Connection mockConnection = mock(Connection.class);
		final Statement mockStatement = mock(Statement.class);
		when(mockStatement.execute(anyString())).thenReturn(true);
		when(mockConnection.createStatement()).thenReturn(mockStatement);
		when(mockDataSource.getConnection()).thenReturn(mockConnection);

		// when
		final HealthCheck.HealthCheckResult result = sut.execute();

		// then
		assertTrue(result.getHealthy());
	}

	@Test
	void whenDatabaseIsNotAvailableShouldReturnHealthyWithValidationQuery() throws Exception {
		// given
		final DatabaseHealthCheck sut = new DatabaseHealthCheck(mockDataSource, null, null, "select 1");
		final Connection mockConnection = mock(Connection.class);
		final Statement mockStatement = mock(Statement.class);
		doThrow(SQLException.class).when(mockStatement).execute(anyString());
		when(mockConnection.createStatement()).thenReturn(mockStatement);
		when(mockDataSource.getConnection()).thenReturn(mockConnection);

		// when
		final HealthCheck.HealthCheckResult result = sut.execute();

		// then
		assertFalse(result.getHealthy());
	}
}
