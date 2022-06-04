/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.write;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.VotingCardSetServiceTestBase;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
class VotingCardSetComputationServiceTest extends VotingCardSetServiceTestBase {

	private static final String ELECTION_EVENT_ID = "a3d790fd1ac543f9b0a05ca79a20c9e2";
	private static final String VERIFICATION_CARD_SET_ID = "9a0";
	private static final String PRECOMPUTED_VALUES_PATH = "computeTest";

	@InjectMocks
	private final VotingCardSetComputationService votingCardSetComputationService = new VotingCardSetComputationService();

	@Mock
	private IdleStatusService idleStatusServiceMock;

	@Mock
	private VotingCardSetRepository votingCardSetRepositoryMock;

	@Mock
	private ConfigurationEntityStatusService configurationEntityStatusServiceMock;

	@Mock
	private VotingCardSetChoiceCodesService votingCardSetChoiceCodesServiceMock;

	@Mock
	private ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepositoryMock;

	@Mock
	private PlatformRootCAService platformRootCAService;

	@Mock
	private CryptolibPayloadSignatureService payloadSignatureService;

	@Test
	void compute() throws ResourceNotFoundException, IOException, URISyntaxException, PayloadStorageException, PayloadVerificationException,
			CertificateManagementException {
		setStatusForVotingCardSetFromRepository(Status.PRECOMPUTED.name(), votingCardSetRepositoryMock);

		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			final ReturnCodeGenerationRequestPayload payload = new ReturnCodeGenerationRequestPayload("tenantId", ELECTION_EVENT_ID,
					VERIFICATION_CARD_SET_ID, emptyList(), 0, new GqGroup(BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(3)),
					emptyList(), new CombinedCorrectnessInformation(Collections.emptyList()));

			when(platformRootCAService.load()).thenReturn(null);
			when(votingCardSetRepositoryMock.getVerificationCardSetId(VOTING_CARD_SET_ID)).thenReturn(VERIFICATION_CARD_SET_ID);
			when(configurationEntityStatusServiceMock.update(anyString(), anyString(), any())).thenReturn("");
			when(returnCodeGenerationRequestPayloadRepositoryMock.getCount(ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID)).thenReturn(1);
			when(returnCodeGenerationRequestPayloadRepositoryMock.retrieve(ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID, 0)).thenReturn(payload);
			when(idleStatusServiceMock.getIdLock(anyString())).thenReturn(true);
			when(payloadSignatureService.verify(any(), any())).thenReturn(true);

			final Path representationsFile = getPathOfFileInResources(Paths.get(PRECOMPUTED_VALUES_PATH)).resolve(ELECTION_EVENT_ID)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION).resolve(VERIFICATION_CARD_SET_ID)
					.resolve(Constants.CONFIG_FILE_NAME_ELECTION_OPTION_REPRESENTATIONS);
			write(representationsFile, singletonList("2"));

			assertDoesNotThrow(() -> votingCardSetComputationService.compute(VOTING_CARD_SET_ID, ELECTION_EVENT_ID));

			verify(votingCardSetChoiceCodesServiceMock, times(1)).sendToCompute(payload);
			verify(configurationEntityStatusServiceMock, times(1)).update(anyString(), anyString(), any());
		}
	}

	@Test
	void computeInvalidParams() {
		when(idleStatusServiceMock.getIdLock(anyString())).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> votingCardSetComputationService.compute("", ELECTION_EVENT_ID));
	}

	@Test
	void computeInvalidStatus() throws ResourceNotFoundException {
		setStatusForVotingCardSetFromRepository("SIGNED", votingCardSetRepositoryMock);
		when(idleStatusServiceMock.getIdLock(anyString())).thenReturn(true);

		assertThrows(InvalidStatusTransitionException.class, () -> votingCardSetComputationService.compute(VOTING_CARD_SET_ID, ELECTION_EVENT_ID));
	}

	@Test
	void computeInvalidSignature() throws ResourceNotFoundException, IOException {
		setStatusForVotingCardSetFromRepository(Status.PRECOMPUTED.name(), votingCardSetRepositoryMock);
		when(idleStatusServiceMock.getIdLock(anyString())).thenReturn(true);

		assertDoesNotThrow(() -> votingCardSetComputationService.compute(VOTING_CARD_SET_ID, ELECTION_EVENT_ID));

		verify(votingCardSetChoiceCodesServiceMock, times(0)).sendToCompute(any());
	}
}
