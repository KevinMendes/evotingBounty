/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.securedatamanager.commons.Constants.NODE_IDS;
import static ch.post.it.evoting.securedatamanager.configuration.ControlComponentPublicKeysPayloadFileRepository.PAYLOAD_FILE_NAME;
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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.securedatamanager.EncryptionParametersFileRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
@DisplayName("ControlComponentPublicKeysPayloadFileRepository")
class ControlComponentPublicKeysPayloadFileRepositoryTest {

	private static final String ELECTION_EVENT_ID = "314bd34dcf6e4de4b771a92fa3849d3d";
	private static final String WRONG_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";
	private static final String CORRUPTED_ELECTION_EVENT_ID = "514bd34dcf6e4de4b771a92fa3849d3d";
	private static final int NODE_ID = 1;
	private static final int CORRUPTED_NODE_ID = 0;
	private static final int NON_EXISTING_NODE_ID = 5;
	private static final int KEY_SIZE = 3;

	private static GqGroup gqGroup;
	private static PathResolver pathResolver;
	private static ObjectMapper objectMapper;
	private static ElGamalGenerator elGamalGenerator;
	private static ControlComponentPublicKeysPayloadFileRepository controlComponentPublicKeysPayloadFileRepository;

	@SystemStub
	private static EnvironmentVariables environmentVariables;

	@BeforeAll
	static void setUpAll() throws URISyntaxException {
		environmentVariables.set("SECURITY_LEVEL", "TESTING_ONLY");
		gqGroup = GroupTestData.getGqGroup();
		elGamalGenerator = new ElGamalGenerator(gqGroup);
		objectMapper = DomainObjectMapper.getNewInstance();

		final Path path = Paths.get(EncryptionParametersFileRepository.class.getResource("/controlComponentPublicKeysTest/").toURI());
		pathResolver = new PathResolver(path.toString());
		controlComponentPublicKeysPayloadFileRepository = new ControlComponentPublicKeysPayloadFileRepository(objectMapper, pathResolver);
	}

