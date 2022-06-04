/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.key.generation.request;

import java.util.List;

public class KeyGenerationRequestParameters {

	private List<String> resourceIds;

	private String keysDateFrom;

	private String keysDateTo;

	private String elGamalEncryptionParameters;

	public List<String> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(List<String> resourceIds) {
		this.resourceIds = resourceIds;
	}

	public String getKeysDateFrom() {
		return keysDateFrom;
	}

	public void setKeysDateFrom(String keysDateFrom) {
		this.keysDateFrom = keysDateFrom;
	}

	public String getKeysDateTo() {
		return keysDateTo;
	}

	public void setKeysDateTo(String keysDateTo) {
		this.keysDateTo = keysDateTo;
	}

	public String getElGamalEncryptionParameters() {
		return elGamalEncryptionParameters;
	}

	public void setElGamalEncryptionParameters(String elGamalEncryptionParameters) {
		this.elGamalEncryptionParameters = elGamalEncryptionParameters;
	}
}
