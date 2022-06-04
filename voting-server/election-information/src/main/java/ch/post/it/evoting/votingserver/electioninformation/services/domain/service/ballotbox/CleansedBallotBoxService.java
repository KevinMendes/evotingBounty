/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CleansedBallotBoxServiceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface defining operations to handle ballot box.
 */
public interface CleansedBallotBoxService {

	/**
	 * Checks if a ballot box is empty, i.e. does not contain any vote.
	 *
	 * @param electionEventId the election event id. Must be non-null.
	 * @param ballotBoxId     the ballot box id. Must be non-null.
	 * @return {@code true} if the ballot is empty, {@code false} otherwise.
	 */
	boolean isBallotBoxEmpty(final String electionEventId, final String ballotBoxId);

	/**
	 * Retrieves the initial payload for a given {@code ballotBoxId}.
	 *
	 * @param ballotBoxId the ballot box id object. Not null.
	 * @return the encrypted votes for the given ballot box.
	 * @throws ResourceNotFoundException         if no payload is found for the given ballot box.
	 * @throws CleansedBallotBoxServiceException if an error occurs during construction of the payload from the encrypted votes
	 */
	MixnetInitialPayload getMixnetInitialPayload(final BallotBoxId ballotBoxId) throws ResourceNotFoundException, CleansedBallotBoxServiceException;

	/**
	 * Stores the vote as cleansed
	 *
	 * @param vote to be stored as cleansed
	 * @throws DuplicateEntryException if the cleansed vote already exists
	 */
	void storeCleansedVote(Vote vote) throws DuplicateEntryException;
}
