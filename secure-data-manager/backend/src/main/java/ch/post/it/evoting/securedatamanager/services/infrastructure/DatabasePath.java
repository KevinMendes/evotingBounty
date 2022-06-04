/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The class with the database path as field.
 */
@Component
public class DatabasePath {

	@Value("${database.path}")
	private String dbPath;

	/**
	 * Returns the current value of the field DB_PATH.
	 *
	 * @return Returns the DB_PATH.
	 */
	public String getDatabasePath() {
		return dbPath;
	}

	/**
	 * Sets the value of the field DB_PATH.
	 *
	 * @param dbPath The DB_PATH to set.
	 */
	public void setDatabasePath(final String dbPath) {
		this.dbPath = dbPath;
	}
}
