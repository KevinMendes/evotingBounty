/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority;

public class ReconstructOutputData {

	private String serializedPrivateKey;

	public String getSerializedPrivateKey() {
		return serializedPrivateKey;
	}

	public void setSerializedPrivateKey(String serializedPrivateKey) {
		this.serializedPrivateKey = serializedPrivateKey;
	}
}
