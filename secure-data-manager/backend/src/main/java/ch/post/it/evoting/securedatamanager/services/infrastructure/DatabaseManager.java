/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Object to manage a single local OrientDB database.
 * <p>
 * This implementation is only thread-safe if there is one DatabaseManager per database.
 * <p>
 */
public class DatabaseManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

	private final ODatabaseType type;
	private final String name;
	private final OrientDB orientDB;
	private final String password;
	private final String username;

	/**
	 * Create a DatabaseManager.
	 *
	 * @param orientDB the database management environment to use to connect to the database.
	 * @param dbType   the database type.
	 * @param dbName   the database name.
	 * @param username the admin username.
	 * @param password the admin password.
	 */
	DatabaseManager(final OrientDB orientDB, final ODatabaseType dbType, final String dbName, final String username, final String password) {
		checkNotNull(orientDB);
		checkNotNull(dbType);
		checkNotNull(dbName);
		checkNotNull(username);
		checkNotNull(password);

		this.orientDB = orientDB;
		this.type = dbType;
		this.name = dbName;
		this.username = username;
		this.password = password;
	}

	/**
	 * Creates the database if it does not exist. The encapsulated database user is added to administrative role if not already done.
	 */
	public synchronized void createDatabase() {
		if (!orientDB.exists(name)) {
			orientDB.create(name, type);
			try (final ODatabaseSession session = orientDB.open(name, OUser.ADMIN, OUser.ADMIN)) {
				// Set the user-defined password of the admin user.
				final ODocument admin = session.getUser().getDocument();
				admin.field("password", password);
				admin.save();
				LOGGER.info("Password for the admin user set.");

				final OSecurity security = session.getMetadata().getSecurity();
				saveRoleAndUser(security);
				session.commit();
			}
		}
	}

	private void saveRoleAndUser(final OSecurity security) {
		final ORole adminRole = security.getRole(ORole.ADMIN);
		if (adminRole == null) {
			throw new IllegalStateException("OrientDB has no Admin role.");
		}

		OUser user = security.getUser(username);
		if (user == null) {
			user = security.createUser(username, password, adminRole);
			if (user == null) {
				throw new IllegalStateException(String.format("Unable to create OrientDb User %s", username));
			}
		}
	}

	/**
	 * Drops the database if it exists.
	 */
	public synchronized void dropDatabase() {
		if (orientDB.exists(name)) {
			orientDB.drop(name);
		}
	}

	/**
	 * Opens the database, returning a {@link ODatabaseDocument} instance which is already activated on the current thread. Client is responsible for
	 * closing the returned object after use. Fails if the database does not exist.
	 *
	 * @return the database interface
	 */
	public ODatabaseDocument openDatabase() {
		return orientDB.open(name, username, password);
	}

}
