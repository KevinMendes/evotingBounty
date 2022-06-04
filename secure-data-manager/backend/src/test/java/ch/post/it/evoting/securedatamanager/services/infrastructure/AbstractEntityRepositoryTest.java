/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;

/**
 * Tests of {@link AbstractEntityRepository}.
 */
class AbstractEntityRepositoryTest {

	private static final String ENTITY_NAME = "Test";

	private DatabaseFixture fixture;
	private DatabaseManager manager;
	private AbstractEntityRepository repository;

	@BeforeEach
	void setUp() throws OException, IOException {
		fixture = new DatabaseFixture(getClass());
		fixture.setUp();
		manager = fixture.databaseManager();
		repository = new TestableEntityRepository(manager);
		repository.initialize();
		final URL resource = getClass().getResource(getClass().getSimpleName() + ".json");
		fixture.createDocuments(ENTITY_NAME, resource);
	}

	@AfterEach
	void tearDown() {
		fixture.tearDown();
	}

	@Test
	void testDeleteDocuments() {
		final String sql = "delete from " + ENTITY_NAME + " where id = :id and age = :age";
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", "3");
		parameters.put("age", 3);
		repository.deleteDocuments(sql, parameters);
		try (final ODatabaseDocument database = manager.openDatabase()) {
			final ORecordIteratorClass<ODocument> iterator = database.browseClass(ENTITY_NAME);
			assertTrue(iterator.hasNext());
			assertODocumentCorrect(iterator.next());
			assertFalse(iterator.hasNext());
		}
	}

	@Test
	void testDeleteDocumentsNoMatching() {
		final String sql = "delete from " + ENTITY_NAME + " where id = :id and age = :age";
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", "1");
		parameters.put("age", 2);
		repository.deleteDocuments(sql, parameters);
		try (final ODatabaseDocument database = manager.openDatabase()) {
			assertTrue(database.browseClass(ENTITY_NAME).hasNext());
		}
	}

