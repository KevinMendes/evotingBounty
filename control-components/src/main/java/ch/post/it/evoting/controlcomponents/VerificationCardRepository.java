/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public interface VerificationCardRepository extends CrudRepository<VerificationCardEntity, Long> {

	Optional<VerificationCardEntity> findByVerificationCardId(final String verificationCardId);

	boolean existsAllByVerificationCardIdIn(final Set<String> verificationCardIds);

}
