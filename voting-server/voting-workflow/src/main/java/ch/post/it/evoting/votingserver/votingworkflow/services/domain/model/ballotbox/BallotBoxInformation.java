/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballotbox;

public class BallotBoxInformation {

	/**
	 * The object containing the ballot box information.
	 */
	private String json;

	private String ballotBoxId;

	private String tenantId;

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public void setBallotBoxId(String ballotBoxId) {
		this.ballotBoxId = ballotBoxId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
}
