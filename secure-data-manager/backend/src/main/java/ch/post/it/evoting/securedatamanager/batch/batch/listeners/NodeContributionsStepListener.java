/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.listeners;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.commons.domain.VcIdCombinedReturnCodesGenerationValues;

@Component
@JobScope
public class NodeContributionsStepListener implements StepExecutionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeContributionsStepListener.class);

	private final BlockingQueue<VcIdCombinedReturnCodesGenerationValues> queue;

	public NodeContributionsStepListener(final BlockingQueue<VcIdCombinedReturnCodesGenerationValues> queue) {
		this.queue = queue;
	}

	@Override
	public void beforeStep(final StepExecution stepExecution) {
		// nothing to do.
	}

	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		try {
			queue.put(VcIdCombinedReturnCodesGenerationValues.poisonPill());
		} catch (final InterruptedException e) {
			LOGGER.error("Unexpected state", e);
			Thread.currentThread().interrupt();
		}
		return ExitStatus.COMPLETED;
	}

}
