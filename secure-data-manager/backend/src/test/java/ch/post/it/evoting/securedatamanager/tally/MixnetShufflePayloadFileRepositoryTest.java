/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.generators.MixnetShufflePayloadGenerator;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("Use MixnetPayloadPersistenceService to ")
class MixnetShufflePayloadFileRepositoryTest {

	private static final String FILE_PREFIX = "mixnetShufflePayload_";
	private static final String electionEventId = "f28493b098604663b6a6969f53f51b56";
	private static final String ballotId = "f0642fdfe4864f4985ac07c057da54b7";
	private static final String ballotBoxId = "672b1b49ed0341a3baa953d611b90b74";
	private static final int nodeId = 1;
	private static final Random random = new SecureRandom();

	private static MixnetShufflePayload expectedMixnetPayload;
	private static MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository;
	private static PathResolver payloadResolver;

	@TempDir
	Path tempDir;

	@BeforeAll
	static void setUpAll() {
		final int numVotes = random.nextInt(10) + 1;
		final int voteSize = random.nextInt(10) + 1;
		final GqGroup group = GroupTestData.getGqGroup();
		expectedMixnetPayload = new MixnetShufflePayloadGenerator(group).genPayload(numVotes, voteSize, nodeId);
		payloadResolver = Mockito.mock(PathResolver.class);
		mixnetShufflePayloadFileRepository = new MixnetShufflePayloadFileRepository(FILE_PREFIX, DomainObjectMapper.getNewInstance(),
				payloadResolver);
	}

	@Test
	@DisplayName("read MixnetPayload file")
	void readMixnetPayload() {
		// Mock payloadResolver path and write payload
		when(payloadResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId)).thenReturn(tempDir);
		mixnetShufflePayloadFileRepository.savePayload(electionEventId, ballotId, ballotBoxId, expectedMixnetPayload);

		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = Mockito.mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			// Read payload and check
			final MixnetShufflePayload actualMixnetPayload = mixnetShufflePayloadFileRepository.getPayload(electionEventId, ballotId, ballotBoxId,
					nodeId);

			assertEquals(expectedMixnetPayload, actualMixnetPayload);
		}
	}

	@Test
	@DisplayName("save MixnetPayload file")
	void saveMixnetPayload() {
		// Mock payloadResolver path
		when(payloadResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId)).thenReturn(tempDir);
		final Path expectedPayloadPath = tempDir.resolve("mixnetShufflePayload_" + nodeId + Constants.JSON);

		assertFalse(Files.exists(expectedPayloadPath), "The mixnet payload file should not exist at this point");

		// Write payload
		final Path actualPayloadPath = mixnetShufflePayloadFileRepository.savePayload(electionEventId, ballotId, ballotBoxId, expectedMixnetPayload);

		assertTrue(Files.exists(actualPayloadPath), "The mixnet payload file should exist at this point");
		assertEquals(expectedPayloadPath, actualPayloadPath, "Both payload paths should resolve to the same file");
	}

}
