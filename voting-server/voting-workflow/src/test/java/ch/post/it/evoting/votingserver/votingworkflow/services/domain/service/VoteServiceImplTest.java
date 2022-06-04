/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AdminBoardCertificateRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.choicecode.ChoiceCodeRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.validation.ValidationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

@ExtendWith(MockitoExtension.class)
class VoteServiceImplTest {

	private final String TENANT_ID = "100";
	private final String ELECTION_EVENT_ID = "1";
	private final String VOTING_CARD_ID = "3cc7e2a0dd394fae8d9bd2ebd4fa4b95";
	private final String VERIFICATION_CARD_ID = "4dffac3879e443d3a3634929f6a2eb07";
	private final String CHOICE_CODES = "THECHOICECODES";
	private final String CHOICE_CODES_COMPUTATION_RESULTS = "THECOMPUTATIONRESULTS";
	private final String CHOICE_CODES_DECRYPTION_RESULTS = "THEDECRYPTIONRESULTS";

	@InjectMocks
	private VoteServiceImpl voteServiceImpl;

	@Mock
	private VotingCardStateService votingCardStateService;

	@Mock
	private ChoiceCodeRepository choiceCodeRepository;

	@Mock
	private ValidationRepository validationRepository;

	@Mock
	private AdminBoardCertificateRepository adminBoardCertificateRepository;

	// Must be mocked, even if not use in this test class
	@Mock
	private VoteRepository voteRepository;

	@Test
	void testValidateAndStoreVoteHappyPath() throws ApplicationException, IOException, DuplicateEntryException, ResourceNotFoundException {

		VotingCardState votingCardState = new VotingCardState();
		votingCardState.setState(VotingCardStates.NOT_SENT);

		ShortChoiceReturnCodeAndComputeResults choiceCode = new ShortChoiceReturnCodeAndComputeResults();
		choiceCode.setShortChoiceReturnCodes(CHOICE_CODES);
		choiceCode.setComputationResults(CHOICE_CODES_COMPUTATION_RESULTS);
		choiceCode.setDecryptionResults(CHOICE_CODES_DECRYPTION_RESULTS);

		ValidationVoteResult successValidationVoteResult = new ValidationVoteResult();
		successValidationVoteResult.setValid(true);

		Vote vote = mock(Vote.class);

		when(votingCardStateService.getVotingCardState(anyString(), anyString(), anyString())).thenReturn(votingCardState);
		when(validationRepository.validateVote(anyString(), anyString(), any())).thenReturn(successValidationVoteResult);
		when(choiceCodeRepository.generateChoiceCodes(anyString(), anyString(), anyString(), any())).thenReturn(choiceCode);

		AdminBoardCertificates adminBoardCertificates = mock(AdminBoardCertificates.class);

		when(adminBoardCertificateRepository.findByTenantElectionEventCertificates(anyString(), anyString())).thenReturn(adminBoardCertificates);

		ValidationVoteResult validationVoteResult = voteServiceImpl
				.validateVoteAndStore(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, VERIFICATION_CARD_ID, vote, "");

		assertTrue(validationVoteResult.isValid());
	}

	@Test
	void testValidateAndStoreVoteWhenVotingCardIsBlockedAndGetNotValidResult()
			throws ApplicationException, IOException, DuplicateEntryException, ResourceNotFoundException {

		VotingCardState votingCardState = new VotingCardState();
		votingCardState.setState(VotingCardStates.BLOCKED);

		Vote vote = mock(Vote.class);

		when(votingCardStateService.getVotingCardState(anyString(), anyString(), anyString())).thenReturn(votingCardState);

		ValidationVoteResult validationVoteResult = voteServiceImpl
				.validateVoteAndStore(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, VERIFICATION_CARD_ID, vote, "");

		String[] errorArgs = validationVoteResult.getValidationError().getErrorArgs();

		assertFalse(validationVoteResult.isValid());
		assertEquals(VotingCardStates.BLOCKED.name(), errorArgs[0]);
	}

	@Test
	void testValidateAndStoreVoteWhenVotingCardDidNotVoteAndGetValidResult()
			throws ApplicationException, IOException, DuplicateEntryException, ResourceNotFoundException {

		VotingCardState votingCardState = new VotingCardState();
		votingCardState.setState(VotingCardStates.NOT_SENT);

		Vote vote = mock(Vote.class);

		ShortChoiceReturnCodeAndComputeResults choiceCode = new ShortChoiceReturnCodeAndComputeResults();
		choiceCode.setShortChoiceReturnCodes(CHOICE_CODES);
		choiceCode.setComputationResults(CHOICE_CODES_COMPUTATION_RESULTS);
		choiceCode.setDecryptionResults(CHOICE_CODES_DECRYPTION_RESULTS);

		ValidationVoteResult successValidationVoteResult = new ValidationVoteResult();
		successValidationVoteResult.setValid(true);

		when(validationRepository.validateVote(anyString(), anyString(), any())).thenReturn(successValidationVoteResult);
		when(choiceCodeRepository.generateChoiceCodes(anyString(), anyString(), anyString(), any())).thenReturn(choiceCode);
		when(votingCardStateService.getVotingCardState(anyString(), anyString(), anyString())).thenReturn(votingCardState);

		AdminBoardCertificates adminBoardCertificates = mock(AdminBoardCertificates.class);

		when(adminBoardCertificateRepository.findByTenantElectionEventCertificates(anyString(), anyString())).thenReturn(adminBoardCertificates);

		ValidationVoteResult validationVoteResult = voteServiceImpl
				.validateVoteAndStore(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, VERIFICATION_CARD_ID, vote, "");

		assertTrue(validationVoteResult.isValid());
	}

	@Test
	void testValidateAndStoreVoteWhenVotingCardDidNotVoteAndGetNotValidResult()
			throws ApplicationException, IOException, DuplicateEntryException, ResourceNotFoundException {

		VotingCardState votingCardState = new VotingCardState();
		votingCardState.setState(VotingCardStates.NOT_SENT);

		Vote vote = mock(Vote.class);

		ShortChoiceReturnCodeAndComputeResults choiceCode = new ShortChoiceReturnCodeAndComputeResults();
		choiceCode.setShortChoiceReturnCodes(CHOICE_CODES);

		ValidationVoteResult successValidationVoteResult = new ValidationVoteResult();
		successValidationVoteResult.setValid(false);

		when(validationRepository.validateVote(anyString(), anyString(), any())).thenReturn(successValidationVoteResult);
		when(votingCardStateService.getVotingCardState(anyString(), anyString(), anyString())).thenReturn(votingCardState);

		ValidationVoteResult validationVoteResult = voteServiceImpl
				.validateVoteAndStore(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, VERIFICATION_CARD_ID, vote, "");

		assertFalse(validationVoteResult.isValid());
	}

	@Test
	void testValidateAndStoreVoteWhenVotingCardAlreadyVotedAndGetNotValidResult()
			throws ApplicationException, IOException, DuplicateEntryException, ResourceNotFoundException {

		VotingCardState votingCardState = new VotingCardState();
		votingCardState.setState(VotingCardStates.CAST);

		Vote vote = mock(Vote.class);

		when(votingCardStateService.getVotingCardState(anyString(), anyString(), anyString())).thenReturn(votingCardState);

		ValidationVoteResult validationVoteResult = voteServiceImpl
				.validateVoteAndStore(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, VERIFICATION_CARD_ID, vote, "");

		assertFalse(validationVoteResult.isValid());
	}

}
