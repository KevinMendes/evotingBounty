/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * Entity representing a ballot box.
 */
@Entity
@Table(name = "MIXDEC_BALLOT_BOX_STATUS")
public class MixDecBallotBoxStatus {

	@EmbeddedId
	private MixDecBallotBoxStatusId id;

	/**
	 * The mixdec status of the ballot box.
	 */
	@Column(name = "STATUS")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_10)
	private String status;

	@Column(name = "ERROR_MESSAGE")
	@Size(max = Constants.COLUMN_LENGTH_150)
	private String errorMessage;

	public void setId(final MixDecBallotBoxStatusId id) {
		this.id = id;
	}

	public String getElectionEventId() {
		return id.getElectionEventId();
	}

	public String getBallotBoxId() {
		return id.getBallotBoxId();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
