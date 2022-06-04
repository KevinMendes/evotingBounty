/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.electoralauthority;

public class ElectoralAuthoritySignInputData {

	private String privateKeyPEM;

	public String getPrivateKeyPEM() {
		return privateKeyPEM;
	}

	public void setPrivateKeyPEM(final String privateKeyPEM) {
		this.privateKeyPEM = privateKeyPEM;
	}

}
