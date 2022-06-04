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
public interface CcrjReturnCodesKeysRepository extends CrudRepository<CcrjReturnCodesKeysEntity, Long> {

	@Query("select e from CcrjReturnCodesKeysEntity e where e.electionEventEntity.electionEventId = ?1")
	Optional<CcrjReturnCodesKeysEntity> findByElectionEventId(final String electionEventId);

}