	@Nested
	@DisplayName("saving")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SaveTest {

		private ControlComponentPublicKeysPayloadFileRepository controlComponentPublicKeysPayloadFileRepositoryTemp;

		private ControlComponentPublicKeysPayload controlComponentPublicKeysPayload;

		@BeforeAll
		void setUpAll(
				@TempDir
				final Path tempDir) throws IOException {

			Files.createDirectories(tempDir.resolve("sdm/config").resolve(ELECTION_EVENT_ID));

			final PathResolver pathResolver = new PathResolver(tempDir.toString());
			controlComponentPublicKeysPayloadFileRepositoryTemp = new ControlComponentPublicKeysPayloadFileRepository(objectMapper, pathResolver);
		}

		@BeforeEach
		void setUp() {
			// Create keys.
			final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey = elGamalGenerator.genRandomPublicKey(KEY_SIZE);
			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey = elGamalGenerator.genRandomPublicKey(KEY_SIZE);
			final ControlComponentPublicKeys controlComponentPublicKeys = new ControlComponentPublicKeys(NODE_ID,
					ccrChoiceReturnCodesEncryptionPublicKey, ccmElectionPublicKey);

			// Create payload.
			controlComponentPublicKeysPayload = new ControlComponentPublicKeysPayload(gqGroup, ELECTION_EVENT_ID, controlComponentPublicKeys);
		}

		@Test
		@DisplayName("valid payload creates file")
		void save() {
			final Path savedPath = controlComponentPublicKeysPayloadFileRepositoryTemp.save(controlComponentPublicKeysPayload);

			assertTrue(Files.exists(savedPath));
		}

		@Test
		@DisplayName("null payload throws NullPointerException")
		void saveNullPayload() {
			assertThrows(NullPointerException.class, () -> controlComponentPublicKeysPayloadFileRepositoryTemp.save(null));
		}

		@Test
		@DisplayName("invalid path throws UncheckedIOException")
		void invalidPath() {
			final PathResolver pathResolver = new PathResolver("invalidPath");
			final ControlComponentPublicKeysPayloadFileRepository repository = new ControlComponentPublicKeysPayloadFileRepository(
					DomainObjectMapper.getNewInstance(), pathResolver);

			final UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> repository.save(controlComponentPublicKeysPayload));

			final Path payloadPath = pathResolver.resolveElectionEventPath(ELECTION_EVENT_ID).resolve(String.format(PAYLOAD_FILE_NAME, NODE_ID));
			final String errorMessage = String.format(
					"Failed to serialize control component public keys payload. [electionEventId: %s, nodeId: %s, path: %s]", ELECTION_EVENT_ID,
					NODE_ID, payloadPath);

			assertEquals(errorMessage, exception.getMessage());
		}

	}

	@Nested
	@DisplayName("calling existsById")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ExistsByIdTest {

		@Test
		@DisplayName("for existing payload returns true")
		void existingPayload() {
			assertTrue(controlComponentPublicKeysPayloadFileRepository.existsById(ELECTION_EVENT_ID, 1));
		}

		@Test
		@DisplayName("with invalid election event id throws FailedValidationException")
		void invalidElectionEventId() {
			assertThrows(FailedValidationException.class, () -> controlComponentPublicKeysPayloadFileRepository.existsById("invalidId", 1));
		}

		@Test
		@DisplayName("for non existing payload returns false")
		void nonExistingPayload() {
			assertFalse(controlComponentPublicKeysPayloadFileRepository.existsById(ELECTION_EVENT_ID, NON_EXISTING_NODE_ID));
		}

	}

	@Nested
	@DisplayName("calling findById")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class FindByIdTest {

		@Test
		@DisplayName("for existing payload returns it")
		void existingPayload() {
			assertTrue(controlComponentPublicKeysPayloadFileRepository.findById(ELECTION_EVENT_ID, NODE_ID).isPresent());
		}

		@Test
		@DisplayName("for non existing payload return empty optional")
		void nonExistingPayload() {
			assertFalse(controlComponentPublicKeysPayloadFileRepository.findById(ELECTION_EVENT_ID, NON_EXISTING_NODE_ID).isPresent());
		}

		@Test
		@DisplayName("for corrupted payload throws UncheckedIOException")
		void corruptedPayload() {
			final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
					() -> controlComponentPublicKeysPayloadFileRepository.findById(CORRUPTED_ELECTION_EVENT_ID, CORRUPTED_NODE_ID));

			final Path electionEventPath = pathResolver.resolveElectionEventPath(CORRUPTED_ELECTION_EVENT_ID);
			final Path payloadPath = electionEventPath.resolve(String.format(PAYLOAD_FILE_NAME, CORRUPTED_NODE_ID));
			final String errorMessage = String.format(
					"Failed to deserialize control component public keys payload. [electionEventId: %s, nodeId: %s, path: %s]",
					CORRUPTED_ELECTION_EVENT_ID, CORRUPTED_NODE_ID, payloadPath);

			assertEquals(errorMessage, exception.getMessage());
		}

	}

	@Nested
	@DisplayName("calling findAllOrderByNodeId")
	class FindAllTest {

		@Test
		@DisplayName("returns all payloads")
		void allPayloads() {
			final List<ControlComponentPublicKeysPayload> publicKeys = controlComponentPublicKeysPayloadFileRepository.findAllOrderByNodeId(ELECTION_EVENT_ID);

			assertEquals(publicKeys.size(), NODE_IDS.size());
		}

		@Test
		@DisplayName("for non existing election event returns empty list")
		void nonExistingElectionEvent() {
			final List<ControlComponentPublicKeysPayload> payloads = controlComponentPublicKeysPayloadFileRepository.findAllOrderByNodeId(WRONG_ELECTION_EVENT_ID);

			assertEquals(Collections.emptyList(), payloads);
		}

		@Test
		@DisplayName("with one corrupted payload throws UncheckedIOException")
		void corruptedPayload() {
			final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
					() -> controlComponentPublicKeysPayloadFileRepository.findAllOrderByNodeId(CORRUPTED_ELECTION_EVENT_ID));

			final Path electionEventPath = pathResolver.resolveElectionEventPath(CORRUPTED_ELECTION_EVENT_ID);
			final Path payloadPath = electionEventPath.resolve(String.format(PAYLOAD_FILE_NAME, CORRUPTED_NODE_ID));
			final String errorMessage = String.format("Failed to deserialize control component public keys payload. [electionEventId: %s, path: %s]",
					CORRUPTED_ELECTION_EVENT_ID, payloadPath);

			assertEquals(errorMessage, exception.getMessage());
		}

	}

}
