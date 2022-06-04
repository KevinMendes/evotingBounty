/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HashServiceTest {

	private static final String TEST_MEMBER = "test";
	private static final byte[] TEST_BYTES = TEST_MEMBER.getBytes(StandardCharsets.UTF_8);
	private static final String TEST_BYTES_B64_HASH = "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=";
	private static final String TEST_FILE_TO_HASH = "fileToHash";

	private static Path fileToHash;
	private static HashService hashService;

	@BeforeAll
	static void setUp() throws Exception {
		hashService = new HashService();
		fileToHash = Files.createTempFile(TEST_FILE_TO_HASH, null);
		fileToHash.toFile().deleteOnExit();
		Files.write(fileToHash, TEST_BYTES);
	}

	@Test
	void testHashFileHappyPath() throws Exception {
		final String fileHash = hashService.getB64FileHash(fileToHash);
		assertEquals(TEST_BYTES_B64_HASH, fileHash);
	}

	@Test
	void testHashBytesHappyPath() throws HashServiceException {
		final String bytesHash = hashService.getB64Hash(TEST_BYTES);
		assertEquals(TEST_BYTES_B64_HASH, bytesHash);
	}

	@Test
	void testHashMemberHappyPath() throws HashServiceException {
		final String memberHash = hashService.getHashValueForMember(TEST_MEMBER);
		assertEquals(TEST_BYTES_B64_HASH, memberHash);
	}

	@Test
	void testHashFileDoesNotExistThrowException() {
		final Path wrongPath = Paths.get("wrongPath");
		final HashServiceException exception = assertThrows(HashServiceException.class, () -> hashService.getB64FileHash(wrongPath));
		assertEquals("Error computing the hash of " + wrongPath.toString(), exception.getMessage());
	}

	@Test
	void testHashBytesNoDataToHash() {
		final byte[] dataToHash = null;
		final HashServiceException exception = assertThrows(HashServiceException.class, () -> hashService.getB64Hash(dataToHash));
		assertEquals("Error computing a hash with provided bytes", exception.getMessage());
	}

	@Test
	void testHashMemberNoDataToHash() {
		final String dataToHash = "";
		final HashServiceException exception = assertThrows(HashServiceException.class, () -> hashService.getHashValueForMember(dataToHash));
		assertEquals("Error computing a hash with provided bytes", exception.getMessage());
	}
}
