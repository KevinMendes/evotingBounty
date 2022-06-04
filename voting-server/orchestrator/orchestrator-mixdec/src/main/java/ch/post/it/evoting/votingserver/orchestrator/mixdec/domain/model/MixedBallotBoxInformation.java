/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model;

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
 * Entity representing the mixed ballot box information table.
 */
@Entity
@Table(name = "MIXED_BALLOT_BOX_INFORMATION")
public class MixedBallotBoxInformation {

	/**
	 * The id of the mixed ballot box information.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "mixedBallotBoxInformationSeq")
	@SequenceGenerator(name = "mixedBallotBoxInformationSeq", sequenceName = "MIXED_BALLOT_BOX_INFORMATION_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The tenant id.
	 */
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String tenantId;

	/**
	 * The election event id.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String electionEventId;

	/**
	 * The ballot box id.
	 */
	@Column(name = "BALLOT_BOX_ID")
	@NotNull
	@Size(max = Constants.COLUMN_LENGTH_100)
	private String ballotBoxId;

	/**
	 * The public key.
	 */
	@Column(name = "PUBLIC_KEY")
	@NotNull
	@Lob
	private String publicKey;

	/**
	 * The encryption parameters.
	 */
	@Column(name = "ENCRYPTION_PARAMETERS")
	@NotNull
	@Lob
	private String encryptionParameters;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public void setBallotBoxId(final String ballotBoxId) {
		this.ballotBoxId = ballotBoxId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(final String publicKey) {
		this.publicKey = publicKey;
	}

	public String getEncryptionParameters() {
		return encryptionParameters;
	}

	public void setEncryptionParameters(final String encryptionParameters) {
		this.encryptionParameters = encryptionParameters;
	}

}
