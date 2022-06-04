/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority;

/**
 * Bean holding the serialized share.
 */
public class ReadShareOutputData {

	private String serializedShare;

	public String getSerializedShare() {
		return serializedShare;
	}

	public void setSerializedShare(final String serializedShare) {
		this.serializedShare = serializedShare;
	}
}
