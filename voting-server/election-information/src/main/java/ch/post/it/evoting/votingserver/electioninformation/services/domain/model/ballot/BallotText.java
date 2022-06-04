/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot;

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
 * The entity storing internationalization texts for a ballot.
 */
@Entity
@Table(name = "BALLOT_TEXT")
public class BallotText {

	/**
	 * The identifier for this ballot text.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ballotTextSeq")
	@SequenceGenerator(name = "ballotTextSeq", sequenceName = "BALLOT_TEXT_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * A provided id which will identify the ballot.
	 */
	@Column(name = "BALLOT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String ballotId;

	/**
	 * The identifier of an election event for the current ballot text.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String electionEventId;

	/**
	 * The identifier of a tenant for the current ballot text.
	 */
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String tenantId;

	/**
	 * The ballot text in json format.
	 */
	@Lob
	@Column(name = "JSON")
	@NotNull
	private String json;

	/**
	 * The ballot text signature.
	 */
	@Lob
	@Column(name = "SIGNATURE")
	@NotNull
	private String signature;

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
	public void setJson(String json) {
		this.json = json;
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
	public void setSignature(String signature) {
		this.signature = signature;
	}
}
