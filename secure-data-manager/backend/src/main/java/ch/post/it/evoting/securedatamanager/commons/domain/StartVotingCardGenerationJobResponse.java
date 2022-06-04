/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain;

import org.springframework.batch.core.BatchStatus;

public class StartVotingCardGenerationJobResponse {

	private String jobId;

	private BatchStatus jobStatus;

	private String createdAt;

	protected StartVotingCardGenerationJobResponse() {
	}

	public StartVotingCardGenerationJobResponse(final String jobId, final BatchStatus jobStatus, final String createdAt) {
		this.jobId = jobId;
		this.jobStatus = jobStatus;
		this.createdAt = createdAt;
	}

	public String getJobId() {
		return jobId;
	}

	public BatchStatus getJobStatus() {
		return jobStatus;
	}

	public String getCreatedAt() {
		return createdAt;
	}

}

