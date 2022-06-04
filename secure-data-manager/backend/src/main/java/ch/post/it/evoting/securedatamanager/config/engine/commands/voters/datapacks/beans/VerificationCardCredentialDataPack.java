/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans;

import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalKeyPair;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.SerializedCredentialDataPack;

public class VerificationCardCredentialDataPack extends SerializedCredentialDataPack {

	private ElGamalKeyPair verificationCardKeyPair;

	public ElGamalKeyPair getVerificationCardKeyPair() {
		return verificationCardKeyPair;
	}

	public void setVerificationCardKeyPair(final ElGamalKeyPair verificationCardKeyPair) {
		this.verificationCardKeyPair = verificationCardKeyPair;
	}
}
