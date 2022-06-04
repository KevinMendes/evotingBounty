/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans;

import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.InputDataPack;

public class VotingCardCredentialInputDataPack extends InputDataPack {

	private final CredentialProperties credentialAuthProperties;

	public VotingCardCredentialInputDataPack(final CredentialProperties credentialAuthProperties) {
		super();
		this.credentialAuthProperties = credentialAuthProperties;
	}

	public CredentialProperties getCredentialAuthProperties() {
		return credentialAuthProperties;
	}
}
