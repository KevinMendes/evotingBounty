/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent;

import java.nio.file.Path;

import ch.post.it.evoting.domain.election.AuthenticationParams;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateElectionEventCertificatePropertiesContainer;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.ConfigurationInput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;

/**
 * The container with all the info needed by the CreateElectionEventGenerator.
 */
public class CreateElectionEventParametersHolder {

	private final ElectionInputDataPack electionInputDataPack;

	private final Path electionFolder;

	private final Path outputFolder;

	private final Path offlineFolder;

	private final Path onlineAuthenticationFolder;

	private final Path onlineElectionInformationFolder;

	private final AuthenticationParams authenticationParams;

	private final String keyForProtectingKeystorePassword;
	private final CreateElectionEventCertificatePropertiesContainer certificatePropertiesInput;
	private ConfigurationInput configurationInput;

	public CreateElectionEventParametersHolder(final ElectionInputDataPack electionInputDataPack, final Path outputFolder, final Path electionFolder,
			final Path offlineFolder, final Path onlineAuthenticationFolder, final Path onlineElectionInformationFolder,
			final AuthenticationParams authenticationParams, final String keyForProtectingKeystorePassword,
			final CreateElectionEventCertificatePropertiesContainer certificatePropertiesInput) {

		this.electionInputDataPack = electionInputDataPack;
		this.outputFolder = outputFolder;
		this.electionFolder = electionFolder;
		this.offlineFolder = offlineFolder;
		this.onlineAuthenticationFolder = onlineAuthenticationFolder;
		this.onlineElectionInformationFolder = onlineElectionInformationFolder;
		this.authenticationParams = authenticationParams;
		this.keyForProtectingKeystorePassword = keyForProtectingKeystorePassword;
		this.certificatePropertiesInput = certificatePropertiesInput;
	}

	public Path getOnlineAuthenticationFolder() {
		return onlineAuthenticationFolder;
	}

	public Path getOnlineElectionInformationFolder() {
		return onlineElectionInformationFolder;
	}

	public Path getOutputFolder() {
		return outputFolder;
	}

	public Path getOfflineFolder() {
		return offlineFolder;
	}

	public AuthenticationParams getAuthenticationParams() {
		return authenticationParams;
	}

	public ElectionInputDataPack getInputDataPack() {
		return electionInputDataPack;
	}

	public ConfigurationInput getConfigurationInput() {
		return configurationInput;
	}

	public void setConfigurationInput(final ConfigurationInput configurationInput) {
		this.configurationInput = configurationInput;
	}

	public String getKeyForProtectingKeystorePassword() {
		return keyForProtectingKeystorePassword;
	}

	public Path getElectionFolder() {
		return electionFolder;
	}

	public CreateElectionEventCertificatePropertiesContainer getCertificatePropertiesInput() {
		return certificatePropertiesInput;
	}
}
