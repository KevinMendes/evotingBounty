/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple health check for a DataSource. If no validationQuery is supplied, it will only try to obtain a connection from, what should be, a
 * pool. If a validationQuery is supplied it will execute the query against the datasource. If we get an SqlException opening the connections or
 * executing the validationQuery we will report the database as unhealthy. To avoid coupling with any kind of framework, we use a datasource directly,
 * instead of EntityManager in JEE of Spring equivalent. This class also works in a polling-based fashion, ie, does not include any kind of
 * scheduling/recurring mechanism (like connection pools do) For this class to work as well as possible we should also configure the database pool to
 * actively check the connections it manages.
 */
public class DatabaseHealthCheck extends HealthCheck {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHealthCheck.class);

	private final DataSource dataSource;
	private final String user;
	private final String password;
	private final String validationQuery;

	public DatabaseHealthCheck(final DataSource dataSource) {
		this(dataSource, null, null, null);
	}

	public DatabaseHealthCheck(final DataSource dataSource, final String validationQuery) {
		this(dataSource, null, null, validationQuery);
	}

	public DatabaseHealthCheck(final DataSource dataSource, final String user, final String password, final String validationQuery) {
		Objects.requireNonNull(dataSource);
		this.dataSource = dataSource;
		this.user = user;
		this.password = password;
		this.validationQuery = validationQuery;
	}

	@Override
	protected HealthCheckResult check() {
		if (validationQuery != null) {
			try (Connection conn = getConnection(); Statement stmnt = conn.createStatement()) {
				// auto-close the statement, otherwise we may get ORA-01000: maximum open cursors exceeded
				// (with oracle)
				// closing the statement _SHOULD_ close the resultset, at least if using apache commons
				// connection pool
				stmnt.execute(validationQuery);
			} catch (SQLException e) {
				LOGGER.error("Database failed health check.", e);
				return HealthCheckResult.unhealthy("Database failed health check. Error: %s", e.getMessage());
			}
		}
		return HealthCheckResult.healthy();
	}

	private Connection getConnection() throws SQLException {
		return (user == null) ? dataSource.getConnection() : dataSource.getConnection(user, password);
	}
}
