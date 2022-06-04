/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.newDirectoryStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.verificationcardset.VerificationCardSetUploadRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetUploadRepository;

/**
 * Service which will upload the information related to the voting card set
 */
@Service
public class VotingCardSetUploadService {
	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetUploadService.class);

	private static final String NULL_ELECTION_EVENT_ID = "";
	private static final String CONSTANT_VOTEVERIFICATIONCONTEXTDATA = "voteVerificationContextData";

	private final VotingCardSetRepository votingCardSetRepository;
	private final VotingCardSetUploadRepository votingCardSetUploadRepository;
	private final VerificationCardSetUploadRepository verificationCardSetUploadRepository;
	private final ElectionEventRepository electionEventRepository;
	private final ExtendedAuthenticationUploadService extendedAuthenticationUploadService;
	private final PathResolver pathResolver;
	private final SignatureService signatureService;

	public VotingCardSetUploadService(final VotingCardSetRepository votingCardSetRepository,
			final VotingCardSetUploadRepository votingCardSetUploadRepository,
			final VerificationCardSetUploadRepository verificationCardSetUploadRepository, final ElectionEventRepository electionEventRepository,
			final ExtendedAuthenticationUploadService extendedAuthenticationUploadService, final PathResolver pathResolver,
			final SignatureService signatureService) {
		this.votingCardSetRepository = votingCardSetRepository;
		this.votingCardSetUploadRepository = votingCardSetUploadRepository;
		this.verificationCardSetUploadRepository = verificationCardSetUploadRepository;
		this.electionEventRepository = electionEventRepository;
		this.extendedAuthenticationUploadService = extendedAuthenticationUploadService;
		this.pathResolver = pathResolver;
		this.signatureService = signatureService;
	}

	/**
	 * Uploads the available ballots and ballot texts to the voter portal.
	 */
	public void uploadSynchronizableVotingCardSets(final String electionEvent) {

		final Map<String, Object> votingCardSetsParams = new HashMap<>();
		addSigned(votingCardSetsParams);
		addPendingToSynchronize(votingCardSetsParams);
		addElectionEventIdIfNotNull(electionEvent, votingCardSetsParams);

		final String votingCardSetDocuments = votingCardSetRepository.list(votingCardSetsParams);

		final JsonArray votingCardSets = JsonUtils.getJsonObject(votingCardSetDocuments).getJsonArray(JsonConstants.RESULT);

		for (int i = 0; i < votingCardSets.size(); i++) {

			final JsonObject votingCardSetInArray = votingCardSets.getJsonObject(i);
			final String electionEventId = votingCardSetInArray.getJsonObject(JsonConstants.ELECTION_EVENT).getString(JsonConstants.ID);

			final JsonObject eEvent = JsonUtils.getJsonObject(electionEventRepository.find(electionEventId));
			final JsonObject adminBoard = eEvent.getJsonObject(JsonConstants.ADMINISTRATION_AUTHORITY);

			final String adminBoardId = adminBoard.getString(JsonConstants.ID);

			final String votingCardSetId = votingCardSetInArray.getString(JsonConstants.ID);
			final String verificationCardSetId = votingCardSetInArray.getString(JsonConstants.VERIFICATION_CARD_SET_ID);

			final Path voterMaterialPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL).resolve(votingCardSetId);

			final Path voteVerificationPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION).resolve(verificationCardSetId);

			final Path extendedAuthenticationPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_FILE_EXTENDED_AUTHENTICATION_DATA).resolve(votingCardSetId);

			final JsonObjectBuilder builder = Json.createObjectBuilder();
			try {
				LOGGER.info("Uploading voter material configuration");
				uploadVoterMaterialContents(voterMaterialPath, electionEventId, votingCardSetId, adminBoardId);
				LOGGER.info("Uploading vote verification configuration");
				uploadVoteVerificationContents(voteVerificationPath, electionEventId, verificationCardSetId, adminBoardId);
				LOGGER.info("Uploading extended authentication");
				extendedAuthenticationUploadService.uploadExtendedAuthenticationFiles(extendedAuthenticationPath, electionEventId, adminBoardId);

				builder.add(JsonConstants.ID, votingCardSetId);
				builder.add(JsonConstants.SYNCHRONIZED, SynchronizeStatus.SYNCHRONIZED.getIsSynchronized().toString());
				builder.add(JsonConstants.DETAILS, SynchronizeStatus.SYNCHRONIZED.getStatus());
				LOGGER.info("The voting card and verification card sets where successfully uploaded");
			} catch (final IOException e) {
				LOGGER.error("An error occurred while uploading the signed voting card set and verification card set", e);
				builder.add(JsonConstants.ID, votingCardSetId);
				builder.add(JsonConstants.DETAILS, SynchronizeStatus.FAILED.getStatus());
			}
			LOGGER.info("Changing the state of the voting card set");
			votingCardSetRepository.update(builder.build().toString());
		}
	}

	private void addElectionEventIdIfNotNull(final String electionEvent, final Map<String, Object> votingCardSetsParams) {
		if (!NULL_ELECTION_EVENT_ID.equals(electionEvent)) {
			votingCardSetsParams.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEvent);
		}
	}

	private void addPendingToSynchronize(final Map<String, Object> votingCardSetsParams) {
		votingCardSetsParams.put(JsonConstants.SYNCHRONIZED, SynchronizeStatus.PENDING.getIsSynchronized().toString());
	}

	private void addSigned(final Map<String, Object> votingCardSetsParams) {
		votingCardSetsParams.put(JsonConstants.STATUS, Status.SIGNED.name());
	}

	private void uploadVoterMaterialContents(final Path voterMaterialPath, final String electionEventId, final String votingCardSetId,
			final String adminBoardId) throws IOException {
		try (final DirectoryStream<Path> files = newDirectoryStream(voterMaterialPath, Constants.CSV_GLOB)) {
			for (final Path file : files) {
				final String name = file.getFileName().toString();
				if (name.startsWith(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION)) {
					LOGGER.info("Uploading voter information file {} and its signature", name);
					uploadVoterInformationFromCSV(electionEventId, votingCardSetId, adminBoardId, file);
				} else if (name.startsWith(Constants.CONFIG_FILE_NAME_CREDENTIAL_DATA)) {
					LOGGER.info("Uploading credential data file {} and its signature", name);
					uploadCredentialDataFromCSV(electionEventId, votingCardSetId, adminBoardId, file);
				}
			}
		}
	}

	private void uploadVoteVerificationContents(final Path voteVerificationPath, final String electionEventId, final String verificationCardSetId,
			final String adminBoardId) throws IOException {
		try (final DirectoryStream<Path> files = newDirectoryStream(voteVerificationPath, Constants.CSV_GLOB)) {
			for (final Path file : files) {
				final String name = file.getFileName().toString();
				if (name.startsWith(Constants.CONFIG_FILE_NAME_CODES_MAPPING)) {
					LOGGER.info("Uploading codes mapping file {} and its signature", name);
					uploadCodesMappingFromCSV(electionEventId, verificationCardSetId, adminBoardId, file);
				} else if (name.startsWith(Constants.CONFIG_FILE_VERIFICATION_CARD_DATA)) {
					LOGGER.info("Uploading verification card data file {} and its signature", name);
					uploadVerificationCardDataFromCSV(electionEventId, verificationCardSetId, adminBoardId, file);
				}
			}
		}
		uploadVerificationCardSetDataFromJSON(electionEventId, verificationCardSetId, adminBoardId, voteVerificationPath);
	}

	private void uploadVoterInformationFromCSV(final String electionEventId, final String votingCardSetId, final String adminBoardId,
			final Path filePath) throws IOException {

		try (final InputStream stream = signatureService.newCSVAndSignatureInputStream(filePath)) {

			votingCardSetUploadRepository.uploadVoterInformation(electionEventId, votingCardSetId, adminBoardId, stream);

		}
	}

	private void uploadCredentialDataFromCSV(final String electionEventId, final String votingCardSetId, final String adminBoardId,
			final Path filePath) throws IOException {

		try (final InputStream stream = signatureService.newCSVAndSignatureInputStream(filePath)) {

			votingCardSetUploadRepository.uploadCredentialData(electionEventId, votingCardSetId, adminBoardId, stream);

		}
	}

	private void uploadCodesMappingFromCSV(final String electionEventId, final String verificationCardSetId, final String adminBoardId,
			final Path filePath) throws IOException {

		try (final InputStream stream = signatureService.newCSVAndSignatureInputStream(filePath)) {

			verificationCardSetUploadRepository.uploadCodesMapping(electionEventId, verificationCardSetId, adminBoardId, stream);

		}
	}

	private void uploadVerificationCardDataFromCSV(final String electionEventId, final String verificationCardSetId, final String adminBoardId,
			final Path filePath) throws IOException {

		try (final InputStream stream = signatureService.newCSVAndSignatureInputStream(filePath)) {

			verificationCardSetUploadRepository.uploadVerificationCardData(electionEventId, verificationCardSetId, adminBoardId, stream);

		}
	}

	private void uploadVerificationCardSetDataFromJSON(final String electionEventId, final String verificationCardSetId, final String adminBoardId,
			final Path filePath) throws IOException {
		final Path verificationContextPath = filePath.resolve(Constants.CONFIG_FILE_NAME_SIGNED_VERIFICATION_CONTEXT_DATA);
		final JsonObject verificationContextData = JsonUtils.getJsonObject(
				new String(Files.readAllBytes(verificationContextPath), StandardCharsets.UTF_8));
		final JsonObject jsonInput = Json.createObjectBuilder().add(CONSTANT_VOTEVERIFICATIONCONTEXTDATA, verificationContextData.toString()).build();

		LOGGER.info("Uploading verification context configuration");

		verificationCardSetUploadRepository.uploadVerificationCardSetData(electionEventId, verificationCardSetId, adminBoardId, jsonInput);
	}
}
