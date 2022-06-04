/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.services.application.service.CertificateManagementException;
import ch.post.it.evoting.securedatamanager.services.application.service.PlatformRootCAService;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(MixnetShufflePayloadServiceTest.PrivateConfiguration.class)
@DisplayName("A MixnetShufflePayloadService")
class MixnetShufflePayloadServiceTest {

	@Autowired
	private MixnetShufflePayloadService mixnetShufflePayloadService;

	@Autowired
	private MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository;

	@Autowired
	private PlatformRootCAService platformRootCAService;

	@Configuration
	static class PrivateConfiguration {

		@Bean
		AsymmetricServiceAPI asymmetricServiceAPI() {
			return new AsymmetricService();
		}

		@Bean
		PlatformRootCAService platformRootCAService() {
			return mock(PlatformRootCAService.class);
		}

		@Bean
		MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository() {
			return mock(MixnetShufflePayloadFileRepository.class);
		}

		@Bean
		CryptolibPayloadSignatureService cryptolibPayloadSignatureService(final AsymmetricServiceAPI asymmetricService) {
			return new CryptolibPayloadSignatureService(asymmetricService, hashService());
		}

		@Bean
		HashService hashService() {
			return HashService.getInstance();
		}

		@Bean
		MixnetShufflePayloadService MixnetShufflePayloadService(final CryptolibPayloadSignatureService cryptolibPayloadSignatureService,
				final MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository,
				final PlatformRootCAService platformRootCAService) {
			return new MixnetShufflePayloadService(cryptolibPayloadSignatureService, mixnetShufflePayloadFileRepository, platformRootCAService);
		}

	}

	@Nested
	@DisplayName("calling areOnlinePayloadSignaturesValid")
	class AreOnlinePayloadSignaturesValidTest {

		private final String electionEventId = "426b2de832ac4cf384af0f68bb2b5d20";
		private final String ballotId = "0fca7e83f8cf41acb2a48b67b2e37a71";
		private final String ballotBoxId = "64ea41b3881e4bef81a2cddab7597ecb";

		@Test
		@DisplayName("with a null input throws a NullPointerException.")
		void nullInputTest() {
			assertAll(() -> assertThrows(NullPointerException.class,
							() -> mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(null, ballotId, ballotBoxId)),
					() -> assertThrows(NullPointerException.class,
							() -> mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(electionEventId, null, ballotBoxId)),
					() -> assertThrows(NullPointerException.class,
							() -> mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(electionEventId, ballotId, null)));
		}

		@Test
		@DisplayName("with a non-UUID input throws a FailedValidationException.")
		void nonUUIDInputTest() {
			assertAll(() -> assertThrows(FailedValidationException.class,
							() -> mixnetShufflePayloadService.areOnlinePayloadSignaturesValid("electionEventId", ballotId, ballotBoxId)),
					() -> assertThrows(FailedValidationException.class,
							() -> mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(electionEventId, "ballotId", ballotBoxId)),
					() -> assertThrows(FailedValidationException.class,
							() -> mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(electionEventId, ballotId, "ballotBoxId")));
		}

		@Test
		@DisplayName("with valid inputs behaves as expected.")
		void happyPathTest() throws URISyntaxException, IOException, CertificateManagementException, GeneralCryptoLibException {

			final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

			final MixnetShufflePayload payload1 = objectMapper.readValue(loadMixnetShufflePayloadPath(1).toFile(), MixnetShufflePayload.class);
			when(mixnetShufflePayloadFileRepository.getPayload(electionEventId, ballotId, ballotBoxId, 1)).thenReturn(payload1);
			final MixnetShufflePayload payload2 = objectMapper.readValue(loadMixnetShufflePayloadPath(2).toFile(), MixnetShufflePayload.class);
			when(mixnetShufflePayloadFileRepository.getPayload(electionEventId, ballotId, ballotBoxId, 2)).thenReturn(payload2);
			final MixnetShufflePayload payload3 = objectMapper.readValue(loadMixnetShufflePayloadPath(3).toFile(), MixnetShufflePayload.class);
			when(mixnetShufflePayloadFileRepository.getPayload(electionEventId, ballotId, ballotBoxId, 3)).thenReturn(payload3);

			when(platformRootCAService.load()).thenReturn(
					(X509Certificate) PemUtils.certificateFromPem(new String(Files.readAllBytes(loadPlatformRootCAPath()), StandardCharsets.UTF_8)));

			assertTrue(mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(electionEventId, ballotId, ballotBoxId));
		}

		private Path loadMixnetShufflePayloadPath(final int controlComponentNodeId) throws URISyntaxException {
			final String path = String
					.format("/MixnetShufflePayloadServiceTest/426b2de832ac4cf384af0f68bb2b5d20/ONLINE/electionInformation/ballots/0fca7e83f8cf41acb2a48b67b2e37a71/ballotBoxes/64ea41b3881e4bef81a2cddab7597ecb/mixnetShufflePayload_%s.json",
							controlComponentNodeId);

			return Paths.get(MixnetShufflePayloadServiceTest.class.getResource(path).toURI());
		}

		private Path loadPlatformRootCAPath() throws URISyntaxException {
			return Paths.get(MixnetShufflePayloadServiceTest.class.getResource("/MixnetShufflePayloadServiceTest/platformRootCA.pem").toURI());
		}
	}
}
