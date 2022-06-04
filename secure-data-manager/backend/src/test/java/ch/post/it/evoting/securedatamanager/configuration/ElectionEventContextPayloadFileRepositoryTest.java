/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.securedatamanager.configuration.ElectionEventContextPayloadFileRepository.PAYLOAD_FILE_NAME;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.domain.SerializationUtils;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ElectionEventContext;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.domain.configuration.VerificationCardSetContext;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("ElectionEventContextPayloadFileRepository")
class ElectionEventContextPayloadFileRepositoryTest {

	private static final String NON_EXISTING_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";
	private static final String EXISTING_ELECTION_EVENT_ID = "a4b65058255041658506196e826f7a86";
	private static final String CORRUPTED_ELECTION_EVENT_ID = "514bd34dcf6e4de4b771a92fa3849d3d";
	private static final CryptoPrimitives CRYPTO_PRIMITIVES = CryptoPrimitivesService.get();

	private static PathResolver pathResolver;
	private static ObjectMapper objectMapper;
	private static ElectionEventContextPayloadFileRepository electionEventContextPayloadFileRepository;

	@BeforeAll
	static void setUpAll() throws URISyntaxException {
		objectMapper = DomainObjectMapper.getNewInstance();

		final Path path = Paths.get(
				ElectionEventContextPayloadFileRepositoryTest.class.getClassLoader().getResource("ElectionEventContextPayloadFileRepositoryTest/")
						.toURI());
		pathResolver = new PathResolver(path.toString());
		electionEventContextPayloadFileRepository = new ElectionEventContextPayloadFileRepository(objectMapper, pathResolver);
	}

	private ElectionEventContextPayload validElectionEventContextPayload() {
		final List<VerificationCardSetContext> verificationCardSetContexts = new ArrayList<>();

		final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys = new ArrayList<>();

		IntStream.rangeClosed(1, 2).forEach((i) -> verificationCardSetContexts.add(generatedVerificationCardSetContext()));

		Constants.NODE_IDS.forEach((nodeId) -> combinedControlComponentPublicKeys.add(generateCombinedControlComponentPublicKeys(nodeId)));

		final ElGamalMultiRecipientPublicKey electoralBoardPublicKey = SerializationUtils.getPublicKey();

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrChoiceReturnCodePublicKeys = combinedControlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::getCcrChoiceReturnCodesEncryptionPublicKey).collect(GroupVector.toGroupVector());

		final ElGamalService elGamalService = new ElGamalService();
		final ElGamalMultiRecipientPublicKey choiceReturnCodesPublicKey = elGamalService.combinePublicKeys(ccrChoiceReturnCodePublicKeys);

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccmElectionPublicKeys = Streams.concat(
				combinedControlComponentPublicKeys.stream()
						.map(ControlComponentPublicKeys::getCcmElectionPublicKey),
				Stream.of(electoralBoardPublicKey)).collect(GroupVector.toGroupVector());

		final ElGamalMultiRecipientPublicKey electionPublicKey = elGamalService.combinePublicKeys(ccmElectionPublicKeys);

		final LocalDateTime startTime = LocalDateTime.now();
		final LocalDateTime finishTime = startTime.plusWeeks(1);

		final ElectionEventContext electionEventContext = new ElectionEventContext(EXISTING_ELECTION_EVENT_ID, verificationCardSetContexts,
				combinedControlComponentPublicKeys, electoralBoardPublicKey, electionPublicKey, choiceReturnCodesPublicKey, startTime, finishTime);

