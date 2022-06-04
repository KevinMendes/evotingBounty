/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.ChallengeInformationValidation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

/**
 * Implementation of {@link ChallengeInformationValidation} which checks that credential identifier passed in URI matches one passed in {@link
 * ChallengeInformation}.
 */
public class ChallengeInformationCredentialIdValidation implements ChallengeInformationValidation {
	public static final int RULE_ORDER = 0;
	private final int order;
	@Inject
	private Logger logger;
	@Inject
	private TrackIdInstance trackId;

	/**
	 * Constructor.
	 */
	public ChallengeInformationCredentialIdValidation() {
		this.order = RULE_ORDER;
	}

	/**
	 * Constructor. For tests only.
	 *
	 * @param logger
	 */
	ChallengeInformationCredentialIdValidation(final Logger logger) {
		this.logger = logger;
		this.order = RULE_ORDER;
	}

	@Override
	public boolean execute(final String tenantId, final String electionEventId, final String credentialId,
			final ChallengeInformation challengeInformation) {
		boolean valid = Objects.equals(credentialId, challengeInformation.getCredentialId());
		if (valid) {
			logger.info("Credential identifier is valid. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId,
					credentialId);
		} else {
			logger.info("Credential identifier is invalid. [tenantId: {}, electionEventId: {}, credentialId: {}, credentialId from challenge: {}]",
					tenantId, electionEventId, credentialId, challengeInformation.getCredentialId());
		}
		return valid;
	}

	@Override
	public int getOrder() {
		return order;
	}
}
