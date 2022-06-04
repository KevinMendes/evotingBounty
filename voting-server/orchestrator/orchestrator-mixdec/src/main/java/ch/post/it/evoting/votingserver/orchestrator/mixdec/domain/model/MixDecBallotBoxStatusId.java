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
public class MixDecBallotBoxStatusId implements Serializable {

	private static final long serialVersionUID = 8944101104605018311L;

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

	@SuppressWarnings("unused") // No-argument constructor to keep JPA happy.
	private MixDecBallotBoxStatusId() {
	}

	public MixDecBallotBoxStatusId(final String electionEventId, final String ballotBoxId) {
		this.electionEventId = electionEventId;
		this.ballotBoxId = ballotBoxId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final MixDecBallotBoxStatusId that = (MixDecBallotBoxStatusId) o;
		return Objects.equals(electionEventId, that.electionEventId) && Objects.equals(ballotBoxId, that.ballotBoxId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, ballotBoxId);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

}
