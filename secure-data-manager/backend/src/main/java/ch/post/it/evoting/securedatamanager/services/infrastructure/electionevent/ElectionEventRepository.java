/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.cryptoprimitives.domain.election.ElectionEvent;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

/**
 * Implementation of operations with election event.
 */
@Repository
public class ElectionEventRepository extends AbstractEntityRepository {

	public ElectionEventRepository(final DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * Returns the election event alias from the election event identified by the given id.
	 *
	 * @param electionEventId identifies the election event where to search.
	 * @return the election event alias.
	 */
	public String getElectionEventAlias(final String electionEventId) {
		validateUUID(electionEventId);

		final String sql = "select alias from " + entityName() + " where id = :id";
		final Map<String, Object> parameters = singletonMap(JsonConstants.ID, electionEventId);
		final List<ODocument> documents;
		try {
			documents = selectDocuments(sql, parameters, 1);
		} catch (final OException e) {
			throw new DatabaseException("Failed to get election event alias.", e);
		}
		return documents.isEmpty() ? "" : documents.get(0).field("alias", String.class);
	}

	/**
	 * Lists all the identifiers.
	 *
	 * @return the identifiers
	 * @throws DatabaseException failed to list the identifier
	 */
	public List<String> listIds() {
		final String sql = "select id from " + entityName();
		final Map<String, Object> parameters = emptyMap();
		final List<ODocument> documents;
		try {
			documents = selectDocuments(sql, parameters, -1);
		} catch (final OException e) {
			throw new DatabaseException("Failed to list identifiers.", e);
		}
		final List<String> ids = new ArrayList<>(documents.size());
		for (final ODocument document : documents) {
			ids.add(document.field(JsonConstants.ID, String.class));
		}
		return ids;
	}

	@Override
	protected String entityName() {
		return ElectionEvent.class.getSimpleName();
	}

	/**
	 * @param electionEventId the election event id. Must be non-null and a valid UUID.
	 * @return the dateFrom field value related to the corresponding election event.
	 * @throws FailedValidationException if {@code electionEventId} is null or not a valid UUID.
	 */
	public String getDateFrom(final String electionEventId) {
		validateUUID(electionEventId);

		return JsonUtils.getJsonObject(getElectionEventJson(electionEventId)).getString(JsonConstants.DATE_FROM);
	}

	/**
	 * @param electionEventId the election event id. Must be non-null and a valid UUID.
	 * @return the dateTo field value related to the corresponding election event.
	 * @throws FailedValidationException if {@code electionEventId} is null or not a valid UUID.
	 */
	public String getDateTo(final String electionEventId) {
		validateUUID(electionEventId);

		return JsonUtils.getJsonObject(getElectionEventJson(electionEventId)).getString(JsonConstants.DATE_TO);
	}

	private String getElectionEventJson(final String electionEventId) {
		final String electionEventJson = find(electionEventId);
		if (electionEventJson == null) {
			throw new IllegalStateException(String.format("Could not find any election event object. [electionEventId: %s]", electionEventId));
		} else if (StringUtils.isBlank(electionEventJson) || JsonConstants.EMPTY_OBJECT.equals(electionEventJson)) {
			throw new IllegalStateException(String.format("Found an empty election event object. [electionEventId: %s]", electionEventId));
		}

		return electionEventJson;
	}
}
