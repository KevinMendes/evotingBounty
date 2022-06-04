/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.model;

public class EncryptedSVK {

	private String encryptedStartVotingKey;

	public EncryptedSVK(String encryptedStartVotingKey) {
		super();
		this.encryptedStartVotingKey = encryptedStartVotingKey;
	}

	public String getEncryptedSVK() {
		return encryptedStartVotingKey;
	}

	public void setEncryptedSVK(String encryptedStartVotingKey) {
		this.encryptedStartVotingKey = encryptedStartVotingKey;
	}
}
