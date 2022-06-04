/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.securedatamanager.VotingCardSetServiceTestBase;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.service.VotingCardSetDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(VotingCardSetServiceTestSpringConfig.class)
class VotingCardSetGenerateServiceTest extends VotingCardSetServiceTestBase {

	private static final String ELECTION_EVENT_ID = "a3d790fd1ac543f9b0a05ca79a20c9e2";

	@Autowired
	private IdleStatusService idleStatusService;

	@Autowired
	private VotingCardSetRepository votingCardSetRepository;

	@Autowired
	private VotingCardSetGenerateService votingCardSetGenerateService;

	@Autowired
	private VotingCardSetDataGeneratorService votingCardSetDataGeneratorService;

	@MockBean
	private VotingCardSetGenerateBallotService votingCardSetGenerateBallotService;

	static Stream<Arguments> invalidParameters() {
		return Stream.of(
				Arguments.of(null, VOTING_CARD_SET_ID),
				Arguments.of("", VOTING_CARD_SET_ID),
				Arguments.of(ELECTION_EVENT_ID, null),
				Arguments.of(ELECTION_EVENT_ID, "")
		);
	}

	@ParameterizedTest
	@MethodSource("invalidParameters")
	void generateWithInvalidParametersThrows(final String electionEventId, final String votingCardSetId) {
		when(idleStatusService.getIdLock(votingCardSetId)).thenReturn(true);
		assertThrows(IllegalArgumentException.class, () -> votingCardSetGenerateService.generate(electionEventId, votingCardSetId));
	}

	@Test
	void generateWithUnsuccessfulBallotGenerationFails() throws ResourceNotFoundException, InvalidStatusTransitionException {
		when(idleStatusService.getIdLock(anyString())).thenReturn(true);

		setStatusForVotingCardSetFromRepository("VCS_DOWNLOADED", votingCardSetRepository);

		final DataGeneratorResponse generateBallotServiceResultMock = new DataGeneratorResponse();
		generateBallotServiceResultMock.setSuccessful(false);
		when(votingCardSetGenerateBallotService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID)).thenReturn(generateBallotServiceResultMock);

		assertFalse(votingCardSetGenerateService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}

	@Test
	void generateWithUnsuccessfulVotingCardSetDataGenerationFails() throws ResourceNotFoundException, InvalidStatusTransitionException {
		when(idleStatusService.getIdLock(anyString())).thenReturn(true);

		setStatusForVotingCardSetFromRepository("VCS_DOWNLOADED", votingCardSetRepository);

		final DataGeneratorResponse generateBallotServiceResultMock = new DataGeneratorResponse();
		when(votingCardSetGenerateBallotService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID)).thenReturn(generateBallotServiceResultMock);

		final DataGeneratorResponse votingCardSetDataResultMock = new DataGeneratorResponse();
		votingCardSetDataResultMock.setSuccessful(false);
		when(votingCardSetDataGeneratorService.generate(VOTING_CARD_SET_ID, ELECTION_EVENT_ID)).thenReturn(votingCardSetDataResultMock);

		assertFalse(votingCardSetGenerateService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}

	@Test
	void generateWithInvalidStatusTransitionFails() throws ResourceNotFoundException {
		when(idleStatusService.getIdLock(anyString())).thenReturn(true);

		setStatusForVotingCardSetFromRepository("COMPUTED", votingCardSetRepository);

		assertThrows(InvalidStatusTransitionException.class, () -> votingCardSetGenerateService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID));
	}

	@Test
	void generateWhenGenerationAlreadyStartedSuccess() throws ResourceNotFoundException, InvalidStatusTransitionException {
		when(idleStatusService.getIdLock(anyString())).thenReturn(false);

		assertTrue(votingCardSetGenerateService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}
}