/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@Service
public class ElectionEventService {

	private final ElectionEventRepository electionEventRepository;

	public ElectionEventService(final ElectionEventRepository electionEventRepository) {
		this.electionEventRepository = electionEventRepository;
	}

	@Transactional
	public ElectionEventEntity save(final String electionEventId, final GqGroup encryptionGroup) {
		validateUUID(electionEventId);
		checkNotNull(encryptionGroup);

		final ElectionEventEntity entityToSave = new ElectionEventEntity(electionEventId, encryptionGroup);
		return electionEventRepository.save(entityToSave);
	}

	@Transactional
	public ElectionEventEntity getElectionEventEntity(final String electionEventId) {
		validateUUID(electionEventId);

		return electionEventRepository.findByElectionEventId(electionEventId)
				.orElseThrow(() -> new IllegalStateException(String.format("Election event not found. [electionEventId: %s]", electionEventId)));
	}

	@Transactional
	@Cacheable("gqGroups")
	public GqGroup getEncryptionGroup(final String electionEventId) {
		validateUUID(electionEventId);

		final Optional<ElectionEventEntity> electionEventEntity = electionEventRepository.findByElectionEventId(electionEventId);

		return electionEventEntity
				.orElseThrow(() -> new IllegalStateException(String.format("Encryption group not found. [electionEventId: %s]", electionEventId)))
				.getEncryptionGroup();
	}

}
