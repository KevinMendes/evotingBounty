/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client;

/**
 * Class to represent a request of Certificate Chain validation.
 */
public class CertificateChainValidationRequest {

	/**
	 * The certificate content to be validated.
	 */
	private String certificateContent;

	public String getCertificateContent() {
		return certificateContent;
	}

	public void setCertificateContent(String certificateContent) {
		this.certificateContent = certificateContent;
	}
}
