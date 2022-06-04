/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.securedatamanager.VotingCardSetServiceTestBase;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotBoxDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(VotingCardSetServiceTestSpringConfig.class)
class VotingCardSetGenerateBallotServiceTest extends VotingCardSetServiceTestBase {

	private static final String ELECTION_EVENT_ID = "a3d790fd1ac543f9b0a05ca79a20c9e2";

	@Autowired
	private BallotBoxRepository ballotBoxRepository;

	@Autowired
	private VotingCardSetRepository votingCardSetRepository;

	@Autowired
	private BallotDataGeneratorService ballotDataGeneratorService;

	@Autowired
	private BallotBoxDataGeneratorService ballotBoxDataGeneratorService;

	@Autowired
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
		assertThrows(IllegalArgumentException.class, () -> votingCardSetGenerateBallotService.generate(electionEventId, votingCardSetId));
	}

	@Test
	void generateWhenBallotBoxIdIsEmptyFails() {
		when(votingCardSetRepository.getBallotBoxId(VOTING_CARD_SET_ID)).thenReturn("");

		assertFalse(votingCardSetGenerateBallotService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}

	@Test
	void generateWhenBallotIdIsEmptyFails() {
		when(votingCardSetRepository.getBallotBoxId(VOTING_CARD_SET_ID)).thenReturn(BALLOT_BOX_ID);

		when(ballotBoxRepository.getBallotId(BALLOT_BOX_ID)).thenReturn("");

		assertFalse(votingCardSetGenerateBallotService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}

	@Test
	void generateWhenUnsuccessfulBallotDataGenerationFails() {
		when(votingCardSetRepository.getBallotBoxId(VOTING_CARD_SET_ID)).thenReturn(BALLOT_BOX_ID);

		when(ballotBoxRepository.getBallotId(BALLOT_BOX_ID)).thenReturn(BALLOT_ID);

		final DataGeneratorResponse ballotDataResult = new DataGeneratorResponse();
		ballotDataResult.setSuccessful(false);
		when(ballotDataGeneratorService.generate(BALLOT_ID, ELECTION_EVENT_ID)).thenReturn(ballotDataResult);

		assertFalse(votingCardSetGenerateBallotService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}

	@Test
	void generateWhenBallotBoxReturnWrongStatusFails() {
		when(votingCardSetRepository.getBallotBoxId(VOTING_CARD_SET_ID)).thenReturn(BALLOT_BOX_ID);

		when(ballotBoxRepository.getBallotId(BALLOT_BOX_ID)).thenReturn(BALLOT_ID);

		final DataGeneratorResponse ballotDataResult = new DataGeneratorResponse();
		when(ballotDataGeneratorService.generate(BALLOT_ID, ELECTION_EVENT_ID)).thenReturn(ballotDataResult);

		when(ballotBoxRepository.find(BALLOT_BOX_ID)).thenReturn(getBallotBoxWithStatus(Status.LOCKED));

		final DataGeneratorResponse ballotBoxDataResult = new DataGeneratorResponse();
		ballotBoxDataResult.setSuccessful(false);
		when(ballotBoxDataGeneratorService.generate(BALLOT_BOX_ID, ELECTION_EVENT_ID)).thenReturn(ballotBoxDataResult);

		assertFalse(votingCardSetGenerateBallotService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}

	@Test
	void generateSuccessful() {
		when(votingCardSetRepository.getBallotBoxId(VOTING_CARD_SET_ID)).thenReturn(BALLOT_BOX_ID);

		when(ballotBoxRepository.getBallotId(BALLOT_BOX_ID)).thenReturn(BALLOT_ID);

		final DataGeneratorResponse ballotDataResult = new DataGeneratorResponse();
		when(ballotDataGeneratorService.generate(BALLOT_ID, ELECTION_EVENT_ID)).thenReturn(ballotDataResult);

		when(ballotBoxRepository.find(BALLOT_BOX_ID)).thenReturn(getBallotBoxWithStatus(Status.LOCKED));

		final DataGeneratorResponse ballotBoxDataResult = new DataGeneratorResponse();
		when(ballotBoxDataGeneratorService.generate(BALLOT_BOX_ID, ELECTION_EVENT_ID)).thenReturn(ballotBoxDataResult);

		assertTrue(votingCardSetGenerateBallotService.generate(ELECTION_EVENT_ID, VOTING_CARD_SET_ID).isSuccessful());
	}
}
