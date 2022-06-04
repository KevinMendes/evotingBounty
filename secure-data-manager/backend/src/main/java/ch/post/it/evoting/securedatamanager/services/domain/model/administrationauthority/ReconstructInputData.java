/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority;

import java.util.List;

/**
 * Bean holding the list of serialized shares required to reconstruct a private key.
 */
public class ReconstructInputData {

	private List<String> serializedShares;

	private String serializedPublicKey;

	public List<String> getSerializedShares() {
		return serializedShares;
	}

	public void setSerializedShares(final List<String> serializedShares) {
		this.serializedShares = serializedShares;
	}

	public String getSerializedPublicKey() {
		return serializedPublicKey;
	}

	public void setSerializedPublicKey(final String serializedPublicKey) {
		this.serializedPublicKey = serializedPublicKey;
	}
}
