/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.validation;

import javax.ejb.Local;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;

/**
 * Repository for handling validations.
 */
@Local
public interface ValidationRepository {

	/**
	 * Sends a vote to be validated to the Election Information.
	 *
	 * @param tenantId        - the tenant id
	 * @param electionEventId - the election event id
	 * @param vote            The vote to validate.
	 * @return the result of the validation containing the response and the rules that failed.
	 * @throws ResourceNotFoundException when the remote call fails.
	 */
	ValidationResult validateVoteInEI(String tenantId, String electionEventId, Vote vote) throws ResourceNotFoundException;

	/**
	 * Validates the election dates given of a ballot box
	 *
	 * @param tenantId        - Tenant Identifier
	 * @param electionEventId - electionEvent identifier
	 * @param ballotBoxId     - identifier of the ballot Box
	 * @return the result of the validation.
	 * @throws ResourceNotFoundException
	 */
	ValidationResult validateElectionDatesInEI(String tenantId, String electionEventId, String ballotBoxId) throws ResourceNotFoundException;

	/**
	 * Validates the vote in both election information and vote verification contexts.
	 *
	 * @param tenantId        - Tenant Identifier
	 * @param electionEventId - electionEvent identifier
	 * @param vote            the vote to be validated.
	 * @return the result of the validation.
	 * @throws ResourceNotFoundException
	 */
	ValidationVoteResult validateVote(String tenantId, String electionEventId, Vote vote) throws ResourceNotFoundException;
}
