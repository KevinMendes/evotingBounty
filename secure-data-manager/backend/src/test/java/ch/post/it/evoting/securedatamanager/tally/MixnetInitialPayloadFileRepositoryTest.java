/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptoprimitives.domain.SerializationTestData;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("Use MixnetInitialPayloadPersistenceService to ")
class MixnetInitialPayloadFileRepositoryTest {

	private static final String FILE_NAME = "mixnetInitialPayload_";
	private static final String electionEventId = "f28493b098604663b6a6969f53f51b56";
	private static final String ballotId = "f0642fdfe4864f4985ac07c057da54b7";
	private static final String ballotBoxId = "672b1b49ed0341a3baa953d611b90b74";

	private static MixnetInitialPayload mixnetInitialPayload;
	private static MixnetInitialPayloadFileRepository mixnetInitialPayloadFileRepository;
	private static PathResolver payloadResolver;

	@TempDir
	Path tempDir;

	@BeforeAll
	static void setUpAll() {
		// Create ciphertexts list.
		final GqGroup gqGroup = SerializationTestData.getGqGroup();
		final int nbrMessage = 4;

		final List<ElGamalMultiRecipientCiphertext> ciphertexts = SerializationTestData.getCiphertexts(nbrMessage);
		final ElGamalMultiRecipientPublicKey electionPublicKey = SerializationTestData.getPublicKey();

		// Generate random bytes for signature content and create payload signature.
		final byte[] randomBytes = new byte[10];
		new SecureRandom().nextBytes(randomBytes);
		final X509Certificate certificate = SerializationTestData.generateTestCertificate();

		mixnetInitialPayload = new MixnetInitialPayload(electionEventId, ballotBoxId, gqGroup, ciphertexts, electionPublicKey);

		payloadResolver = Mockito.mock(PathResolver.class);
		mixnetInitialPayloadFileRepository = new MixnetInitialPayloadFileRepository(FILE_NAME, DomainObjectMapper.getNewInstance(), payloadResolver);
	}

	@Test
	@DisplayName("save MixnetInitialPayload file")
	void saveMixnetPayload() {
		// Mock payloadResolver path
		when(payloadResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId)).thenReturn(tempDir);
		final Path payloadPath = tempDir.resolve(FILE_NAME + "0" + Constants.JSON);

		assertFalse(Files.exists(payloadPath), "The mixnet payload file should not exist at this point");

		// Write payload
		assertNotNull(mixnetInitialPayloadFileRepository.savePayload(electionEventId, ballotId, ballotBoxId, mixnetInitialPayload));

		assertTrue(Files.exists(payloadPath), "The mixnet payload file should exist at this point");
	}

}
