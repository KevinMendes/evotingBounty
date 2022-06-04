/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.votingcard.VotingCardWriter;

/**
 * Provides operations on the ballot box repository.
 */
@Local
public interface BallotBoxRepository extends BaseRepository<BallotBox, Integer> {

	/**
	 * Returns a stored vote in a ballot box.
	 *
	 * @param tenantId        - the tenant id.
	 * @param electionEventId - the election event id.
	 * @param votingCardId    - the voting card id.
	 * @return the vote stored in the ballot box.
	 * @throws ResourceNotFoundException if the vote is not found.
	 */
	BallotBox findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException;

	/**
	 * Returns the list of encrypted votes in the ballot box identified by tenantId, electionEventId
	 * and ballotBoxId.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the electionEvent.
	 * @param ballotBoxId     - the identifier of the ballot box.
	 * @param firstElement    -the first element to extract
	 * @param lastElement     -the last element to extract
	 * @return a list of encrypted votes in the ballot box in csv format (each encrypted vote is
	 * separated by semicolon).
	 */
	List<ExportedBallotBoxItem> getEncryptedVotesByTenantIdElectionEventIdBallotBoxId(String tenantId, String electionEventId, String ballotBoxId,
			int firstElement, int lastElement);

	/**
	 * Returns a list of ballot boxes for a tenant and election event.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the electionEvent.
	 * @param ballotBoxId     - the identifier of the ballot box.
	 * @return a list of ballot boxes for the given parameters.
	 */
	List<BallotBox> findByTenantIdElectionEventIdBallotBoxId(String tenantId, String electionEventId, String ballotBoxId);

	/**
	 * Returns a stored vote in a ballot box for a given tenant, election event, voting card and
	 * ballot.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the electionEvent.
	 * @param votingCardId    - the identifier of a voting card.
	 * @param ballotBoxId     - the identifier of the ballot box.
	 * @param ballotId        - the identifier of the ballot.
	 * @return the votes stored in the ballot box.
	 */
	List<BallotBox> findByTenantIdElectionEventIdVotingCardIdBallotBoxIdBallotId(String tenantId, String electionEventId, String votingCardId,
			String ballotBoxId, String ballotId);

	/**
	 * Finds all the VotingCards that have votes saved in ballotboxes and writes the id's into the
	 * specified writer
	 *
	 * @param tenantId        - the tenant identifier
	 * @param electionEventId - the election event identifier
	 * @param writer          - the writer to write the used voting cards
	 * @throws IOException
	 */
	void findAndWriteUsedVotingCards(String tenantId, String electionEventId, VotingCardWriter writer) throws IOException;

}
