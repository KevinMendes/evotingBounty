/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.domain.model.ballottext.BallotText;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

/**
 * Implementation of the Ballot Repository
 */
@Repository
public class BallotTextRepository extends AbstractEntityRepository {

	/**
	 * Constructor
	 *
	 * @param databaseManager the injected database manager
	 */
	@Autowired
	public BallotTextRepository(final DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	public void updateSignedBallotText(final String ballotTextId, final String signedBallotText) {
		try {
			final ODocument text = getDocument(ballotTextId);
			text.field(JsonConstants.SIGNED_OBJECT, signedBallotText);
			text.field(JsonConstants.STATUS, Status.SIGNED.name());
			saveDocument(text);
		} catch (final OException e) {
			throw new DatabaseException("Failed to update signed ballot text.", e);
		}
	}

	/**
	 * Lists the signatures of the entities matching the criteria.
	 *
	 * @param criteria the criteria
	 * @return the signatures
	 * @throws DatabaseException failed to list signatures.
	 */
	public List<String> listSignatures(final Map<String, Object> criteria) {
		final List<ODocument> documents;
		try {
			documents = listDocuments(criteria);
		} catch (final OException e) {
			throw new DatabaseException("Failed to list signatures.", e);
		}
		final List<String> signatures = new ArrayList<>(documents.size());
		for (final ODocument document : documents) {
			signatures.add(document.field(JsonConstants.SIGNED_OBJECT, String.class));
		}
		return signatures;
	}

	@Override
	protected String entityName() {
		return BallotText.class.getSimpleName();
	}
}
