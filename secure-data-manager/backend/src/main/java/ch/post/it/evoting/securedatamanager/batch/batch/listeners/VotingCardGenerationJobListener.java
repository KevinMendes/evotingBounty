/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.listeners;

import static java.text.MessageFormat.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.batch.batch.VotingCardGenerationJobExecutionContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;

@Component
public class VotingCardGenerationJobListener implements JobExecutionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardGenerationJobListener.class);

	@Autowired
	private JobExecutionObjectContext stepExecutionObjectContext;

	@Override
	public void beforeJob(final JobExecution jobExecution) {
		final VotingCardGenerationJobExecutionContext jobExecutionContext = new VotingCardGenerationJobExecutionContext(
				jobExecution.getExecutionContext());

		// init counters
		jobExecutionContext.setGeneratedCardCount(0);
		jobExecutionContext.setErrorCount(0);

	}

	@Override
	public void afterJob(final JobExecution jobExecution) {
		final VotingCardGenerationJobExecutionContext jobExecutionContext = new VotingCardGenerationJobExecutionContext(
				jobExecution.getExecutionContext());

		// clear all temporary data related to this job
		final String jobInstanceId = jobExecutionContext.getJobInstanceId();
		// the job may fail right away and so the context can be empty.
		// Therefore, we need to prevent NullPointerExceptions.
		if (jobInstanceId != null) {
			stepExecutionObjectContext.removeAll(jobInstanceId);
		}

		if (ExitStatus.COMPLETED.equals(jobExecution.getExitStatus())) {

			final double generatedCount = jobExecutionContext.getGeneratedCardCount();
			final int errorCount = jobExecutionContext.getErrorCount();

			LOGGER.info("Job [{}] completed. Results: [voting cards generated={}, errors={}]", jobInstanceId, generatedCount, errorCount);

		} else {
			LOGGER.warn("Job [{}] failed with the exit status: {}", jobInstanceId, jobExecution.getExitStatus());
			for (final Throwable e : jobExecution.getAllFailureExceptions()) {
				LOGGER.warn(format("Job [{0}] failed because of error:", jobInstanceId), e);
			}
		}

	}
}
