/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority;

import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.json.JsonArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.domain.model.electoralauthority.ElectoralAuthority;
import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

/**
 * Implementation of the interface which offers operations on the repository of electoral repository.
 */
@Repository
public class ElectoralAuthorityRepository extends AbstractEntityRepository {

	@Autowired
	BallotBoxRepository ballotBoxRepository;

	/**
	 * The constructor.
	 *
	 * @param databaseManager the injected database manager
	 */
	@Autowired
	public ElectoralAuthorityRepository(final DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * Updates the related ballot box(es).
	 *
	 * @param list The list of identifiers of the electoral authorities where to update the identifiers of the related ballot boxes.
	 */
	public void updateRelatedBallotBox(final List<String> electoralAuthoritiesIds) {
		try {
			for (final String id : electoralAuthoritiesIds) {
				final ODocument authority = getDocument(id);
				final List<String> aliases = getBallotBoxAliases(id);
				authority.field(JsonConstants.BALLOT_BOX_ALIAS, aliases);
				saveDocument(authority);
			}
		} catch (final OException e) {
			throw new DatabaseException("Failed to update related ballot box.", e);
		}
	}

	/**
	 * Lists the electoral authorities matching a specific election event ID.
	 *
	 * @param electionEventId the election event identifier
	 * @return the electoral authorities in JSON format
	 * @throws DatabaseException failed to list the electoral authorities
	 */
	public String listByElectionEvent(final String electionEventId) {
		return list(singletonMap("electionEvent.id", electionEventId));
	}

	@Override
	protected String entityName() {
		return ElectoralAuthority.class.getSimpleName();
	}

	// Return the aliases of all the ballot boxes for the electoral authority
	// identified by electoralAuthorityId.
	private List<String> getBallotBoxAliases(final String electoralAuthorityId) {
		final JsonArray ballotBoxesResult = JsonUtils.getJsonObject(ballotBoxRepository.findByElectoralAuthority(electoralAuthorityId))
				.getJsonArray(JsonConstants.RESULT);
		final List<String> ballotBoxIds = new ArrayList<>();
		for (int index = 0; index < ballotBoxesResult.size(); index++) {
			ballotBoxIds.add(ballotBoxesResult.getJsonObject(index).getString(JsonConstants.ALIAS));
		}

		return ballotBoxIds;
	}
}
