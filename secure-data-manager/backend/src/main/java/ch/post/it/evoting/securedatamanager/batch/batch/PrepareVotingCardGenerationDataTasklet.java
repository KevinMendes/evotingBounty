/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import static ch.post.it.evoting.securedatamanager.commons.Constants.CREDENTIAL_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.KEYSTORE_PIN;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersHolderInitializer;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;

@Component
public class PrepareVotingCardGenerationDataTasklet implements Tasklet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrepareVotingCardGenerationDataTasklet.class);

	private final JobExecutionObjectContext stepExecutionObjectContext;
	private final VotersHolderInitializer votersHolderInitializer;
	private final PrimitivesServiceAPI primitivesService;

	public PrepareVotingCardGenerationDataTasklet(final JobExecutionObjectContext stepExecutionObjectContext,
			final VotersHolderInitializer votersHolderInitializer, final PrimitivesServiceAPI primitivesService) {
		this.stepExecutionObjectContext = stepExecutionObjectContext;
		this.votersHolderInitializer = votersHolderInitializer;
		this.primitivesService = primitivesService;
	}

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

		try {
			final VotingCardGenerationJobExecutionContext jobExecutionContext = new VotingCardGenerationJobExecutionContext(
					chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext());

			final String electionEventId = jobExecutionContext.getElectionEventId();
			final String jobInstanceId = jobExecutionContext.getJobInstanceId();
			final VotersParametersHolder parametersHolder = stepExecutionObjectContext.get(jobInstanceId, VotersParametersHolder.class);
			final VotersParametersHolder updatedParametersHolder;
			try (final InputStream is = getKeysConfiguration()) {
				updatedParametersHolder = votersHolderInitializer.init(parametersHolder, is);
			}
			stepExecutionObjectContext.put(jobInstanceId, updatedParametersHolder, VotersParametersHolder.class);

			final byte[] saltCredentialId = primitivesService.getHash((CREDENTIAL_ID + electionEventId).getBytes(StandardCharsets.UTF_8));
			jobExecutionContext.setSaltCredentialId(Base64.getEncoder().encodeToString(saltCredentialId));
			final byte[] saltKeystoreSymmetricEncryptionKey = primitivesService.getHash(
					(KEYSTORE_PIN + electionEventId).getBytes(StandardCharsets.UTF_8));
			jobExecutionContext.setSaltKeystoreSymmetricEncryptionKey(Base64.getEncoder().encodeToString(saltKeystoreSymmetricEncryptionKey));

		} catch (final CreateVotingCardSetException e) {
			LOGGER.error("Failed to generate card set data pack.", e);
			throw e;
		} catch (final GeneralCryptoLibException e) {
			LOGGER.error("Failed salt (credential|pin) hash values.", e);
			throw e;
		} catch (final Exception e) {
			LOGGER.error("Prepare voting card generation task failed.", e);
			throw e;
		}

		return RepeatStatus.FINISHED;
	}

	private InputStream getKeysConfiguration() {
		return getClass().getClassLoader().getResourceAsStream(Constants.KEYS_CONFIG_FILENAME);
	}
}
