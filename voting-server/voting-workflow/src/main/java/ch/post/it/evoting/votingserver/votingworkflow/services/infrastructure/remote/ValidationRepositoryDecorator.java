/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.validation.ValidationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;

/**
 * Decorator of the validation repository.
 */
@Decorator
public abstract class ValidationRepositoryDecorator implements ValidationRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationRepositoryDecorator.class);

	@Inject
	@Delegate
	private ValidationRepository validationRepository;

	/**
	 * @see ValidationRepository#validateVoteInEI(String, String, Vote)
	 */
	@Override
	public ValidationResult validateVoteInEI(String tenantId, String electionEventId, Vote vote) throws ResourceNotFoundException {
		return validationRepository.validateVoteInEI(tenantId, electionEventId, vote);
	}

	/**
	 * @see ValidationRepository#validateElectionDatesInEI(String, String, String)
	 */
	@Override
	public ValidationResult validateElectionDatesInEI(String tenantId, String electionEventId, String ballotBoxId) throws ResourceNotFoundException {
		try {
			LOGGER.info("Validating election dates in authentication");
			ValidationResult ballotBoxStatus = validationRepository.validateElectionDatesInEI(tenantId, electionEventId, ballotBoxId);
			LOGGER.info("Ballot box status is: {}", ballotBoxStatus.getValidationError().getValidationErrorType());
			return ballotBoxStatus;
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Error validating dates", e);
			throw e;
		}
	}

	/**
	 * @see ValidationRepository#validateVote(String, String, Vote)
	 */
	@Override
	public ValidationVoteResult validateVote(String tenantId, String electionEventId, Vote vote) throws ResourceNotFoundException {
		ValidationVoteResult validationVoteResult;
		try {
			return validationRepository.validateVote(tenantId, electionEventId, vote);
		} catch (RetrofitException e) {
			LOGGER.error("Error validating vote: ", e);
			if (e.getHttpCode() == 404) {
				throw new ResourceNotFoundException("Vote was not found", e);
			} else {
				validationVoteResult = new ValidationVoteResult();
				validationVoteResult.setValid(false);
				final ValidationError validationError = new ValidationError(ValidationErrorType.FAILED);
				validationError.setErrorArgs(new String[] { e.getMessage() });
				validationVoteResult.setValidationError(validationError);
			}
		}
		return validationVoteResult;
	}
}
