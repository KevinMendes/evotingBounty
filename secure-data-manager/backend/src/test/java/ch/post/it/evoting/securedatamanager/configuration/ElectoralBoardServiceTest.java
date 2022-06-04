/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.SigningTestData;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

@ExtendWith(MockitoExtension.class)
class ElectoralBoardServiceTest {

	private static final String ELECTION_EVENT_ID = "0b149cfdaad04b04b990c3b1d4ca7639";
	private static final String ELECTORAL_AUTHORITY_ID = "16e020d934594544a6e17d1e410da513";
	private static final String ELECTORAL_AUTHORITY_LOCKED_JSON = ElectoralBoardServiceTest.class.getSimpleName() + "/electoralAuthority_LOCKED.json";
	private static final String ELECTORAL_AUTHORITY_READY_JSON = ElectoralBoardServiceTest.class.getSimpleName() + "/electoralAuthority_READY.json";
	private static final String ELECTORAL_AUTHORITY_JSON_WITHOUT_STATUS = "{" +
			"    \"id\": \"16e020d934594544a6e17d1e410da513\"," +
			"    \"defaultTitle\": \"Electoral authority\"," +
			"    \"defaultDescription\": \"A  sample EA\"," +
			"    \"alias\": \"EA1\"," +
			"    \"electionEvent\": {" +
			"        \"id\": \"0b149cfdaad04b04b990c3b1d4ca7639\"" +
			"    }," +
			"    \"minimumThreshold\": \"2\"," +
			"    \"electoralBoard\": [" +
			"        \"John\"," +
			"        \"Peter\"" +
			"    ]" +
			"}";

	private static JsonObject electoralAuthorityRepositoryReadyJson;
	private static JsonObject electoralAuthorityRepositoryLockedJson;

	private static final ElectoralAuthorityRepository electoralAuthorityRepositoryMock = mock(ElectoralAuthorityRepository.class);
	private static final ConfigurationEntityStatusService statusService = mock(ConfigurationEntityStatusService.class);
	private static final PathResolver pathResolver = mock(PathResolver.class);
	private static final ConfigObjectMapper configObjectMapper = new ConfigObjectMapper();
	private static final ElectoralBoardConstitutionService constituteService = mock(ElectoralBoardConstitutionService.class);

	private static ElectoralBoardService electoralBoardService;

	@BeforeAll
	static void setUp() throws IOException, URISyntaxException {

		final Path pathReady = Paths.get(ElectoralBoardServiceTest.class.getClassLoader().getResource(ELECTORAL_AUTHORITY_READY_JSON).toURI());
		electoralAuthorityRepositoryReadyJson = JsonUtils.getJsonObject(new String(Files.readAllBytes(pathReady), StandardCharsets.UTF_8));

		final Path pathLocked = Paths.get(ElectoralBoardServiceTest.class.getClassLoader().getResource(ELECTORAL_AUTHORITY_LOCKED_JSON).toURI());
		electoralAuthorityRepositoryLockedJson = JsonUtils.getJsonObject(new String(Files.readAllBytes(pathLocked), StandardCharsets.UTF_8));

		electoralBoardService = new ElectoralBoardService(electoralAuthorityRepositoryMock, statusService, pathResolver, configObjectMapper,
				constituteService);
	}

	@Test
	void notSignAnElectoralAuthorityWithoutStatus() throws ResourceNotFoundException, GeneralCryptoLibException, IOException {
		when(electoralAuthorityRepositoryMock.find(anyString())).thenReturn(ELECTORAL_AUTHORITY_JSON_WITHOUT_STATUS);

		assertFalse(electoralBoardService.sign(ELECTION_EVENT_ID, ELECTORAL_AUTHORITY_ID, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void signHappyPath(
			@TempDir
			final Path tempDir) throws ResourceNotFoundException, GeneralCryptoLibException, IOException {
		when(electoralAuthorityRepositoryMock.find(anyString())).thenReturn(electoralAuthorityRepositoryReadyJson.toString());
		when(pathResolver.resolve(any())).thenReturn(tempDir);

		assertTrue(electoralBoardService.sign(ELECTION_EVENT_ID, ELECTORAL_AUTHORITY_ID, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void constituteHappyPath() {
		when(electoralAuthorityRepositoryMock.find(anyString())).thenReturn(electoralAuthorityRepositoryLockedJson.toString());

		assertTrue(electoralBoardService.constitute(ELECTION_EVENT_ID, ELECTORAL_AUTHORITY_ID));
	}

	@Test
	void constituteElectoralAuthorityReady() {
		when(electoralAuthorityRepositoryMock.find(anyString())).thenReturn(electoralAuthorityRepositoryReadyJson.toString());

		assertFalse(electoralBoardService.constitute(ELECTION_EVENT_ID, ELECTORAL_AUTHORITY_ID));
	}

	@Test
	void constituteElectoralAuthorityNotfound() {
		when(electoralAuthorityRepositoryMock.find(anyString())).thenReturn(null);

		assertFalse(electoralBoardService.constitute(ELECTION_EVENT_ID, ELECTORAL_AUTHORITY_ID));
	}

}
