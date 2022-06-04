/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface VerificationCardSetRepository extends CrudRepository<VerificationCardSetEntity, Long> {

	Optional<VerificationCardSetEntity> findByVerificationCardSetId(final String verificationCardSetId);

	boolean existsByVerificationCardSetId(final String verificationCardSetId);

}
