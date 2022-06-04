/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.services.application.service.ConsistencyCheckService;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;

/**
 * Tests of {@link BallotBoxDataGeneratorService}.
 */
class BallotDataGeneratorServiceTest {

	private static final String JSON = "{\"ballot\": \"Test Ballot\"}";
	private static final String ID = "id";
	private static final String ELECTION_EVENT_ID = "electionEventId";

	private BallotRepository repository;
	private Path folder;
	private Path file;
	private BallotDataGeneratorService service;
	private ConsistencyCheckService consistencyCheckService;

	private static void deleteRecursively(final Path file) throws IOException {
		if (isDirectory(file)) {
			try (DirectoryStream<Path> children = newDirectoryStream(file)) {
				for (Path child : children) {
					deleteRecursively(child);
				}
			}
		}
		deleteIfExists(file);
	}

	@BeforeEach
	void setUp() throws IOException {
		repository = mock(BallotRepository.class);
		when(repository.find(ID)).thenReturn(JSON);
		folder = createTempDirectory("user");
		file = folder.resolve(ELECTION_EVENT_ID).resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION)
				.resolve(Constants.CONFIG_DIR_NAME_BALLOTS).resolve(ID).resolve(Constants.CONFIG_FILE_NAME_BALLOT_JSON);
		PathResolver resolver = mock(PathResolver.class);
		when(resolver.resolve(Constants.CONFIG_FILES_BASE_DIR)).thenReturn(folder);
		when(resolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER,
				Constants.CONFIG_DIR_NAME_OUTPUT, Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV))
				.thenReturn(Paths.get("src", "test", "resources", "primes.csv"));
		consistencyCheckService = mock(ConsistencyCheckService.class);
		service = new BallotDataGeneratorService(repository, resolver, consistencyCheckService);
	}

	@AfterEach
	void tearDown() throws IOException {
		deleteRecursively(folder);
	}

	@Test
	void testGenerate() throws IOException {

		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(true);

		DataGeneratorResponse response = service.generate(ID, ELECTION_EVENT_ID);
		assertTrue(response.isSuccessful());
		assertArrayEquals(JSON.getBytes(StandardCharsets.UTF_8), readAllBytes(file));
	}

	@Test
	void testGenerateBallotNotFound() {
		when(repository.find(ID)).thenReturn(JsonConstants.EMPTY_OBJECT);
		DataGeneratorResponse response = service.generate(ID, ELECTION_EVENT_ID);
		assertFalse(response.isSuccessful());
		assertFalse(exists(file));
	}

	@Test
	void testGenerateFailedToCreateFolder() throws IOException {
		createFile(folder.resolve(ELECTION_EVENT_ID));

		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(true);

		DataGeneratorResponse response = service.generate(ID, ELECTION_EVENT_ID);
		assertFalse(response.isSuccessful());
	}

	@Test
	void testGenerateFileAlreadyExists() throws IOException, InterruptedException {
		createDirectories(file.getParent());
		write(file, JSON.getBytes(StandardCharsets.UTF_8));
		FileTime time = getLastModifiedTime(file);
		Thread.sleep(1000);

		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(true);

		DataGeneratorResponse response = service.generate(ID, ELECTION_EVENT_ID);
		assertTrue(response.isSuccessful());
		assertEquals(time, getLastModifiedTime(file));
	}

	@Test
	void testGenerateFileAlreadyExistsDifferent() throws IOException {
		createDirectories(file.getParent());
		write(file, "Something different".getBytes(StandardCharsets.UTF_8));

		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(true);

		DataGeneratorResponse response = service.generate(ID, ELECTION_EVENT_ID);
		assertTrue(response.isSuccessful());
		assertArrayEquals(JSON.getBytes(StandardCharsets.UTF_8), readAllBytes(file));
	}

	@Test
	void testGenerateIOException() throws IOException {
		createDirectories(file);

		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(true);

		DataGeneratorResponse response = service.generate(ID, ELECTION_EVENT_ID);
		assertFalse(response.isSuccessful());
	}

	@Test
	void testGenerateNonConsistentPrimes() throws IOException {

		when(consistencyCheckService.representationsConsistent(anyString(), any())).thenReturn(false);

		DataGeneratorResponse response = service.generate(ID, ELECTION_EVENT_ID);

		assertFalse(response.isSuccessful());
		assertEquals("Consistency check of the representations used on the ballot options failed.", response.getResult());
	}
}
