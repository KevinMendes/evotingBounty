/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetPreparationService.CHALLENGE_PROFILE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.security.KeyPair;
import java.security.PrivateKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.securedatamanager.services.application.sign.FileSignerService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(VotingCardSetPreparationServiceTest.PrivateConfiguration.class)
@DisplayName("Use VotingCardSetPreparationService to run prepare step")
class VotingCardSetPreparationServiceTest {

	private final String electionEventId = "3a2434c5a1004d71ac53b55d3ccdbfb8";
	private final String votingCardSetId = "b7e28ca876364dfa9a9315d795f59172";
	private String privateKeyPEM;

	@Mock
	private VotingCardSetRepository votingCardSetRepositoryMock;

	@Mock
	private ElectionEventService electionEventServiceMock;

	@Mock
	private PathResolver pathResolverMock;

	@Autowired
	private FileSignerService fileSignerService;

	@Autowired
	private ObjectMapper objectMapper;

	private VotingCardSetPreparationService votingCardSetPreparationService;

	@BeforeEach
	void setUp() throws GeneralCryptoLibException {
		final AsymmetricService asymmetricService = new AsymmetricService();
		final KeyPair keyPairForSigning = asymmetricService.getKeyPairForSigning();
		final PrivateKey privateKey = keyPairForSigning.getPrivate();
		privateKeyPEM = PemUtils.privateKeyToPem(privateKey);

		votingCardSetPreparationService = new VotingCardSetPreparationService(votingCardSetRepositoryMock, electionEventServiceMock, pathResolverMock,
				fileSignerService, objectMapper, CHALLENGE_PROFILE);
	}

	@Test
	@DisplayName("with an invalid profiles")
	void prepareInvalidProfileTest() {
		votingCardSetPreparationService = new VotingCardSetPreparationService(votingCardSetRepositoryMock, electionEventServiceMock, pathResolverMock,
				fileSignerService, objectMapper, "invalidProfile1,invalidProfile2");

		votingCardSetPreparationService.prepare(electionEventId, votingCardSetId, privateKeyPEM);

		verify(pathResolverMock, Mockito.times(0)).resolvePrintingPath(electionEventId);
	}

	@Test
	@DisplayName("with an empty profile")
	void prepareEmptyProfileTest() {
		votingCardSetPreparationService = new VotingCardSetPreparationService(votingCardSetRepositoryMock, electionEventServiceMock, pathResolverMock,
				fileSignerService, objectMapper, "");

		votingCardSetPreparationService.prepare(electionEventId, votingCardSetId, privateKeyPEM);

		verify(pathResolverMock, Mockito.times(0)).resolvePrintingPath(electionEventId);
	}

	@Test
	@DisplayName("with null parameters")
	void prepareNullParametersTest() {
		assertAll(() -> assertThrows(NullPointerException.class, () -> votingCardSetPreparationService.prepare(null, votingCardSetId, privateKeyPEM)),
				() -> assertThrows(NullPointerException.class, () -> votingCardSetPreparationService.prepare(electionEventId, null, privateKeyPEM)),
				() -> assertThrows(NullPointerException.class,
						() -> votingCardSetPreparationService.prepare(electionEventId, votingCardSetId, null)));
	}

	@Test
	@DisplayName("with invalid parameters")
	void prepareInvalidParametersTest() {
		assertAll(() -> assertThrows(IllegalArgumentException.class,
						() -> votingCardSetPreparationService.prepare("a12", votingCardSetId, privateKeyPEM)),
				() -> assertThrows(IllegalArgumentException.class,
						() -> votingCardSetPreparationService.prepare(electionEventId, "b34", privateKeyPEM)));
	}

	@Configuration
	static class PrivateConfiguration {

		@Bean
		AsymmetricServiceAPI asymmetricServiceAPI() {
			return new AsymmetricService();
		}

		@Bean
		HashService hashService() {
			return HashService.getInstance();
		}

		@Bean
		FileSignerService metadataFileSigner(final AsymmetricServiceAPI asymmetricServiceAPI, final HashService hashService) {
			return new FileSignerService(asymmetricServiceAPI, hashService);
		}

		@Bean
		public ObjectMapper objectMapper() {
			return DomainObjectMapper.getNewInstance();
		}
	}
}
