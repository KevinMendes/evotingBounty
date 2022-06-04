/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.platform;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCAEntity;

/**
 * @see PlatformCAEntity
 */
@Entity
@Table(name = "PLATFORM_CERTIFICATE")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class PlatformCertificate extends PlatformCAEntity {

	/**
	 * The identifier for this entity.
	 */
	@Id
	@Column(name = "ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "platformCertificateSeq")
	@SequenceGenerator(name = "platformCertificateSeq", sequenceName = "PLATFORM_CERTIFICATE_SEQ", allocationSize = 1)
	@JsonIgnore
	private Long id;

	/**
	 * Gets The identifier for this entity..
	 *
	 * @return Value of The identifier for this entity..
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the identifier for this entity..
	 *
	 * @param id New value of The identifier for this entity..
	 */
	public void setId(final Long id) {
		this.id = id;
	}
}
