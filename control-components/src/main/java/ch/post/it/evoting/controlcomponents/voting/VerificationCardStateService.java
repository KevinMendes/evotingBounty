/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationCardStateService {

	private final VerificationCardStateRepository verificationCardStateRepository;

	public VerificationCardStateService(
			final VerificationCardStateRepository verificationCardStateRepository) {
		this.verificationCardStateRepository = verificationCardStateRepository;
	}

	@Transactional
	public boolean isNotPartiallyDecrypted(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);

		return !verificationCardStateEntity.isPartiallyDecrypted();
	}

	@Transactional
	public void setPartiallyDecrypted(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);
		verificationCardStateEntity.setPartiallyDecrypted(true);

		verificationCardStateRepository.save(verificationCardStateEntity);
	}

	@Transactional
	public boolean isLCCShareCreated(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);

		return verificationCardStateEntity.isLccShareCreated();
	}

	@Transactional
	public void setLCCShareCreated(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);
		verificationCardStateEntity.setLccShareCreated(true);

		verificationCardStateRepository.save(verificationCardStateEntity);
	}

	@Transactional
	public boolean isNotConfirmed(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);

		return !verificationCardStateEntity.isConfirmed();
	}

	@Transactional
	public void setConfirmed(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);
		verificationCardStateEntity.setConfirmed(true);

		verificationCardStateRepository.save(verificationCardStateEntity);
	}

	@Transactional
	public int getConfirmationAttempts(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);

		return verificationCardStateEntity.getConfirmationAttempts();
	}

	@Transactional
	public void incrementConfirmationAttempts(final String verificationCardId) {
		validateUUID(verificationCardId);

		final VerificationCardStateEntity verificationCardStateEntity = getVerificationCardState(verificationCardId);

		int confirmationAttempts = verificationCardStateEntity.getConfirmationAttempts();
		verificationCardStateEntity.setConfirmationAttempts(++confirmationAttempts);

		verificationCardStateRepository.save(verificationCardStateEntity);
	}

	private VerificationCardStateEntity getVerificationCardState(final String verificationCardId) {
		validateUUID(verificationCardId);

		return verificationCardStateRepository.findByVerificationCardId(verificationCardId)
				.orElseThrow(() -> new IllegalStateException("No verification card state found. [verificationCardId: %s]"));
	}
}
