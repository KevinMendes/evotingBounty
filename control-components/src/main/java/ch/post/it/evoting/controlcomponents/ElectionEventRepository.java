/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ElectionEventRepository extends CrudRepository<ElectionEventEntity, Long> {

	Optional<ElectionEventEntity> findByElectionEventId(final String electionEventId);

}
