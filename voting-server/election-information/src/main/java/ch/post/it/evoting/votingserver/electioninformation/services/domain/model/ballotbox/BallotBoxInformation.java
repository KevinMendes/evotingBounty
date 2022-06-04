/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

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
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

@Entity
@Table(name = "BALLOT_BOX_INFORMATION", uniqueConstraints = @UniqueConstraint(name = "BALLOT_BOX_INFORMATION_UK1", columnNames = { "BALLOT_BOX_ID",
		"ELECTION_EVENT_ID", "TENANT_ID" }))

public class BallotBoxInformation {

	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ballotBoxInformationSeq")
	@SequenceGenerator(name = "ballotBoxInformationSeq", sequenceName = "BALLOT_BOX_INFORMATION_SEQ", allocationSize = 1)
	private Integer id;

	/**
	 * The object containing the ballot box information.
	 */
	@Lob
	@Column(name = "JSON", length = Integer.MAX_VALUE)
	@NotNull
	private String json;

	@Column(name = "BALLOT_BOX_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonProperty("id")
	private String ballotBoxId;

	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String tenantId;

	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String electionEventId;

	@JsonIgnore
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public void setBallotBoxId(String ballotBoxId) {
		this.ballotBoxId = ballotBoxId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(String electionEventId) {
		this.electionEventId = electionEventId;
	}

}
