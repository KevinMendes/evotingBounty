/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues;

import java.sql.Clob;

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

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * Entity that contains the information about the computed values.
 */
@Entity
@Table(name = "COMPUTED_VALUES")
public class ComputedValues {

	/**
	 * The identifier of this computed value.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "computedValuesSeq")
	@SequenceGenerator(name = "computedValuesSeq", sequenceName = "COMPUTED_VALUES_SEQ", allocationSize = 1)
	@JsonIgnore
	private Integer id;

	/**
	 * The identifier of the verification card set, to which this computed values belong.
	 */
	@Column(name = "VERIFICATION_CARD_SET_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String verificationCardSetId;

	/**
	 * The identifier of the tenant, to which this computed values belong.
	 */
	@Column(name = "TENANT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String tenantId;

	/**
	 * The identifier of the election event, to which this computed values belong.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String electionEventId;

	/**
	 * The identifier of the chunk.
	 */
	@Column(name = "CHUNK_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	private int chunkId;

	/**
	 * The field containing the computation results
	 **/
	@Column(name = "JSON")
	@Lob
	private Clob json;

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

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public void setVerificationCardSetId(final String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

	public int getChunkId() {
		return chunkId;
	}

	public void setChunkId(final int chunkId) {
		this.chunkId = chunkId;
	}

	public Clob getJson() {
		return json;
	}

	public void setJson(final Clob json) {
		this.json = json;
	}
}
