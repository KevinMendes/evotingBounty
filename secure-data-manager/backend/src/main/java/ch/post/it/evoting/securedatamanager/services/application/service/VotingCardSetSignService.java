/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.function.BooleanSupplier;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.SignedObject;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * This is an application service that manages voting card sets.
 */
@Service
public class VotingCardSetSignService extends BaseVotingCardSetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetSignService.class);
	private static final String TWO_PARAMETERS_LOGGER_STRING = "{} {}";

	private final SignatureService signatureService;
	private final ExtendedAuthenticationService extendedAuthenticationService;
	private final ConfigurationEntityStatusService configurationEntityStatusService;

	public VotingCardSetSignService(
			final SignatureService signatureService,
			final ExtendedAuthenticationService extendedAuthenticationService,
			final ConfigurationEntityStatusService configurationEntityStatusService) {
		this.signatureService = signatureService;
		this.extendedAuthenticationService = extendedAuthenticationService;
		this.configurationEntityStatusService = configurationEntityStatusService;
	}

	/**
	 * Change the state of the voting card set from generated to SIGNED for a given election event and voting card set id.
	 *
	 * @param electionEventId the election event id.
	 * @param votingCardSetId the voting card set id.
	 * @param privateKeyPEM   PEM file containing the private key.
	 * @return true if the status is successfully changed to signed. Otherwise, false.
	 * @throws ResourceNotFoundException if the voting card set is not found.
	 */
	public boolean sign(final String electionEventId, final String votingCardSetId, final String privateKeyPEM)
			throws ResourceNotFoundException, GeneralCryptoLibException, IOException {

		boolean result = false;

		final JsonObject votingCardSetJson = votingCardSetRepository.getVotingCardSetJson(electionEventId, votingCardSetId);

		if (votingCardSetJson != null && votingCardSetJson.containsKey(JsonConstants.STATUS)) {
			final String status = votingCardSetJson.getString(JsonConstants.STATUS);
			if (Status.GENERATED.name().equals(status)) {

				final PrivateKey privateKey = PemUtils.privateKeyFromPem(privateKeyPEM);

				final String verificationCardSetId = votingCardSetJson.getString(JsonConstants.VERIFICATION_CARD_SET_ID);

				LOGGER.info("Signing voting card set {}", votingCardSetId);
				LOGGER.info("Signing voter material configuration");
				signVoterMaterial(electionEventId, votingCardSetId, privateKey);
				LOGGER.info("Signing verification card set {}", verificationCardSetId);
				LOGGER.info("Signing vote verification configuration");
				signVoteVerification(electionEventId, verificationCardSetId, votingCardSetId, privateKey);
				LOGGER.info("Signing the extended authentication");
				signExtendedAuthentication(electionEventId, votingCardSetId, verificationCardSetId, privateKey);
				LOGGER.info("Signing the printing");
				signPrinting(electionEventId, votingCardSetId, privateKey);
				LOGGER.info("Changing the status of the voting card set");
				configurationEntityStatusService
						.updateWithSynchronizedStatus(Status.SIGNED.name(), votingCardSetId, votingCardSetRepository, SynchronizeStatus.PENDING);
				result = true;
			}
		}

		return result;
	}

	private static boolean evaluateUploadResult(final BooleanSupplier arg) {
		return arg.getAsBoolean();
	}

	private static void signJSONs(final PrivateKey privateKey, final Path voteVerificationPath) throws IOException {

		final ConfigObjectMapper mapper = new ConfigObjectMapper();

		final Path verificationContextPath = voteVerificationPath.resolve(Constants.CONFIG_FILE_NAME_VERIFICATION_CONTEXT_DATA);

		final Path signedVerificationContextPath = voteVerificationPath.resolve(Constants.CONFIG_FILE_NAME_SIGNED_VERIFICATION_CONTEXT_DATA);

		final VoteVerificationContextData voteVerificationContextData = mapper
				.fromJSONFileToJava(new File(verificationContextPath.toString()), VoteVerificationContextData.class);

		LOGGER.info("Signing vote verification context data");

		final String signedVoteVerificationContextData = JsonSignatureService.sign(privateKey, voteVerificationContextData);
		final SignedObject signedSignedVoteVerificationContextDataObject = new SignedObject();
		signedSignedVoteVerificationContextDataObject.setSignature(signedVoteVerificationContextData);
		mapper.fromJavaToJSONFile(signedSignedVoteVerificationContextDataObject, new File(signedVerificationContextPath.toString()));
	}

	private void signVoteVerification(final String electionEventId, final String verificationCardSetId, final String votingCardSetId,
			final PrivateKey privateKey) throws IOException {

		final Path voteVerificationPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
				.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION).resolve(verificationCardSetId);

		final boolean correctSigning = signAllVoteVerificationCSVFiles(privateKey, voteVerificationPath);

		if (!correctSigning) {
			LOGGER.error("An error occurred while signing the verification card set, rolling back to its original state");
			signatureService.deleteSignaturesFromCSVs(voteVerificationPath);

			final Path voterMaterialPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL).resolve(votingCardSetId);

			signatureService.deleteSignaturesFromCSVs(voterMaterialPath);
		} else {
			signJSONs(privateKey, voteVerificationPath);
		}

	}

	private void signExtendedAuthentication(final String electionEventId, final String votingCardSetId, final String verificationCardSetId,
			final PrivateKey privateKey) throws IOException {

		final boolean signatureResult = extendedAuthenticationService.signExtendedAuthentication(electionEventId, votingCardSetId, privateKey);

		if (!signatureResult) {

			LOGGER.error("An error occurred while signing the extended authentication , rolling back to its original state");
			final Path voteVerificationPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION).resolve(verificationCardSetId);
			signatureService.deleteSignaturesFromCSVs(voteVerificationPath);

			final Path voterMaterialPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL).resolve(votingCardSetId);

			signatureService.deleteSignaturesFromCSVs(voterMaterialPath);
		}

	}

	private boolean signAllVoteVerificationCSVFiles(final PrivateKey privateKey, final Path voteVerificationPath) {
		return evaluateUploadResult(() -> {
			final long numberFailed;
			try {
				numberFailed = Files.walk(voteVerificationPath, 1).filter(csvPath -> {
					boolean failed = false;
					try {
						final String name = csvPath.getFileName().toString();
						if ((name.startsWith(Constants.CONFIG_FILE_NAME_CODES_MAPPING) && name.endsWith(Constants.CSV)) || (
								name.startsWith(Constants.CONFIG_FILE_VERIFICATION_CARD_DATA) && name.endsWith(Constants.CSV))) {

							LOGGER.info(TWO_PARAMETERS_LOGGER_STRING, Constants.SIGNING_FILE, name);
							final String signatureB64 = signatureService.signCSV(privateKey, csvPath.toFile());
							LOGGER.info(Constants.SAVING_SIGNATURE);
							signatureService.saveCSVSignature(signatureB64, csvPath);
						}
					} catch (final IOException | GeneralCryptoLibException e) {
						LOGGER.warn("Error trying to sign All Vote Verification CSV Files.", e);
						failed = true;
					}
					return failed;
				}).count();
			} catch (final IOException e) {
				LOGGER.warn("Error trying to sign All Vote Verification CSV Files.", e);
				return Boolean.FALSE;
			}
			return numberFailed == 0;
		});
	}

	private void signVoterMaterial(final String electionEventId, final String votingCardSetId, final PrivateKey privateKey) throws IOException {

		final Path voterMaterialPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
				.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL).resolve(votingCardSetId);

		final boolean correctSigning = signAllVoterMaterialCSVFiles(privateKey, voterMaterialPath);

		if (!correctSigning) {
			LOGGER.error("An error occurred while signing the voting card set, rolling back to its original state");
			signatureService.deleteSignaturesFromCSVs(voterMaterialPath);
		}

	}

	private boolean signAllVoterMaterialCSVFiles(final PrivateKey privateKey, final Path voterMaterialPath) {
		return evaluateUploadResult(() -> {
			final long numberFailed;
			try {
				numberFailed = Files.walk(voterMaterialPath, 1).filter(csvPath -> {
					boolean failed = false;
					try {
						final String name = csvPath.getFileName().toString();
						if ((name.startsWith(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION) && name.endsWith(Constants.CSV)) || (
								name.startsWith(Constants.CONFIG_FILE_NAME_CREDENTIAL_DATA) && name.endsWith(Constants.CSV))) {

							LOGGER.info(TWO_PARAMETERS_LOGGER_STRING, Constants.SIGNING_FILE, name);
							final String signatureB64 = signatureService.signCSV(privateKey, csvPath.toFile());
							LOGGER.info(Constants.SAVING_SIGNATURE);
							signatureService.saveCSVSignature(signatureB64, csvPath);
						}
					} catch (final IOException | GeneralCryptoLibException e) {
						LOGGER.warn("Error trying to sign All Vote Material CSV Files.", e);
						failed = true;
					}
					return failed;
				}).count();
			} catch (final IOException e) {
				LOGGER.warn("Error trying to sign All Vote Material CSV Files.", e);
				return Boolean.FALSE;
			}
			return numberFailed == 0;
		});
	}

	private void signPrinting(final String electionEventId, final String votingCardSetId, final PrivateKey privateKey) throws IOException {

		final Path printingPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
				.resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_PRINTING).resolve(votingCardSetId);

		final boolean correctSigning = signAllPrintingDataCSVFiles(privateKey, printingPath);

		if (!correctSigning) {
			LOGGER.error("An error occurred while signing the printing data, rolling back to its original state");
			signatureService.deleteSignaturesFromCSVs(printingPath);
		}

	}

	private boolean signAllPrintingDataCSVFiles(final PrivateKey privateKey, final Path voteVerificationPath) {
		return evaluateUploadResult(() -> {
			final long numberFailed;
			try {
				numberFailed = Files.walk(voteVerificationPath, 1).filter(csvPath -> {
					boolean failed = false;
					try {
						final String name = csvPath.getFileName().toString();
						if ((name.startsWith(Constants.PRINTING_DATA) && name.endsWith(Constants.CSV))) {
							LOGGER.info(TWO_PARAMETERS_LOGGER_STRING, Constants.SIGNING_FILE, name);
							final String signatureB64 = signatureService.signCSV(privateKey, csvPath.toFile());
							LOGGER.info(Constants.SAVING_SIGNATURE);
							signatureService.saveCSVSignature(signatureB64, csvPath);
						}
					} catch (final IOException | GeneralCryptoLibException e) {
						LOGGER.warn("Error trying to sign All Printing Data CSV Files.", e);
						failed = true;
					}
					return failed;
				}).count();
			} catch (final IOException e) {
				LOGGER.warn("Error trying to sign All Printing Data CSV Files.", e);
				return Boolean.FALSE;
			}
			return numberFailed == 0;
		});
	}
}
