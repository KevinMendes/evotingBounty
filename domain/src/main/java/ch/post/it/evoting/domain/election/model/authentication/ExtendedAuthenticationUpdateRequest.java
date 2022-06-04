/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.authentication;

public class ExtendedAuthenticationUpdateRequest {

	private String signature;

	private String certificate;

	/**
	 * Gets certificate.
	 *
	 * @return Value of certificate.
	 */
	public String getCertificate() {
		return certificate;
	}

	/**
	 * Sets new certificate.
	 *
	 * @param certificate New value of certificate.
	 */
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	/**
	 * Gets signature.
	 *
	 * @return Value of signature.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets new signature.
	 *
	 * @param signature New value of signature.
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}
}
