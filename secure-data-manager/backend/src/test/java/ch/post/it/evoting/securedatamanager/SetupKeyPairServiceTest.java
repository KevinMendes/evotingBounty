/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.utils.SignatureVerifier;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("SetupKeyPairService")
class SetupKeyPairServiceTest {

	private static final String ELECTION_EVENT_ID = "314bd34dcf6e4de4b771a92fa3849d3d";
	private static final String WRONG_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";
	private static final String INVALID_ELECTION_EVENT_ID = "invalidId";
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final RandomService randomService = new RandomService();

	private static GqGroup gqGroup;
	private static ObjectMapper objectMapper;
	private static SetupKeyPairService setupKeyPairService;
	private static EncryptionParametersFileRepository encryptionParametersFileRepository;

	@BeforeAll
	static void setUpAll() throws CertificateException, NoSuchProviderException, URISyntaxException {
		final Path path = Paths.get(EncryptionParametersFileRepository.class.getResource("/setupKeyPairTest/").toURI());

		gqGroup = GroupTestData.getGqGroup();
		objectMapper = DomainObjectMapper.getNewInstance();
		final PathResolver pathResolver = new PathResolver(path.toString());

		final SignatureVerifier signatureVerifier = new SignatureVerifier();
		encryptionParametersFileRepository = new EncryptionParametersFileRepository(objectMapper, pathResolver,
				signatureVerifier);
		final SetupKeyPairFileRepository setupKeyPairFileRepository = new SetupKeyPairFileRepository(objectMapper, pathResolver,
				encryptionParametersFileRepository);
		setupKeyPairService = new SetupKeyPairService(setupKeyPairFileRepository);
	}

	@Nested
	@DisplayName("saving")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SaveTest {

		private ElGamalMultiRecipientKeyPair setupKeyPair;
		private SetupKeyPairService setupKeyPairServiceTempDir;

		@BeforeAll
		void setUpAll(
				@TempDir
				final Path tempDir) throws IOException {

			Files.createDirectories(tempDir.resolve("sdm/config").resolve(ELECTION_EVENT_ID).resolve(Constants.CONFIG_DIR_NAME_OFFLINE));

			final PathResolver pathResolver = new PathResolver(tempDir.toString());

			final SetupKeyPairFileRepository setupKeyPairFileRepository = new SetupKeyPairFileRepository(objectMapper, pathResolver,
					encryptionParametersFileRepository);
			setupKeyPairServiceTempDir = new SetupKeyPairService(setupKeyPairFileRepository);
		}

		@BeforeEach
		void setUp() {
			final int numElements = secureRandom.nextInt(10) + 1;
			setupKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, numElements, randomService);
		}

		@Test
		@DisplayName("a valid key pair does not throw")
		void saveValidKeyPair() {
			assertDoesNotThrow(() -> setupKeyPairServiceTempDir.save(ELECTION_EVENT_ID, setupKeyPair));
		}

		@Test
		@DisplayName("with invalid election event id throws FailedValidationException")
		void saveInvalidElectionEventId() {
			assertThrows(FailedValidationException.class, () -> setupKeyPairServiceTempDir.save(INVALID_ELECTION_EVENT_ID, setupKeyPair));
		}

		@Test
		@DisplayName("with null setup key pair throws NullPointerException")
		void saveNullSetupKeyPair() {
			assertThrows(NullPointerException.class, () -> setupKeyPairServiceTempDir.save(ELECTION_EVENT_ID, null));
		}

	}

	@Nested
	@DisplayName("loading")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class LoadTest {

		@Test
		@DisplayName("existing key pair returns it")
		void loadExistingKeyPair() {
			assertNotNull(setupKeyPairService.load(ELECTION_EVENT_ID));
		}

		@Test
		@DisplayName("invalid election event id")
		void loadInvalidElectionEventId() {
			assertThrows(FailedValidationException.class, () -> setupKeyPairService.load(INVALID_ELECTION_EVENT_ID));
		}

		@Test
		@DisplayName("non existing key pair throws IllegalArgumentException")
		void loadNonExistingKeyPair() {
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> setupKeyPairService.load(WRONG_ELECTION_EVENT_ID));

			final String errorMessage = String.format("Setup key pair not found. [electionEventId: %s]", WRONG_ELECTION_EVENT_ID);
			assertEquals(errorMessage, Throwables.getRootCause(exception).getMessage());
		}

	}

}