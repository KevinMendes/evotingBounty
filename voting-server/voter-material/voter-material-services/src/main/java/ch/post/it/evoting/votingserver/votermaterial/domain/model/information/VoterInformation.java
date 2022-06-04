/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.information;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * The entity representing the voter information.
 */
@Entity
@Table(name = "VOTER_INFORMATION")
public class VoterInformation {

	/**
	 * The identifier of the entity.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "voterInformationSeq")
	@SequenceGenerator(name = "voterInformationSeq", sequenceName = "VOTER_INFORMATION_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The tenant identifier.
	 */
	@Column(name = "TENANT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String tenantId;

	/**
	 * The election event identifier.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String electionEventId;

	/**
	 * The voting card identifier.
	 */
	@Column(name = "VOTING_CARD_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String votingCardId;

	/**
	 * The ballot identifier.
	 */
	@Column(name = "BALLOT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String ballotId;

	/**
	 * The credential identifier.
	 */
	@Column(name = "CREDENTIAL_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String credentialId;

	/**
	 * The verification card identifier.
	 */
	@Column(name = "VERIFICATION_CARD_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String verificationCardId;

	/**
	 * The ballot box identifier.
	 */
	@Column(name = "BALLOT_BOX_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String ballotBoxId;

	/**
	 * The verification card set identifier.
	 */
	@Column(name = "VERIFICATION_CARD_SET_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String verificationCardSetId;

	/**
	 * The ballot box identifier.
	 */
	@Column(name = "VOTING_CARD_SET_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String votingCardSetId;

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
	 * Returns the current value of the field credentialId.
	 *
	 * @return Returns the credentialId.
	 */
	public String getCredentialId() {
		return credentialId;
	}

	/**
	 * Sets the value of the field credentialId.
	 *
	 * @param credentialId The credentialId to set.
	 */
	public void setCredentialId(final String credentialId) {
		this.credentialId = credentialId;
	}

	/**
	 * Returns the current value of the field verificationCardId.
	 *
	 * @return Returns the verificationCardId.
	 */
	public String getVerificationCardId() {
		return verificationCardId;
	}

	/**
	 * Sets the value of the field verificationCardId.
	 *
	 * @param verificationCardId The verificationCardId to set.
	 */
	public void setVerificationCardId(final String verificationCardId) {
		this.verificationCardId = verificationCardId;
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
	 * Returns the current value of the field verificationCardSetId.
	 *
	 * @return Returns the verificationCardSetId.
	 */
	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	/**
	 * Sets the value of the field verificationCardSetId.
	 *
	 * @param verificationCardSetId The verificationCardSetId to set.
	 */
	public void setVerificationCardSetId(final String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

	/**
	 * Returns the current value of the field votingCardSetId.
	 *
	 * @return Returns the votingCardSetId.
	 */
	public String getVotingCardSetId() {
		return votingCardSetId;
	}

	/**
	 * Sets the value of the field votingCardSetId.
	 *
	 * @param votingCardSetId The votingCardSetId to set.
	 */
	public void setVotingCardSetId(final String votingCardSetId) {
		this.votingCardSetId = votingCardSetId;
	}
}
