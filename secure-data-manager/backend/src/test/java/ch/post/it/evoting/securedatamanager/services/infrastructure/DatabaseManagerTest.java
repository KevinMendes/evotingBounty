/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OUser;

/**
 * Tests of {@link DatabaseManager}.
 */
class DatabaseManagerTest {

	private static final ODatabaseType TYPE = ODatabaseType.MEMORY;
	private static final String PATH = "";
	private static final String NAME = DatabaseManager.class.getSimpleName();
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	private DatabaseManager manager;
	private OrientDB orientDB;

	@BeforeEach
	void setUp() {
		this.orientDB = new OrientDB(TYPE.name().toLowerCase() + ":" + PATH, OrientDBConfig.defaultConfig());
		manager = new DatabaseManager(orientDB, TYPE, NAME, USERNAME, PASSWORD);
	}

	@AfterEach
	void tearDown() {
		manager.dropDatabase();
		this.orientDB.close();
	}

	@Test
	void testCreateDatabase() {
		manager.createDatabase();
		assertTrue(orientDB.exists(NAME));
		try (ODatabaseSession session = orientDB.open(NAME, USERNAME, PASSWORD)) {
			OSecurity security = session.getMetadata().getSecurity();
			ORole role = security.getRole(ORole.ADMIN);
			assertTrue(role.allow(ResourceGeneric.BYPASS_RESTRICTED, null, ORole.PERMISSION_ALL));
			OUser user = security.getUser(USERNAME);
			assertTrue(user.checkPassword(PASSWORD));
			assertTrue(user.getRoles().contains(role));
		}
	}

	@Test
	void testCreateDatabaseExisting() {
		orientDB.create(NAME, ODatabaseType.MEMORY);
		manager.createDatabase();

		try (ODatabaseSession session = orientDB.open(NAME, OUser.ADMIN, OUser.ADMIN)) {
			assertNull(session.getMetadata().getSecurity().getUser(USERNAME));
		}
	}

	@Test
	void testDropDatabase() {
		manager.createDatabase();
		manager.dropDatabase();
		assertFalse(orientDB.exists(NAME));
	}

	@Test
	void testDropDatabaseNonExisting() {
		assertDoesNotThrow(() -> manager.dropDatabase());
	}

	@Test
	void testOpenDatabase() {
		manager.createDatabase();
		try (ODatabaseDocument database = manager.openDatabase()) {
			assertNotNull(database.getMetadata().getSecurity().getUser(USERNAME));
		}
	}

}
