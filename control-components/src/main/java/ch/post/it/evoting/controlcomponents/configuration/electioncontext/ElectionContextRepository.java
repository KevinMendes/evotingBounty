/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.electioncontext;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ElectionContextRepository extends CrudRepository<ElectionContextEntity, Long> {

	@Query("select e from ElectionContextEntity e where e.electionEventEntity.electionEventId = ?1")
	Optional<ElectionContextEntity> findByElectionEventId(final String electionEventId);

}
