/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * Entity representing a ballot box.
 */
@Entity
@Table(name = "BALLOT_BOX")
public class BallotBox {

	/**
	 * The id of a ballot box.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ballotBoxSeq")
	@SequenceGenerator(name = "ballotBoxSeq", sequenceName = "BALLOT_BOX_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The tenant id.
	 */
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String tenantId;

	/**
	 * The voting card id.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String electionEventId;

	/**
	 * The voting card id.
	 */
	@Column(name = "VOTING_CARD_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String votingCardId;

	/**
	 * The ballot id.
	 */
	@Column(name = "BALLOT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String ballotId;

	/**
	 * The ballot box id.
	 */
	@Column(name = "BALLOT_BOX_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String ballotBoxId;

	/**
	 * The string of a vote.
	 */
	@Column(name = "VOTE", length = Integer.MAX_VALUE)
	@NotNull
	@Lob
	private String vote;

	/**
	 * The computation results.
	 */
	@Column(name = "COMPUTATION_RESULTS")
	@NotNull
	@Lob
	private byte[] computationResults;

	/**
	 * Returns the current value of the field id.
	 *
	 * @return Returns the id.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the value of the field id.
	 *
	 * @param id The id to set.
	 */
	public void setId(final Integer id) {
		this.id = id;
	}

	/**
	 * Returns the current value of the field tenantId.
	 *
	 * @return Returns the tenantId.
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets the value of the field tenantId.
	 *
	 * @param tenantId The tenantId to set.
	 */
	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Returns the current value of the field ballotId.
	 *
	 * @return Returns the ballotId.
	 */
	public String getBallotId() {
		return ballotId;
	}

	/**
	 * Sets the value of the field ballotId.
	 *
	 * @param ballotId The ballotId to set.
	 */
	public void setBallotId(final String ballotId) {
		this.ballotId = ballotId;
	}

	/**
	 * Returns the current value of the field vote.
	 *
	 * @return Returns the vote.
	 */
	public String getVote() {
		return vote;
	}

	/**
	 * Sets the value of the field vote.
	 *
	 * @param vote The vote to set.
	 */
	public void setVote(final String vote) {
		this.vote = vote;
	}

	/**
	 * Returns the current value of the field ballotBoxId.
	 *
	 * @return Returns the ballotBoxId.
	 */
	public String getBallotBoxId() {
		return ballotBoxId;
	}

	/**
	 * Sets the value of the field ballotBoxId.
	 *
	 * @param ballotBoxId The ballotBoxId to set.
	 */
	public void setBallotBoxId(final String ballotBoxId) {
		this.ballotBoxId = ballotBoxId;
	}

	/**
	 * Returns the current value of the field votingCardId.
	 *
	 * @return Returns the votingCardId.
	 */
	public String getVotingCardId() {
		return votingCardId;
	}

	/**
	 * Sets the value of the field votingCardId.
	 *
	 * @param votingCardId The votingCardId to set.
	 */
	public void setVotingCardId(final String votingCardId) {
		this.votingCardId = votingCardId;
	}

	/**
	 * Returns the current value of the field electionEventId.
	 *
	 * @return Returns the electionEventId.
	 */
	public String getElectionEventId() {
		return electionEventId;
	}

	/**
	 * Sets the value of the field electionEventId.
	 *
	 * @param electionEventId The electionEventId to set.
	 */
	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public byte[] getComputationResults() {
		return computationResults;
	}

	public void setComputationResults(final byte[] computationResults) {
		this.computationResults = computationResults;
	}

}
