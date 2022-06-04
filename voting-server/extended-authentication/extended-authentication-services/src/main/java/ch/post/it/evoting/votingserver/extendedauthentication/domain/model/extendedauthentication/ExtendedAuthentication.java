/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

@Entity
@Table(name = "EXTENDED_AUTHENTICATION", uniqueConstraints = @UniqueConstraint(name = "EXTENDED_AUTHENTICATION_PK", columnNames = { "ELECTION_EVENT",
		"TENANT_ID", "AUTH_ID" }))

@IdClass(value = ExtendedAuthenticationPK.class)
public class ExtendedAuthentication {

	@Id
	@Column(name = "AUTH_ID", nullable = false)
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String authId;

	@Column(name = "EXTRA_PARAM")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_50)
	private String extraParam;

	@Column(name = "SALT")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = 200)
	private String salt;

	@Column(name = "ATTEMPTS")
	@NotNull(groups = SyntaxErrorGroup.class)
	private Integer attempts;

	@Column(name = "ENCRYPTED_SVK", nullable = false)
	@Size(max = Constants.COLUMN_LENGTH_150)
	private String encryptedStartVotingKey;

	@Id
	@Column(name = "TENANT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String tenantId;

	@Id
	@Column(name = "ELECTION_EVENT")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String electionEvent;

	@Column(name = "CREDENTIAL_ID", nullable = false)
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String credentialId;

	public String getExtraParam() {
		return extraParam;
	}

	public void setExtraParam(String extraParam) {
		this.extraParam = extraParam;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	public String getEncryptedStartVotingKey() {
		return encryptedStartVotingKey;
	}

	public void setEncryptedStartVotingKey(String encryptedStartVotingKey) {
		this.encryptedStartVotingKey = encryptedStartVotingKey;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getElectionEvent() {
		return electionEvent;
	}

	public void setElectionEvent(String electionEvent) {
		this.electionEvent = electionEvent;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

}
