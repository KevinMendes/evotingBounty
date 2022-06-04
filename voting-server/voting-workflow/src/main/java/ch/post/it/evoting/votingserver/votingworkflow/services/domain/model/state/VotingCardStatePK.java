/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class representing the primaryKey
 */
public class VotingCardStatePK implements Serializable {

	private static final long serialVersionUID = -979293365911810294L;

	private String tenantId;

	private String electionEventId;

	private String votingCardId;

	public VotingCardStatePK() {
		super();
	}

	public VotingCardStatePK(final String tenantId, final String electionEventId, final String votingCardId) {
		this.tenantId = tenantId;
		this.electionEventId = electionEventId;
		this.votingCardId = votingCardId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VotingCardStatePK that = (VotingCardStatePK) o;

		if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) {
			return false;
		}
		if (electionEventId != null ? !electionEventId.equals(that.electionEventId) : that.electionEventId != null) {
			return false;
		}
		return votingCardId != null ? votingCardId.equals(that.votingCardId) : that.votingCardId == null;

	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, electionEventId, votingCardId);
	}

	public String getTenantId() {
		return tenantId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVotingCardId() {
		return votingCardId;
	}

}
