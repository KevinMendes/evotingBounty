/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersHolderInitializer;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersSerializationDestProvider;

@Configuration
public class TestConfigServices {

	@Bean
	JobExecutionObjectContext executionObjectContext() {
		return mock(JobExecutionObjectContext.class);
	}

	@Bean
	VotersHolderInitializer votersHolderInitializer(final VotersParametersHolder holder) throws GeneralCryptoLibException {
		final VotersHolderInitializer initializer = mock(VotersHolderInitializer.class);
		when(initializer.init(any(), any(InputStream.class))).thenReturn(holder);
		return initializer;
	}

	@Bean
	public PrimitivesServiceAPI primitivesServiceAPI() {
		return new PrimitivesService();
	}

	@Bean
	VotersSerializationDestProvider serializationDestProvider() {
		return mock(VotersSerializationDestProvider.class);
	}

}
