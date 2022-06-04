/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.platform;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * class for defining an entity representing a platform root CA
 */
@MappedSuperclass
public class PlatformCAEntity {

	/**
	 * The column length 100.
	 */
	public static final int COLUMN_LENGTH_100 = 100;

	/**
	 * The platform name
	 */
	@Column(name = "PLATFORM_NAME")
	@NotNull
	@Size(max = COLUMN_LENGTH_100)
	private String platformName;

	/**
	 * The certificate name
	 */
	@Column(name = "CERTIFICATE_NAME")
	@NotNull
	@Size(max = COLUMN_LENGTH_100)
	private String certificateName;

	/**
	 * The content of the certificate
	 */
	@Column(name = "CERTIFICATE_CONTENT", length = Integer.MAX_VALUE)
	@NotNull
	@Lob
	private String certificateContent;

	/**
	 * Gets platformName.
	 *
	 * @return Value of platformName.
	 */
	public String getPlatformName() {
		return platformName;
	}

	/**
	 * Sets new platformName.
	 *
	 * @param platformName New value of platformName.
	 */
	public void setPlatformName(final String platformName) {
		this.platformName = platformName;
	}

	/**
	 * Gets certificateName.
	 *
	 * @return Value of certificateName.
	 */
	public String getCertificateName() {
		return certificateName;
	}

	/**
	 * Sets new certificateName.
	 *
	 * @param certificateName New value of certificateName.
	 */
	public void setCertificateName(final String certificateName) {
		this.certificateName = certificateName;
	}

	/**
	 * Gets certificateContent.
	 *
	 * @return Value of certificateContent.
	 */
	public String getCertificateContent() {
		return certificateContent;
	}

	/**
	 * Sets new certificateContent.
	 *
	 * @param certificateContent New value of certificateContent.
	 */
	public void setCertificateContent(final String certificateContent) {
		this.certificateContent = certificateContent;
	}
}
