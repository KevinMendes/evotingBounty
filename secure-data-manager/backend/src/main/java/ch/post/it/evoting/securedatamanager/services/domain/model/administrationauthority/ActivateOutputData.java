/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority;

public class ActivateOutputData {

	private String issuerPublicKeyPEM;

	private String serializedSubjectPublicKey;

	public String getIssuerPublicKeyPEM() {
		return issuerPublicKeyPEM;
	}

	public void setIssuerPublicKeyPEM(final String issuerPublicKeyPEM) {
		this.issuerPublicKeyPEM = issuerPublicKeyPEM;
	}

	public String getSerializedSubjectPublicKey() {
		return serializedSubjectPublicKey;
	}

	public void setSerializedSubjectPublicKey(final String serializedSubjectPublicKey) {
		this.serializedSubjectPublicKey = serializedSubjectPublicKey;
	}
}
