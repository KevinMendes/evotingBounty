/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.returncodes.ComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.AdditionalData;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence.BallotBoxAccess;
import ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence.BallotBoxFactory;

/**
 * Implementation of the interface VoteService
 */
@Stateless(name = "ei-VoteService")
public class VoteServiceImpl implements VoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteServiceImpl.class);
	@Inject
	private BallotBoxRepository ballotBoxRepository;
	@Inject
	private BallotBoxAccess ballotBoxAccess;
	@Inject
	private BallotBoxFactory ballotBoxFactory;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void saveVote(final VoteAndComputeResults voteAndComputeResults, final String authenticationToken)
			throws DuplicateEntryException, ValidationException {

		final Vote vote = voteAndComputeResults.getVote();
		final ComputeResults computationResults = voteAndComputeResults.getComputeResults();

		// validate vote
		ValidationUtils.validate(vote);

		LOGGER.info("Storing the vote for tenant: {} and ballot: {} into ballotBox: {}.", vote.getTenantId(), vote.getBallotId(),
				vote.getBallotBoxId());

		final List<AdditionalData> listAdditionalData = new ArrayList<>();

		// store the vote
		ballotBoxAccess.save(ballotBoxFactory.from(vote, computationResults, authenticationToken, listAdditionalData));

		LOGGER.info("Stored vote for tenant: {} and ballot: {} into ballotBox: {}.", vote.getTenantId(), vote.getBallotId(), vote.getBallotBoxId());
	}

	@Override
	public VoteAndComputeResults retrieveVote(final String tenantId, final String electionEventId, final String votingCardId)
			throws ApplicationException, ResourceNotFoundException {

		LOGGER.info("Attempting to retrieve partial decryption results from ballot box for tenant: {} and election event: {} and voting card: {}",
				tenantId, electionEventId, votingCardId);

		final BallotBox ballotBox;
		try {
			ballotBox = ballotBoxRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);
		} catch (final ResourceNotFoundException e) {
			LOGGER.info("Did not find the vote with partial decryption results in ballot box");
			throw e;
		}

		try {

			final VoteAndComputeResults voteAndComputeResults = ballotBoxFactory.to(ballotBox);

			LOGGER.info("Successfully obtained the vote with partial decryption results");

			return voteAndComputeResults;

		} catch (final IOException e) {

			final String errorMsg = "Exception when trying to recover the vote with partial decryption results";
			LOGGER.error(errorMsg);
			throw new ApplicationException(errorMsg, e);
		}
	}

}
