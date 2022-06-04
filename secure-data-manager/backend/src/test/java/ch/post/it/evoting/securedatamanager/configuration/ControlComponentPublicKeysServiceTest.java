/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.securedatamanager.commons.Constants.NODE_IDS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
@DisplayName("A ControlComponentPublicKeysService")
class ControlComponentPublicKeysServiceTest {

	private static final String ELECTION_EVENT_ID = "314bd34dcf6e4de4b771a92fa3849d3d";
	private static final String WRONG_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";
	private static final String NOT_ENOUGH_ELECTION_EVENT_ID = "614bd34dcf6e4de4b771a92fa3849d3d";
	private static final String TOO_MANY_ELECTION_EVENT_ID = "714bd34dcf6e4de4b771a92fa3849d3d";
	private static final String INVALID_ID = "invalidId";
	private static final int NODE_ID = 1;
	private static final int KEY_SIZE = 3;

	private static GqGroup gqGroup;
	private static ObjectMapper objectMapper;
	private static ElGamalGenerator elGamalGenerator;
	private static CryptolibPayloadSignatureService signatureServiceMock;
	private static PlatformRootCertificateService platformRootCertificateService;
	private static ControlComponentPublicKeysService controlComponentPublicKeysService;

	@SystemStub
	private static EnvironmentVariables environmentVariables;

	@BeforeAll
	static void setUpAll() throws URISyntaxException {
		environmentVariables.set("SECURITY_LEVEL", "TESTING_ONLY");
		gqGroup = GroupTestData.getGqGroup();
		elGamalGenerator = new ElGamalGenerator(gqGroup);
		objectMapper = DomainObjectMapper.getNewInstance();
		final Path path = Paths.get(ControlComponentPublicKeysServiceTest.class.getResource("/controlComponentPublicKeysTest/").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());

		final ControlComponentPublicKeysPayloadFileRepository controlComponentPublicKeysPayloadFileRepository = new ControlComponentPublicKeysPayloadFileRepository(
				objectMapper, pathResolver);

		final PlatformRootCertificateFileRepository platformRootCertificateFileRepository = new PlatformRootCertificateFileRepository(pathResolver);
		platformRootCertificateService = new PlatformRootCertificateService(platformRootCertificateFileRepository);

		signatureServiceMock = mock(CryptolibPayloadSignatureService.class);

		controlComponentPublicKeysService = new ControlComponentPublicKeysService(platformRootCertificateService, signatureServiceMock,
				controlComponentPublicKeysPayloadFileRepository);
	}

	@Nested
	@DisplayName("saving")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SaveTest {

		private ControlComponentPublicKeysService controlComponentPublicKeysServiceTemp;

		private ControlComponentPublicKeysPayload controlComponentPublicKeysPayload;

		@BeforeAll
		void setUpAll(
				@TempDir
				final Path tempDir) throws IOException {

			Files.createDirectories(tempDir.resolve("sdm/config").resolve(ELECTION_EVENT_ID));

			final PathResolver pathResolver = new PathResolver(tempDir.toString());
			final ControlComponentPublicKeysPayloadFileRepository controlComponentPublicKeysPayloadFileRepositoryTemp = new ControlComponentPublicKeysPayloadFileRepository(
					objectMapper, pathResolver);

			controlComponentPublicKeysServiceTemp = new ControlComponentPublicKeysService(platformRootCertificateService, signatureServiceMock,
					controlComponentPublicKeysPayloadFileRepositoryTemp);
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
		@DisplayName("a valid payload does not throw")
		void saveValidPayload() {
			assertDoesNotThrow(() -> controlComponentPublicKeysServiceTemp.save(controlComponentPublicKeysPayload));
		}

		@Test
		@DisplayName("a null payload throws NullPointerException")
		void saveNullPayload() {
			assertThrows(NullPointerException.class, () -> controlComponentPublicKeysServiceTemp.save(null));
		}

	}

	@Nested
	@DisplayName("calling exist")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ExistTest {

		@Test
		@DisplayName("for valid election event returns true")
		void existValidElectionEvent() {
			assertTrue(controlComponentPublicKeysService.exist(ELECTION_EVENT_ID));
		}

		@Test
		@DisplayName("for invalid election event id throws FailedValidationException")
		void existInvalidElectionEvent() {
			assertThrows(FailedValidationException.class, () -> controlComponentPublicKeysService.exist(INVALID_ID));
		}

		@Test
		@DisplayName("for non existing election event returns false")
		void existNonExistingElectionEvent() {
			assertFalse(controlComponentPublicKeysService.exist(WRONG_ELECTION_EVENT_ID));
		}

		@Test
		@DisplayName("for missing payloads return false")
		void existMissingPayloads() {
			assertFalse(controlComponentPublicKeysService.exist(NOT_ENOUGH_ELECTION_EVENT_ID));
		}

	}

	@Nested
	@DisplayName("loading")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class LoadTest {

		@BeforeEach
		void setUp() throws PayloadVerificationException {
			when(signatureServiceMock.verify(any(), any())).thenReturn(true);
		}

		@Test
		@DisplayName("existing election event returns all payloads")
		void loadExistingElectionEvent() {
			final List<ControlComponentPublicKeys> controlComponentPublicKeys = controlComponentPublicKeysService.load(ELECTION_EVENT_ID);

			final List<Integer> payloadsNodeIds = controlComponentPublicKeys.stream()
					.map(ControlComponentPublicKeys::getNodeId)
					.collect(Collectors.toList());

			assertEquals(NODE_IDS.size(), controlComponentPublicKeys.size());
			assertTrue(payloadsNodeIds.containsAll(NODE_IDS));
		}

		@Test
		@DisplayName("invalid election event id throws FailedValidationException")
		void loadInvalidElectionEventId() {
			assertThrows(FailedValidationException.class, () -> controlComponentPublicKeysService.load(INVALID_ID));
		}

		@Test
		@DisplayName("existing election event with missing payloads throws IllegalStateException")
		void loadMissingPayloads() {
			final IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> controlComponentPublicKeysService.load(NOT_ENOUGH_ELECTION_EVENT_ID));

			final String errorMessage = String.format("Wrong number of control component public keys payloads. [required node ids: %s, found: %s]",
					NODE_IDS, Collections.singletonList(1));
			assertEquals(errorMessage, Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("existing election event with too many payloads throws IllegalStateException")
		void loadTooManyPayloads() {
			final IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> controlComponentPublicKeysService.load(TOO_MANY_ELECTION_EVENT_ID));

			final String errorMessage = String.format("Wrong number of control component public keys payloads. [required node ids: %s, found: %s]",
					NODE_IDS, Arrays.asList(1, 2, 3, 4, 4));
			assertEquals(errorMessage, Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("payload with invalid signature throws IllegalStateException")
		void loadInvalidPayloadSignature() throws PayloadVerificationException {
			when(signatureServiceMock.verify(any(), any())).thenReturn(false);

			final IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> controlComponentPublicKeysService.load(ELECTION_EVENT_ID));

			final String errorMessage = String.format(
					"The signature of the control component public keys payload is invalid. [electionEventId: %s, nodeId: %s]",
					ELECTION_EVENT_ID, 1);
			assertEquals(errorMessage, Throwables.getRootCause(exception).getMessage());
		}

	}

}
