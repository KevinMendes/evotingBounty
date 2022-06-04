/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Fixture to manage the database in tests of repositories.
 */
public final class DatabaseFixture {
	private static final String TYPE = "memory";
	private static final String PATH = "";
	private static String name;
	private DatabaseManagerFactory factory;
	private DatabaseManager databaseManager;

	public DatabaseFixture(final Class<?> testClass) {
		name = testClass.getSimpleName();
	}

	/**
	 * Creates documents from the specified resource. The content of the resource must be a valid JSON array of JSON objects to be inserted into the
	 * database.
	 *
	 * @param entityName the entity name
	 * @param resource   the resource
	 * @throws IOException I/O error occurred
	 * @throws OException  failed to save the document.
	 */
	public void createDocuments(final String entityName, final URL resource) throws IOException, OException {
		final JsonArray array;
		try (final JsonReader reader = Json.createReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
			array = reader.readArray();
		}
		try (final ODatabaseDocument database = databaseManager.openDatabase()) {
			for (final JsonValue value : array) {
				final ODocument document = new ODocument(entityName);
				document.fromJSON(value.toString());
				database.save(document);
			}
		}
	}

	/**
	 * Returns the database manager.
	 *
	 * @return the database manager.
	 */
	public DatabaseManager databaseManager() {
		return databaseManager;
	}

	/**
	 * Creates a {@link DatabaseManager} instance and a database.
	 *
	 * @throws OException failed to setup.
	 */
	public void setUp() throws OException {
		factory = new DatabaseManagerFactory(TYPE, PATH, "src/test/resources/config/database_password.properties");
		databaseManager = factory.newDatabaseManager(name);
		databaseManager.createDatabase();
	}

	/**
	 * Drops the database and clear the {@link DatabaseManager} instance.
	 */
	public void tearDown() throws OException {
		databaseManager.dropDatabase();
		databaseManager = null;
		factory.close();
	}
}
