/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import static java.text.MessageFormat.format;

import java.io.IOException;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.ConfirmationInformationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastCodeRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;

/**
 * Implementation of {@link CastVoteService} as a stateless session bean.
 */
@Stateless
public class CastVoteServiceImpl implements CastVoteService {

	private static final String FAILED_TO_CAST_VOTE = "Failed to cast vote.";

	private static final Logger LOGGER = LoggerFactory.getLogger(CastVoteServiceImpl.class);

	@Inject
	private VotingCardStateService stateService;

	@Inject
	private VoteCastCodeRepository codeRepository;

	@Inject
	private VoteCastCodeService codeService;

	@Inject
	private ConfirmationInformationRepository confirmationRepository;

	@Override
	public VoteCastResult castVote(final String authenticationToken, final VoterInformation voter, final ConfirmationInformation confirmation)
			throws ResourceNotFoundException {
		VoteCastResult result;
		try {
			result = doCastVote(authenticationToken, voter, confirmation);
		} catch (WrongBallotCastingKeyException e) {
			result = handleWrongBallotCastingKey(voter, e.getState());
			LOGGER.debug(FAILED_TO_CAST_VOTE, e);
		} catch (NumberOfAttemptsExceededException e) {
			result = handleNumberOfAttemptsExceeded(voter);
			LOGGER.debug(FAILED_TO_CAST_VOTE, e);
		} catch (IllegalVotingCardStateException e) {
			result = handleIllegalVotingCardState(voter);
			LOGGER.debug(FAILED_TO_CAST_VOTE, e);
		}
		return result;
	}

	private ShortVoteCastReturnCodeAndComputeResults createVoteCastMessage(final String token, final VoterInformation voter,
			final ConfirmationInformation confirmation, final VotingCardState state)
			throws WrongBallotCastingKeyException, NumberOfAttemptsExceededException {
		ShortVoteCastReturnCodeAndComputeResults message = generateVoteCastMessage(token, voter, confirmation, state);
		storeVoteCastMessage(state, message);
		return message;
	}

	private VoteCastResult doCastVote(final String token, final VoterInformation voter, final ConfirmationInformation confirmation)
			throws WrongBallotCastingKeyException, NumberOfAttemptsExceededException, IllegalVotingCardStateException, ResourceNotFoundException {
		VotingCardState state = getVotingCardState(voter);
		validateVotingCardState(state);
		validateConfirmation(token, confirmation, state);
		incrementNumberOfAttempts(state);
		ShortVoteCastReturnCodeAndComputeResults message = createVoteCastMessage(token, voter, confirmation, state);
		setVotingCardState(state, VotingCardStates.CAST);
		return generateVoteCastResult(voter, state, message);
	}

	private ShortVoteCastReturnCodeAndComputeResults generateVoteCastMessage(String authenticationToken, final VoterInformation voter,
			final ConfirmationInformation confirmation, final VotingCardState state)
			throws WrongBallotCastingKeyException, NumberOfAttemptsExceededException {
		ShortVoteCastReturnCodeAndComputeResults message = null;
		try {
			AuthenticationToken authenticationTokenObject = DomainObjectMapper.getNewInstance()
					.readValue(authenticationToken, AuthenticationToken.class);
			message = codeRepository
					.generateCastCode(state.getTenantId(), state.getElectionEventId(), voter.getVerificationCardId(), voter.getVotingCardId(),
							authenticationTokenObject.getSignature(), confirmation.getConfirmationMessage());
		} catch (CryptographicOperationException | IOException e) {
			checkRemainingAttempts(state, e);
		}
		return message;
	}

	private VoteCastResult generateVoteCastResult(final VoterInformation voter, final VotingCardState state,
			final ShortVoteCastReturnCodeAndComputeResults message) {
		VoteCastResult result;
		try {
			result = codeService.generateVoteCastResult(state.getElectionEventId(), state.getVotingCardId(), voter.getVerificationCardId(), message);
		} catch (ResourceNotFoundException | IOException e) {
			throw new EJBException(format("Failed to generate vote cast result for voting card ''{0}.''.", state.getVotingCardId()), e);
		}
		return result;
	}

	private VotingCardState getVotingCardState(final VoterInformation voter) {
		VotingCardState state;
		try {
			state = stateService.getVotingCardState(voter.getTenantId(), voter.getElectionEventId(), voter.getVotingCardId());
		} catch (ApplicationException e) {
			throw new EJBException(format("Failed to get voting card ''{0}''.", voter.getVotingCardId()), e);
		}
		return state;
	}

	private VoteCastResult handleIllegalVotingCardState(final VoterInformation voter) {
		VoteCastResult result = new VoteCastResult();
		result.setValid(false);
		result.setElectionEventId(voter.getElectionEventId());
		result.setVerificationCardId(voter.getVerificationCardId());
		result.setVotingCardId(voter.getVotingCardId());
		return result;
	}

	private VoteCastResult handleNumberOfAttemptsExceeded(final VoterInformation voter) {
		VoteCastResult result = new VoteCastResult();
		result.setValid(false);
		ValidationError error = new ValidationError(ValidationErrorType.BCK_ATTEMPTS_EXCEEDED);
		result.setValidationError(error);
		result.setElectionEventId(voter.getElectionEventId());
		result.setVerificationCardId(voter.getVerificationCardId());
		result.setVotingCardId(voter.getVotingCardId());
		return result;
	}

