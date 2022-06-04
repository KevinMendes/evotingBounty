/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.verificationset;

/**
 * Data of a verification set.
 */
public class VerificationSetData {
	// the voting card set id
	private String id;

	// encryption public key
	private String choicesCodesEncryptionPublicKey;

	private String verificationCardSetId;

	private String electionEventId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChoicesCodesEncryptionPublicKey() {
		return choicesCodesEncryptionPublicKey;
	}

	public void setChoicesCodesEncryptionPublicKey(String choicesCodesEncryptionPublicKey) {
		this.choicesCodesEncryptionPublicKey = choicesCodesEncryptionPublicKey;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public void setVerificationCardSetId(String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(String electionEventId) {
		this.electionEventId = electionEventId;
	}
}
