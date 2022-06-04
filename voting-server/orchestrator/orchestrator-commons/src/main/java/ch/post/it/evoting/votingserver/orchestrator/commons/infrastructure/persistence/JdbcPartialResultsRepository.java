/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

/**
 * <p>
 * Implementation of {@link PartialResultsRepository} which stores partial results in a relational database.
 * <p>
 * Client is responsible for transaction management.
 */
@Singleton
@Local(PartialResultsRepository.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Jdbc
public final class JdbcPartialResultsRepository implements PartialResultsRepository<byte[]> {
	private DataSource dataSource;

	@Override
	public void deleteAll(final UUID correlationId) {
		final String sql = "delete from or_partial_results where correlation_id_hi = ? and correlation_id_lo = ?";
		try (final Connection connection = dataSource.getConnection(); final PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, correlationId.getMostSignificantBits());
			statement.setLong(2, correlationId.getLeastSignificantBits());
			statement.executeUpdate();
		} catch (final SQLException e) {
			throw new IllegalStateException("Failed to delete partial results.", e);
		}

	}

	@Override
	public boolean hasAll(final UUID correlationId, final int count) {
		try (final Connection connection = dataSource.getConnection()) {
			return count == count(connection, correlationId);
		} catch (final SQLException e) {
			throw new IllegalStateException("Failed to check partial result count.", e);
		}
	}

	@Override
	public List<byte[]> listAll(final UUID correlationId) {
		try (final Connection connection = dataSource.getConnection()) {
			return list(connection, correlationId);
		} catch (final SQLException e) {
			throw new IllegalStateException("Failed to list partial results.", e);
		}
	}

	@Override
	public Optional<List<byte[]>> listIfHasAll(final UUID correlationId, final int count) {
		try (final Connection connection = dataSource.getConnection()) {
			if (count != count(connection, correlationId)) {
				return Optional.empty();
			}
			return Optional.of(list(connection, correlationId));
		} catch (final SQLException e) {
			throw new IllegalStateException("Failed to list partial results.", e);
		}
	}

	@Override
	public void save(final UUID correlationId, final byte[] result) {
		final String sql = "insert into or_partial_results(correlation_id_hi, correlation_id_lo, result) values (?, ?, ?)";
		try (final Connection connection = dataSource.getConnection(); final PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, correlationId.getMostSignificantBits());
			statement.setLong(2, correlationId.getLeastSignificantBits());
			statement.setBytes(3, result);
			statement.executeUpdate();
		} catch (final SQLException e) {
			throw new IllegalStateException("Failed to save partial result.", e);
		}
	}

	/**
	 * Sets the data source.
	 *
	 * @param dataSource the data source.
	 */
	@Resource
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private int count(final Connection connection, final UUID correlationId) throws SQLException {
		final String sql = "select count(*) from or_partial_results where correlation_id_hi = ? and correlation_id_lo = ?";
		try (final PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, correlationId.getMostSignificantBits());
			statement.setLong(2, correlationId.getLeastSignificantBits());
			try (final ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return 0;
				}
				return resultSet.getInt(1);
			}
		}
	}

	private List<byte[]> list(final Connection connection, final UUID correlationId) throws SQLException {
		final List<byte[]> results = new ArrayList<>();
		final String sql = "select result from or_partial_results where correlation_id_hi = ? and correlation_id_lo = ?";
		try (final PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, correlationId.getMostSignificantBits());
			statement.setLong(2, correlationId.getLeastSignificantBits());
			try (final ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					results.add(resultSet.getBytes(1));
				}
			}
		}
		return results;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Optional<List<byte[]>> deleteListIfHasAll(final UUID correlationId, final int count) {
		final Optional<List<byte[]>> results = listIfHasAll(correlationId, count);
		if (results.isPresent()) {
			deleteAll(correlationId);
		}
		return results;
	}
}