	private VoteCastResult handleWrongBallotCastingKey(final VoterInformation voter, final VotingCardState state) {
		String remainingAttempts = Long.toString(state.getRemainingAttempts());

		VoteCastResult result = new VoteCastResult();
		result.setValid(false);
		ValidationError validationError = new ValidationError(ValidationErrorType.WRONG_BALLOT_CASTING_KEY);
		String[] errorArgs = { remainingAttempts };
		validationError.setErrorArgs(errorArgs);
		result.setValidationError(validationError);
		result.setElectionEventId(voter.getElectionEventId());
		result.setVerificationCardId(voter.getVerificationCardId());
		result.setVotingCardId(voter.getVotingCardId());
		return result;
	}

	private void incrementNumberOfAttempts(final VotingCardState state) {
		try {
			stateService.incrementVotingCardAttempts(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId());
		} catch (ResourceNotFoundException | ApplicationException | DuplicateEntryException e) {
			throw new EJBException(format("Failed to increment the number of attempts of voting card ''{0}''.", state.getVotingCardId()), e);
		}
	}

	private void checkRemainingAttempts(final VotingCardState state, final Throwable e)
			throws WrongBallotCastingKeyException, NumberOfAttemptsExceededException {
		if (state.hasRemainingAttempts()) {
			throw new WrongBallotCastingKeyException(state, e);
		} else {
			setVotingCardState(state, VotingCardStates.WRONG_BALLOT_CASTING_KEY);
			throw new NumberOfAttemptsExceededException(state, e);
		}
	}

	private void setVotingCardState(final VotingCardState state, final VotingCardStates value) {
		try {
			stateService.updateVotingCardState(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId(), value);
		} catch (ResourceNotFoundException | ApplicationException | DuplicateEntryException e) {
			throw new EJBException(format("Failed to set the state of voting card ''{0}'' to ''{1}''.", state.getVotingCardId(), value), e);
		}
		state.setState(value);
	}

	private void storeVoteCastMessage(final VotingCardState state, final ShortVoteCastReturnCodeAndComputeResults message) {
		String electionEventId = state.getElectionEventId();
		String votingCardId = state.getVotingCardId();
		boolean stored;
		try {
			stored = codeRepository.storesCastCode(state.getTenantId(), electionEventId, votingCardId, message);
		} catch (ResourceNotFoundException e) {
			throw new EJBException(format("Failed to store vote cast code for voting card ''{0}''.", votingCardId), e);
		}
		if (!stored) {
			throw new EJBException(format("Failed to store vote cast code for voting card ''{0}''.", votingCardId));
		}
	}

	private void validateConfirmation(final String token, final ConfirmationInformation confirmation, final VotingCardState state)
			throws WrongBallotCastingKeyException, NumberOfAttemptsExceededException, ResourceNotFoundException {
		ConfirmationInformationResult result = confirmationRepository
				.validateConfirmationMessage(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId(), confirmation, token);
		if (!result.isValid()) {
			if (result.getValidationError() != null) {
				ValidationErrorType type = result.getValidationError().getValidationErrorType();
				if (ValidationErrorType.WRONG_BALLOT_CASTING_KEY.equals(type)) {
					incrementNumberOfAttempts(state);
					checkRemainingAttempts(state, null);
				}
			}
			throw new WrongBallotCastingKeyException(state);
		}
	}

	private void validateVotingCardState(final VotingCardState state) throws NumberOfAttemptsExceededException, IllegalVotingCardStateException {
		switch (state.getState()) {
		case SENT_BUT_NOT_CAST:
			break;
		case WRONG_BALLOT_CASTING_KEY:
			throw new NumberOfAttemptsExceededException(state);
		default:
			throw new IllegalVotingCardStateException(state);
		}
	}

	private static class CastVoteException extends Exception {
		private static final long serialVersionUID = -6245489356768280082L;

		private final transient VotingCardState state;

		public CastVoteException(final VotingCardState state) {
			this.state = state;
		}

		public CastVoteException(final VotingCardState state, final Throwable throwable) {
			super(throwable);
			this.state = state;
		}

		public VotingCardState getState() {
			return state;
		}

	}

	private static class IllegalVotingCardStateException extends CastVoteException {
		private static final long serialVersionUID = 1022274489710389341L;

		public IllegalVotingCardStateException(final VotingCardState state) {
			super(state);
		}
	}

	private static class NumberOfAttemptsExceededException extends CastVoteException {
		private static final long serialVersionUID = 932620521268368648L;

		public NumberOfAttemptsExceededException(final VotingCardState state) {
			super(state);
		}

		public NumberOfAttemptsExceededException(final VotingCardState state, final Throwable throwable) {
			super(state, throwable);
		}
	}

	private static class WrongBallotCastingKeyException extends CastVoteException {
		private static final long serialVersionUID = 6533401291620561667L;

		public WrongBallotCastingKeyException(final VotingCardState state) {
			super(state);
		}

		public WrongBallotCastingKeyException(final VotingCardState state, final Throwable throwable) {
			super(state, throwable);
		}
	}
}
