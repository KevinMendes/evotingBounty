/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.impl;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;
import static java.nio.file.Files.notExists;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateVotingCardSetInput;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;

@Service
public class VotersParametersHolderAdapter {

	private final ConfigObjectMapper mapper;

	public VotersParametersHolderAdapter(final ConfigObjectMapper mapper) {
		this.mapper = mapper;
	}

	public VotersParametersHolder adapt(
			@RequestBody
			final CreateVotingCardSetInput input) {

		final int numberVotingCards;
		final String ballotID;
		final String ballotBoxID;
		final String votingCardSetID;
		final String verificationCardSetID;
		final String electoralAuthorityID;
		final Path absoluteBasePath;
		final String eeID;
		final Ballot ballot;
		final VotersParametersHolder holder;

		numberVotingCards = input.getNumberVotingCards();
		ballotID = input.getBallotID();
		ballotBoxID = input.getBallotBoxID();
		votingCardSetID = input.getVotingCardSetID();
		verificationCardSetID = input.getVerificationCardSetID();
		electoralAuthorityID = input.getElectoralAuthorityID();

		checkGivenIDsAreUUIDs(ballotID, ballotBoxID, votingCardSetID);

		absoluteBasePath = parseBaseToAbsolutePath(input.getBasePath());
		eeID = absoluteBasePath.getFileName().toString();

		ballot = getBallot(input.getBallotPath());

		validateBallotAndBallotIDMatch(ballot, ballotID);

		final ZonedDateTime startValidityPeriod;
		final ZonedDateTime endValidityPeriod;

		final String end = input.getEnd();

		final Integer validityPeriod = input.getValidityPeriod();

		startValidityPeriod = ZonedDateTime.now(ZoneOffset.UTC);

		final ZonedDateTime electionEndDate = ZonedDateTime.ofInstant(Instant.parse(end), ZoneOffset.UTC);

		final String platformRootCACertificate = input.getPlatformRootCACertificate();

		endValidityPeriod = electionEndDate.plusYears(validityPeriod);

		if (electionEndDate.isAfter(endValidityPeriod)) {
			throw new IllegalArgumentException("End date cannot be after Start date plus validity period.");
		}

		holder = new VotersParametersHolder(numberVotingCards, ballotID, ballot, ballotBoxID, votingCardSetID, verificationCardSetID,
				electoralAuthorityID, absoluteBasePath, eeID, startValidityPeriod, endValidityPeriod, input.getVotingCardSetAlias(),
				platformRootCACertificate, input.getCreateVotingCardSetCertificateProperties());

		return holder;
	}

	private Path parseBaseToAbsolutePath(final String basePath) {

		final Path baseAbsolutePath = Paths.get(basePath).toAbsolutePath();

		final String prefixErrorMessage = "The given base path: \"" + basePath;

		checkFile(basePath, baseAbsolutePath);

		final String eeid = baseAbsolutePath.getFileName().toString();
		try {
			validateUUID(eeid);
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException(prefixErrorMessage + "\" requires an election event id in UUID format.", e);
		}

		return baseAbsolutePath;
	}

	private void checkGivenIDsAreUUIDs(final String ballotID, final String ballotBoxID, final String votingCardSetID) {

		validateUUID(ballotID);
		validateUUID(ballotBoxID);
		validateUUID(votingCardSetID);
	}

	private Ballot getBallot(final String ballotPath) {

		final Path ballotAbsolutePath = Paths.get(ballotPath).toAbsolutePath();

		final File ballotFile = ballotAbsolutePath.toFile();

		checkFile(ballotPath, ballotAbsolutePath);

		return getBallotFromFile(ballotPath, ballotFile);
	}

	private Ballot getBallotFromFile(final String ballotPath, final File ballotFile) {
		final Ballot ballot;
		try {
			ballot = mapper.fromJSONFileToJava(ballotFile, Ballot.class);
		} catch (final IOException e) {
			throw new IllegalArgumentException("An error occurred while mapping \"" + ballotPath + "\" to a Ballot.", e);
		}
		return ballot;
	}

	private void validateBallotAndBallotIDMatch(final Ballot ballot, final String ballotID) {
		if (!ballot.getId().equals(ballotID)) {
			throw new IllegalArgumentException("The given Ballot with ID: " + ballot.getId() + " and the given ballotID: " + ballotID
					+ " are different. They must be the same.");
		}
	}

	private void checkFile(final String path, final Path absolutePath) {
		final String errorMessageBallot = "The given file: \"" + path + "\"";

		final String errorHelpMessage = " The given path should be either (1) relative to the execution path or (2) absolute.";

		if (notExists(absolutePath)) {
			throw new IllegalArgumentException(errorMessageBallot + " could not be found." + errorHelpMessage);
		}
	}

}
