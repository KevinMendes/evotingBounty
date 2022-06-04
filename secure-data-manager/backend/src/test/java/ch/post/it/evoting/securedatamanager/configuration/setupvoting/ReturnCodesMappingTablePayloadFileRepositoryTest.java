/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("ReturnCodesMappingTablePayloadFileRepository")
class ReturnCodesMappingTablePayloadFileRepositoryTest {

	private static final String ELECTION_EVENT_ID = "8b733b29be224c01b4d1f82fe2a5fbea";
	private static final String CORRUPTED_ELECTION_EVENT_ID = "1b733b29be224c01b4d1f82fe2a5fbea";
	private static final String VERIFICATION_CARD_SET_ID = "0b5bf763c0d44d66b775399d08ae4811";
	private static final String CORRUPTED_VERIFICATION_CARD_SET_ID = "1b5bf763c0d44d66b775399d08ae4811";
	private static final String NON_EXISTING_ID = "abcdef0123456789abcdef0123456789";

	private static ObjectMapper objectMapper;

	private static ReturnCodesMappingTablePayloadFileRepository returnCodesMappingTablePayloadFileRepository;

	@BeforeAll
	static void setUpAll() throws URISyntaxException {
		objectMapper = DomainObjectMapper.getNewInstance();
		final Path path = Paths.get(
				ReturnCodesMappingTablePayloadFileRepositoryTest.class.getResource("/returnCodesMappingTablePayloadFileRepositoryTest/").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());
		returnCodesMappingTablePayloadFileRepository = new ReturnCodesMappingTablePayloadFileRepository(objectMapper, pathResolver);
	}

	@Nested
	@DisplayName("saving")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SaveTest {

		private ReturnCodesMappingTablePayloadFileRepository returnCodesMappingTablePayloadFileRepositoryTemp;

		private ReturnCodesMappingTablePayload returnCodesMappingTablePayload;

		@BeforeAll
		void setUpAll(
				@TempDir
				final Path tempDir) throws IOException {

			Files.createDirectories(
					tempDir.resolve("sdm/config").resolve(ELECTION_EVENT_ID).resolve("ONLINE/voteVerification").resolve(VERIFICATION_CARD_SET_ID));

			final PathResolver pathResolver = new PathResolver(tempDir.toString());
			returnCodesMappingTablePayloadFileRepositoryTemp = new ReturnCodesMappingTablePayloadFileRepository(objectMapper, pathResolver);
		}

		@BeforeEach
		void setUp() {
			final Map<String, String> map = new HashMap<>();
			map.put("HASH", "VALUE");
			final ImmutableMap<String, String> immutableMap = ImmutableMap.copyOf(map);

			// Create payload.
			returnCodesMappingTablePayload = new ReturnCodesMappingTablePayload.Builder()
					.setElectionEventId(ELECTION_EVENT_ID)
					.setVerificationCardSetId(VERIFICATION_CARD_SET_ID)
					.setReturnCodesMappingTable(immutableMap)
					.build();
		}

		@Test
		@DisplayName("valid payload creates file")
		void save() {
			final Path savedPath = returnCodesMappingTablePayloadFileRepositoryTemp.save(returnCodesMappingTablePayload);

			assertTrue(Files.exists(savedPath));
		}

		@Test
		@DisplayName("null payload throws NullPointerException")
		void saveNullPayload() {
			assertThrows(NullPointerException.class, () -> returnCodesMappingTablePayloadFileRepositoryTemp.save(null));
		}

		@Test
		@DisplayName("invalid path throws UncheckedIOException")
		void invalidPath() {
			final PathResolver pathResolver = new PathResolver("invalidPath");
			final ReturnCodesMappingTablePayloadFileRepository repository = new ReturnCodesMappingTablePayloadFileRepository(
					DomainObjectMapper.getNewInstance(), pathResolver);

			final UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> repository.save(returnCodesMappingTablePayload));

			final String errorMessage = String.format(
					"Unable to write the return codes mapping table payload file. [electionEventId: %s, verificationCardSetId: %s]",
					ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID);

			assertEquals(errorMessage, exception.getMessage());
		}

	}

	@Nested
	@DisplayName("calling findById")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class FindByIdTest {

		@Test
		@DisplayName("for existing payload returns it")
		void existingPayload() {
			assertTrue(returnCodesMappingTablePayloadFileRepository.findByElectionEventIdAndVerificationCardSetId(ELECTION_EVENT_ID,
					VERIFICATION_CARD_SET_ID).isPresent());
		}

		@Test
		@DisplayName("for not existing payload return empty optional")
		void nonExistingPayload() {
			assertAll(
					() -> assertFalse(returnCodesMappingTablePayloadFileRepository.findByElectionEventIdAndVerificationCardSetId(NON_EXISTING_ID,
							VERIFICATION_CARD_SET_ID).isPresent()),
					() -> assertFalse(returnCodesMappingTablePayloadFileRepository.findByElectionEventIdAndVerificationCardSetId(ELECTION_EVENT_ID,
							NON_EXISTING_ID).isPresent())
			);
		}

		@Test
		@DisplayName("for corrupted payload throws UncheckedIOException")
		void corruptedPayload() {
			final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
					() -> returnCodesMappingTablePayloadFileRepository.findByElectionEventIdAndVerificationCardSetId(CORRUPTED_ELECTION_EVENT_ID,
							CORRUPTED_VERIFICATION_CARD_SET_ID));

			final String errorMessage = String.format(
					"Failed to deserialize return codes mapping table payload. [electionEventId: %s, verificationCardSetId: %s]",
					CORRUPTED_ELECTION_EVENT_ID, CORRUPTED_VERIFICATION_CARD_SET_ID);

			assertEquals(errorMessage, exception.getMessage());
		}

	}

}