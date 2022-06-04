/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.test.util.ReflectionTestUtils;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.PrefixPathResolver;
import ch.post.it.evoting.securedatamanager.commons.domain.StartVotingCardGenerationJobResponse;
import ch.post.it.evoting.securedatamanager.config.commons.config.commons.progress.JobProgressDetails;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.ConfigurationService;
import ch.post.it.evoting.securedatamanager.services.application.service.PlatformRootCAService;
import ch.post.it.evoting.securedatamanager.services.domain.model.config.VotingCardGenerationJobStatus;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.VotersParametersHolderAdapter;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.progress.VotingCardSetProgressManagerService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
class VotingCardSetDataGeneratorServiceTest {

	private static final String RESOURCES_FOLDER_NAME = "VotingCardSetDataGeneratorServiceImplTest";
	private static final String resourcesDir = "src/test/resources/";
	private static final String electionEventId = "0b149cfdaad04b04b990c3b1d4ca7639";
	private static final String votingCardSetId = "4652f56eb88a4d7dac24a4239fbe16c3";
	private static final String votingCardSetRepositoryPath = "/votingCardSetRepository.json";
	private static final String ballotBoxRepositoryPath = "/ballotBoxRepository.json";
	private static final String electionEventRepositoryPath = "/electionEventRepository.json";
	private static final String PLATFORM_ROOT_CA_PEM = "platformRootCA.pem";
	private static final String certificatePropertiesDirname = "certificateProperties";
	private static final String credentialAuthCertificatePropertiessFilename = "credentialAuthX509Certificate.properties";
	private static final String verificationCardSetCertificateFilename = "verificationCardSetX509Certificate.properties";

	private static PathResolver resourcesPathResolver;
	private static JsonObject votingCardSetRepositoryJson;
	private static JsonObject ballotBoxRepositoryJson;
	private static JsonObject electionEventRepositoryJson;
	private static X509Certificate platformRootCA;

	@Spy
	@InjectMocks
	private VotingCardSetDataGeneratorService votingCardSetDataGeneratorService;

	@Mock
	private VotingCardSetRepository votingCardSetRepositoryMock;

	@Mock
	private BallotBoxRepository ballotBoxRepositoryMock;

	@Mock
	private ElectionEventRepository electionEventRepositoryMock;

	@Mock
	private PlatformRootCAService platformRootCAServiceMock;

	@Mock
	private VotersParametersHolderAdapter votersParametersHolderAdapter;

	@Mock
	private ConfigurationService configurationService;

	@Mock
	private VotingCardSetProgressManagerService votingCardSetProgressManagerService;

	@BeforeAll
	static void init() throws URISyntaxException, IOException, GeneralCryptoLibException {
		setDefaultMockedPathResolvers();
		setDefaultMockedReturnValues();
	}

	private static void setDefaultMockedPathResolvers() {
		String resourcesPath = Paths.get(resourcesDir).toAbsolutePath().toString();
		resourcesPathResolver = new PrefixPathResolver(resourcesPath);
	}

	private static void setDefaultMockedReturnValues() throws URISyntaxException, IOException, GeneralCryptoLibException {
		URL repoUrl = VotingCardSetDataGeneratorServiceTest.class.getResource(votingCardSetRepositoryPath);
		Path repoPath = Paths.get(repoUrl.toURI());
		votingCardSetRepositoryJson = JsonUtils.getJsonObject(new String(Files.readAllBytes(repoPath), StandardCharsets.UTF_8));

		repoUrl = VotingCardSetDataGeneratorServiceTest.class.getResource(ballotBoxRepositoryPath);
		repoPath = Paths.get(repoUrl.toURI());
		ballotBoxRepositoryJson = JsonUtils.getJsonObject(new String(Files.readAllBytes(repoPath), StandardCharsets.UTF_8));

		repoUrl = VotingCardSetDataGeneratorServiceTest.class.getResource(electionEventRepositoryPath);
		repoPath = Paths.get(repoUrl.toURI());
		electionEventRepositoryJson = JsonUtils.getJsonObject(new String(Files.readAllBytes(repoPath), StandardCharsets.UTF_8));

		repoUrl = VotingCardSetDataGeneratorServiceTest.class.getClassLoader()
				.getResource(RESOURCES_FOLDER_NAME + File.separator + PLATFORM_ROOT_CA_PEM);
		repoPath = Paths.get(repoUrl.toURI());
		platformRootCA = (X509Certificate) PemUtils.certificateFromPem(new String(Files.readAllBytes(repoPath), StandardCharsets.UTF_8));
	}

