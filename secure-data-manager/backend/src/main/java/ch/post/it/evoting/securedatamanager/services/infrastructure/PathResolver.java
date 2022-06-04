/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.securedatamanager.commons.Constants;

@Service
public class PathResolver {

	private final String workspace;

	public PathResolver(
			@Value("${sdm.workspace}")
			final String workspace) {
		this.workspace = workspace;
	}

	/**
	 * Provides the config directory path in the SDM workspace.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}.
	 *
	 * @return the config directory path.
	 */
	public Path resolveConfigPath() {
		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR);
	}

	/**
	 * Provides the election event directory path in the SDM workspace for the given election event.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}.
	 *
	 * @param electionEventId the election event id. Must be non-null and a valid UUID.
	 * @return the election event path in the SDM workspace.
	 * @throws NullPointerException      if {@code electionEventId} is null.
	 * @throws FailedValidationException if {@code electionEventId} is not valid.
	 */
	public Path resolveElectionEventPath(final String electionEventId) {
		validateUUID(electionEventId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId);
	}

	/**
	 * Provides the offline directory path in the SDM workspace for the given election event.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}/{@value
	 * Constants#CONFIG_DIR_NAME_OFFLINE}.
	 *
	 * @param electionEventId the election event id for which to retrieve the election information directory. Must be non-null and a valid UUID.
	 * @return the offline path in the SDM workspace.
	 * @throws NullPointerException      if {@code electionEventId} is null.
	 * @throws FailedValidationException if {@code electionEventId} is not valid.
	 */
	public Path resolveOfflinePath(final String electionEventId) {
		validateUUID(electionEventId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_OFFLINE);
	}

	/**
	 * Provides the election information directory path in the SDM workspace for the given election event.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}/{@value
	 * Constants#CONFIG_DIR_NAME_ONLINE}/{@value Constants#CONFIG_DIR_NAME_ELECTIONINFORMATION}.
	 *
	 * @param electionEventId the election event id for which to retrieve the election information directory. Must be non-null and a valid UUID.
	 * @return the election information path in the SDM workspace.
	 * @throws NullPointerException      if {@code electionEventId} is null.
	 * @throws FailedValidationException if {@code electionEventId} is not valid.
	 */
	public Path resolveElectionInformationPath(final String electionEventId) {
		validateUUID(electionEventId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION);
	}

	/**
	 * Provides the ballot box directory path in the SDM workspace for the given election event, ballot and ballot box.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}/{@value
	 * Constants#CONFIG_DIR_NAME_ONLINE}/{@value Constants#CONFIG_DIR_NAME_ELECTIONINFORMATION}/{@value Constants#CONFIG_DIR_NAME_BALLOTS}/ {@code
	 * ballotId}/{@value Constants#CONFIG_DIR_NAME_BALLOTBOXES}/{@code ballotBoxId}.
	 *
	 * @param electionEventId the election event the ballot box belongs to. Must be non-null and a valid UUID.
	 * @param ballotId        the ballot the ballot box belongs to. Must be non-null and a valid UUID.
	 * @param ballotBoxId     the expected ballot box. Must be non-null and a valid UUID.
	 * @return the ballot box path in the SDM workspace.
	 * @throws NullPointerException      if any of the inputs is null.
	 * @throws FailedValidationException if any of the inputs is not valid.
	 */
	public Path resolveBallotBoxPath(final String electionEventId, final String ballotId, final String ballotBoxId) {
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION, Constants.CONFIG_DIR_NAME_BALLOTS, ballotId, Constants.CONFIG_DIR_NAME_BALLOTBOXES,
				ballotBoxId);
	}

	/**
	 * Provides the verification card set directory path in the SDM workspace for the given election event and verification card set.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}/{@value
	 * Constants#CONFIG_DIR_NAME_ONLINE}/{@value Constants#CONFIG_DIR_NAME_VOTEVERIFICATION}/{@code verificationCardSetId}.
	 *
	 * @param electionEventId       the election event the ballot box belongs to. Must be non-null and a valid UUID.
	 * @param verificationCardSetId the expected verification card set. Must be non-null and a valid UUID.
	 * @return the verification card set path in the SDM workspace.
	 * @throws NullPointerException      if any of the inputs is null.
	 * @throws FailedValidationException if any of the inputs is not valid.
	 */
	public Path resolveVerificationCardSetPath(final String electionEventId, final String verificationCardSetId) {
		validateUUID(electionEventId);
		validateUUID(verificationCardSetId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_VOTEVERIFICATION, verificationCardSetId);
	}

	/**
	 * Provides the election event printing directory path in the SDM workspace for the given election event.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}/{@value
	 * Constants#CONFIG_DIR_NAME_ONLINE}/{@value Constants#CONFIG_DIR_NAME_PRINTING}.
	 *
	 * @param electionEventId the election event the printing directory belongs to. Must be non-null and a valid UUID.
	 * @return the election event printing path in the SDM workspace.
	 * @throws NullPointerException      if {@code electionEventId} is null.
	 * @throws FailedValidationException if {@code electionEventId} is not valid.
	 */
	public Path resolvePrintingPath(final String electionEventId) {
		validateUUID(electionEventId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_PRINTING);
	}

	/**
	 * Provides the election event customer output directory path in the SDM workspace for the given election event.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}/{@value
	 * Constants#CONFIG_DIR_NAME_CUSTOMER}/{@value Constants#CONFIG_DIR_NAME_OUTPUT}.
	 *
	 * @param electionEventId the election event the customer output directory belongs to. Must be non-null and a valid UUID.
	 * @return the election event customer output path in the SDM workspace.
	 * @throws NullPointerException      if {@code electionEventId} is null.
	 * @throws FailedValidationException if {@code electionEventId} is not valid.
	 */
	public Path resolveOutputPath(final String electionEventId) {
		validateUUID(electionEventId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_CUSTOMER,
				Constants.CONFIG_DIR_NAME_OUTPUT);
	}

	/**
	 * Provides the election event customer input directory path in the SDM workspace for the given election event.
	 * <p>
	 * The path corresponds to the location {@value Constants#CONFIG_FILES_BASE_DIR}/{@code electionEventId}/{@value
	 * Constants#CONFIG_DIR_NAME_CUSTOMER}/{@value Constants#CONFIG_DIR_NAME_INPUT}.
	 *
	 * @param electionEventId the election event the customer input directory belongs to. Must be non-null and a valid UUID.
	 * @return the election event customer input path in the SDM workspace.
	 * @throws NullPointerException      if {@code electionEventId} is null.
	 * @throws FailedValidationException if {@code electionEventId} is not valid.
	 */
	public Path resolveInputPath(final String electionEventId) {
		validateUUID(electionEventId);

		return Paths.get(workspace, Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_CUSTOMER,
				Constants.CONFIG_DIR_NAME_INPUT);
	}

	/**
	 * Provides the integration election event output directory path in the SDM workspace for the given election alias.
	 * <p>
	 * The path corresponds to the location {@value Constants#INTEGRATION_FILES_BASE_DIR}/{@value Constants#INTEGRATION_DIR_NAME_ELECTION_EVENTS}/{@code
	 * electionEventAlias}/{@value Constants#INTEGRATION_DIR_NAME_OUTPUT}.
	 *
	 * @param electionEventAlias the election event the customer output directory belongs to. Must be non-null.
	 * @return the integration election event output path in the SDM workspace.
	 * @throws NullPointerException if {@code electionEventAlias} is null.
	 */
	public Path resolveIntegrationOutputPath(final String electionEventAlias) {
		checkNotNull(electionEventAlias);

		return Paths.get(workspace, Constants.INTEGRATION_FILES_BASE_DIR, Constants.INTEGRATION_DIR_NAME_ELECTION_EVENTS, electionEventAlias,
				Constants.INTEGRATION_DIR_NAME_OUTPUT);
	}

	/**
	 * Provides the integration election event input directory path in the SDM workspace for the given election alias.
	 * <p>
	 * The path corresponds to the location {@value Constants#INTEGRATION_FILES_BASE_DIR}/{@value Constants#INTEGRATION_DIR_NAME_ELECTION_EVENTS}/{@code
	 * electionEventAlias}/{@value Constants#INTEGRATION_DIR_NAME_INPUT}.
	 *
	 * @param electionEventAlias the election event the customer input directory belongs to. Must be non-null.
	 * @return the integration election event input path in the SDM workspace.
	 * @throws NullPointerException if {@code electionEventAlias} is null.
	 */
	public Path resolveIntegrationInputPath(final String electionEventAlias) {
		checkNotNull(electionEventAlias);

		return Paths.get(workspace, Constants.INTEGRATION_FILES_BASE_DIR, Constants.INTEGRATION_DIR_NAME_ELECTION_EVENTS, electionEventAlias,
				Constants.INTEGRATION_DIR_NAME_INPUT);
	}

}
