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
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * The entity representing a ballot. A ballot contains all the voting options for a selection of elections from an election event.
 */
@Entity
@Table(name = "BALLOT")
public class Ballot {

	/**
	 * The identifier for this ballot.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ballotSeq")
	@SequenceGenerator(name = "ballotSeq", sequenceName = "BALLOT_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * A provided ballot id which will identify the ballot.
	 */
	@Column(name = "BALLOT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonProperty("id")
	private String ballotId;

	/**
	 * The identifier of a tenant for the current ballot.
	 */
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String tenantId;

	/**
	 * The identifier of an election event for the current ballot.
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
	@NotNull
	private String json;

	/**
	 * The ballot signature.
	 */
	@Lob
	@Column(name = "SIGNATURE")
	@NotNull
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
