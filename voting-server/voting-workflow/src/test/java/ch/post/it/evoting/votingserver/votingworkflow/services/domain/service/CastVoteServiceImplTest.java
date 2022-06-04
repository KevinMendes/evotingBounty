/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.confirmation.ConfirmationMessage;
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
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastMessage;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;

/**
 * Tests of {@link CastVoteServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CastVoteServiceImplTest {
	private static final String AUTHENTICATION_TOKEN = "{\"signature\":\"value\"}";
	@InjectMocks
	private final CastVoteServiceImpl service = new CastVoteServiceImpl();
	private VoterInformation voter;
	private ConfirmationInformation confirmation;
	private VotingCardState state;
	private ConfirmationInformationResult confirmationResult;
	private ShortVoteCastReturnCodeAndComputeResults codeComputationResult;
	private VoteCastResult result;
	@Mock
	private VotingCardStateService stateService;
	@Mock
	private VoteCastCodeRepository codeRepository;
	@Mock
	private VoteCastCodeService codeService;
	@Mock
	private ConfirmationInformationRepository confirmationRepository;

	@Before
	public void setUp()
			throws ApplicationException, CryptographicOperationException, ResourceNotFoundException, IOException, DuplicateEntryException {
		voter = new VoterInformation();
		voter.setBallotBoxId("ballotBoxId");
		voter.setBallotId("ballotId");
		voter.setCredentialId("credentialId");
		voter.setElectionEventId("electionEventId");
		voter.setId(1);
		voter.setTenantId("tenantId");
		voter.setVerificationCardId("verificationCardId");
		voter.setVerificationCardSetId("verificationCardSetId");
		voter.setVotingCardId("votingCardId");
		voter.setVotingCardSetId("votingCardSetId");

		confirmation = new ConfirmationInformation();
		confirmation.setCredentialId("credentialId");
		ConfirmationMessage confirmationMessage = new ConfirmationMessage();
		confirmationMessage.setConfirmationKey("confirmationKey");
		confirmation.setConfirmationMessage(confirmationMessage);

		state = new VotingCardState();
		state.setAttempts(0L);
		state.setElectionEventId(voter.getElectionEventId());
		state.setTenantId(voter.getTenantId());
		state.setVotingCardId(voter.getVotingCardId());
		state.setState(VotingCardStates.SENT_BUT_NOT_CAST);

		confirmationResult = new ConfirmationInformationResult();
		confirmationResult.setElectionEventId(voter.getElectionEventId());
		confirmationResult.setValid(true);
		confirmationResult.setVotingCardId(voter.getVotingCardId());

		codeComputationResult = new ShortVoteCastReturnCodeAndComputeResults();
		codeComputationResult.setShortVoteCastReturnCode("voteCastCode");

		result = new VoteCastResult();
		result.setElectionEventId(state.getElectionEventId());
		result.setValid(true);
		result.setVerificationCardId(voter.getVerificationCardId());
		result.setVoteCastMessage(new VoteCastMessage(codeComputationResult.getShortVoteCastReturnCode()));
		result.setVotingCardId(state.getVotingCardId());

		when(stateService.getVotingCardState(voter.getTenantId(), voter.getElectionEventId(), voter.getVotingCardId())).thenReturn(state);
		// this is actual behavior of the dependency
		doAnswer(i -> {
			state.incrementAttempts();
			return null;
		}).when(stateService).incrementVotingCardAttempts(voter.getTenantId(), voter.getElectionEventId(), voter.getVotingCardId());
		AuthenticationToken authenticationTokenObject = DomainObjectMapper.getNewInstance()
				.readValue(AUTHENTICATION_TOKEN, AuthenticationToken.class);
		when(codeRepository.generateCastCode(state.getTenantId(), state.getElectionEventId(), voter.getVerificationCardId(), voter.getVotingCardId(),
				authenticationTokenObject.getSignature(), confirmationMessage)).thenReturn(codeComputationResult);
		when(codeRepository.storesCastCode(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId(), codeComputationResult))
				.thenReturn(true);

		when(codeService
				.generateVoteCastResult(state.getElectionEventId(), state.getVotingCardId(), voter.getVerificationCardId(), codeComputationResult))
				.thenReturn(result);

		when(confirmationRepository
				.validateConfirmationMessage(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId(), confirmation,
						AUTHENTICATION_TOKEN)).thenReturn(confirmationResult);
	}

	@Test
	public void testCastVote() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		Assert.assertEquals(result, service.castVote(AUTHENTICATION_TOKEN, voter, confirmation));
		verify(stateService).incrementVotingCardAttempts(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId());
		verify(stateService).updateVotingCardState(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId(), VotingCardStates.CAST);
		verify(codeRepository).storesCastCode(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId(), codeComputationResult);
	}

	@Test
	public void testCastVoteFailedLastAttempt()
			throws ResourceNotFoundException, ApplicationException, DuplicateEntryException, CryptographicOperationException {
		state.setAttempts(4L);
		when(codeRepository.generateCastCode(anyString(), anyString(), anyString(), anyString(), anyString(), any(ConfirmationMessage.class)))
				.thenThrow(new CryptographicOperationException("test"));
		VoteCastResult result = service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
		assertFalse(result.isValid());
		ValidationError error = result.getValidationError();
		assertEquals(ValidationErrorType.BCK_ATTEMPTS_EXCEEDED, error.getValidationErrorType());
		assertEquals(voter.getElectionEventId(), result.getElectionEventId());
		assertEquals(voter.getVotingCardId(), result.getVotingCardId());
		assertEquals(voter.getVerificationCardId(), result.getVerificationCardId());
		assertNull(result.getVoteCastMessage());
		verify(stateService).incrementVotingCardAttempts(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId());
		verify(stateService).updateVotingCardState(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId(),
				VotingCardStates.WRONG_BALLOT_CASTING_KEY);
	}

	@Test
	public void testCastVoteWrongBallotCastingKeyState() throws ResourceNotFoundException {
		state.setState(VotingCardStates.WRONG_BALLOT_CASTING_KEY);
		VoteCastResult result = service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
		assertFalse(result.isValid());
		ValidationError error = result.getValidationError();
		assertEquals(ValidationErrorType.BCK_ATTEMPTS_EXCEEDED, error.getValidationErrorType());
		assertEquals(voter.getElectionEventId(), result.getElectionEventId());
		assertEquals(voter.getVotingCardId(), result.getVotingCardId());
		assertEquals(voter.getVerificationCardId(), result.getVerificationCardId());
		assertNull(result.getVoteCastMessage());
	}

	@Test
	public void testCastVoteIllegalState() throws ResourceNotFoundException {
		state.setState(VotingCardStates.CAST);
		VoteCastResult result = service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
		assertFalse(result.isValid());
		assertEquals(voter.getElectionEventId(), result.getElectionEventId());
		assertEquals(voter.getVotingCardId(), result.getVotingCardId());
		assertEquals(voter.getVerificationCardId(), result.getVerificationCardId());
		assertNull(result.getVoteCastMessage());
	}

	@Test
	public void testCastVoteFailedConfirmation() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		state.setAttempts(2L);
		confirmationResult.setValid(false);
		VoteCastResult result = service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
		assertFalse(result.isValid());
		ValidationError error = result.getValidationError();
		assertEquals(ValidationErrorType.WRONG_BALLOT_CASTING_KEY, error.getValidationErrorType());
		assertEquals(voter.getElectionEventId(), result.getElectionEventId());
		assertEquals(voter.getVotingCardId(), result.getVotingCardId());
		assertEquals(voter.getVerificationCardId(), result.getVerificationCardId());
		assertNull(result.getVoteCastMessage());
		verify(stateService, never()).incrementVotingCardAttempts(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId());
	}

	@Test
	public void testCastVoteFailedConfirmationDueToZeroBCK() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		state.setAttempts(2L);
		confirmationResult.setValid(false);
		ValidationError errZeroBCK = new ValidationError();
		errZeroBCK.setValidationErrorType(ValidationErrorType.WRONG_BALLOT_CASTING_KEY);
		confirmationResult.setValidationError(errZeroBCK);
		VoteCastResult result = service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
		assertFalse(result.isValid());
		ValidationError error = result.getValidationError();
		assertEquals(ValidationErrorType.WRONG_BALLOT_CASTING_KEY, error.getValidationErrorType());
		assertEquals(voter.getElectionEventId(), result.getElectionEventId());
		assertEquals(voter.getVotingCardId(), result.getVotingCardId());
		assertEquals(voter.getVerificationCardId(), result.getVerificationCardId());
		assertNull(result.getVoteCastMessage());
		verify(stateService).incrementVotingCardAttempts(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId());
	}

	@Test
	public void testCastVoteFailedToGenerateVoteCastMessage()
			throws CryptographicOperationException, ResourceNotFoundException, ApplicationException, DuplicateEntryException, IOException {
		state.setAttempts(2L);
		AuthenticationToken authenticationTokenObject = DomainObjectMapper.getNewInstance()
				.readValue(AUTHENTICATION_TOKEN, AuthenticationToken.class);
		when(codeRepository.generateCastCode(state.getTenantId(), state.getElectionEventId(), voter.getVerificationCardId(), voter.getVotingCardId(),
				authenticationTokenObject.getSignature(), confirmation.getConfirmationMessage()))
				.thenThrow(new CryptographicOperationException("test"));
		VoteCastResult result = service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
		assertFalse(result.isValid());
		ValidationError error = result.getValidationError();
		assertEquals(ValidationErrorType.WRONG_BALLOT_CASTING_KEY, error.getValidationErrorType());
		assertEquals(voter.getElectionEventId(), result.getElectionEventId());
		assertEquals(voter.getVotingCardId(), result.getVotingCardId());
		assertEquals(voter.getVerificationCardId(), result.getVerificationCardId());
		assertNull(result.getVoteCastMessage());
		verify(stateService).incrementVotingCardAttempts(state.getTenantId(), state.getElectionEventId(), state.getVotingCardId());
	}

	@Test(expected = EJBException.class)
	public void testCastVoteFailedToStoreVoteCastMessage() throws ResourceNotFoundException {
		when(codeRepository.storesCastCode(state.getTenantId(), state.getElectionEventId(), voter.getVotingCardId(), codeComputationResult))
				.thenReturn(false);
		service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
	}

	@Test(expected = EJBException.class)
	public void testCastVoteFailedToGenerateVoteCastResult() throws ResourceNotFoundException, IOException {
		when(codeService
				.generateVoteCastResult(state.getElectionEventId(), state.getVotingCardId(), voter.getVerificationCardId(), codeComputationResult))
				.thenThrow(new IOException("test"));
		service.castVote(AUTHENTICATION_TOKEN, voter, confirmation);
	}
}
