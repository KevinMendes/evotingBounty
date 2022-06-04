/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ShufflePayloadRepository extends CrudRepository<ShufflePayloadEntity, Long> {

	List<ShufflePayloadEntity> findByElectionEventIdAndBallotBoxId(final String electionEventId, final String ballotBoxId);

	Integer countByElectionEventIdAndBallotBoxId(final String electionEventId, final String ballotBoxId);
}
