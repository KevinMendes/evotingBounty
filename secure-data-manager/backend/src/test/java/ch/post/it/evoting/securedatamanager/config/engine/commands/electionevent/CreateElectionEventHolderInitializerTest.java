/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.securedatamanager.config.commons.readers.ConfigurationInputReader;

@ExtendWith(MockitoExtension.class)
class CreateElectionEventHolderInitializerTest {

	@Mock
	ConfigurationInputReader reader;

	@InjectMocks
	CreateElectionEventHolderInitializer sut;

	@Test
	void setKeysConfigurationFromStream() throws Exception {

		// given
		final CreateElectionEventParametersHolder holder = mock(CreateElectionEventParametersHolder.class);
		final InputStream configInputStream = mock(InputStream.class);

		// when
		sut.init(holder, configInputStream);

		// then
		verify(holder, times(1)).setConfigurationInput(any());
	}
}