	@BeforeEach
	void setUp() {
		setDefaultMockedMethods();
		setDefaultMockedValues();
	}

	@Test
	void generate() throws Exception {
		when(votingCardSetRepositoryMock.find(anyString())).thenReturn(votingCardSetRepositoryJson.toString());
		when(ballotBoxRepositoryMock.find(anyString())).thenReturn(ballotBoxRepositoryJson.toString());
		when(electionEventRepositoryMock.find(anyString())).thenReturn(electionEventRepositoryJson.toString());
		when(platformRootCAServiceMock.load()).thenReturn(platformRootCA);

		final String jobId = "123e4567-e89b-42d3-a456-556642440000";
		final String createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toString();
		final StartVotingCardGenerationJobResponse startVotingCardGenerationJobResponse =
				new StartVotingCardGenerationJobResponse(jobId, BatchStatus.UNKNOWN, createdAt);
		when(configurationService.startVotingCardGenerationJob(any(), any(), any())).thenReturn(startVotingCardGenerationJobResponse);

		final CompletableFuture<VotingCardGenerationJobStatus> future = new CompletableFuture<>();
		final VotingCardGenerationJobStatus votingCardGenerationJobStatus = new VotingCardGenerationJobStatus(UUID.fromString(jobId),
				BatchStatus.COMPLETED, Instant.EPOCH, null, JobProgressDetails.EMPTY, null, 0, 0);
		future.complete(votingCardGenerationJobStatus);
		when(votingCardSetProgressManagerService.registerJob(any())).thenReturn(future);

		final DataGeneratorResponse generate = votingCardSetDataGeneratorService.generate(votingCardSetId, electionEventId);

		assertTrue(generate.isSuccessful());
	}

	@Test
	void generateWithNullVotingCardSetId() {
		DataGeneratorResponse result = votingCardSetDataGeneratorService.generate(null, electionEventId);
		assertFalse(result.isSuccessful());
	}

	@Test
	void generateWithEmptyVotingCardSetId() {
		DataGeneratorResponse result = votingCardSetDataGeneratorService.generate("", electionEventId);
		assertFalse(result.isSuccessful());
	}

	@Test
	void generateWithEmptyVotingCardSet() {
		when(votingCardSetRepositoryMock.find(anyString())).thenReturn(JsonConstants.EMPTY_OBJECT);

		DataGeneratorResponse result = votingCardSetDataGeneratorService.generate(votingCardSetId, electionEventId);
		assertFalse(result.isSuccessful());
	}

	private void setDefaultMockedMethods() {
		ReflectionTestUtils.setField(votingCardSetDataGeneratorService, "pathResolver", resourcesPathResolver);
		ReflectionTestUtils.setField(votingCardSetDataGeneratorService, "votingCardSetRepository", votingCardSetRepositoryMock);
		ReflectionTestUtils.setField(votingCardSetDataGeneratorService, "ballotBoxRepository", ballotBoxRepositoryMock);
		ReflectionTestUtils.setField(votingCardSetDataGeneratorService, "electionEventRepository", electionEventRepositoryMock);
		ReflectionTestUtils.setField(votingCardSetDataGeneratorService, "platformRootCAService", platformRootCAServiceMock);
		ReflectionTestUtils.setField(votingCardSetDataGeneratorService, "jobCompletionExecutor", mock(ExecutorService.class));
	}

	private void setDefaultMockedValues() {
		Path certificatePropertiesPath = resourcesPathResolver.resolve(Constants.SDM_CONFIG_DIR_NAME, certificatePropertiesDirname);
		String credentialAuthCertificatePropertiesMock = resourcesPathResolver
				.resolve(certificatePropertiesPath.toString(), credentialAuthCertificatePropertiessFilename).toString();
		String verificationCardSetCertificatePropertiesMock = resourcesPathResolver
				.resolve(certificatePropertiesPath.toString(), verificationCardSetCertificateFilename).toString();

		ReflectionTestUtils
				.setField(votingCardSetDataGeneratorService, "credentialAuthCertificateProperties", credentialAuthCertificatePropertiesMock);
		ReflectionTestUtils.setField(votingCardSetDataGeneratorService, "verificationCardSetCertificateProperties",
				verificationCardSetCertificatePropertiesMock);
	}
}
