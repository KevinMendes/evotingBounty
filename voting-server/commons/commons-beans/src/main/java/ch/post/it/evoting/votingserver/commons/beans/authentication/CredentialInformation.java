/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.authentication;

import ch.post.it.evoting.votingserver.commons.beans.challenge.ServerChallengeMessage;

/**
 * Represents a credential information.
 */
public class CredentialInformation {

	private Credential credentialData;

	private ServerChallengeMessage serverChallengeMessage;

	// the authentication token certificate
	private String certificates;

	private String certificatesSignature;

	public CredentialInformation() {
		// Intentionally left blank.
	}

	public CredentialInformation(final Credential credentialData, final ServerChallengeMessage serverChallengeMessage, final String certificates,
			final String certificatesSignature) {
		this.credentialData = credentialData;
		this.serverChallengeMessage = serverChallengeMessage;
		this.certificates = certificates;
		this.certificatesSignature = certificatesSignature;
	}

	public Credential getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(Credential credentialData) {
		this.credentialData = credentialData;
	}

	public ServerChallengeMessage getServerChallengeMessage() {
		return serverChallengeMessage;
	}

	public void setServerChallengeMessage(ServerChallengeMessage serverChallengeMessage) {
		this.serverChallengeMessage = serverChallengeMessage;
	}

	public String getCertificates() {
		return certificates;
	}

	public void setCertificates(String certificates) {
		this.certificates = certificates;
	}

	public String getCertificatesSignature() {
		return certificatesSignature;
	}

	public void setCertificatesSignature(String certificatesSignature) {
		this.certificatesSignature = certificatesSignature;
	}

}
