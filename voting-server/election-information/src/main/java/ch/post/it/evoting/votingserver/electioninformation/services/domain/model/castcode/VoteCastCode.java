/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * The Class VoteCastCodeEntity.
 */
@Entity
@Table(name = "VOTE_CAST_CODE", uniqueConstraints = @UniqueConstraint(name = "VOTE_CAST_CODE_UK1", columnNames = { "TENANT_ID", "ELECTION_EVENT_ID",
		"VOTING_CARD_ID" }))

public class VoteCastCode {

	/**
	 * The id of a vote cast code.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "voteCastCodeSeq")
	@SequenceGenerator(name = "voteCastCodeSeq", sequenceName = "VOTE_CAST_CODE_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The tenant id.
	 */
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String tenantId;

	/**
	 * The election event id.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String electionEventId;

	/**
	 * The voting card id.
	 */
	@Column(name = "VOTING_CARD_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String votingCardId;

	/**
	 * The string of a vote cast code.
	 */
	@Column(name = "VOTE_CAST_CODE")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String castCode;

	/**
	 * The string containing all Cast Code computation outputs for audit purposes
	 **/
	@Lob
	@Column(name = "COMPUTATION_RESULTS")
	@NotNull
	private byte[] computationResults;

	/**
	 * Empty constructor for hibernate.
	 */
	public VoteCastCode() {
	}

	/**
	 * Instantiates a new vote cast code entity.
	 *
	 * @param tenantId        - the tenant id.
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @param voteCastCode    the vote cast code
	 */
	public VoteCastCode(String tenantId, String electionEventId, String votingCardId, String voteCastCode) {
		super();
		this.tenantId = tenantId;
		this.electionEventId = electionEventId;
		this.votingCardId = votingCardId;
		this.castCode = voteCastCode;
	}

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
	public void setId(Integer id) {
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
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
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
	public void setElectionEventId(String electionEventId) {
		this.electionEventId = electionEventId;
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
	public void setVotingCardId(String votingCardId) {
		this.votingCardId = votingCardId;
	}

	/**
	 * Returns the current value of the field voteCastCode.
	 *
	 * @return Returns the voteCastCode.
	 */
	public String getVoteCastCode() {
		return castCode;
	}

	/**
	 * Sets the value of the field voteCastCode.
	 *
	 * @param voteCastCode The voteCastCode to set.
	 */
	public void setVoteCastCode(String voteCastCode) {
		this.castCode = voteCastCode;
	}

	public byte[] getComputationResults() {
		return computationResults;
	}

	public void setComputationResults(byte[] computationResults) {
		this.computationResults = computationResults;
	}
}