	@Test
	void testDeleteMapOfStringObject() throws OException {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "3");
		criteria.put("age", 3);
		criteria.put("electionEvent.alias", "alias4");
		repository.delete(criteria);
		try (final ODatabaseDocument database = manager.openDatabase()) {
			final ORecordIteratorClass<ODocument> iterator = database.browseClass(ENTITY_NAME);
			assertTrue(iterator.hasNext());
			assertODocumentCorrect(iterator.next());
			assertFalse(iterator.hasNext());
		}
	}

	@Test
	void testDeleteMapOfStringObjectNoMatching() throws OException {
		repository.delete(singletonMap("age", 2));
		try (final ODatabaseDocument database = manager.openDatabase()) {
			assertTrue(database.browseClass(ENTITY_NAME).hasNext());
		}
	}

	@Test
	void testDeleteString() throws OException {
		repository.delete("3");
		try (final ODatabaseDocument database = manager.openDatabase()) {
			final ORecordIteratorClass<ODocument> iterator = database.browseClass(ENTITY_NAME);
			assertTrue(iterator.hasNext());
			assertODocumentCorrect(iterator.next());
			assertFalse(iterator.hasNext());
		}
	}

	@Test
	void testDeleteStringNoMatching() throws OException {
		repository.delete("2");
		try (final ODatabaseDocument database = manager.openDatabase()) {
			final ORecordIteratorClass<ODocument> iterator = database.browseClass(ENTITY_NAME);
			assertTrue(iterator.hasNext());
			ODocument document = iterator.next();
			if ("1".equals(document.field(JsonConstants.ID, String.class))) {
				assertODocumentCorrect(document);
			}
			assertTrue(iterator.hasNext());
			document = iterator.next();
			if ("1".equals(document.field(JsonConstants.ID, String.class))) {
				assertODocumentCorrect(document);
			}
			assertFalse(iterator.hasNext());
		}
	}

	@Test
	void testFindDocumentMapOfStringObject() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 1);
		criteria.put("electionEvent.alias", null);
		assertODocumentCorrect(repository.findDocument(criteria));
	}

	@Test
	void testFindDocumentMapOfStringObjectNoMatching() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 2);
		criteria.put("electionEvent.alias", null);
		assertNull(repository.findDocument(criteria));
	}

	@Test
	void testFindDocumentString() {
		assertODocumentCorrect(repository.findDocument("1"));
	}

	@Test
	void testFindDocumentStringNoMatching() {
		assertNull(repository.findDocument("2"));
	}

	@Test
	void testFindMapOfStringObject() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 1);
		criteria.put("electionEvent.alias", null);
		assertJsonObjectCorrect(JsonUtils.getJsonObject(repository.find(criteria)));
	}

	@Test
	void testFindMapOfStringObjectNoMatching() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 2);
		criteria.put("electionEvent.alias", null);
		assertTrue(JsonUtils.getJsonObject(repository.find(criteria)).isEmpty());
	}

	@Test
	void testFindString() {
		assertJsonObjectCorrect(JsonUtils.getJsonObject(repository.find("1")));
	}

	@Test
	void testFindStringNoMatching() {
		assertTrue(JsonUtils.getJsonObject(repository.find("2")).isEmpty());
	}

	@Test
	void testGetDocument() {
		assertODocumentCorrect(repository.getDocument("1"));
	}

	@Test
	void testGetDocumentMissing() {
		assertThrows(OException.class, () -> repository.getDocument("2"));
	}

	@Test
	void testInitialize() {
		try (final ODatabaseDocument database = manager.openDatabase()) {
			final OSchema schema = database.getMetadata().getSchema();
			final OClass entityClass = schema.getClass(ENTITY_NAME);
			final OProperty property = entityClass.getProperty(JsonConstants.ID);
			assertEquals(OType.STRING, property.getType());
			assertTrue(property.isMandatory());
			assertTrue(property.isNotNull());
			final Collection<OIndex> indexes = property.getAllIndexes();
			assertEquals(1, indexes.size());
			final OIndex index = indexes.iterator().next();
			assertEquals(ENTITY_NAME + "_Unique", index.getName());
			assertEquals(INDEX_TYPE.UNIQUE.name(), index.getType());
		}
	}

	@Test
	void testList() {
		final String json = repository.list();
		final JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertEquals(2, array.size());
		for (final JsonValue value : array) {
			final JsonObject object = ((JsonObject) value);
			if ("1".equals(object.getString(JsonConstants.ID))) {
				assertJsonObjectCorrect(object);
			}
		}
	}

	@Test
	void testListDocuments() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 1);
		criteria.put("electionEvent.alias", null);
		final List<ODocument> documents = repository.listDocuments(criteria);
		assertEquals(1, documents.size());
		assertODocumentCorrect(documents.get(0));
	}

	@Test
	void testListDocumentsNoMatching() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 2);
		criteria.put("electionEvent.alias", null);
		assertTrue(repository.listDocuments(criteria).isEmpty());
	}

	@Test
	void testListMapOfStringObject() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 1);
		criteria.put("electionEvent.alias", null);
		final String json = repository.list(criteria);
		final JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertEquals(1, array.size());
		assertJsonObjectCorrect(array.getJsonObject(0));
	}

	@Test
	void testListMapOfStringObjectNoMatching() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("id", "1");
		criteria.put("age", 2);
		criteria.put("electionEvent.alias", null);
		final String json = repository.list(criteria);
		assertTrue(JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT).isEmpty());
	}

	@Test
	void testNewDocument() {
		assertTrue(repository.newDocument().isEmpty());
	}

	@Test
	void testNewDocumentString() {
		final String json = repository.find("1");
		assertODocumentCorrect(repository.newDocument(json));
	}

	@Test
	void testOpenDatabase() {
		try (final ODatabaseDocument database = repository.openDatabase()) {
			assertEquals(getClass().getSimpleName(), database.getName());
		}
	}

	@Test
	void testSave() {
		final String json = repository.find("1");
		repository.delete("1");
		assertEquals(json, repository.save(json));
		assertEquals(json, repository.find("1"));
	}

	@Test
	void testSaveDocument() {
		final String json = repository.find("1");
		repository.delete("1");
		final ODocument document = repository.newDocument(json);
		repository.saveDocument(document);
		assertEquals(json, repository.find("1"));
	}

	@Test
	void testSelectDocuments() {
		final String sql = "select name, electionEvent.name as eventName, electionEvent.alias from " + ENTITY_NAME + " where id = :id and age = :age";
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", "1");
		parameters.put("age", 1);
		final List<ODocument> documents = repository.selectDocuments(sql, parameters, -1);
		assertEquals(1, documents.size());
		final ODocument document = documents.get(0);
		assertEquals("name1", document.field("name", String.class));
		assertEquals("name2", document.field("eventName", String.class));
		assertNull(document.field("eventAlias", String.class));
	}

	@Test
	void testSelectDocumentsLimit() {
		final String sql = "select name, electionEvent.name as eventName, electionEvent.alias from " + ENTITY_NAME + " order by name";
		final Map<String, Object> parameters = emptyMap();
		final List<ODocument> documents = repository.selectDocuments(sql, parameters, 1);
		assertEquals(1, documents.size());
		final ODocument document = documents.get(0);
		assertEquals("name1", document.field("name", String.class));
		assertEquals("name2", document.field("eventName", String.class));
		assertNull(document.field("eventAlias", String.class));
	}

	@Test
	void testSelectDocumentsNoMatching() {
		final String sql = "select name, electionEvent.name as eventName, electionEvent.alias from " + ENTITY_NAME + " where id = :id and age = :age";
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", "1");
		parameters.put("age", 2);
		assertTrue(repository.selectDocuments(sql, parameters, -1).isEmpty());
	}

	@Test
	void testUpdate() {
		final String json = repository.find("1");
		final ODocument document = repository.getDocument("1");
		document.field("name", "name3");
		repository.saveDocument(document);
		final String update = repository.update(json);
		assertEquals(json, repository.find("1"));
		final JsonArray array = JsonUtils.getJsonObject(update).getJsonArray(JsonConstants.RESULT);
		assertEquals(2, array.size());
		assertEquals("name", array.getString(0));
		assertEquals("electionEvent", array.getString(1));
	}

	@Test
	void testUpdateMissing() {
		final String json = repository.find("1");
		repository.delete("1");
		assertThrows(DatabaseException.class, () -> repository.update(json));
	}

	private void assertJsonObjectCorrect(final JsonObject object) {
		assertEquals("1", object.getString("id"));
		assertEquals("name1", object.getString("name"));
		assertEquals(1, object.getInt("age"));
		final JsonObject eventObject = object.getJsonObject("electionEvent");
		assertEquals("2", eventObject.getString("id"));
		assertEquals("name2", eventObject.getString("name"));
		assertEquals(JsonValue.NULL, eventObject.get("alias"));
	}

	private void assertODocumentCorrect(final ODocument document) {
		assertEquals("1", document.field("id", String.class));
		assertEquals("name1", document.field("name", String.class));
		assertEquals(Integer.valueOf(1), document.field("age", Integer.class));
		assertEquals("2", document.field("electionEvent.id", String.class));
		assertEquals("name2", document.field("electionEvent.name", String.class));
		assertNull(document.field("electionEvent.alias", String.class));
	}

	private static class TestableEntityRepository extends AbstractEntityRepository {

		public TestableEntityRepository(final DatabaseManager manager) {
			super(manager);
		}

		@Override
		protected String entityName() {
			return ENTITY_NAME;
		}
	}
}
