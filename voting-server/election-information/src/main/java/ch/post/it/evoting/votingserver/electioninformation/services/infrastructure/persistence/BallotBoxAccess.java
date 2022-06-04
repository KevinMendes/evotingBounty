/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote.VoteValidationService;

@Stateless
@javax.ejb.ApplicationException(rollback = true)
public class BallotBoxAccess {

	@EJB
	private BallotBoxRepository ballotBoxRepository;

	@Inject
	private VoteValidationService voteValidationService;

	public BallotBox save(BallotBox ballotBox) throws DuplicateEntryException, ValidationException {
		checkIfVoteIsValid(ballotBox);
		return insert(ballotBox);
	}

	private void checkIfVoteIsValid(BallotBox ballotBox) throws ValidationException {
		voteValidationService.isValid(ballotBox);
	}

	BallotBox insert(BallotBox ballotBox) throws DuplicateEntryException {
		return ballotBoxRepository.save(ballotBox);
	}

}
