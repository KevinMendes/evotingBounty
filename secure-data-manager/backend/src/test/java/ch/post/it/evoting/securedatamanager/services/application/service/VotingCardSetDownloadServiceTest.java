/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.securedatamanager.VotingCardSetServiceTestBase;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(VotingCardSetServiceTestSpringConfig.class)
class VotingCardSetDownloadServiceTest extends VotingCardSetServiceTestBase {

	private static final String ELECTION_EVENT_ID = "a3d790fd1ac543f9b0a05ca79a20c9e2";
	private static final String VERIFICATION_CARD_SET_ID = "9a0";
	private static final String PRECOMPUTED_VALUES_PATH = "computeTest";
	private static final String ONLINE_PATH = "ONLINE";
	private static final String VOTE_VERIFICATION_FOLDER = "voteVerification";

	@Autowired
	private VotingCardSetDownloadService votingCardSetDownloadService;

	@Autowired
	private VotingCardSetRepository votingCardSetRepositoryMock;

	@Autowired
	private IdleStatusService idleStatusService;

	@Autowired
	private PathResolver pathResolver;

	@Autowired
	private ConfigurationEntityStatusService configurationEntityStatusServiceMock;

	@Autowired
	private VotingCardSetChoiceCodesService votingCardSetChoiceCodesServiceMock;

	@Autowired
	private ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository;

	@Test
	void download() throws ResourceNotFoundException, URISyntaxException, PayloadStorageException, IOException {
		setStatusForVotingCardSetFromRepository(Status.COMPUTED.name(), votingCardSetRepositoryMock);

		final Path basePath = getPathOfFileInResources(Paths.get(PRECOMPUTED_VALUES_PATH));
		final Path folder = basePath.resolve(ELECTION_EVENT_ID).resolve(ONLINE_PATH).resolve(VOTE_VERIFICATION_FOLDER)
				.resolve(VERIFICATION_CARD_SET_ID);

		when(pathResolver.resolve(any())).thenReturn(basePath);
		when(configurationEntityStatusServiceMock.update(anyString(), anyString(), any())).thenReturn("");
		when(returnCodeGenerationRequestPayloadRepository.getCount(ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID)).thenReturn(3);
		when(votingCardSetChoiceCodesServiceMock.download(anyString(), anyString(), anyInt()))
				.thenReturn(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), new ByteArrayInputStream(new byte[] { 4, 5, 6 }),
						new ByteArrayInputStream(new byte[] { 7, 8, 9 }));
		when(idleStatusService.getIdLock(anyString())).thenReturn(true);

		assertAll(() -> assertDoesNotThrow(() -> votingCardSetDownloadService.download(VOTING_CARD_SET_ID, ELECTION_EVENT_ID)),
				() -> assertTrue(exists(folder.resolve(Constants.CONFIG_FILE_NAME_NODE_CONTRIBUTIONS + ".0" + Constants.JSON))),
				() -> assertTrue(exists(folder.resolve(Constants.CONFIG_FILE_NAME_NODE_CONTRIBUTIONS + ".1" + Constants.JSON))),
				() -> assertTrue(exists(folder.resolve(Constants.CONFIG_FILE_NAME_NODE_CONTRIBUTIONS + ".2" + Constants.JSON))));

	}

	@Test
	void downloadInvalidStatus() throws ResourceNotFoundException {
		setStatusForVotingCardSetFromRepository("SIGNED", votingCardSetRepositoryMock);

		when(idleStatusService.getIdLock(anyString())).thenReturn(true);

		assertThrows(InvalidStatusTransitionException.class, () -> votingCardSetDownloadService.download(VOTING_CARD_SET_ID, ELECTION_EVENT_ID));
	}
}
