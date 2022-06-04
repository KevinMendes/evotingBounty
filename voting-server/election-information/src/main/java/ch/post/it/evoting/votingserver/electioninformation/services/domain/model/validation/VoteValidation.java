/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * Entity storing the hash of the vote iff all vote validations are ok.
 */
@Entity
@Table(name = "VOTE_VALIDATION")
public class VoteValidation {

	/**
	 * The id of a validation.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "voteValidationSeq")
	@SequenceGenerator(name = "voteValidationSeq", sequenceName = "VOTE_VALIDATION_SEQ", allocationSize = 1)
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
	 * The vote hash
	 */
	@Column(name = "VOTE_HASH")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_150)
	private String voteHash;

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
	 * Returns the current value of the field voteHash.
	 *
	 * @return Returns the voteHash.
	 */
	public String getVoteHash() {
		return voteHash;
	}

	/**
	 * Sets the value of the field voteHash.
	 *
	 * @param voteHash The voteHash to set.
	 */
	public void setVoteHash(final String voteHash) {
		this.voteHash = voteHash;
	}

}
