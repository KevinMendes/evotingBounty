/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.output.ElectionEventServiceOutput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventHolderInitializer;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventOutput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventSerializer;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;

@ExtendWith(MockitoExtension.class)
class ElectionEventDataGeneratorServiceTest {

	@TempDir
	static Path temporaryFolder;

	@InjectMocks
	@Spy
	private ElectionEventDataGeneratorService electionEventDataGeneratorService;

	@Mock
	private PathResolver pathResolverMock;

	@Mock
	private Path configPathMock;

	@Mock
	private ElectionEventRepository electionEventRepositoryMock;

	@Mock
	private CreateElectionEventGenerator createElectionEventGeneratorMock;

	@Mock
	private CreateElectionEventSerializer createElectionEventSerializerMock;

	@Mock
	private CreateElectionEventHolderInitializer electionEventHolderInitializerMock;

	private String electionEventId;

	@Test
	void generateWithIdNull() throws IOException {
		assertFalse(electionEventDataGeneratorService.generate(null).isSuccessful());
	}

	@Test
	void generateWithIdEmpty() throws IOException {
		electionEventId = "";

		assertFalse(electionEventDataGeneratorService.generate(electionEventId).isSuccessful());
	}

	@Test
	void generateMakePathThrowIOException() throws IOException {
		electionEventId = "123";

		when(pathResolverMock.resolve(anyString())).thenReturn(configPathMock);
		doThrow(new IOException()).when(electionEventDataGeneratorService).makePath(configPathMock);

		assertFalse(electionEventDataGeneratorService.generate(electionEventId).isSuccessful());
	}

	@Test
	void generateElectionEventNotFound() throws IOException {
		electionEventId = "123";

		when(pathResolverMock.resolve(anyString())).thenReturn(configPathMock);
		doReturn(configPathMock).when(electionEventDataGeneratorService).makePath(configPathMock);
		when(electionEventRepositoryMock.find(electionEventId)).thenReturn(JsonConstants.EMPTY_OBJECT);

		assertFalse(electionEventDataGeneratorService.generate(electionEventId).isSuccessful());
	}

	@Test
	void generateCorrectOutputFoldersFromInputParametersWhenCreatingElectionEvent() throws GeneralCryptoLibException, IOException {
		CreateElectionEventParametersHolder inputParameters = getCreateElectionEventInputParameters();

		CreateElectionEventOutput mockOutput = mock(CreateElectionEventOutput.class);
		doAnswer(invocationOnMock -> null).when(mockOutput).clearPasswords();

		when(createElectionEventGeneratorMock.generate(any())).thenReturn(mockOutput);
		doAnswer(invocationOnMock -> null).when(createElectionEventSerializerMock).serialize(any(), any());

		final ElectionEventServiceOutput output = electionEventDataGeneratorService.createElectionEvent(inputParameters);

		assertEquals(output.getOfflineFolder(), inputParameters.getOfflineFolder().toString());
		assertEquals(output.getOnlineAuthenticationFolder(), inputParameters.getOnlineAuthenticationFolder().toString());
		assertEquals(output.getOnlineElectionInformationFolder(), inputParameters.getOnlineElectionInformationFolder().toString());
	}

	private CreateElectionEventParametersHolder getCreateElectionEventInputParameters() {
		Path outputFolder = temporaryFolder;
		Path offlineFolder = outputFolder.resolve("OFFLINE");
		Path onlineAuthenticationFolder = outputFolder.resolve("ONLINE").resolve("authentication");
		Path onlineElectionInformationFolder = outputFolder.resolve("ONLINE").resolve("electionInformation");

		return new CreateElectionEventParametersHolder(null, outputFolder, null, offlineFolder, onlineAuthenticationFolder,
				onlineElectionInformationFolder, null, null, null);

	}
}
