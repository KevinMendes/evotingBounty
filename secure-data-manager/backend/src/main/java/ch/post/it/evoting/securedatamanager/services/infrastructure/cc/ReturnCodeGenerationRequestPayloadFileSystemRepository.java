/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.cc;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;

/**
 * Manages the storage and retrieval of return code generation (for both choice return codes and vote cast return codes) request payloads stored on
 * the file system.
 */
@Repository
public class ReturnCodeGenerationRequestPayloadFileSystemRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodeGenerationRequestPayloadFileSystemRepository.class);

	private final PathResolver pathResolver;

	private final ObjectMapper objectMapper;

	@Autowired
	public ReturnCodeGenerationRequestPayloadFileSystemRepository(final PathResolver pathResolver, final ObjectMapper objectMapper) {
		this.pathResolver = pathResolver;
		this.objectMapper = objectMapper;
	}

	/**
	 * Obtains the path where the pre-computed data is stored.
	 *
	 * @param electionEventId       the election event the payload belongs to
	 * @param verificationCardSetId the verification card set the payload was generated for
	 * @param chunkId               the chunk identifier
	 */
	public static Path getStoragePath(final PathResolver pathResolver, final String electionEventId, final String verificationCardSetId, final int chunkId) {
		final String fileName = Constants.CONFIG_FILE_NAME_PREFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD + chunkId
				+ Constants.CONFIG_FILE_NAME_SUFFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD;
		return getVerificationCardSetFolder(pathResolver, electionEventId, verificationCardSetId).resolve(fileName);
	}

	private static Path getVerificationCardSetFolder(final PathResolver pathResolver, final String electionEventId, final String verificationCardSetId) {
		return pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION).resolve(verificationCardSetId);
	}

	private static boolean isPayloadFile(final Path file) {
		final String name = file.getFileName().toString();
		return name.startsWith(Constants.CONFIG_FILE_NAME_PREFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD) && name
				.endsWith(Constants.CONFIG_FILE_NAME_SUFFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD);
	}

	/**
	 * Stores a return code generation request payload.
	 *
	 * @param payload the payload to store.
	 * @throws PayloadStorageException if the storage did not succeed
	 */
	public void store(final ReturnCodeGenerationRequestPayload payload) throws PayloadStorageException {
		final String electionEventId = payload.getElectionEventId();
		final String verificationCardSetId = payload.getVerificationCardSetId();
		final int chunkId = payload.getChunkId();

		LOGGER.info("Storing choice code generation request payload {}-{}-{}...", electionEventId, verificationCardSetId, chunkId);

		final Path file = getStoragePath(pathResolver, electionEventId, verificationCardSetId, chunkId);
		try {
			if (!exists(file.getParent())) {
				createDirectories(file.getParent());
			}
			try (final OutputStream stream = newOutputStream(file)) {
				objectMapper.writeValue(stream, payload);
			}
		} catch (final IOException e) {
			throw new PayloadStorageException(e);
		}

		LOGGER.info("Choice code generation request payload {}-{}-{} is now stored in {}", electionEventId, verificationCardSetId, chunkId,
				file.toAbsolutePath());
	}

	/**
	 * Retrieves a return code generation request payload.
	 *
	 * @param electionEventId       the identifier of the election event the verification card set belongs to
	 * @param verificationCardSetId the identifier of the verification card set the payload is for
	 * @param chunkId               the chunk identifier
	 * @return the requested return code generation request payload
	 * @throws PayloadStorageException if retrieving the payload did not succeed
	 */
	public ReturnCodeGenerationRequestPayload retrieve(final String electionEventId, final String verificationCardSetId, final int chunkId)
			throws PayloadStorageException {
		final Path file = getStoragePath(pathResolver, electionEventId, verificationCardSetId, chunkId);

		LOGGER.info("Retrieving choice code generation request payload {}-{}-{} from {}...", electionEventId, verificationCardSetId, chunkId,
				file.toAbsolutePath());

		final ReturnCodeGenerationRequestPayload payload;
		try (final InputStream stream = newInputStream(file)) {
			payload = objectMapper.readValue(stream, ReturnCodeGenerationRequestPayload.class);
		} catch (final IOException e) {
			throw new PayloadStorageException(e);
		}

		LOGGER.info("Choice code generation request payload {}-{}-{} retrieved.", electionEventId, verificationCardSetId, chunkId);

		return payload;
	}

	/**
	 * Removes all the payloads for given election event and verification card set.
	 *
	 * @param electionEventId       the election event identifier
	 * @param verificationCardSetId the verification card set identifier
	 * @throws PayloadStorageException failed to remove payloads
	 */
	public void remove(final String electionEventId, final String verificationCardSetId) throws PayloadStorageException {
		try (final DirectoryStream<Path> files = getPayloadFiles(electionEventId, verificationCardSetId)) {
			for (final Path file : files) {
				deleteIfExists(file);
			}
		} catch (final NoSuchFileException e) {
			LOGGER.debug("The verification card set folder does not exist.", e);
			// nothing to do, the verification card set folder does not exist.
		} catch (final IOException e) {
			throw new PayloadStorageException(e);
		}
	}

	/**
	 * Returns the number of payloads for given election event and verification card set.
	 *
	 * @param electionEventId       the election event identifier
	 * @param verificationCardSetId the verification card set identifier
	 * @return the number of payloads
	 * @throws PayloadStorageException failed to get the number of payloads
	 */
	public int getCount(final String electionEventId, final String verificationCardSetId) throws PayloadStorageException {
		int count = 0;
		try (final DirectoryStream<Path> files = getPayloadFiles(electionEventId, verificationCardSetId)) {
			for (
					@SuppressWarnings("unused")
					final
					Path file : files) {
				count++;
			}
		} catch (final IOException e) {
			throw new PayloadStorageException(e);
		}
		return count;
	}

	private DirectoryStream<Path> getPayloadFiles(final String electionEventId, final String verificationCardSetId) throws IOException {
		final Path folder = getVerificationCardSetFolder(pathResolver, electionEventId, verificationCardSetId);
		final Filter<? super Path> filter = ReturnCodeGenerationRequestPayloadFileSystemRepository::isPayloadFile;
		return newDirectoryStream(folder, filter);
	}
}
