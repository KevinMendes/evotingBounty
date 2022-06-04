/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

@Embeddable
public class MixDecId implements Serializable {

	private static final long serialVersionUID = -1627650493332590139L;

	// The election event identifier
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String electionEventId;

	// The ballot box identifier
	@Column(name = "BALLOT_BOX_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String ballotBoxId;

	// The id of the node that processed the payload
	@Column(name = "NODE_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String nodeId;

	@SuppressWarnings("unused") // No-argument constructor to keep JPA happy.
	private MixDecId() {
	}

	public MixDecId(final String electionEventId, final String ballotBoxId, final String nodeId) {
		this.electionEventId = electionEventId;
		this.ballotBoxId = ballotBoxId;
		this.nodeId = nodeId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final MixDecId that = (MixDecId) o;
		return Objects.equals(electionEventId, that.electionEventId) && Objects.equals(ballotBoxId, that.ballotBoxId)
				&& Objects.equals(nodeId, that.nodeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, ballotBoxId, nodeId);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public String getNodeId() {
		return nodeId;
	}
}
