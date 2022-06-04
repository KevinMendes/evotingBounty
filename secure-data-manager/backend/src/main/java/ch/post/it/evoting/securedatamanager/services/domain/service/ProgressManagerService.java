/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import java.util.List;
import java.util.concurrent.Future;

import ch.post.it.evoting.securedatamanager.services.domain.common.GenericJobStatus;

/**
 * This interface defines the API for a service which accesses to ProgressManager
 */
public interface ProgressManagerService<T extends GenericJobStatus> {

	/**
	 * @param jobId the job identifier
	 * @return the job status
	 */
	T getForJob(final String jobId);

	Future<T> registerJob(final String jobId);

	List<T> getAllByStatus(final String status);

	List<T> getAll();
}
