/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;

/**
 * Interface with operations for validation service.
 */
public interface VoteValidationService {

	/**
	 * Validates a given vote for a given tenant and ballot.
	 *
	 * @param vote            - the vote to validate.
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the election event identifier.
	 * @param ballotId        - the external identifier of the ballot.
	 * @return a ValidationResult containing the result and a list of rules that failed, if any.
	 * @throws ApplicationException      if an exception is thrown by the infrastructure layer or if the input parameters are not valid.
	 * @throws ResourceNotFoundException if the ballot is not found for a given tenant, election event and ballot identifiers.
	 */
	ValidationResult validate(Vote vote, String tenantId, String electionEventId, String ballotId)
			throws ApplicationException, ResourceNotFoundException;

	/**
	 * Checks if a vote is valid.
	 *
	 * @param ballotBox - the ballot box containing the vote.
	 * @return true if the vote is valid. Otherwise, validation exception is throws.
	 * @throws ValidationException
	 */
	boolean isValid(BallotBox ballotBox) throws ValidationException;

}
