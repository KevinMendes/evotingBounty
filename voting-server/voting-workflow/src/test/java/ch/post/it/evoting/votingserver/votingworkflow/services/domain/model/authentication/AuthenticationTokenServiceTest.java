/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.verificationset.VerificationSet;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot.BallotRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot.BallotTextRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification.VerificationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.service.VotingCardStateService;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenServiceTest {

	private final String VERIFICATION_CARD_SET_ID = "1";
	private final String TENANT_ID = "100";
	private final String ELECTION_EVENT_ID = "1";
	private final String CREDENTIAL_ID = "1";
	private final String VOTING_CARD_ID = "1";
	private final String BALLOT_ID = "1";
	private final String BALLOT_BOX_ID = "1";
	private final String VERIFICATION_CARD_ID = "1";
	@InjectMocks
	AuthenticationTokenService sut = new AuthenticationTokenService();
	// The EJB instance for retrieving authentication tokens
	@Mock
	private AuthenticationTokenRepository authenticationTokenRepository;
	// The EJB instance for retrieving ballot information
	@Mock
	private BallotRepository ballotRepository;
	// The EJB instance for retrieving ballot information
	@Mock
	private BallotBoxInformationRepository ballotBoxInformationRepository;
	// The EJB instance for retrieving ballot information
	@Mock
	private VerificationRepository verificationRepository;
	// The EJB instance for retrieving ballot text
	@Mock
	private BallotTextRepository ballotTextRepository;
	// The voting card state service
	@Mock
	private VotingCardStateService votingCardStateService;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.initMocks(AuthenticationTokenServiceTest.class);
	}

	@Test
	public void testValidateAuthenticationToken() throws IOException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setId("1");

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(true);

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		when(authenticationTokenRepository
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, objectMapper.writeValueAsString(authenticationToken)))
				.thenReturn(validationResultMock);
		sut.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, objectMapper.writeValueAsString(authenticationToken));

		assertTrue(validationResultMock.isResult());
	}

	@Test
	public void testGetAuthenticationTokenSuccessful()
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		VoterInformation voterInformationMock = new VoterInformation();
		voterInformationMock.setVotingCardId(VOTING_CARD_ID);
		voterInformationMock.setBallotId(BALLOT_ID);
		voterInformationMock.setBallotBoxId(BALLOT_BOX_ID);
		voterInformationMock.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformationMock.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);

		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();
		authTokenMessageMock.setAuthenticationToken(new AuthenticationToken(voterInformationMock, "base64authToken", "timestamp", "signature"));
		authTokenMessageMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);

		Verification verificationMock = new Verification();
		verificationMock.setId("1");
		verificationMock.setVerificationCardKeystore("1");

		VerificationSet verificationSetMock = new VerificationSet();
		verificationSetMock.setId("1");

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenReturn(authTokenMessageMock);
		when(votingCardStateService.getVotingCardState(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(votingCardStateMock);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID)).thenReturn("{}");
		when(ballotTextRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID))
				.thenReturn("{ \"ballotTexts\": [ \"text1\", \"text2\" ], \"ballotTextsSignature\": [ \"signature1\",  \"signature2\" ] }");
		when(ballotBoxInformationRepository.getBallotBoxInfoByTenantIdElectionEventIdBallotBoxId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID))
				.thenReturn("{}");
		when(verificationRepository.findByTenantElectionEventVotingCard(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID))
				.thenReturn(verificationMock);

		JsonObject authToken = sut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
		Assert.assertNotNull(authToken);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetAuthenticationToken_AuthTokenNotFound()
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		VoterInformation voterInformationMock = new VoterInformation();
		voterInformationMock.setVotingCardId(VOTING_CARD_ID);
		voterInformationMock.setBallotId(BALLOT_ID);
		voterInformationMock.setBallotBoxId(BALLOT_BOX_ID);
		voterInformationMock.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformationMock.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);

		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();
		authTokenMessageMock.setAuthenticationToken(new AuthenticationToken(voterInformationMock, "base64authToken", "timestamp", "signature"));
		authTokenMessageMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);

		Verification verificationMock = new Verification();
		verificationMock.setId("1");
		verificationMock.setVerificationCardKeystore("1");

		VerificationSet verificationSetMock = new VerificationSet();
		verificationSetMock.setId("1");

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenThrow(new ResourceNotFoundException("exception"));

		sut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test(expected = ApplicationException.class)
	public void testGetAuthenticationToken_VoteCardStateNotFound()
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		VoterInformation voterInformationMock = new VoterInformation();
		voterInformationMock.setVotingCardId(VOTING_CARD_ID);
		voterInformationMock.setBallotId(BALLOT_ID);
		voterInformationMock.setBallotBoxId(BALLOT_BOX_ID);
		voterInformationMock.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformationMock.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);

		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();
		authTokenMessageMock.setAuthenticationToken(new AuthenticationToken(voterInformationMock, "base64authToken", "timestamp", "signature"));
		authTokenMessageMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);

		Verification verificationMock = new Verification();
		verificationMock.setId("1");
		verificationMock.setVerificationCardKeystore("1");

		VerificationSet verificationSetMock = new VerificationSet();
		verificationSetMock.setId("1");

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenReturn(authTokenMessageMock);
		when(votingCardStateService.getVotingCardState(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenThrow(new ApplicationException("exception"));

		sut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetAuthenticationToken_BallotNotFound()
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		VoterInformation voterInformationMock = new VoterInformation();
		voterInformationMock.setVotingCardId(VOTING_CARD_ID);
		voterInformationMock.setBallotId(BALLOT_ID);
		voterInformationMock.setBallotBoxId(BALLOT_BOX_ID);
		voterInformationMock.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformationMock.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);

		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();
		authTokenMessageMock.setAuthenticationToken(new AuthenticationToken(voterInformationMock, "base64authToken", "timestamp", "signature"));
		authTokenMessageMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);

		Verification verificationMock = new Verification();
		verificationMock.setId("1");
		verificationMock.setVerificationCardKeystore("1");

		VerificationSet verificationSetMock = new VerificationSet();
		verificationSetMock.setId("1");

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenReturn(authTokenMessageMock);
		when(votingCardStateService.getVotingCardState(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(votingCardStateMock);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID))
				.thenThrow(new ResourceNotFoundException("exception"));

		sut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetAuthenticationToken_BallotTextNotFound()
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		VoterInformation voterInformationMock = new VoterInformation();
		voterInformationMock.setVotingCardId(VOTING_CARD_ID);
		voterInformationMock.setBallotId(BALLOT_ID);
		voterInformationMock.setBallotBoxId(BALLOT_BOX_ID);
		voterInformationMock.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformationMock.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);

		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();
		authTokenMessageMock.setAuthenticationToken(new AuthenticationToken(voterInformationMock, "base64authToken", "timestamp", "signature"));
		authTokenMessageMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);

		Verification verificationMock = new Verification();
		verificationMock.setId("1");
		verificationMock.setVerificationCardKeystore("1");

		VerificationSet verificationSetMock = new VerificationSet();
		verificationSetMock.setId("1");

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenReturn(authTokenMessageMock);
		when(votingCardStateService.getVotingCardState(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(votingCardStateMock);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID)).thenReturn("{}");
		when(ballotTextRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID))
				.thenThrow(new ResourceNotFoundException("exception"));

		sut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetAuthenticationToken_BallotBoxInfoNotFound()
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		VoterInformation voterInformationMock = new VoterInformation();
		voterInformationMock.setVotingCardId(VOTING_CARD_ID);
		voterInformationMock.setBallotId(BALLOT_ID);
		voterInformationMock.setBallotBoxId(BALLOT_BOX_ID);
		voterInformationMock.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformationMock.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);

		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();
		authTokenMessageMock.setAuthenticationToken(new AuthenticationToken(voterInformationMock, "base64authToken", "timestamp", "signature"));
		authTokenMessageMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);

		Verification verificationMock = new Verification();
		verificationMock.setId("1");
		verificationMock.setVerificationCardKeystore("1");

		VerificationSet verificationSetMock = new VerificationSet();
		verificationSetMock.setId("1");

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenReturn(authTokenMessageMock);
		when(votingCardStateService.getVotingCardState(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(votingCardStateMock);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID)).thenReturn("{}");
		when(ballotTextRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID))
				.thenReturn("{ \"ballotTexts\": [ \"text1\", \"text2\" ], \"ballotTextsSignature\": [ \"signature1\",  \"signature2\" ] }");
		when(ballotBoxInformationRepository.getBallotBoxInfoByTenantIdElectionEventIdBallotBoxId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID))
				.thenThrow(new ResourceNotFoundException("exception"));

		sut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetAuthenticationToken_VerificationNotFound()
			throws ApplicationException, ResourceNotFoundException, IOException, DuplicateEntryException, EntryPersistenceException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		VoterInformation voterInformationMock = new VoterInformation();
		voterInformationMock.setVotingCardId(VOTING_CARD_ID);
		voterInformationMock.setBallotId(BALLOT_ID);
		voterInformationMock.setBallotBoxId(BALLOT_BOX_ID);
		voterInformationMock.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformationMock.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);

		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();
		authTokenMessageMock.setAuthenticationToken(new AuthenticationToken(voterInformationMock, "base64authToken", "timestamp", "signature"));
		authTokenMessageMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);

		Verification verificationMock = new Verification();
		verificationMock.setId("1");
		verificationMock.setVerificationCardKeystore("1");

		VerificationSet verificationSetMock = new VerificationSet();
		verificationSetMock.setId("1");

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenReturn(authTokenMessageMock);
		when(votingCardStateService.getVotingCardState(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(votingCardStateMock);
		when(ballotRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID)).thenReturn("{}");
		when(ballotTextRepository.findByTenantIdElectionEventIdBallotId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_ID))
				.thenReturn("{ \"ballotTexts\": [ \"text1\", \"text2\" ], \"ballotTextsSignature\": [ \"signature1\",  \"signature2\" ] }");
		when(ballotBoxInformationRepository.getBallotBoxInfoByTenantIdElectionEventIdBallotBoxId(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID))
				.thenReturn("{}");
		when(verificationRepository.findByTenantElectionEventVotingCard(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID))
				.thenThrow(new ResourceNotFoundException("exception"));

		sut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

}
