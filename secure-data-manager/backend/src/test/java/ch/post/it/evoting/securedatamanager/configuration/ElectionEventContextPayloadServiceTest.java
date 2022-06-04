/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
import com.google.common.base.Throwables;
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

@DisplayName("An ElectionEventContextPayloadService")
class ElectionEventContextPayloadServiceTest {

	private static final String ELECTION_EVENT_ID = "a4b65058255041658506196e826f7a86";
	private static final String WRONG_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";
	private static final String NOT_EXISTING_ELECTION_EVENT_PAYLOAD = "614bd34dcf6e4de4b771a92fa3849d3d";
	private static final String INVALID_ID = "invalidId";
	private static final CryptoPrimitives CRYPTO_PRIMITIVES = CryptoPrimitivesService.get();

	private static ObjectMapper objectMapper;
	private static PathResolver pathResolver;
	private static ElectionEventContextPayloadService electionEventContextPayloadService;

	@BeforeAll
	static void setUpAll() throws URISyntaxException {
		objectMapper = DomainObjectMapper.getNewInstance();
		final Path path = Paths.get(ElectionEventContextPayloadServiceTest.class.getResource("/ElectionEventContextServiceTest/").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());

		final ElectionEventContextPayloadFileRepository electionEventContextPayloadFileRepository = new ElectionEventContextPayloadFileRepository(
				objectMapper, pathResolver);

		electionEventContextPayloadService = new ElectionEventContextPayloadService(electionEventContextPayloadFileRepository);
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

		final ElectionEventContext electionEventContext = new ElectionEventContext(ELECTION_EVENT_ID, verificationCardSetContexts,
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

		private ElectionEventContextPayloadService electionEventContextPayloadServiceTemp;

		private ElectionEventContextPayload electionEventContextPayload;

		@BeforeAll
		void setUpAll(
				@TempDir
				final Path tempDir) throws IOException {

			Files.createDirectories(tempDir.resolve("sdm/config").resolve(ELECTION_EVENT_ID));

			pathResolver = new PathResolver(tempDir.toString());
			final ElectionEventContextPayloadFileRepository electionEventContextPayloadFileRepositoryTemp = new ElectionEventContextPayloadFileRepository(
					objectMapper, pathResolver);

			electionEventContextPayloadServiceTemp = new ElectionEventContextPayloadService(electionEventContextPayloadFileRepositoryTemp);
		}

		@BeforeEach
		void setUp() {
			// Create payload.
			electionEventContextPayload = validElectionEventContextPayload();
		}

		@Test
		@DisplayName("a valid payload does not throw")
		void saveValidPayload() {
			assertDoesNotThrow(() -> electionEventContextPayloadServiceTemp.save(electionEventContextPayload));

			assertTrue(Files.exists(
					pathResolver.resolveElectionEventPath(ELECTION_EVENT_ID).resolve(ElectionEventContextPayloadFileRepository.PAYLOAD_FILE_NAME)));
		}

		@Test
		@DisplayName("a null payload throws NullPointerException")
		void saveNullPayload() {
			assertThrows(NullPointerException.class, () -> electionEventContextPayloadServiceTemp.save(null));
		}

	}

	@Nested
	@DisplayName("calling exist")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ExistTest {

		@Test
		@DisplayName("for valid election event returns true")
		void existValidElectionEvent() {
			assertTrue(electionEventContextPayloadService.exist(ELECTION_EVENT_ID));
		}

		@Test
		@DisplayName("for invalid election event id throws FailedValidationException")
		void existInvalidElectionEvent() {
			assertThrows(FailedValidationException.class, () -> electionEventContextPayloadService.exist(INVALID_ID));
		}

		@Test
		@DisplayName("for non existing election event returns false")
		void existNonExistingElectionEvent() {
			assertFalse(electionEventContextPayloadService.exist(WRONG_ELECTION_EVENT_ID));
		}

	}

	@Nested
	@DisplayName("loading")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class LoadTest {

		@Test
		@DisplayName("existing election event returns expected election event context payload")
		void loadExistingElectionEvent() {
			final ElectionEventContextPayload electionEventContextPayload = electionEventContextPayloadService.load(ELECTION_EVENT_ID);

			assertNotNull(electionEventContextPayload);
		}

		@Test
		@DisplayName("invalid election event id throws FailedValidationException")
		void loadInvalidElectionEventId() {
			assertThrows(FailedValidationException.class, () -> electionEventContextPayloadService.load(INVALID_ID));
		}

		@Test
		@DisplayName("existing election event with missing payload throws IllegalStateException")
		void loadMissingPayload() {
			final IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> electionEventContextPayloadService.load(NOT_EXISTING_ELECTION_EVENT_PAYLOAD));

			final String errorMessage = String.format("Requested election event context payload is not present. [electionEventId: %s]",
					NOT_EXISTING_ELECTION_EVENT_PAYLOAD);
			assertEquals(errorMessage, Throwables.getRootCause(exception).getMessage());
		}

	}

}
