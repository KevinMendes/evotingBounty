/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.output.BallotBoxesServiceOutput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox.BallotBoxGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox.BallotBoxParametersHolder;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.BallotBoxParametersHolderAdapter;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

@ExtendWith(MockitoExtension.class)
class BallotBoxDataGeneratorServiceImplTest {

	@TempDir
	static Path temporaryFolder;
	@InjectMocks
	@Spy
	private BallotBoxDataGeneratorService ballotBoxDataGeneratorService;
	@Mock
	private BallotBoxRepository ballotBoxRepositoryMock;
	@Mock
	private BallotBoxParametersHolderAdapter ballotBoxParametersHolderAdapterMock;
	@Mock
	private BallotBoxGenerator ballotBoxGeneratorMock;
	@Mock
	private EncryptionParametersService encryptionParametersServiceMock;

	@Test
	void generateWithIdNull() {
		final DataGeneratorResponse result = ballotBoxDataGeneratorService.generate(null, null);
		assertFalse(result.isSuccessful());
	}

	@Test
	void generateWithIdEmpty() {
		final DataGeneratorResponse result = ballotBoxDataGeneratorService.generate("", null);
		assertFalse(result.isSuccessful());
	}

	@Test
	void generateWithIdNotFound() {
		final String ballotBoxId = "123456";

		when(ballotBoxRepositoryMock.find(ballotBoxId)).thenReturn(JsonConstants.EMPTY_OBJECT);

		final DataGeneratorResponse result = ballotBoxDataGeneratorService.generate(ballotBoxId, null);
		assertFalse(result.isSuccessful());
	}

	@Test
	void generateWithValidId() {
		final String ballotBoxId = "1234aa1d3f194c11aac03e421472b6bb";
		final String electionEventId = "1234aa1d3f194c11aac03e421444b6bb";

		final DataGeneratorResponse response = new DataGeneratorResponse();
		doReturn(response).when(ballotBoxDataGeneratorService).generate(ballotBoxId, electionEventId);

		final DataGeneratorResponse result = ballotBoxDataGeneratorService.generate(ballotBoxId, electionEventId);
		assertTrue(result.isSuccessful());
	}

	@Test
	void generateCorrectOutputFromInputParametersWhenCreatingBallotBoxes() {
		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);

			// given
			final BallotBoxParametersHolder inputParameters = getCreateBallotBoxesInputParameters();
			final Path fullBallotBoxPath = temporaryFolder.resolve("genBallot.txt");

			final BallotBoxesServiceOutput mockOutput = mock(BallotBoxesServiceOutput.class);
			when(mockOutput.getOutputPath()).thenReturn(fullBallotBoxPath.toString());
			when(ballotBoxGeneratorMock.generate(any())).thenReturn(mockOutput);

			final BigInteger p = new BigInteger("11");
			final BigInteger q = new BigInteger("5");
			final BigInteger g = new BigInteger("3");
			final GqGroup group = new GqGroup(p, q, g);
			when(encryptionParametersServiceMock.load(any())).thenReturn(group);

			final BallotBoxesServiceOutput output = ballotBoxDataGeneratorService.createBallotBoxes(inputParameters);

			assertEquals(output.getOutputPath(), fullBallotBoxPath.toString());
		}
	}

	private BallotBoxParametersHolder getCreateBallotBoxesInputParameters() {

		final Path outputFolder = temporaryFolder;

		return new BallotBoxParametersHolder("", "", "", "", outputFolder, "", null, null, null, null);
	}
}
