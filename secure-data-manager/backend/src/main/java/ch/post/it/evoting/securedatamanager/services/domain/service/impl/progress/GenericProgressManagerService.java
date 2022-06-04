/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.impl.progress;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClientException;

import ch.post.it.evoting.securedatamanager.services.domain.common.GenericJobStatus;
import ch.post.it.evoting.securedatamanager.services.domain.service.ProgressManagerService;

public abstract class GenericProgressManagerService<T extends GenericJobStatus> implements ProgressManagerService<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenericProgressManagerService.class);
	private final Map<String, ProgressData<T>> jobMap;
	private final ThreadPoolTaskScheduler taskScheduler;
	private boolean isCheckProgressTaskStopped = true;
	private ScheduledFuture<?> checkProgressTaskFuture;

	protected GenericProgressManagerService() {
		taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.afterPropertiesSet();
		jobMap = new ConcurrentHashMap<>();
	}

	@Override
	public Future<T> registerJob(final String jobId) {
		checkNotNull(jobId);

		LOGGER.info("Registered job [id={}]", jobId);

		// create a future that the client will 'wait' on, until we signal the job is complete
		final CompletableFuture<T> future = new CompletableFuture<>();
		final ProgressData<T> progressData = new ProgressData<>(future, defaultData(jobId));
		jobMap.put(jobId, progressData);

		synchronized (this) {
			if (isCheckProgressTaskStopped) {
				LOGGER.debug("Starting 'check progress' task....");
				checkProgressTaskFuture = taskScheduler.scheduleWithFixedDelay(this::checkProgress, 2000);
				isCheckProgressTaskStopped = false;
			}

			return future;
		}
	}

	@Override
	public T getForJob(final String jobId) {
		checkNotNull(jobId);

		final ProgressData<T> progressData = jobMap.get(jobId);
		if (progressData == null) {
			LOGGER.warn("Job '{}' was not registered, returning default data.", jobId);
			return null;
		}

		final T jobStatus = progressData.getProgressStatus();
		processIfCompleted(jobStatus);
		return jobStatus;
	}

	@Override
	public List<T> getAllByStatus(final String status) {
		checkNotNull(status);

		final BatchStatus wantedStatus = BatchStatus.valueOf(status.toUpperCase());
		// get only jobStatus that match the 'status' we were supplied
		final List<T> registeredJobsStatus = findRegisteredJobs(pd -> wantedStatus.equals(pd.getProgressStatus().getStatus()));
		registeredJobsStatus.forEach(this::processIfCompleted);
		return registeredJobsStatus;
	}

	@Override
	public List<T> getAll() {
		// do not filter, return all
		final List<T> registeredJobsStatus = findRegisteredJobs(ignored -> true);
		registeredJobsStatus.forEach(this::processIfCompleted);
		return registeredJobsStatus;
	}

	private List<T> findRegisteredJobs(final Predicate<ProgressData<T>> jobFilter) {
		final Map<String, ProgressData<T>> registeredJobs = Collections.unmodifiableMap(jobMap);
		return registeredJobs.values().stream().filter(jobFilter).map(ProgressData::getProgressStatus).collect(Collectors.toList());
	}

	protected abstract T defaultData(final String jobId);

	protected abstract List<T> getJobs();

	protected void processJobStatus(final T jobStatus) {
		final ProgressData<T> progressData = jobMap.get(jobStatus.getJobId());
		if (progressData == null) {
			LOGGER.warn(
					"Job '{}' was not found in the 'job registry'. If this was a job you started it will not be updated correctly and you should restart it.",
					jobStatus.getJobId());
		} else {
			// update in-memory 'cache'
			progressData.setProgressStatus(jobStatus);
			// check if the job has completed
			if (BatchStatus.COMPLETED.equals(jobStatus.getStatus()) || BatchStatus.FAILED.equals(jobStatus.getStatus())) {
				// unblock future to update the job in the database
				progressData.getFuture().complete(jobStatus);
			}
		}
	}

	protected void checkProgress() {
		final int registeredJobsCount = jobMap.size();
		if (registeredJobsCount == 0) {
			LOGGER.debug("No remaining jobs registered, stopping 'check progress' task....");
			checkProgressTaskFuture.cancel(false);
			isCheckProgressTaskStopped = true;
		}

		LOGGER.debug("Registered jobs remaining: {}", registeredJobsCount);
		doCheckProgress();
	}

	protected void doCheckProgress() {
		try {
			getJobs().stream()
					// from the complete list of jobs, keep the ones we know and that are not yet completed or failed
					.filter(job -> jobMap.containsKey(job.getJobId()))
					.filter(job -> !(BatchStatus.COMPLETED.equals(jobMap.get(job.getJobId()).getProgressStatus().getStatus())
							|| BatchStatus.FAILED.equals(jobMap.get(job.getJobId()).getProgressStatus().getStatus())))
					.forEach(this::processJobStatus);
		} catch (final RestClientException e) {
			LOGGER.error("Failed to retrieve job progress status. Check if the server is up and running. Retrying...", e);
		}
	}

	protected void processIfCompleted(final T jobStatus) {
		// we remove it here because the front-end will receive this data and should stop asking for progress of this job
		if (BatchStatus.COMPLETED.equals(jobStatus.getStatus()) || BatchStatus.FAILED.equals(jobStatus.getStatus())) {
			jobMap.remove(jobStatus.getJobId());
		}
	}

}

