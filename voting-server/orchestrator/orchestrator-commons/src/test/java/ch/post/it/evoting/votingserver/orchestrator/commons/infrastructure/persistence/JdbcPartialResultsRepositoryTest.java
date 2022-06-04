/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests of {@link JdbcPartialResultsRepository}.
 */
public class JdbcPartialResultsRepositoryTest {
	private static final UUID CORRELATION_ID = new UUID(1, 2);

	private static final byte[] RESULT1 = { 3 };

	private static final byte[] RESULT2 = { 4 };

	private ResultSet resultSet;

	private PreparedStatement statement;

	private Connection connection;

	private DataSource dataSource;

	private JdbcPartialResultsRepository repository;

	@Before
	public void setUp() throws SQLException {
		resultSet = mock(ResultSet.class);

		statement = mock(PreparedStatement.class);
		when(statement.executeQuery()).thenReturn(resultSet);

		connection = mock(Connection.class);
		when(connection.prepareStatement(anyString())).thenReturn(statement);

		dataSource = mock(DataSource.class);
		when(dataSource.getConnection()).thenReturn(connection);

		repository = new JdbcPartialResultsRepository();
		repository.setDataSource(dataSource);
	}

	@Test
	public void testDeleteAll() throws SQLException {
		repository.deleteAll(CORRELATION_ID);

		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeUpdate();
		verify(statement).close();
		verify(connection).close();
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteAllSQLException() throws SQLException {
		when(statement.executeUpdate()).thenThrow(new SQLException("test"));

		try {
			repository.deleteAll(CORRELATION_ID);
		} finally {
			verify(statement).close();
			verify(connection).close();
		}
	}

	@Test
	public void testHasAll() throws SQLException {
		when(resultSet.getInt(1)).thenReturn(2);
		when(resultSet.next()).thenReturn(true, false);

		assertTrue(repository.hasAll(CORRELATION_ID, 2));
		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeQuery();
		verify(resultSet).close();
		verify(statement).close();
		verify(connection).close();
	}

	@Test
	public void testHasAllNotAll() throws SQLException {
		when(resultSet.getInt(1)).thenReturn(1);
		when(resultSet.next()).thenReturn(true, false);

		assertFalse(repository.hasAll(CORRELATION_ID, 2));

		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeQuery();
		verify(resultSet).close();
		verify(statement).close();
		verify(connection).close();
	}

	@Test
	public void testHasAllNotFound() throws SQLException {
		when(resultSet.getInt(1)).thenReturn(0);
		when(resultSet.next()).thenReturn(true, false);

		assertFalse(repository.hasAll(CORRELATION_ID, 2));

		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeQuery();
		verify(resultSet).close();
		verify(statement).close();
		verify(connection).close();
	}

	@Test(expected = IllegalStateException.class)
	public void testHasAllSQLException() throws SQLException {
		when(resultSet.getInt(1)).thenThrow(new SQLException("test"));
		when(resultSet.next()).thenReturn(true, false);

		try {
			repository.hasAll(CORRELATION_ID, 2);
		} finally {
			verify(resultSet).close();
			verify(statement).close();
			verify(connection).close();
		}
	}

	@Test
	public void testListAll() throws SQLException {
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getBytes(1)).thenReturn(RESULT1, RESULT2);

		List<byte[]> results = repository.listAll(CORRELATION_ID);

		assertEquals(2, results.size());
		assertArrayEquals(RESULT1, results.get(0));
		assertArrayEquals(RESULT2, results.get(1));
		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeQuery();
		verify(resultSet).close();
		verify(statement).close();
		verify(connection).close();
	}

	@Test
	public void testListAllNotFound() throws SQLException {
		when(resultSet.next()).thenReturn(false);

		List<byte[]> results = repository.listAll(CORRELATION_ID);

		assertTrue(results.isEmpty());
		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeQuery();
		verify(resultSet).close();
		verify(statement).close();
		verify(connection).close();
	}

	@Test(expected = IllegalStateException.class)
	public void testListAllSQLException() throws SQLException {
		when(resultSet.next()).thenReturn(true, false);
		when(resultSet.getBytes(1)).thenThrow(new SQLException("test"));

		try {
			repository.listAll(CORRELATION_ID);
		} finally {
			verify(resultSet).close();
			verify(statement).close();
			verify(connection).close();
		}
	}

	@Test
	public void testListIfHasAll() throws SQLException {
		when(resultSet.getInt(1)).thenReturn(2);
		when(resultSet.next()).thenReturn(true, true, true, false);
		when(resultSet.getBytes(1)).thenReturn(RESULT1, RESULT2);

		List<byte[]> results = repository.listIfHasAll(CORRELATION_ID, 2).get();

		assertEquals(2, results.size());
		assertArrayEquals(RESULT1, results.get(0));
		assertArrayEquals(RESULT2, results.get(1));
		verify(statement, times(2)).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement, times(2)).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement, times(2)).executeQuery();
		verify(resultSet, times(2)).close();
		verify(statement, times(2)).close();
		verify(connection).close();
	}

	@Test
	public void testListIfHasAllNotAll() throws SQLException {
		when(resultSet.getInt(1)).thenReturn(1);
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getBytes(1)).thenReturn(RESULT1);

		assertFalse(repository.listIfHasAll(CORRELATION_ID, 2).isPresent());

		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeQuery();
		verify(resultSet).close();
		verify(statement).close();
		verify(connection).close();
	}

	@Test
	public void testListIfHasAllNotFound() throws SQLException {
		when(resultSet.getInt(1)).thenReturn(0);
		when(resultSet.next()).thenReturn(true, false);

		assertFalse(repository.listIfHasAll(CORRELATION_ID, 2).isPresent());

		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).executeQuery();
		verify(resultSet).close();
		verify(statement).close();
		verify(connection).close();
	}

	@Test(expected = IllegalStateException.class)
	public void testListIfHasAllSQLException() throws SQLException {
		when(resultSet.getInt(1)).thenReturn(2);
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getBytes(1)).thenThrow(new SQLException("test"));

		try {
			repository.listIfHasAll(CORRELATION_ID, 2).isPresent();
		} finally {
			verify(resultSet, times(2)).close();
			verify(statement, times(2)).close();
			verify(connection).close();
		}
	}

	@Test
	public void testSave() throws SQLException {
		repository.save(CORRELATION_ID, RESULT1);

		verify(statement).setLong(1, CORRELATION_ID.getMostSignificantBits());
		verify(statement).setLong(2, CORRELATION_ID.getLeastSignificantBits());
		verify(statement).setBytes(3, RESULT1);
		verify(statement).executeUpdate();
		verify(statement).close();
		verify(connection).close();
	}

	@Test(expected = IllegalStateException.class)
	public void testSaveSQLException() throws SQLException {
		when(statement.executeUpdate()).thenThrow(new SQLException("test"));
		try {
			repository.save(CORRELATION_ID, RESULT1);
		} finally {
			verify(statement).close();
			verify(connection).close();
		}
	}
}
