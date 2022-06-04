/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext.BallotTextRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

@ExtendWith(MockitoExtension.class)
class BallotServiceTest {

	private static final String BALLOT_ID = "c32a5bc357194002bbfd164425226052";
	private static final String ELECTION_EVENT_ID = "bf346e85f64747dda4f37a64439bc942";

	private static String JSON_BALLOT_WITH_REPRESENTATIONS;
	private static String JSON_BALLOT_WITH_INVALID_REPRESENTATIONS;
	private static String JSON_BALLOT_WITHOUT_REPRESENTATIONS;
	private static String BALLOT_TEXT;
	private static Path representationsFile;

	@InjectMocks
	private final BallotService ballotService = new BallotService();

	@Mock
	private ConsistencyCheckService consistencyCheckService;

	@Mock
	private ConfigurationEntityStatusService statusServiceMock;

	@Mock
	private BallotRepository ballotRepositoryMock;

	@Mock
	private PathResolver pathResolver;

	@Mock
	private BallotTextRepository ballotTextRepositoryMock;

	@BeforeAll
	public static void init() throws IOException {
		final ClassLoader classLoader = BallotServiceTest.class.getClassLoader();
		JSON_BALLOT_WITH_REPRESENTATIONS = IOUtils
				.toString(classLoader.getResource("ballot_result_with_representations.json").openStream(), StandardCharsets.UTF_8);
		JSON_BALLOT_WITH_INVALID_REPRESENTATIONS = IOUtils
				.toString(classLoader.getResource("ballot_result_with_invalid_representations.json").openStream(), StandardCharsets.UTF_8);
		JSON_BALLOT_WITHOUT_REPRESENTATIONS = IOUtils
				.toString(classLoader.getResource("ballot_result_without_representations.json").openStream(), StandardCharsets.UTF_8);
		BALLOT_TEXT = IOUtils.toString(classLoader.getResource("ballotText_result.json").openStream(), StandardCharsets.UTF_8);

		representationsFile = Paths.get("src", "test", "resources", "primes.csv");

		Security.addProvider(new BouncyCastleProvider());
	}

	@Test
	void sign() throws IOException {

		final String electionEventId = "db033b3f729c45719db8aba15d24043c";
		final String ballotId = "8365b61d3f194c11bbd03e421462b6ad";

		when(pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, electionEventId, Constants.CONFIG_DIR_NAME_CUSTOMER,
				Constants.CONFIG_DIR_NAME_OUTPUT, Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(representationsFile);
		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(true);
		when(ballotRepositoryMock.list(anyMap())).thenReturn(JSON_BALLOT_WITHOUT_REPRESENTATIONS);
		when(ballotTextRepositoryMock.list(anyMap())).thenReturn(BALLOT_TEXT);

		assertDoesNotThrow(() -> ballotService.sign(electionEventId, ballotId, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void signBallotWithRepresentations() throws IOException {

		when(pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER,
				Constants.CONFIG_DIR_NAME_OUTPUT, Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(representationsFile);
		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(true);
		when(ballotRepositoryMock.list(anyMap())).thenReturn(JSON_BALLOT_WITH_REPRESENTATIONS);
		when(ballotTextRepositoryMock.list(anyMap())).thenReturn(BALLOT_TEXT);

		assertDoesNotThrow(() -> ballotService.sign(ELECTION_EVENT_ID, BALLOT_ID, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void signBallotWithInvalidRepresentations() throws IOException {

		when(pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER,
				Constants.CONFIG_DIR_NAME_OUTPUT, Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(representationsFile);
		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(false);
		when(ballotRepositoryMock.list(anyMap())).thenReturn(JSON_BALLOT_WITH_INVALID_REPRESENTATIONS);

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> ballotService.sign(ELECTION_EVENT_ID, BALLOT_ID, SigningTestData.PRIVATE_KEY_PEM));
		assertEquals("Validation of the representations used on the ballot options failed.", exception.getMessage());
	}

	@Test
	void signFails() {
		final String ballotId = "8365b61d3f194c11bbd03e421462b6ad";

		when(ballotRepositoryMock.list(anyMap())).thenThrow(DatabaseException.class);

		assertThrows(DatabaseException.class, () -> ballotService.sign(ELECTION_EVENT_ID, ballotId, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void signEmptyJsonBallot() {
		final String ballotId = "8365b61d3f194c11bbd03e421462b6ad";

		when(ballotRepositoryMock.list(anyMap())).thenReturn("{}");

		assertThrows(ResourceNotFoundException.class, () -> ballotService.sign(ELECTION_EVENT_ID, ballotId, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void signEmptyStringBallot() {
		final String ballotId = "8365b61d3f194c11bbd03e421462b6ad";

		when(ballotRepositoryMock.list(anyMap())).thenReturn("");

		assertThrows(ResourceNotFoundException.class, () -> ballotService.sign(ELECTION_EVENT_ID, ballotId, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void signNullBallot() {
		final String ballotId = "8365b61d3f194c11bbd03e421462b6ad";

		when(ballotRepositoryMock.list(anyMap())).thenReturn(null);

		assertThrows(ResourceNotFoundException.class, () -> ballotService.sign(ELECTION_EVENT_ID, ballotId, SigningTestData.PRIVATE_KEY_PEM));
	}
}
