/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.content;

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
 * Class representing the entity VerificationContent
 */
@Entity
@Table(name = "CODES_MAPPING", uniqueConstraints = {
		@UniqueConstraint(name = "CODES_MAPPING_UK1", columnNames = { "TENANT_ID", "ELECTION_EVENT_ID", "VERIFICATION_CARD_ID" }) })
public class CodesMapping {

	/**
	 * The identifier for this codes mapping
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "codesMappingSeq")
	@SequenceGenerator(name = "codesMappingSeq", sequenceName = "CODES_MAPPING_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The identifier of a tenant for the current codes mapping.
	 */
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String tenantId;

	/**
	 * The identifier of an election event for the current codes mapping.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String electionEventId;

	/**
	 * The identifier of a verification card that identifies the current codes mapping.
	 */
	@Column(name = "VERIFICATION_CARD_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String verificationCardId;

	/**
	 * The codes mapping in json format.
	 */
	@Lob
	@Column(name = "JSON")
	@NotNull
	private String json;

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
	 * Returns the current value of the field json.
	 *
	 * @return Returns the json.
	 */
	public String getJson() {
		return json;
	}

	/**
	 * Sets the value of the field json.
	 *
	 * @param json The json to set.
	 */
	public void setJson(final String json) {
		this.json = json;
	}

}
