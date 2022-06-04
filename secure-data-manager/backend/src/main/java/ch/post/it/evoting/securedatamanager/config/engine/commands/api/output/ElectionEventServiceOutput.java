/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.api.output;

public class ElectionEventServiceOutput {

	private String offlineFolder;

	private String onlineAuthenticationFolder;

	private String onlineElectionInformationFolder;

	public String getOfflineFolder() {
		return offlineFolder;
	}

	public void setOfflineFolder(final String offlineFolder) {
		this.offlineFolder = offlineFolder;
	}

	public String getOnlineAuthenticationFolder() {
		return onlineAuthenticationFolder;
	}

	public void setOnlineAuthenticationFolder(final String onlineAuthenticationFolder) {
		this.onlineAuthenticationFolder = onlineAuthenticationFolder;
	}

	public String getOnlineElectionInformationFolder() {
		return onlineElectionInformationFolder;
	}

	public void setOnlineElectionInformationFolder(final String onlineElectionInformationFolder) {
		this.onlineElectionInformationFolder = onlineElectionInformationFolder;
	}
}
