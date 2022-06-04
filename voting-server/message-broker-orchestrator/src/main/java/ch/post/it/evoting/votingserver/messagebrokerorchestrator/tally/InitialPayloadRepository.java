/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface InitialPayloadRepository extends CrudRepository<InitialPayloadEntity, Long> {

	InitialPayloadEntity save(final InitialPayloadEntity shufflePayloadEntity);

	Optional<InitialPayloadEntity> findByElectionEventIdAndBallotBoxId(final String electionEventId, final String ballotBoxId);
}
