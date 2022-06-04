/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.common.io.OIOException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.domain.model.EntityRepository;

/**
 * Basic abstract implementation of {@link EntityRepository}.
 * <p>
 * Subclasses can use the following methods for manipulating {@link ODocument}
 * <ul>
 * <li>{@link #newDocument()}. Creates new empty {@link ODocument}.</li>
 * <li>{@link #newDocument(String)}. Creates new {@link ODocument} from given JSON.</li>
 * <li>{@link #saveDocument(ODocument)}. Saves a given {@link ODocument} in the database.</li>
 * <li>{@link #selectDocuments(String, Map, int)}. Runs an arbitrary SELECT query.</li>
 * <li>{@link #findDocument(Map)}. Finds a {@link ODocument} matching a given criteria.</li>
 * <li>{@link #listDocuments(Map)}. Lists {@link ODocument} matching a given criteria.</li>
 * <li>{@link #deleteDocuments(String, Map)}. Runs an arbitrary delete query.</li>
 * </ul>
 * For more low level manipulation of {@link ODocument} subclasses can use {@link ODatabaseDocument}
 * returned by {@link #openDatabase()} method.
 * <p>
 * Instances of concrete subclasses must be initialized using {@link #initialize()} method before
 * they are published for general access.
 */
public abstract class AbstractEntityRepository implements EntityRepository {

	public static final int MAX_RETRY_ORIENTDB = 5;

	private final DatabaseManager manager;

	/**
	 * Constructor.
	 *
	 * @param manager
	 */
	public AbstractEntityRepository(final DatabaseManager manager) {
		this.manager = manager;
	}

	@Override
	public void delete(final Map<String, Object> criteria) {
		final Map<String, Object> parameters = new HashMap<>();
		final String sql = buildDeleteSQL(criteria, parameters);
		try {
			deleteDocuments(sql, parameters);
		} catch (final OException e) {
			throw new DatabaseException("Failed to delete.", e);
		}
	}

	@Override
	public void delete(final String id) {
		delete(singletonMap(JsonConstants.ID, id));
	}

	@Override
	public String find(final Map<String, Object> criteria) {
		final ODocument document;
		try {
			document = findDocument(criteria);
		} catch (final OException e) {
			throw new DatabaseException("Failed to find.", e);
		}
		return document != null ? document.toJSON("") : JsonConstants.EMPTY_OBJECT;
	}

	@Override
	public String find(final String id) {
		return find(singletonMap(JsonConstants.ID, id));
	}

	/**
	 * Initializes the repository. This method must be called before the repository instance is published.
	 *
	 * @throws DatabaseException failed to initialize the repository.
	 */
	public void initialize() {
		try (final ODatabaseDocument database = openDatabase()) {
			final OSchema schema = database.getMetadata().getSchema();
			if (!schema.existsClass(entityName())) {
				final OClass entity = schema.createClass(entityName());
				if (!entity.existsProperty(JsonConstants.ID)) {
					entity.createProperty(JsonConstants.ID, OType.STRING).setMandatory(true).setNotNull(true);
				}
				if (!entity.areIndexed(JsonConstants.ID)) {
					entity.createIndex(entityName() + "_Unique", OClass.INDEX_TYPE.UNIQUE, JsonConstants.ID);
				}
			}
		} catch (final OException e) {
			throw new DatabaseException("Failed to initialize.", e);
		}
	}

	@Override
	public String list() {
		return list(emptyMap());
	}

	@Override
	public String list(final Map<String, Object> criteria) {
		final List<ODocument> documents;
		try {
			documents = listDocuments(criteria);
		} catch (final OException e) {
			throw new DatabaseException("Failed to list.", e);
		}
		final String prefix = "{\"" + JsonConstants.RESULT + "\":[";
		final String suffix = "]}";
		final StringJoiner joiner = new StringJoiner(",", prefix, suffix);
		for (final ODocument document : documents) {
			joiner.add(document.toJSON(""));
		}
		return joiner.toString();
	}

	@Override
	public String save(final String json) {
		try {
			saveDocument(newDocument(json));
		} catch (final OException e) {
			throw new DatabaseException("Failed to save.", e);
		}
		return json;
	}

	@Override
	public String update(final String json) {
		final ODocument newDocument = newDocument(json);
		final String id = newDocument.field(JsonConstants.ID, String.class);
		String[] dirtyFields = new String[0];
		try (final ODatabaseDocument database = openDatabase()) {
			for (int retry = 0; retry < MAX_RETRY_ORIENTDB; ++retry) {
				database.begin();
				final ODocument oldDocument = findDocument(database, singletonMap(JsonConstants.ID, id));
				if (oldDocument == null) {
					throw new DatabaseException(format("Entity ''{0}'' does not exist.", id));
				}
				try {
					oldDocument.merge(newDocument, true, true);
					dirtyFields = oldDocument.getDirtyFields();
					saveDocument(database, oldDocument);
					database.commit();
					break;
				} catch (final ONeedRetryException e) {
					oldDocument.reload();
				}
			}
		} catch (final OException e) {
			throw new DatabaseException("Failed to update.", e);
		}
		final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (final String field : dirtyFields) {
			arrayBuilder.add(field);
		}
		final JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(JsonConstants.RESULT, arrayBuilder.build());
		return builder.build().toString();
	}

	/**
	 * Deletes the documents specified by given DELETE SQL and parameters.
	 *
	 * @param sql        the SQL
	 * @param parameters the parameters
	 * @throws OException failed to delete documents.
	 */
	protected void deleteDocuments(final String sql, final Map<String, Object> parameters) {
		try (final ODatabaseDocument database = openDatabase()) {
			final OCommandSQL command = new OCommandSQL(sql);
			database.command(command).execute(parameters);
		}
	}

