/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.config;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.batch.core.BatchStatus;

import ch.post.it.evoting.securedatamanager.config.commons.config.commons.progress.JobProgressDetails;
import ch.post.it.evoting.securedatamanager.services.domain.common.GenericJobStatus;

public class VotingCardGenerationJobStatus extends GenericJobStatus {

	public static final VotingCardGenerationJobStatus UNKNOWN = new VotingCardGenerationJobStatus(new UUID(0, 0), BatchStatus.UNKNOWN, Instant.EPOCH,
			null, JobProgressDetails.EMPTY, null, 0, 0);

	private final Instant startTime;
	private String verificationCardSetId;
	private int generatedCount;
	private int errorCount;

	public VotingCardGenerationJobStatus(final String jobId) {
		this(UUID.fromString(jobId), BatchStatus.UNKNOWN, Instant.EPOCH, null, JobProgressDetails.EMPTY, null, 0, 0);
	}

	public VotingCardGenerationJobStatus(final UUID jobId, final BatchStatus jobStatus, final Instant startTime, final String statusDetails,
			final JobProgressDetails progressDetails, final String verificationCardSetId, final int generatedCount, final int errorCount) {
		super(jobId.toString(), jobStatus, statusDetails, progressDetails);

		this.startTime = startTime;
		this.verificationCardSetId = verificationCardSetId;
		this.generatedCount = generatedCount;
		this.errorCount = errorCount;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	protected void setVerificationCardSetId(final String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

	public int getGeneratedCount() {
		return generatedCount;
	}

	protected void setGeneratedCount(final int generatedCount) {
		this.generatedCount = generatedCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	protected void setErrorCount(final int errorCount) {
		this.errorCount = errorCount;
	}

	public String getStartTime() {
		return startTime.atZone(ZoneId.systemDefault()).toString();
	}

	@Override
	public String toString() {
		return "{" + "jobId=" + getJobId()
				+ ", status='" + getStatus() + '\''
				+ ", startTime=" + startTime
				+ ", statusDetails='" + getStatusDetails() + '\''
				+ ", progressDetails=" + getProgressDetails()
				+ ", verificationCardSetId='" + verificationCardSetId + '\''
				+ ", generatedCount=" + generatedCount
				+ ", errorCount=" + errorCount
				+ '}';
	}
}
