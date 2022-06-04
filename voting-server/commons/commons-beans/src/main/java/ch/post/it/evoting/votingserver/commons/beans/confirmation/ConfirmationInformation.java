/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.confirmation;

import ch.post.it.evoting.domain.election.model.confirmation.ConfirmationMessage;

/**
 * Represents the confirmation information sent by the client.
 */
public class ConfirmationInformation {

	private String credentialId;

	private ConfirmationMessage confirmationMessage;

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(final String credentialId) {
		this.credentialId = credentialId;
	}

	public ConfirmationMessage getConfirmationMessage() {
		return confirmationMessage;
	}

	public void setConfirmationMessage(final ConfirmationMessage confirmationMessage) {
		this.confirmationMessage = confirmationMessage;
	}
}
