/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface CcmjElectionKeysRepository extends CrudRepository<CcmjElectionKeysEntity, Long> {

	@Query("select e from CcmjElectionKeysEntity e where e.electionEventEntity.electionEventId = ?1")
	Optional<CcmjElectionKeysEntity> findByElectionEventId(final String electionEventId);

}
