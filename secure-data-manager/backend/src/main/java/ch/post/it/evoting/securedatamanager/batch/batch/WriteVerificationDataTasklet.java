/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersSerializationDestProvider;

public class WriteVerificationDataTasklet implements Tasklet {

	private static final Logger LOGGER = LoggerFactory.getLogger(WriteVerificationDataTasklet.class);

	private final VotersSerializationDestProvider destProvider;

	private final JobExecutionObjectContext objectContext;

	public WriteVerificationDataTasklet(final VotersSerializationDestProvider destProvider, final JobExecutionObjectContext objectContext) {
		this.destProvider = destProvider;
		this.objectContext = objectContext;
	}

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

		final VotingCardGenerationJobExecutionContext jobExecutionContext = new VotingCardGenerationJobExecutionContext(
				chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext());

		try {

			final String jobInstanceId = jobExecutionContext.getJobInstanceId();
			final String electionEventId = jobExecutionContext.getElectionEventId();
			final String verificationCardSetId = jobExecutionContext.getVerificationCardSetId();
			final String electoralAuthorityId = jobExecutionContext.getElectoralAuthorityId();

			final VotersParametersHolder votersParametersHolder = objectContext.get(jobInstanceId, VotersParametersHolder.class);

			final VoteVerificationContextData voteVerificationContextData = new VoteVerificationContextData();
			voteVerificationContextData.setElectionEventId(electionEventId);
			voteVerificationContextData.setVerificationCardSetId(verificationCardSetId);
			voteVerificationContextData.setEncryptionParameters(votersParametersHolder.getEncryptionParameters());

			voteVerificationContextData.setElectoralAuthorityId(electoralAuthorityId);

			final Path voteVerContextDataJSON = destProvider.getVoteVerificationContextData();
			final File voteVerificationContextDataFile = voteVerContextDataJSON.toFile();
			new ConfigObjectMapper().fromJavaToJSONFile(voteVerificationContextData, voteVerificationContextDataFile);

		} catch (final Exception e) {
			LOGGER.error("Write verification data task failed.", e);
			throw e;
		}
		return RepeatStatus.FINISHED;
	}
}
