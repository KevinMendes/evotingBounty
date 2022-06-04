/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Optional;

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

class SetupKeyPairFileRepositoryTest {

	private static final String ELECTION_EVENT_ID = "314bd34dcf6e4de4b771a92fa3849d3d";
	private static final String WRONG_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";
	private static final String CORRUPTED_ELECTION_EVENT_ID = "514bd34dcf6e4de4b771a92fa3849d3d";
	private static final String INVALID_ELECTION_EVENT_ID = "invalidId";
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final RandomService randomService = new RandomService();

	private static GqGroup gqGroup;
	private static ObjectMapper objectMapper;
	private static PathResolver pathResolver;
	private static SetupKeyPairFileRepository setupKeyPairFileRepository;
	private static EncryptionParametersFileRepository encryptionParametersFileRepository;

	@BeforeAll
	static void setUpAll() throws CertificateException, NoSuchProviderException, URISyntaxException {
		gqGroup = GroupTestData.getGqGroup();
		objectMapper = DomainObjectMapper.getNewInstance();

		final Path path = Paths.get(EncryptionParametersFileRepository.class.getResource("/setupKeyPairTest/").toURI());
		pathResolver = new PathResolver(path.toString());

		final SignatureVerifier signatureVerifier = new SignatureVerifier();
		encryptionParametersFileRepository = new EncryptionParametersFileRepository(objectMapper, pathResolver, signatureVerifier);

		setupKeyPairFileRepository = new SetupKeyPairFileRepository(objectMapper, pathResolver, encryptionParametersFileRepository);
	}

	@Nested
	@DisplayName("saving")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SaveTest {

		private ElGamalMultiRecipientKeyPair setupKeyPair;
		private SetupKeyPairFileRepository setupKeyPairFileRepositoryTemp;

		@BeforeAll
		void setUpAll(
				@TempDir
				final Path tempDir) throws IOException {

			Files.createDirectories(tempDir.resolve("sdm/config").resolve(ELECTION_EVENT_ID).resolve(Constants.CONFIG_DIR_NAME_OFFLINE));

			final PathResolver pathResolver = new PathResolver(tempDir.toString());
			setupKeyPairFileRepositoryTemp = new SetupKeyPairFileRepository(objectMapper, pathResolver, encryptionParametersFileRepository);
		}

		@BeforeEach
		void setUp() {
			final int numElements = secureRandom.nextInt(10) + 1;
			setupKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, numElements, randomService);
		}

		@Test
		@DisplayName("valid key pair creates file")
		void saveValidKeyPair() {
			final Path savedPath = setupKeyPairFileRepositoryTemp.save(ELECTION_EVENT_ID, setupKeyPair);

			assertTrue(Files.exists(savedPath));
		}

		@Test
		@DisplayName("null key pair throws NullPointerException")
		void saveNullKeyPair() {
			assertThrows(NullPointerException.class, () -> setupKeyPairFileRepositoryTemp.save(ELECTION_EVENT_ID, null));
		}

		@Test
		@DisplayName("invalid election end id throws FailedValidationException")
		void saveInvalidElectionEventId() {
			assertThrows(FailedValidationException.class, () -> setupKeyPairFileRepositoryTemp.save(INVALID_ELECTION_EVENT_ID, setupKeyPair));
		}

		@Test
		@DisplayName("invalid path throws UncheckedIOException")
		void saveInvalidPath() {
			final PathResolver pathResolver = new PathResolver("invalidPath");
			final SetupKeyPairFileRepository repository = new SetupKeyPairFileRepository(objectMapper, pathResolver,
					encryptionParametersFileRepository);

			final UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> repository.save(ELECTION_EVENT_ID, setupKeyPair));

			final Path setupKeyPairPath = pathResolver.resolveOfflinePath(ELECTION_EVENT_ID).resolve(Constants.SETUP_KEY_PAIR_FILE_NAME);
			final String errorMessage = String.format("Failed to serialize setup key pair. [electionEventId: %s, path: %s]", ELECTION_EVENT_ID,
					setupKeyPairPath);

			assertEquals(errorMessage, exception.getMessage());
		}

		@Nested
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		@DisplayName("finding by id")
		class FindByIdTest {

			@Test
			@DisplayName("for existing key pair is present")
			void findByIdExistingKeyPair() {
				assertTrue(setupKeyPairFileRepository.findById(ELECTION_EVENT_ID).isPresent());
			}

			@Test
			@DisplayName("with invalid election event id throws NullPointerException")
			void findByIdInvalidId() {
				assertThrows(FailedValidationException.class, () -> setupKeyPairFileRepository.findById(INVALID_ELECTION_EVENT_ID));
			}

			@Test
			@DisplayName("with wrong election event id returns empty optional")
			void findByIdWrongElectionEventId() {
				assertFalse(setupKeyPairFileRepository.findById(WRONG_ELECTION_EVENT_ID).isPresent());
			}

			@Test
			@DisplayName("with missing encryption parameters throws IllegalStateException")
			void findByIdMissingEncryptionParameters() {
				final EncryptionParametersFileRepository encryptionParametersFileRepositoryMock = mock(EncryptionParametersFileRepository.class);
				when(encryptionParametersFileRepositoryMock.load(ELECTION_EVENT_ID)).thenReturn(Optional.empty());

				final SetupKeyPairFileRepository repository = new SetupKeyPairFileRepository(objectMapper, pathResolver,
						encryptionParametersFileRepositoryMock);

				final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> repository.findById(ELECTION_EVENT_ID));

				final String errorMessage = String.format("Encryption parameters not found. [electionEventId: %s]", ELECTION_EVENT_ID);
				assertEquals(errorMessage, Throwables.getRootCause(exception).getMessage());
			}

			@Test
			@DisplayName("with corrupted key pair throws UncheckedIOException")
			void findByIdCorruptedKeyPair() {
				final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
						() -> setupKeyPairFileRepository.findById(CORRUPTED_ELECTION_EVENT_ID));

				final Path setupKeyPairPath = pathResolver.resolveOfflinePath(CORRUPTED_ELECTION_EVENT_ID)
						.resolve(Constants.SETUP_KEY_PAIR_FILE_NAME);
				final String errorMessage = String.format("Failed to deserialize setup key pair. [electionEventId: %s, path: %s]",
						CORRUPTED_ELECTION_EVENT_ID, setupKeyPairPath);
				assertEquals(errorMessage, exception.getMessage());
			}
		}

	}

}