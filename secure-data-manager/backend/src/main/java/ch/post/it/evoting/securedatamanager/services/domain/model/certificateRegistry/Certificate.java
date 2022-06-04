/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.certificateRegistry;

public class Certificate {

	private String certificateName;

	private String certificateContent;

	public String getCertificateName() {
		return certificateName;
	}

	public void setCertificateName(final String certificateName) {
		this.certificateName = certificateName;
	}

	public String getCertificateContent() {
		return certificateContent;
	}

	public void setCertificateContent(final String certificateContent) {
		this.certificateContent = certificateContent;
	}
}
