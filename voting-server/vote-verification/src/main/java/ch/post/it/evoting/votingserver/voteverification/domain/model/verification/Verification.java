/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.verification;

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
 * Entity that contains the information about the verification.
 */
@Entity
@Table(name = "VERIFICATION_DATA")
public class Verification {

	/**
	 * The identifier of this verification.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "verificationSeq")
	@SequenceGenerator(name = "verificationSeq", sequenceName = "VERIFICATION_DATA_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The identifier of the tenant, to which this verification belongs.
	 */
	@Column(name = "TENANT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String tenantId;

	/**
	 * The identifier of the election event, to which this verification belongs.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	@JsonProperty
	private String electionEventId;

	/**
	 * The external id provided.
	 */
	@Column(name = "VERIFICATION_CARD_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	@JsonProperty(value = "id")
	private String verificationCardId;

	/**
	 * The verification card set id.
	 */
	@Column(name = "VERIFICATION_CARD_SET_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String verificationCardSetId;

	/**
	 * The masking value information for verification purposes
	 */
	@Column(name = "VERIFICATION_CARD_KEYSTORE")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Lob
	private String verificationCardKeystore;

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
	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	/**
	 * Gets the value of the field verificationId.
	 *
	 * @return the verification id
	 */
	public String getVerificationCardId() {
		return verificationCardId;
	}

	/**
	 * Sets the value of field verificationCardId.
	 *
	 * @param verificationCardId the new verification card id
	 */
	public void setVerificationCardId(final String verificationCardId) {
		this.verificationCardId = verificationCardId;
	}

	/**
	 * Gets the value of the field verificationCardKeystore.
	 *
	 * @return the verificationCardKeystore
	 */
	public String getVerificationCardKeystore() {
		return verificationCardKeystore;
	}

	/**
	 * Sets the value of verificationCardKeystore field.
	 *
	 * @param verificationCardKeystore The masking value to set
	 */
	public void setVerificationCardKeystore(final String verificationCardKeystore) {
		this.verificationCardKeystore = verificationCardKeystore;
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
}
