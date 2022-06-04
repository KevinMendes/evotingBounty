/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;

/**
 * Decorator of the validation service.
 */
@Decorator
public abstract class VoteValidationServiceDecorator implements VoteValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteValidationServiceDecorator.class);

	@Inject
	@Delegate
	private VoteValidationService validationService;

	/**
	 * @see VoteValidationService#validate(Vote, String, String, String)
	 */
	@Override
	public ValidationResult validate(Vote vote, String tenantId, String electionEventId, String ballotId)
			throws ApplicationException, ResourceNotFoundException {
		LOGGER.info("Start the validation of the vote. [tenantId: {}, ballotId: {}]", tenantId, ballotId);

		ValidationResult validationResult = validationService.validate(vote, tenantId, electionEventId, ballotId);
		if (validationResult.isResult()) {
			LOGGER.info("Result of validation. [result: {}]", validationResult.isResult());
		}

		LOGGER.info("End of validation!");

		return validationResult;
	}

	@Override
	public boolean isValid(BallotBox ballotBox) throws ValidationException {
		return validationService.isValid(ballotBox);
	}
}
