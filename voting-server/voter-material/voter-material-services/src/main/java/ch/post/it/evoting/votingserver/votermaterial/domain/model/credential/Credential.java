/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.credential;

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
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * Entity that contains the information about the credential.
 */
@Entity
@Table(name = "CREDENTIAL_DATA")
public class Credential {

	/**
	 * The identifier of this credential.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "credentialSeq")
	@SequenceGenerator(name = "credentialSeq", sequenceName = "CREDENTIAL_DATA_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The provided identifier.
	 */
	@Column(name = "CREDENTIAL_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String credentialId;

	/**
	 * The identifier of the tenant, to which this credential belongs.
	 */
	@Column(name = "TENANT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	@JsonIgnore
	private String tenantId;

	/**
	 * The identifier of the election event, to which this credential belongs.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	@JsonIgnore
	private String electionEventId;

	/**
	 * The String representing the keyStore.
	 */
	@Column(name = "KEYSTORE", length = Integer.MAX_VALUE)
	@NotNull(groups = SyntaxErrorGroup.class)
	@Lob
	private String data;

	/**
	 * Returns the current value of the field id.
	 *
	 * @return Returns the id.
	 */
	@JsonIgnore
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the value of the field id.
	 *
	 * @param id The id to set.
	 */
	@JsonIgnore
	public void setId(final Integer id) {
		this.id = id;
	}

	/**
	 * Returns the current value of the field tenantId.
	 *
	 * @return Returns the tenantId.
	 */
	@JsonIgnore
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets the value of the field tenantId.
	 *
	 * @param tenantId The tenantId to set.
	 */
	@JsonProperty
	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Returns the current value of the field electionEventId.
	 *
	 * @return Returns the electionEventId.
	 */
	@JsonIgnore
	public String getElectionEventId() {
		return electionEventId;
	}

	/**
	 * Sets the value of the field electionEventId.
	 *
	 * @param electionEventId The electionEventId to set.
	 */
	@JsonProperty
	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	/**
	 * Gets the value of the field credential id.
	 *
	 * @return the credential id
	 */
	@JsonProperty(value = "id")
	public String getCredentialId() {
		return credentialId;
	}

	/**
	 * Sets the value of the credential id.
	 *
	 * @param credentialId the new credential id
	 */
	public void setCredentialId(final String credentialId) {
		this.credentialId = credentialId;
	}

	/**
	 * Gets the value of the field data.
	 *
	 * @return the data
	 */
	@JsonProperty
	public String getData() {
		return data;
	}

	/**
	 * Sets the value of the data.
	 *
	 * @param data the new data
	 */
	@JsonProperty
	public void setData(final String data) {
		this.data = data;
	}
}
