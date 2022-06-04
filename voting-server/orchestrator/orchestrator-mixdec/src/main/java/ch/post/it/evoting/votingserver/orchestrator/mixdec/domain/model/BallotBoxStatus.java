/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model;

public class BallotBoxStatus {

	protected String ballotBoxId;

	protected MixDecStatus processStatus = MixDecStatus.UNKNOWN;

	protected String errorMessage = null;

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public void setBallotBoxId(String ballotBoxId) {
		this.ballotBoxId = ballotBoxId;
	}

	public MixDecStatus getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(MixDecStatus processStatus) {
		this.processStatus = processStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
