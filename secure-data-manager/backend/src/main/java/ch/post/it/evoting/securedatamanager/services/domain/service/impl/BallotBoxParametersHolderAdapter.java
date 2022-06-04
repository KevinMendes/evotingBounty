/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.impl;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.domain.CreateBallotBoxesInput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox.BallotBoxParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;

@Service
public final class BallotBoxParametersHolderAdapter {

	public BallotBoxParametersHolder adapt(final CreateBallotBoxesInput input) {
		final String ballotID = input.getBallotID();
		final String electoralAuthorityId = input.getElectoralAuthorityID();
		final String ballotBoxID = input.getBallotBoxID();
		final String alias = input.getAlias();
		final String isTest = input.getTest();
		final String gracePeriod = input.getGracePeriod();

		final Path absolutePath = Paths.get(input.getOutputFolder()).toAbsolutePath();
		final String eeID = validatePathAndExtractEeID(absolutePath);

		final ZonedDateTime electionStartDate = ZonedDateTime.ofInstant(Instant.parse(input.getStart()), ZoneOffset.UTC);
		final ZonedDateTime electionEndDate = ZonedDateTime.ofInstant(Instant.parse(input.getEnd()), ZoneOffset.UTC);
		final ZonedDateTime startValidityPeriod = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime endValidityPeriod = electionEndDate.plusYears(input.getValidityPeriod());
		if (electionEndDate.isAfter(endValidityPeriod)) {
			throw new IllegalArgumentException("End date cannot be after Start date plus validity period.");
		}

		final ElectionInputDataPack electionInputDataPack = new ElectionInputDataPack();
		electionInputDataPack.setEeid(eeID);
		electionInputDataPack.setElectionStartDate(electionStartDate);
		electionInputDataPack.setElectionEndDate(electionEndDate);
		electionInputDataPack.setStartDate(startValidityPeriod);
		electionInputDataPack.setEndDate(endValidityPeriod);

		return new BallotBoxParametersHolder(ballotID, electoralAuthorityId, ballotBoxID, alias, absolutePath, eeID, electionInputDataPack, isTest,
				gracePeriod, input.getWriteInAlphabet());
	}

	private String validatePathAndExtractEeID(final Path outputPath) {

		if (!outputPath.toFile().exists()) {
			throw new IllegalArgumentException(String.format("The given output path does not exist. [path: %s]", outputPath));
		}

		final String electionEventID = outputPath.getFileName().toString();
		validateUUID(electionEventID);

		return electionEventID;
	}

}
