/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.securedatamanager.config.commons.utils.SignatureVerifier;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("EncryptionParametersService")
class EncryptionParametersServiceTest {

	private static final String ELECTION_EVENT_ID = "314bd34dcf6e4de4b771a92fa3849d3d";
	private static final String WRONG_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";

	private static EncryptionParametersService encryptionParametersService;

	@BeforeAll
	static void setUpAll() throws CertificateException, NoSuchProviderException, URISyntaxException {
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		final Path path = Paths.get(EncryptionParametersFileRepository.class.getResource("/encryptionParametersTest/").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());
		final SignatureVerifier signatureVerifier = new SignatureVerifier();
		final EncryptionParametersFileRepository encryptionParametersFileRepository = new EncryptionParametersFileRepository(objectMapper,
				pathResolver,
				signatureVerifier);

		encryptionParametersService = new EncryptionParametersService(encryptionParametersFileRepository);
	}

	@Test
	@DisplayName("loading existing parameters returns them")
	void loadExisting() {
		final GqGroup gqGroup = assertDoesNotThrow(() -> encryptionParametersService.load(ELECTION_EVENT_ID));
		assertNotNull(gqGroup);
	}

	@Test
	@DisplayName("loading invalid election event id throws FailedValidationException")
	void invalidElectionEventId() {
		assertThrows(FailedValidationException.class, () -> encryptionParametersService.load("invalidId"));
	}

	@Test
	@DisplayName("loading non existing parameters throws IllegalStateException")
	void loadNonExisting() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> encryptionParametersService.load(WRONG_ELECTION_EVENT_ID));

		final String errorMessage = String.format("Encryption parameters not found. [electionEventId: %s]", WRONG_ELECTION_EVENT_ID);
		assertEquals(errorMessage, Throwables.getRootCause(exception).getMessage());
	}
}