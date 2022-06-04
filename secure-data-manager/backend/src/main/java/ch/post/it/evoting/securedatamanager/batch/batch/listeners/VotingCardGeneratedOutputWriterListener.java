/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.listeners;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.ExecutionContext;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.batch.batch.VotingCardGenerationJobExecutionContext;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.CompositeOutputWriter;
import ch.post.it.evoting.securedatamanager.config.engine.commands.progress.ProgressManager;

public class VotingCardGeneratedOutputWriterListener implements ItemWriteListener<GeneratedVotingCardOutput> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardGeneratedOutputWriterListener.class);

	private final ProgressManager progressManager;
	private final UUID jobId;
	private final VotingCardGenerationJobExecutionContext jobExecutionContext;

	public VotingCardGeneratedOutputWriterListener(final UUID jobId, final ExecutionContext executionContext, final ProgressManager progressManager) {
		this.jobId = jobId;
		this.jobExecutionContext = new VotingCardGenerationJobExecutionContext(executionContext);
		this.progressManager = progressManager;
	}

	/**
	 * before writing an item, verify if the item is in error state. if it is, increment the error count and log the error we will not filter the item
	 * here. The 'root' writer {@link CompositeOutputWriter#write} will make sure that we do not try to write an invalid output
	 *
	 * @param items
	 */
	@Override
	public void beforeWrite(final List<? extends GeneratedVotingCardOutput> items) {
		items.forEach(item -> {
			if (item.isError()) {
				final int errorCount = jobExecutionContext.getErrorCount();
				jobExecutionContext.setErrorCount(errorCount + 1);

				LOGGER.warn("Generated output " + item.getVotingCardId() + " is in error due to an 'expected' exception. "
						+ "The output will not be written.", item.getError());

				progressManager.updateProgress(jobId, 1);
			}
		});
	}

	/**
	 * after successfully writing an item (which must have been valid), increment the generated counter.
	 *
	 * @param items
	 */
	@Override
	public void afterWrite(final List<? extends GeneratedVotingCardOutput> items) {
		items.forEach(item -> {
			if (!item.isError()) {
				final int generatedCount = jobExecutionContext.getGeneratedCardCount();
				jobExecutionContext.setGeneratedCardCount(generatedCount + 1);

				progressManager.updateProgress(jobId, 1);

				// clear passwords (FIX: 6870)
				item.getVerificationCardCredentialDataPack().clearPassword();
				item.getVoterCredentialDataPack().clearPassword();

			}
		});
	}

	/**
	 * in case any of the writers failed to write the output, log it here
	 *
	 * @param exception the error
	 * @param items     the outputs that failed to be written.
	 */
	@Override
	public void onWriteError(final Exception exception, final List<? extends GeneratedVotingCardOutput> items) {
		LOGGER.warn("Failed to write output values. Verify the output destination is valid and/or the application has " + "enough permissions.",
				ExceptionUtils.getRootCause(exception));

		progressManager.updateProgress(jobId, items.size());
	}
}
