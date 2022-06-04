/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.verificationset;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a verification set data.
 */
public class VerificationSet {

	@JsonProperty("id")
	private String id;

	/**
	 * The choices codes public key and certificates information for verification purposes.
	 */
	@JsonProperty("data")
	private VerificationSetData data;

	@JsonProperty("signature")
	private String signature;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public VerificationSetData getData() {
		return data;
	}

	public void setData(VerificationSetData data) {
		this.data = data;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
}
