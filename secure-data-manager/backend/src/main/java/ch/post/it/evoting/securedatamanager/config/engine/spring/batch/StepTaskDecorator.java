/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.spring.batch;

import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.core.scope.context.JobSynchronizationManager;
import org.springframework.core.task.TaskDecorator;

/**
 * Implementation of {@link TaskDecorator} which allows to run the supplied step task in appropriate context.
 */
public class StepTaskDecorator implements TaskDecorator {
	private final String tenantId;

	/**
	 * Constructor.
	 *
	 * @param tenantId
	 */
	public StepTaskDecorator(final String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public Runnable decorate(final Runnable task) {
		final JobContext context = JobSynchronizationManager.getContext();
		return () -> runTask(task, context);
	}

	private void runTask(final Runnable task, final JobContext context) {
		JobSynchronizationManager.register(context.getJobExecution());
		try {
			task.run();
		} finally {
			JobSynchronizationManager.close();
		}
	}
}
