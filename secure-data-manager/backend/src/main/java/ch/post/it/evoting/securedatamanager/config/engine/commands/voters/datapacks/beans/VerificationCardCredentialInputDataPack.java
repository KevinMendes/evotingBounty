/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans;

import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.InputDataPack;

public class VerificationCardCredentialInputDataPack extends InputDataPack {

	private final CredentialProperties verificationCardProperties;

	public VerificationCardCredentialInputDataPack(final CredentialProperties verificationCardProperties) {
		super();

		this.verificationCardProperties = verificationCardProperties;
	}

	public CredentialProperties getVerificationCardProperties() {
		return verificationCardProperties;
	}
}
