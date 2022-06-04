/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.certificate;

/**
 * Entity representing the info of a Certificate
 */

public class CertificateEntity {

	/**
	 * The identifier of a tenant for the current certificate.
	 */
	private String tenantId;

	/**
	 * The identifier of an election event for the current certificate.
	 */
	private String electionEventId;

	/**
	 * The name of the certificate
	 */
	private String certificateName;

	/**
	 * the platform name
	 **/
	private String platformName;

	/**
	 * The content of the certificate in json format.
	 */
	private String certificateContent;

	/**
	 * Gets The name of the certificate.
	 *
	 * @return Value of The name of the certificate.
	 */
	public String getCertificateName() {
		return certificateName;
	}

	/**
	 * Sets the name of the certificate.
	 *
	 * @param certificateName New value of The name of the certificate.
	 */
	public void setCertificateName(final String certificateName) {
		this.certificateName = certificateName;
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
	 * Gets The content of the certificate in json format..
	 *
	 * @return Value of The content of the certificate in json format..
	 */
	public String getCertificateContent() {
		return certificateContent;
	}

	/**
	 * Sets the content of the certificate in json format..
	 *
	 * @param certificateContent New value of The content of the certificate in json format..
	 */
	public void setCertificateContent(final String certificateContent) {
		this.certificateContent = certificateContent;
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

	/**
	 * Gets the platformName
	 *
	 * @return Value of platformName
	 */
	public String getPlatformName() {
		return platformName;
	}

	/**
	 * sets the platformName.
	 *
	 * @param platformName New value of platform name.
	 */
	public void setPlatformName(final String platformName) {
		this.platformName = platformName;
	}

}
