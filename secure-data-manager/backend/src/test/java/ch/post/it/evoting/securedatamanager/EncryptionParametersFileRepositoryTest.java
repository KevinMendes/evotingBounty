/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.SignatureVerifier;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("EncryptionParametersFileRepository")
class EncryptionParametersFileRepositoryTest {

	private static final String ELECTION_EVENT_ID = "314bd34dcf6e4de4b771a92fa3849d3d";
	private static final String WRONG_ELECTION_EVENT_ID = "414bd34dcf6e4de4b771a92fa3849d3d";

	private static EncryptionParametersFileRepository encryptionParametersFileRepository;

	@BeforeAll
	static void setUpAll() throws CertificateException, NoSuchProviderException, URISyntaxException {
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		final Path path = Paths.get(EncryptionParametersFileRepository.class.getResource("/encryptionParametersTest/").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());
		final SignatureVerifier signatureVerifier = new SignatureVerifier();

		encryptionParametersFileRepository = new EncryptionParametersFileRepository(objectMapper, pathResolver, signatureVerifier);
	}

	@Test
	@DisplayName("loading existing parameters returns them")
	void loadExisting() {
		assertTrue(encryptionParametersFileRepository.load(ELECTION_EVENT_ID).isPresent());
	}

	@Test
	@DisplayName("loading invalid election event id throws")
	void invalidElectionEventId() {
		assertThrows(FailedValidationException.class, () -> encryptionParametersFileRepository.load("invalidId"));
	}

	@Test
	@DisplayName("loading non existing parameters returns empty")
	void loadNonExisting() {
		assertFalse(encryptionParametersFileRepository.load(WRONG_ELECTION_EVENT_ID).isPresent());
	}

}