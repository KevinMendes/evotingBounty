/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface VerificationCardStateRepository extends CrudRepository<VerificationCardStateEntity, Long> {

	@Query("select e from VerificationCardStateEntity e where e.verificationCardEntity.verificationCardId = ?1")
	Optional<VerificationCardStateEntity> findByVerificationCardId(final String verificationCardId);

}
