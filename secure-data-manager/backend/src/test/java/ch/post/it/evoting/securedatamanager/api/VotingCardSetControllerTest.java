/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.IdleStatusService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetComputationService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetDownloadService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetPrecomputationService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetPreparationService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetSignService;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.votingcardset.VotingCardSetUpdateInputData;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration
class VotingCardSetControllerTest {

	private final String VALID_ELECTION_EVENT_ID = "17ccbe962cf341bc93208c26e911090c";
	private final String VALID_VOTING_CARD_SET_ID = "17ccbe962cf341bc93208c26e911090c";

	@Mock
	IdleStatusService idleStatusService;

	@Mock
	VotingCardSetRepository votingCardSetRepository;

	@Mock
	VotingCardSetPrecomputationService votingCardSetPrecomputationService;

	@Mock
	VotingCardSetSignService votingCardSetSignService;

	@Mock
	VotingCardSetDownloadService votingCardSetDownloadService;

	@Mock
	VotingCardSetComputationService votingCardSetComputationService;

	@Mock
	VotingCardSetPreparationService votingCardSetPreparationService;

	@InjectMocks
	VotingCardSetController votingCardSetController;

	static Stream<Arguments> correctStatusProvider() {
		return Stream.of(
				Arguments.of(Status.PRECOMPUTED),
				Arguments.of(Status.COMPUTING),
				Arguments.of(Status.VCS_DOWNLOADED)
		);
	}

	@ParameterizedTest
	@MethodSource("correctStatusProvider")
	void testCorrectStatusTransition(final Status status)
			throws GeneralCryptoLibException, PayloadVerificationException, IOException, PayloadStorageException, ResourceNotFoundException,
			PayloadSignatureException {

		final HttpStatus statusCode = votingCardSetController.setVotingCardSetStatus(VALID_ELECTION_EVENT_ID, VALID_VOTING_CARD_SET_ID,
				new VotingCardSetUpdateInputData(status, null, null)).getStatusCode();

		assertEquals(HttpStatus.NO_CONTENT, statusCode);
	}

	@Test
	void testUnsupportedStatusTransition()
			throws ResourceNotFoundException, InvalidStatusTransitionException, IOException, GeneralCryptoLibException, PayloadStorageException,
			PayloadSignatureException, PayloadVerificationException {

		doThrow(InvalidStatusTransitionException.class).when(votingCardSetDownloadService).download(anyString(), anyString());

		final HttpStatus statusCode = votingCardSetController.setVotingCardSetStatus(VALID_ELECTION_EVENT_ID, VALID_VOTING_CARD_SET_ID,
				new VotingCardSetUpdateInputData(Status.VCS_DOWNLOADED, null, null)).getStatusCode();

		assertEquals(HttpStatus.BAD_REQUEST, statusCode);
	}

}
