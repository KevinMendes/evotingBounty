/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority;

/**
 * Bean holding the information about the smart card pin
 */
public class ReadShareInputData {

	private String pin;

	private String publicKeyPEM;

	public String getPin() {
		return pin;
	}

	public void setPin(final String pin) {
		this.pin = pin;
	}

	public String getPublicKeyPEM() {
		return publicKeyPEM;
	}

	public void setPublicKeyPEM(final String publicKeyPEM) {
		this.publicKeyPEM = publicKeyPEM;
	}
}
