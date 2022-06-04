/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;

/**
 * Class representing a voter information.
 */
public class VoterInformation {

	/**
	 * The identifier of the entity.
	 */
	@JsonIgnore
	private Integer id;

	/**
	 * The tenant identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String tenantId;

	/**
	 * The election event identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String electionEventId;

	/**
	 * The voting card identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String votingCardId;

	/**
	 * The ballot identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String ballotId;

	/**
	 * The credential identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String credentialId;

	/**
	 * The verification card identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String verificationCardId;

	/**
	 * The ballot box identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String ballotBoxId;

	/**
	 * The voting card set identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String votingCardSetId;

	/**
	 * The verification card set identifier.
	 */
	@NotNull(groups = SyntaxErrorGroup.class)
	private String verificationCardSetId;

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
	public void setBallotId(String ballotId) {
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
	public void setCredentialId(String credentialId) {
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
	public void setVerificationCardId(String verificationCardId) {
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
	public void setBallotBoxId(String ballotBoxId) {
		this.ballotBoxId = ballotBoxId;
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
	public void setVotingCardSetId(String votingCardSetId) {
		this.votingCardSetId = votingCardSetId;
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
	public void setVerificationCardSetId(String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

}