		return new ElectionEventContextPayload(electionPublicKey.getGroup(), electionEventContext);
	}

	private VerificationCardSetContext generatedVerificationCardSetContext() {
		final String verificationCardSetId = CRYPTO_PRIMITIVES.genRandomBase16String(32);
		final String ballotBoxId = CRYPTO_PRIMITIVES.genRandomBase16String(32);
		boolean testBallotBox = Math.random() < 0.5;
		int numberOfWriteInFields = 1;
		return new VerificationCardSetContext(verificationCardSetId, ballotBoxId, testBallotBox, numberOfWriteInFields);
	}

	private ControlComponentPublicKeys generateCombinedControlComponentPublicKeys(int nodeId) {
		final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey = SerializationUtils.getPublicKey();
		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey = SerializationUtils.getPublicKey();
		return new ControlComponentPublicKeys(nodeId, ccrChoiceReturnCodesEncryptionPublicKey, ccmElectionPublicKey);
	}

	@Nested
	@DisplayName("saving")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SaveTest {

		private ElectionEventContextPayloadFileRepository electionEventContextPayloadFileRepositoryTemp;

		private ElectionEventContextPayload electionEventContextPayload;

		@BeforeAll
		void setUpAll(
				@TempDir
				final Path tempDir) throws IOException {

			Files.createDirectories(tempDir.resolve("sdm/config").resolve(EXISTING_ELECTION_EVENT_ID));

			final PathResolver pathResolver = new PathResolver(tempDir.toString());
			electionEventContextPayloadFileRepositoryTemp = new ElectionEventContextPayloadFileRepository(objectMapper, pathResolver);
		}

		@BeforeEach
		void setUp() {
			electionEventContextPayload = validElectionEventContextPayload();
		}

		@Test
		@DisplayName("valid election event context payload creates file")
		void save() {
			final Path savedPath = electionEventContextPayloadFileRepositoryTemp.save(electionEventContextPayload);

			assertTrue(Files.exists(savedPath));
		}

		@Test
		@DisplayName("null election event context payload throws NullPointerException")
		void saveNullElectionEventContext() {
			assertThrows(NullPointerException.class, () -> electionEventContextPayloadFileRepositoryTemp.save(null));
		}

		@Test
		@DisplayName("invalid path throws UncheckedIOException")
		void invalidPath() {
			final PathResolver pathResolver = new PathResolver("invalidPath");
			final ElectionEventContextPayloadFileRepository repository = new ElectionEventContextPayloadFileRepository(
					DomainObjectMapper.getNewInstance(),
					pathResolver);

			final UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> repository.save(electionEventContextPayload));

			final Path electionEventContextPath = pathResolver.resolveElectionEventPath(EXISTING_ELECTION_EVENT_ID).resolve(PAYLOAD_FILE_NAME);
			final String errorMessage = String.format(
					"Failed to serialize election event context payload. [electionEventId: %s, path: %s]", EXISTING_ELECTION_EVENT_ID,
					electionEventContextPath);

			assertEquals(exception.getMessage(), errorMessage);
		}

	}

	@Nested
	@DisplayName("calling existsById")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ExistsByIdTest {

		@Test
		@DisplayName("for existing election event context payload returns true")
		void existingElectionEventContext() {
			assertTrue(electionEventContextPayloadFileRepository.existsById(EXISTING_ELECTION_EVENT_ID));
		}

		@Test
		@DisplayName("with invalid election event id throws FailedValidationException")
		void invalidElectionEventId() {
			assertThrows(FailedValidationException.class, () -> electionEventContextPayloadFileRepository.existsById("invalidId"));
		}

		@Test
		@DisplayName("for non existing election event context payload returns false")
		void nonExistingElectionEventContext() {
			assertFalse(electionEventContextPayloadFileRepository.existsById(NON_EXISTING_ELECTION_EVENT_ID));
		}

	}

	@Nested
	@DisplayName("calling findById")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class FindByIdTest {

		@Test
		@DisplayName("for existing election event context payload returns it")
		void existingElectionEventContext() {
			assertTrue(electionEventContextPayloadFileRepository.findById(EXISTING_ELECTION_EVENT_ID).isPresent());
		}

		@Test
		@DisplayName("for non existing election event context payload return empty optional")
		void nonExistingElectionEventContext() {
			assertFalse(electionEventContextPayloadFileRepository.findById(NON_EXISTING_ELECTION_EVENT_ID).isPresent());
		}

		@Test
		@DisplayName("for corrupted election event context payload throws UncheckedIOException")
		void corruptedElectionEventContext() {
			final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
					() -> electionEventContextPayloadFileRepository.findById(CORRUPTED_ELECTION_EVENT_ID));

			final Path electionEventPath = pathResolver.resolveElectionEventPath(CORRUPTED_ELECTION_EVENT_ID);
			final Path electionEventContextPath = electionEventPath.resolve(PAYLOAD_FILE_NAME);
			final String errorMessage = String.format("Failed to deserialize election event context payload. [electionEventId: %s, path: %s]",
					CORRUPTED_ELECTION_EVENT_ID, electionEventContextPath);

			assertEquals(errorMessage, exception.getMessage());
		}

	}

}
