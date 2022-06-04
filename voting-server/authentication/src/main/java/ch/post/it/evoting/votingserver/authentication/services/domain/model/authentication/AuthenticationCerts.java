/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

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

import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * Class representing the entity AuthenticationCerts
 */
@Entity
@Table(name = "AUTHENTICATION_CERTS")
public class AuthenticationCerts {
	/**
	 * The identifier for this entity.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "authenticationCertsSeq")
	@SequenceGenerator(name = "authenticationCertsSeq", sequenceName = "AUTHENTICATION_CERTS_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The identifier of a tenant for the current authentication content.
	 */
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String tenantId;

	/**
	 * The identifier of an election event for the current authentication content.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String electionEventId;

	/**
	 * The ballot in json format.
	 */
	@Lob
	@Column(name = "JSON")
	@NotNull(groups = SyntaxErrorGroup.class)
	private String json;

	/**
	 * The signature.
	 */
	@Lob
	@Column(name = "SIGNATURE")
	@NotNull(groups = SyntaxErrorGroup.class)
	private String signature;

	/**
	 * Returns the current value of the field ballot.
	 *
	 * @return Returns the json ballot.
	 */
	public String getJson() {
		return json;
	}

	/**
	 * Sets the value of the field ballot.
	 *
	 * @param json The json ballot to set.
	 */
	public void setJson(final String json) {
		this.json = json;
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
	 * Returns the current value of the field signature.
	 *
	 * @return Returns the signature.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets the value of the field signature.
	 *
	 * @param signature The signature to set.
	 */
	public void setSignature(final String signature) {
		this.signature = signature;
	}

}
