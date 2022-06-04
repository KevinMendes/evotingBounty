/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

import java.io.IOException;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides extra operations on the ballot box information repository.
 */
@Local
public interface BallotBoxInformationRepository extends BaseRepository<BallotBoxInformation, Integer> {

	/**
	 * Searches for a ballot with the given tenant, election event and ballot box id.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the tenant.
	 * @param ballotBoxId     - the external identifier of the ballot.
	 * @return a entity representing the ballot.
	 */
	BallotBoxInformation findByTenantIdElectionEventIdBallotBoxId(String tenantId, String electionEventId, String ballotBoxId)
			throws ResourceNotFoundException;

	/**
	 * Adds "Ballot Box Information" for a ballot box to the repository
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the electionEvent.
	 * @param ballotBoxId     - the identifier of the ballot box.
	 * @param jsonContent     - JSON serialized content to add
	 * @return void
	 */
	void addBallotBoxInformation(String tenantId, String electionEventId, String ballotBoxId, String jsonContent)
			throws DuplicateEntryException, IOException;
}
