/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionCredentialDataPack;

/**
 * The class that contains all the info to be saved by the serializer
 */
public class CreateElectionEventOutput {

	private final Map<String, ElectionCredentialDataPack> dataPackMap = new LinkedHashMap<>();

	private ElectionCredentialDataPack authTokenSigner;

	public Map<String, ElectionCredentialDataPack> getDataPackMap() {
		return dataPackMap;
	}

	/**
	 * @return Returns the authTokenSigner.
	 */
	public ElectionCredentialDataPack getAuthTokenSigner() {
		return authTokenSigner;
	}

	/**
	 * @param authTokenSigner The authTokenSigner to set.
	 */
	public void setAuthTokenSigner(final ElectionCredentialDataPack authTokenSigner) {
		this.authTokenSigner = authTokenSigner;
	}

	/**
	 * Clears passwords in contained {@link ElectionCredentialDataPack} instances.
	 */
	public void clearPasswords() {
		for (final ElectionCredentialDataPack pack : dataPackMap.values()) {
			pack.clearPassword();
		}
		authTokenSigner.clearPassword();
	}

}
