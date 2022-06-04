/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans;

import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.InputDataPack;

public class VotingCardSetCredentialInputDataPack extends InputDataPack {

	private final CredentialProperties credentialProperties;

	public VotingCardSetCredentialInputDataPack(final CredentialProperties credentialProperties) {
		super();
		this.credentialProperties = credentialProperties;
	}

	public CredentialProperties getCredentialProperties() {
		return credentialProperties;
	}
}
