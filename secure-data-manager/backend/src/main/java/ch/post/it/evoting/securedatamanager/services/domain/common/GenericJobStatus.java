/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.common;

import org.springframework.batch.core.BatchStatus;

import ch.post.it.evoting.securedatamanager.config.commons.config.commons.progress.JobProgressDetails;

public class GenericJobStatus {

	private String jobId;
	private BatchStatus status;
	private String statusDetails;
	private JobProgressDetails progressDetails;

	protected GenericJobStatus() {
	}

	public GenericJobStatus(final String jobId, final BatchStatus jobStatus, final String statusDetails, final JobProgressDetails progressDetails) {

		this.jobId = jobId;
		this.status = jobStatus;
		this.statusDetails = statusDetails;
		this.progressDetails = progressDetails;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(final String jobId) {
		this.jobId = jobId;
	}

	public BatchStatus getStatus() {
		return status;
	}

	public void setStatus(final BatchStatus status) {
		this.status = status;
	}

	public JobProgressDetails getProgressDetails() {
		return progressDetails;
	}

	public void setProgressDetails(final JobProgressDetails progressDetails) {
		this.progressDetails = progressDetails;
	}

	public String getStatusDetails() {
		return statusDetails;
	}

	public void setStatusDetails(final String statusDetails) {
		this.statusDetails = statusDetails;
	}
}