	/**
	 * Returns the entity name.
	 *
	 * @return the entity name.
	 */
	protected abstract String entityName();

	/**
	 * Finds a document matching the specified criteria.
	 *
	 * @param criteria the criteria
	 * @return the document or {@code null} if the document does not exist
	 * @throws OException failed to find the document.
	 */
	protected ODocument findDocument(final Map<String, Object> criteria) {
		final Map<String, Object> parameters = new HashMap<>();
		final String sql = buildSelectSQL(criteria, parameters);
		final List<ODocument> documents = selectDocuments(sql, parameters, 1);
		return documents.isEmpty() ? null : documents.get(0);
	}

	protected ODocument findDocument(final ODatabaseDocument oDatabaseDocument, final Map<String, Object> criteria) {
		final Map<String, Object> parameters = new HashMap<>();
		final String sql = buildSelectSQL(criteria, parameters);
		final List<ODocument> documents = selectDocuments(oDatabaseDocument, sql, parameters, 1);
		return documents.isEmpty() ? null : documents.get(0);
	}

	/**
	 * Finds the document with the specified identifier. This is a shortcut for {@code find(Collections.singletonMap("id", id))}.
	 *
	 * @param id the identifier
	 * @return the document or {@code null} if the document does not exist.
	 * @throws OException failed to find the document.
	 */
	protected ODocument findDocument(final String id) {
		return findDocument(singletonMap(JsonConstants.ID, id));
	}

	/**
	 * Returns the document with the specified identifier.
	 *
	 * @param id the identifier
	 * @return the document
	 * @throws OException the document does not exist or error occurred.
	 */
	protected ODocument getDocument(final String id) {
		final ODocument document = findDocument(id);
		if (document == null) {
			throw new OIOException(format("Document ''{0}'' does not exist.", id));
		}
		return document;
	}

	/**
	 * Lists the documents matching the specified criteria
	 *
	 * @param criteria the criteria
	 * @return the documents
	 * @throws OException failed to list the documents
	 */
	protected List<ODocument> listDocuments(final Map<String, Object> criteria) {
		final Map<String, Object> parameters = new HashMap<>();
		final String sql = buildSelectSQL(criteria, parameters);
		return selectDocuments(sql, parameters, -1);
	}

	/**
	 * Creates a new empty document.
	 *
	 * @return the document.
	 */
	protected ODocument newDocument() {
		return new ODocument(entityName());
	}

	/**
	 * Creates a new document from a given JSON.
	 *
	 * @return the document.
	 */
	protected ODocument newDocument(final String json) {
		requireNonNull(json, "JSON is null");
		final ODocument document = newDocument();
		document.fromJSON(json);
		return document;
	}

	/**
	 * Opens the database. Caller is responsible for closing the returned database.
	 *
	 * @return the database
	 * @throws OException failed to open the database.
	 */
	protected ODatabaseDocument openDatabase() {
		return manager.openDatabase();
	}

	/**
	 * Saves a given document.
	 *
	 * @param document the document
	 * @throws OException failed to save the document.
	 */
	protected void saveDocument(final ODocument document) {
		requireNonNull(document, "Document is null.");
		try (final ODatabaseDocument database = openDatabase()) {
			database.save(document);
		}
	}

	protected void saveDocument(final ODatabaseDocument database, final ODocument document) {
		requireNonNull(document, "Document is null.");
		database.save(document);
	}

	/**
	 * Selects documents specified by given SELECT SQL, parameters and limit.
	 *
	 * @param sql        the SQL
	 * @param parameters the parameters
	 * @param limit      the limit or {@code -1} to retrieve all
	 * @return the documents
	 * @throws OException failed to select the documents.
	 */
	protected List<ODocument> selectDocuments(final String sql, final Map<String, Object> parameters, final int limit) {
		final List<ODocument> documents;
		try (final ODatabaseDocument database = openDatabase()) {
			final OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sql, limit);
			documents = database.query(query, parameters);
		}
		return documents;
	}

	protected List<ODocument> selectDocuments(final ODatabaseDocument database, final String sql, final Map<String, Object> parameters,
			final int limit) {
		final OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sql, limit);
		return database.query(query, parameters);
	}

	private String buildConditions(final Map<String, Object> criteria, final Map<String, Object> parameters) {
		final StringJoiner conditions = new StringJoiner(" and ");
		int i = 0;
		for (final Entry<String, Object> entry : criteria.entrySet()) {
			final String attribute = entry.getKey();
			requireNonNull(attribute, "Attribute is null.");
			final Object value = entry.getValue();
			if (value != null) {
				final String parameter = "p" + i++;
				conditions.add(attribute + "=:" + parameter);
				parameters.put(parameter, value);
			} else {
				conditions.add(attribute + " is null");
			}
		}
		return conditions.toString();
	}

	private String buildDeleteSQL(final Map<String, Object> criteria, final Map<String, Object> parameters) {
		final StringBuilder sql = new StringBuilder();
		sql.append("delete from ").append(entityName());
		if (!criteria.isEmpty()) {
			sql.append(" where ");
			sql.append(buildConditions(criteria, parameters));
		}
		return sql.toString();
	}

	private String buildSelectSQL(final Map<String, Object> criteria, final Map<String, Object> parameters) {
		final StringBuilder sql = new StringBuilder();
		sql.append("select from ").append(entityName());
		if (!criteria.isEmpty()) {
			sql.append(" where ");
			sql.append(buildConditions(criteria, parameters));
		}
		return sql.toString();
	}
}
