/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.securedatamanager.configuration.setuptally.SetupTallyEBService;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenerateVerificationCardSetKeysService;
import ch.post.it.evoting.securedatamanager.services.application.service.ElectionEventService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
class ElectoralBoardConstitutionServiceTest {

	private static final String ELECTION_EVENT_ID = "0b149cfdaad04b04b990c3b1d4ca7639";
	private static final String CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_1 =
			ElectoralBoardConstitutionServiceTest.class.getSimpleName() + "/controlComponentPublicKeysPayload.1.json";
	private static final String CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_2 =
			ElectoralBoardConstitutionServiceTest.class.getSimpleName() + "/controlComponentPublicKeysPayload.2.json";
	private static final String CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_3 =
			ElectoralBoardConstitutionServiceTest.class.getSimpleName() + "/controlComponentPublicKeysPayload.3.json";
	private static final String CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_4 =
			ElectoralBoardConstitutionServiceTest.class.getSimpleName() + "/controlComponentPublicKeysPayload.4.json";

	private static final ControlComponentPublicKeysService controlComponentPublicKeysServiceMock = mock(ControlComponentPublicKeysService.class);
	private static final GenerateVerificationCardSetKeysService generateVerificationCardSetKeysService =
			new GenerateVerificationCardSetKeysService(new ElGamalService());
	private static final SetupTallyEBService setupTallyEBService = new SetupTallyEBService(new RandomService(), new ElGamalService());

	private static final VotingCardSetRepository votingCardSetRepositoryMock = mock(VotingCardSetRepository.class);
	private static final BallotBoxRepository ballotBoxRepositoryMock = mock(BallotBoxRepository.class);
	private static final ElectionEventService electionEventServiceMock = mock(ElectionEventService.class);

	private static PathResolver pathResolver;
	private static ElectoralBoardConstitutionService electoralBoardConstitutionService;

	@BeforeAll
	static void setUp(
			@TempDir
			final Path tempDir) throws IOException, URISyntaxException {

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

		final Path controlComponentPublicKeysPayload1 =
				Paths.get(ElectoralBoardConstitutionServiceTest.class.getClassLoader().getResource(CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_1).toURI());
		final Path controlComponentPublicKeysPayload2 =
				Paths.get(ElectoralBoardConstitutionServiceTest.class.getClassLoader().getResource(CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_2).toURI());
		final Path controlComponentPublicKeysPayload3 =
				Paths.get(ElectoralBoardConstitutionServiceTest.class.getClassLoader().getResource(CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_3).toURI());
		final Path controlComponentPublicKeysPayload4 =
				Paths.get(ElectoralBoardConstitutionServiceTest.class.getClassLoader().getResource(CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD_4).toURI());

		final List<ControlComponentPublicKeysPayload> controlComponentPublicKeysPayloadList = Arrays.asList(
				objectMapper.readValue(controlComponentPublicKeysPayload1.toFile(), ControlComponentPublicKeysPayload.class),
				objectMapper.readValue(controlComponentPublicKeysPayload2.toFile(), ControlComponentPublicKeysPayload.class),
				objectMapper.readValue(controlComponentPublicKeysPayload3.toFile(), ControlComponentPublicKeysPayload.class),
				objectMapper.readValue(controlComponentPublicKeysPayload4.toFile(), ControlComponentPublicKeysPayload.class));

		final List<ControlComponentPublicKeys> controlComponentPublicKeysList = controlComponentPublicKeysPayloadList.stream()
				.map(ControlComponentPublicKeysPayload::getControlComponentPublicKeys).collect(Collectors.toList());

		when(controlComponentPublicKeysServiceMock.load(ELECTION_EVENT_ID)).thenReturn(controlComponentPublicKeysList);
		when(votingCardSetRepositoryMock.findAllVotingCardSetIds(any())).thenReturn(Collections.singletonList("1"));

		when(votingCardSetRepositoryMock.getVerificationCardSetId(any())).thenReturn("1");
		when(votingCardSetRepositoryMock.getBallotBoxId(any())).thenReturn("1");
		when(ballotBoxRepositoryMock.isTestBallotBox(any())).thenReturn(true);
		when(electionEventServiceMock.getDateFrom(any())).thenReturn(LocalDateTime.now());
		when(electionEventServiceMock.getDateTo(any())).thenReturn(LocalDateTime.now());

		Files.createDirectories(tempDir.resolve("sdm/config").resolve(ELECTION_EVENT_ID));
		pathResolver = new PathResolver(tempDir.toString());

		final ElectionEventContextPayloadFileRepository electionEventContextPayloadFileRepository = new ElectionEventContextPayloadFileRepository(
				objectMapper, pathResolver);

		final ElectionEventContextPayloadService electionEventContextPayloadService = new ElectionEventContextPayloadService(
				electionEventContextPayloadFileRepository);

		electoralBoardConstitutionService = new ElectoralBoardConstitutionService(
				controlComponentPublicKeysServiceMock, generateVerificationCardSetKeysService, setupTallyEBService, votingCardSetRepositoryMock,
				ballotBoxRepositoryMock, electionEventContextPayloadService, electionEventServiceMock);
	}

	@Test
	void constituteHappyPath() {
		assertDoesNotThrow(() -> electoralBoardConstitutionService.constitute(ELECTION_EVENT_ID));

		assertTrue(Files.exists(
				pathResolver.resolveElectionEventPath(ELECTION_EVENT_ID).resolve(ElectionEventContextPayloadFileRepository.PAYLOAD_FILE_NAME)));
	}

}
