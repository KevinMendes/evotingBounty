/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface EncryptedVerifiableVoteRepository extends CrudRepository<EncryptedVerifiableVoteEntity, Long> {

	@Query("select e from EncryptedVerifiableVoteEntity e where e.verificationCardEntity.verificationCardId = ?1")
	Optional<EncryptedVerifiableVoteEntity> findByVerificationCardId(final String verificationCardId);

}
