/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.challenge;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;

/**
 * Class representing the challenge information including server challenge message and client challenge message.
 */
public class ChallengeInformation {

	@NotNull(groups = SyntaxErrorGroup.class)
	@Valid
	private ServerChallengeMessage serverChallengeMessage;

	@NotNull(groups = SyntaxErrorGroup.class)
	@Valid
	private ClientChallengeMessage clientChallengeMessage;

	// The credential authentication X.509 Certificate.
	@NotNull(groups = SyntaxErrorGroup.class)
	private String certificate;

	@NotNull(groups = SyntaxErrorGroup.class)
	private String credentialId;

	/**
	 * Returns the current value of the field serverChallengeMessage.
	 *
	 * @return Returns the serverChallengeMessage.
	 */
	public ServerChallengeMessage getServerChallengeMessage() {
		return serverChallengeMessage;
	}

	/**
	 * Sets the value of the field serverChallengeMessage.
	 *
	 * @param serverChallengeMessage The serverChallengeMessage to set.
	 */
	public void setServerChallengeMessage(final ServerChallengeMessage serverChallengeMessage) {
		this.serverChallengeMessage = serverChallengeMessage;
	}

	/**
	 * Returns the current value of the field clientChallengeMessage.
	 *
	 * @return Returns the clientChallengeMessage.
	 */
	public ClientChallengeMessage getClientChallengeMessage() {
		return clientChallengeMessage;
	}

	/**
	 * Sets the value of the field clientChallengeMessage.
	 *
	 * @param clientChallengeMessage The clientChallengeMessage to set.
	 */
	public void setClientChallengeMessage(final ClientChallengeMessage clientChallengeMessage) {
		this.clientChallengeMessage = clientChallengeMessage;
	}

	/**
	 * Returns the current value of the field certificate.
	 *
	 * @return Returns the certificate.
	 */
	public String getCertificate() {
		return certificate;
	}

	/**
	 * Sets the value of the field certificate.
	 *
	 * @param certificate The certificate to set.
	 */
	public void setCertificate(final String certificate) {
		this.certificate = certificate;
	}

	/**
	 * Returns the current value of the field credentialId.
	 *
	 * @return Returns the credentialId.
	 */
	public String getCredentialId() {
		return credentialId;
	}

	/**
	 * Sets the value of the field credentialId.
	 *
	 * @param credentialId The credentialId to set.
	 */
	public void setCredentialId(final String credentialId) {
		this.credentialId = credentialId;
	}

}
