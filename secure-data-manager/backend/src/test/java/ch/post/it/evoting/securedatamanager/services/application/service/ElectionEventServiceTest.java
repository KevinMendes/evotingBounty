/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.SetupKeyPairService;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.configuration.ControlComponentPublicKeysService;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenSetupEncryptionKeysService;
import ch.post.it.evoting.securedatamanager.services.application.exception.CCKeysNotExistException;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.service.ElectionEventDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * JUnit for the class {@link ElectionEventService}.
 */
@ExtendWith(MockitoExtension.class)
class ElectionEventServiceTest {

	private static ElGamalMultiRecipientKeyPair keyPair;

	@Spy
	private final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	@Mock
	private PathResolver pathResolver;
	@Mock
	private SetupKeyPairService setupKeyPairService;
	@Mock
	private EncryptionParametersService encryptionParametersServiceMock;
	@Mock
	private GenSetupEncryptionKeysService genSetupEncryptionKeysService;
	@Mock
	private ConfigurationEntityStatusService configurationEntityStatusService;
	@Mock
	private ControlComponentPublicKeysService controlComponentPublicKeysService;
	@Mock
	private ElectionEventDataGeneratorService electionEventDataGeneratorServiceMock;

	@InjectMocks
	private ElectionEventService electionEventService;

	@BeforeAll
	static void init() {
		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			final BigInteger p = new BigInteger("11");
			final BigInteger q = new BigInteger("5");
			final BigInteger g = new BigInteger("3");
			final GqGroup group = new GqGroup(p, q, g);
			keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(group, 1, new RandomService());
		}
	}

	@Test
	void createElectionEventGenerateFails() throws IOException, CCKeysNotExistException {
		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);

			when(controlComponentPublicKeysService.exist(anyString())).thenReturn(true);

			when(pathResolver.resolveOfflinePath(anyString())).thenReturn(Paths.get("test"));

			final BigInteger p = new BigInteger("11");
			final BigInteger q = new BigInteger("5");
			final BigInteger g = new BigInteger("3");
			final GqGroup group = new GqGroup(p, q, g);
			when(encryptionParametersServiceMock.load(any())).thenReturn(group);

			when(genSetupEncryptionKeysService.genSetupEncryptionKeys(any())).thenReturn(keyPair);

			doNothing().when(setupKeyPairService).save(anyString(), any());

			doNothing().when(mapper).writeValue((File) any(), any());

			final DataGeneratorResponse resultElectionEventGeneration = new DataGeneratorResponse();
			resultElectionEventGeneration.setSuccessful(false);
			when(electionEventDataGeneratorServiceMock.generate(anyString())).thenReturn(resultElectionEventGeneration);

			assertFalse(electionEventService.create("").isSuccessful());
		}
	}

	@Test
	void create(
			@TempDir
			final Path tempDir) throws IOException, CCKeysNotExistException {
		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);

			when(controlComponentPublicKeysService.exist(anyString())).thenReturn(true);

			final Path offlinePath = tempDir.resolve(Constants.CONFIG_DIR_NAME_OFFLINE);
			Files.createDirectories(offlinePath);
			when(pathResolver.resolveOfflinePath(anyString())).thenReturn(offlinePath);

			final BigInteger p = BigInteger.valueOf(11);
			final BigInteger q = BigInteger.valueOf(5);
			final BigInteger g = BigInteger.valueOf(3);
			final GqGroup gqGroup = new GqGroup(p, q, g);
			when(encryptionParametersServiceMock.load(any())).thenReturn(gqGroup);

			when(genSetupEncryptionKeysService.genSetupEncryptionKeys(any())).thenReturn(keyPair);

			doNothing().when(setupKeyPairService).save(anyString(), any());

			final DataGeneratorResponse resultElectionEventGeneration = new DataGeneratorResponse();
			resultElectionEventGeneration.setSuccessful(true);
			when(electionEventDataGeneratorServiceMock.generate(anyString())).thenReturn(resultElectionEventGeneration);

			when(configurationEntityStatusService.update(anyString(), anyString(), any())).thenReturn("");

			assertTrue(electionEventService.create("").isSuccessful());
			assertTrue(Files.exists(offlinePath.resolve(Constants.SETUP_SECRET_KEY_FILE_NAME)));
		}
	}
}
