/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.rule.AbstractRule;
import ch.post.it.evoting.votingserver.commons.domain.service.RuleExecutor;
import ch.post.it.evoting.votingserver.commons.logging.service.VoteHashService;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.VoteValidation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.VoteValidationRepository;

/**
 * Handles operations on vote validation.
 */
@Stateless(name = "ei-validationService")
public class VoteValidationServiceImpl implements VoteValidationService {

	private static final String VOTE = "vote";

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteValidationServiceImpl.class);

	private final Collection<AbstractRule<Vote>> rules = new ArrayList<>();

	@Inject
	private VoteValidationRepository voteValidationRepository;

	@Inject
	private VoteHashService voteHashService;

	@Inject
	private RuleExecutor<Vote> ruleExecutor;

	@Inject
	@Any
	void setRules(Instance<AbstractRule<Vote>> instance) {
		for (AbstractRule<Vote> rule : instance) {
			rules.add(rule);
		}
	}

	/**
	 * @see VoteValidationService#validate(
	 *Vote, String,
	 * String, String)
	 */
	@Override
	public ValidationResult validate(Vote vote, String tenantId, String electionEventId, String ballotId)
			throws ApplicationException, ResourceNotFoundException {
		validateInput(vote, tenantId, ballotId);
		ValidationError executorRulesResult = ruleExecutor.execute(rules, vote);
		ValidationResult validationResult = new ValidationResult();
		boolean result = executorRulesResult.getValidationErrorType().equals(ValidationErrorType.SUCCESS);
		validationResult.setResult(result);
		validationResult.setValidationError(executorRulesResult);

		if (result) {
			try {
				VoteValidation voteValidation = new VoteValidation();
				voteValidation.setTenantId(tenantId);
				voteValidation.setElectionEventId(electionEventId);
				voteValidation.setVotingCardId(vote.getVotingCardId());
				String voteHash = voteHashService.hash(vote);
				voteValidation.setVoteHash(voteHash);
				voteValidationRepository.save(voteValidation);
				LOGGER.info("All vote validations OK: the hash of the vote was stored.");
			} catch (GeneralCryptoLibException | DuplicateEntryException e) {
				LOGGER.error("Error computing the hash of the vote in validation ", e);
			}
		}

		return validationResult;
	}

	// Validates whether the input is correct
	private void validateInput(Vote vote, String tenantId, String ballotId) throws ApplicationException {
		// it is an exception if the input parameters are not valid, because
		// normally they should be already validated when
		// passed to this method, as this service does not validate data
		if (vote == null) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_VOTE_IS_NULL);
		}
		if (tenantId == null) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (ballotId == null) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_BALLOT_ID_IS_NULL);
		}
	}

	/**
	 * @see VoteValidationService#isValid(BallotBox)
	 */
	@Override
	public boolean isValid(BallotBox ballotBox) throws ValidationException {
		// recover validation result from db
		try {
			JsonObject voteJsonObject = JsonUtils.getJsonObject(ballotBox.getVote());
			Vote vote = DomainObjectMapper.getNewInstance().readValue(voteJsonObject.getJsonObject(VOTE).toString(), Vote.class);
			String voteHash = voteHashService.hash(vote);
			voteValidationRepository
					.findByTenantIdElectionEventIdVotingCardId(ballotBox.getTenantId(), ballotBox.getElectionEventId(), ballotBox.getVotingCardId(),
							voteHash);
			LOGGER.info("All the vote validations are ok. Hash successfully retrieved from VOTE_VALIDATION.");
			return true;
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Vote hash not found in VOTE_VALIDATION. Invalid vote.", e);
			throw new ValidationException(null,
					"Invalid vote. The hash of the vote is not found at VOTE_VALIDATION table: " + ExceptionUtils.getRootCauseMessage(e));
		} catch (GeneralCryptoLibException e) {
			throw new ValidationException(null, "Invalid vote. Error computing vote hash:" + ExceptionUtils.getRootCauseMessage(e));
		} catch (IOException e) {
			throw new ValidationException(null, "Invalid vote. Could not convert json to object:" + ExceptionUtils.getRootCauseMessage(e));
		}
	}
}
