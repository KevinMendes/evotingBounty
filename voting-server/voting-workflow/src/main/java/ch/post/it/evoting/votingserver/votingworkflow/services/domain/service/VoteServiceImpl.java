/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import java.io.IOException;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.returncodes.ComputeResults;
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AdminBoardCertificateRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.choicecode.ChoiceCodeRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStateEvaluator;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.validation.ValidationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

@Stateless(name = "vw-voteService")
public class VoteServiceImpl implements VoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteServiceImpl.class);

	@Inject
	private VotingCardStateService votingCardStateService;

	@Inject
	private VoteRepository voteRepository;

	@Inject
	private ChoiceCodeRepository choiceCodeRepository;

	@Inject
	private ValidationRepository validationRepository;

	@Inject
	private AdminBoardCertificateRepository adminBoardCertificateRepository;

	/**
	 * Validates and saves the vote
	 *
	 * @param tenantId
	 * @param electionEventId
	 * @param votingCardId
	 * @param verificationCardId
	 * @param vote
	 * @param authenticationTokenJsonString
	 * @return
	 * @throws ApplicationException
	 * @throws ResourceNotFoundException
	 * @throws DuplicateEntryException
	 * @throws IOException
	 */

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ValidationVoteResult validateVoteAndStore(String tenantId, String electionEventId, String votingCardId, String verificationCardId,
			Vote vote, String authenticationTokenJsonString)
			throws ApplicationException, ResourceNotFoundException, DuplicateEntryException, IOException {

		VotingCardState votingCardState = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);

		ValidationVoteResult validationVoteResult = new ValidationVoteResult();

		VotingCardStateEvaluator votingCardStateEvaluator = VotingCardStateEvaluator.forState(votingCardState.getState());

		if (votingCardStateEvaluator.votingCardIsBlocked()) {
			final ValidationError validationError = new ValidationError(ValidationErrorType.INVALID_VOTING_CARD_ID);
			validationError.setErrorArgs(new String[] { VotingCardStates.BLOCKED.name() });
			validationVoteResult.setValid(false);
			validationVoteResult.setValidationError(validationError);

			return validationVoteResult;
		}

		if (votingCardStateEvaluator.votingCardDidNotVote()) {

			validationVoteResult = validationRepository.validateVote(tenantId, electionEventId, vote);

			if (validationVoteResult.isValid()) {

				try {
					final VoteAndComputeResults voteAndComputeResults = new VoteAndComputeResults();
					voteAndComputeResults.setVote(vote);

					AdminBoardCertificates certificatesInformation = adminBoardCertificateRepository
							.findByTenantElectionEventCertificates(tenantId, electionEventId);

					// add chain of certificates to verify signed content on Control Components
					voteAndComputeResults.setCredentialInfoCertificates(certificatesInformation.getCertificates());

					// generate choice codes
					ShortChoiceReturnCodeAndComputeResults generatedChoiceCodes = choiceCodeRepository
							.generateChoiceCodes(tenantId, electionEventId, verificationCardId, voteAndComputeResults);

					validationVoteResult.setChoiceCodes(generatedChoiceCodes.getShortChoiceReturnCodes());

					voteAndComputeResults.setComputeResults(new ComputeResults());
					voteAndComputeResults.getComputeResults().setComputationResults(generatedChoiceCodes.getComputationResults());
					voteAndComputeResults.getComputeResults().setDecryptionResults(generatedChoiceCodes.getDecryptionResults());

					voteRepository.save(tenantId, electionEventId, voteAndComputeResults, authenticationTokenJsonString);
					// change voting card state to SENT BUT NOT CAST
					votingCardStateService.updateVotingCardState(tenantId, electionEventId, votingCardId, VotingCardStates.SENT_BUT_NOT_CAST);

				} catch (EJBException | ResourceNotFoundException e) {

					validationVoteResult.setValid(Boolean.FALSE);
					validationVoteResult.setValidationError(new ValidationError(ValidationErrorType.FAILED));

				}
			}

		} else {
			LOGGER.info("VotingCard [tenant={}, election={}, votingCardId={}] already voted.", tenantId, electionEventId, votingCardId);
		}

		return validationVoteResult;

	}
}
