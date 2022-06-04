/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.commons.util.ZipUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode.VoteCastCode;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode.VoteCastCodeRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox.CleansedBallotBoxService;

@Stateless(name = "ei-VoteCastCodeService")
public class VoteCastCodeServiceImpl implements VoteCastCodeService {

	private static final String VOTE = "vote";
	private static final Logger LOGGER = LoggerFactory.getLogger(VoteCastCodeServiceImpl.class);
	@Inject
	private CleansedBallotBoxService cleansedBallotBoxService;
	@Inject
	private BallotBoxRepository ballotBoxRepository;
	@Inject
	private VoteCastCodeRepository voteCastCodeRepository;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void save(String tenantId, String electionEventId, String votingCardId, ShortVoteCastReturnCodeAndComputeResults voteCastCode)
			throws ApplicationException, IOException {

		try {
			ValidationUtils.validate(voteCastCode);
		} catch (SyntaxErrorException | SemanticErrorException e) {
			LOGGER.error("Vote cast code validation failed", e);
			throw new ApplicationException("Vote cast code validation failed");
		}

		try {
			VoteCastCode voteCastCodeEntity = new VoteCastCode();
			voteCastCodeEntity.setVoteCastCode(voteCastCode.getShortVoteCastReturnCode());
			voteCastCodeEntity.setComputationResults(ZipUtils.zipText(voteCastCode.getComputationResults()));

			voteCastCodeRepository.save(tenantId, electionEventId, votingCardId, voteCastCodeEntity);
		} catch (DuplicateEntryException e) {
			LOGGER.error("Error saving vote cast code due to duplicate entry", e);
			throw new ApplicationException("Duplicate vote cast code");
		}

		try {
			BallotBox ballotBox = ballotBoxRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);
			String voteString = ballotBox.getVote();
			JsonObject voteJsonObject = JsonUtils.getJsonObject(voteString);
			Vote vote = DomainObjectMapper.getNewInstance().readValue(voteJsonObject.get(VOTE).toString(), Vote.class);
			cleansedBallotBoxService.storeCleansedVote(vote);
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Ballot box not found", e);
			throw new ApplicationException("Ballot box not found");
		} catch (DuplicateEntryException e) {
			LOGGER.error("Duplicate cleansed ballot box", e);
			throw new ApplicationException("Duplicate cleansed ballot box");
		}
	}

}
