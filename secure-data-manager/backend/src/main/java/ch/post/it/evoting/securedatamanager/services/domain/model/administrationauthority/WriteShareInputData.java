/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority;

/**
 * Bean holding the information about the smart card pin
 */
public class WriteShareInputData {

	private String pin;

	public String getPin() {
		return pin;
	}

	public void setPin(final String pin) {
		this.pin = pin;
	}
}
