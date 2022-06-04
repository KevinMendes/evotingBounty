/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import ch.post.it.evoting.domain.election.AuthenticationParams;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateElectionEventCertificatePropertiesContainer;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateElectionEventInput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;

@Service
public class CreateElectionEventParametersHolderAdapter {

	public CreateElectionEventParametersHolder adapt(
			@RequestBody
			final CreateElectionEventInput input) {
		final ElectionInputDataPack electionInputDataPack = new ElectionInputDataPack();

		// get EEID parameter
		final String eeid = input.getEeid();
		electionInputDataPack.setEeid(eeid);
		final ReplacementsHolder replacementHolder = new ReplacementsHolder(eeid);
		electionInputDataPack.setReplacementsHolder(replacementHolder);

		final String end = input.getEnd();
		final Integer validityPeriod = input.getValidityPeriod();

		// ISO_INSTANT format => 2011-12-03T10:15:30Z
		final ZonedDateTime startValidityPeriod = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime electionEndDate = ZonedDateTime.ofInstant(Instant.parse(end), ZoneOffset.UTC);
		final ZonedDateTime endValidityPeriod = electionEndDate.plusYears(validityPeriod);

		if (electionEndDate.isAfter(endValidityPeriod)) {
			throw new IllegalArgumentException("End date cannot be after Start date plus validity period.");
		}

		electionInputDataPack.setStartDate(startValidityPeriod);
		electionInputDataPack.setEndDate(endValidityPeriod);

		final String challengeResExpTime = input.getChallengeResExpTime();
		final String authTokenExpTime = input.getAuthTokenExpTime();
		final String challengeLength = input.getChallengeLength();

		final AuthenticationParams authenticationParams = new AuthenticationParams(challengeResExpTime, authTokenExpTime, challengeLength);

		final String outputPath = input.getOutputPath();

		final Path electionFolder = Paths.get(outputPath, eeid);

		final Path offlinePath = electionFolder.resolve(Constants.CONFIG_DIR_NAME_OFFLINE);

		final Path onlinePath = electionFolder.resolve(Constants.CONFIG_DIR_NAME_ONLINE);

		final Path autenticationPath = onlinePath.resolve(Constants.CONFIG_DIR_NAME_AUTHENTICATION);

		final Path electionInformationPath = onlinePath.resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION);

		final String keyForProtectingKeystorePassword = input.getKeyForProtectingKeystorePassword();

		final CreateElectionEventCertificatePropertiesContainer certificatePropertiesInput = input.getCertificatePropertiesInput();

		return new CreateElectionEventParametersHolder(electionInputDataPack, Paths.get(outputPath), electionFolder, offlinePath, autenticationPath,
				electionInformationPath, authenticationParams, keyForProtectingKeystorePassword, certificatePropertiesInput);
	}
}
