/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.post.it.evoting.votingserver.commons.domain.model.Constants;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCAEntity;

/**
 * Entity representing the info of a Certificate
 */
@Entity
@Table(name = "CERTIFICATE", uniqueConstraints = @UniqueConstraint(name = "CERTIFICATE_UK1", columnNames = { "CERTIFICATE_NAME", "PLATFORM_NAME",
		"TENANT_ID" }))

public class CertificateEntity extends PlatformCAEntity {

	/**
	 * The identifier for this cerficate
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "certificatesRegistrySeq")
	@SequenceGenerator(name = "certificatesRegistrySeq", sequenceName = "CERTIFICATE_SEQ", allocationSize = 1)
	@JsonIgnore
	private Long id;

	/**
	 * The identifier of a tenant for the current certificate.
	 */
	@Column(name = "TENANT_ID")
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String tenantId;

	/**
	 * The identifier of an election event for the current certificate.
	 */
	@Column(name = "ELECTION_EVENT_ID")
	@Size(max = Constants.COLUMN_LENGTH_100)
	@JsonIgnore
	private String electionEventId;

	/**
	 * Gets The identifier for this certificate..
	 *
	 * @return Value of The identifier for this ballot..
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the identifier for this certificate..
	 *
	 * @param id New value of The identifier for this ballot..
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Gets The identifier of an election event for the current certificate..
	 *
	 * @return Value of The identifier of an election event for the current ballot..
	 */
	public String getElectionEventId() {
		return electionEventId;
	}

	/**
	 * Sets the identifier of an election event for the current certificate..
	 *
	 * @param electionEventId New value of The identifier of an election event for the current certificate..
	 */
	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	/**
	 * Gets The identifier of a tenant for the current certificate..
	 *
	 * @return Value of The identifier of a tenant for the current certificate..
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets the identifier of a tenant for the current certificate..
	 *
	 * @param tenantId New value of The identifier of a tenant for the current certificate..
	 */
	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

}
