/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationInput;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.cryptoadapters.CryptoAdapters;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.commons.VerificationCardSet;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenVerDatOutput;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenVerDatService;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * Service that deals with the pre-computation of voting card sets.
 * <p>
 * The service invokes the GenVerDat algorithm described in the cryptographic protocol.
 */
@Service
public class VotingCardSetPrecomputationService extends BaseVotingCardSetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetPrecomputationService.class);

	private final BallotService ballotService;
	private final GenVerDatService genVerDatService;
	private final AdminBoardService adminBoardService;
	private final IdleStatusService idleStatusService;
	private final BallotBoxRepository ballotBoxRepository;
	private final EncryptionParametersService encryptionParametersService;
	private final CryptolibPayloadSignatureService payloadSignatureService;
	private final ConfigurationEntityStatusService configurationEntityStatusService;
	private final ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository;

	@Value("${tenantID}")
	private String tenantId;

	@Value("${choiceCodeGenerationChunkSize:100}")
	private int chunkSize;

	private ElGamalMultiRecipientPublicKey setupPublicKey;

	@Autowired
	public VotingCardSetPrecomputationService(
			final BallotService ballotService,
			final GenVerDatService genVerDatService,
			final AdminBoardService adminBoardService,
			final IdleStatusService idleStatusService,
			final BallotBoxRepository ballotBoxRepository,
			final EncryptionParametersService encryptionParametersService,
			final CryptolibPayloadSignatureService payloadSignatureService,
			final ConfigurationEntityStatusService configurationEntityStatusService,
			final ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository) {

		this.ballotService = ballotService;
		this.genVerDatService = genVerDatService;
		this.adminBoardService = adminBoardService;
		this.idleStatusService = idleStatusService;
		this.ballotBoxRepository = ballotBoxRepository;
		this.payloadSignatureService = payloadSignatureService;
		this.encryptionParametersService = encryptionParametersService;
		this.configurationEntityStatusService = configurationEntityStatusService;
		this.returnCodeGenerationRequestPayloadRepository = returnCodeGenerationRequestPayloadRepository;
	}

	/**
	 * Pre-compute a voting card set.
	 *
	 * @throws InvalidStatusTransitionException If the original status does not allow pre-computing
	 * @throws GeneralCryptoLibException        If an error occurs while processing operations with the cryptolib.
	 * @throws PayloadStorageException          If an error occurs while storing the payload.
	 * @throws PayloadSignatureException        If an error occurs while signing the payload.
	 */
	public void precompute(final String votingCardSetId, final String electionEventId, final String administrationBoardPrivateKeyPEM,
			final String adminBoardId)
			throws ResourceNotFoundException, InvalidStatusTransitionException, GeneralCryptoLibException, PayloadStorageException,
			PayloadSignatureException {

		validateUUID(votingCardSetId);
		validateUUID(electionEventId);

		if (!idleStatusService.getIdLock(votingCardSetId)) {
			return;
		}

		// Construct the matching verification card set context.
		final String verificationCardSetId = votingCardSetRepository.getVerificationCardSetId(votingCardSetId);
		final String ballotBoxId = votingCardSetRepository.getBallotBoxId(votingCardSetId);

		final VerificationCardSet precomputeContext = new VerificationCardSet(electionEventId, ballotBoxId, votingCardSetId, verificationCardSetId,
				adminBoardId
		);

		try {
			performPrecompute(precomputeContext, administrationBoardPrivateKeyPEM);
		} finally {
			idleStatusService.freeIdLock(votingCardSetId);
		}
	}

	private void performPrecompute(final VerificationCardSet precomputeContext, final String administrationBoardPrivateKeyPEM)
			throws ResourceNotFoundException, InvalidStatusTransitionException, PayloadStorageException, PayloadSignatureException {

		LOGGER.info("Starting pre-computation of voting card set {}...", precomputeContext.getVotingCardSetId());

		final Status fromStatus = Status.LOCKED;
		final Status toStatus = Status.PRECOMPUTED;

		checkVotingCardSetStatusTransition(precomputeContext.getElectionEventId(), precomputeContext.getVotingCardSetId(), fromStatus, toStatus);

		final int numberOfVotingCardsToGenerate = votingCardSetRepository.getNumberOfVotingCards(precomputeContext.getElectionEventId(),
				precomputeContext.getVotingCardSetId());

		LOGGER.info("Generating {} voting cards for votingCardSetId {}...", numberOfVotingCardsToGenerate, precomputeContext.getVotingCardSetId());

		// Get the setup public key.
		setupPublicKey = getSetupPublicKey(precomputeContext);
		final GqGroup electionGqGroup = setupPublicKey.getGroup();

		returnCodeGenerationRequestPayloadRepository.remove(precomputeContext.getElectionEventId(), precomputeContext.getVerificationCardSetId());

		// Retrieve the prime numbers representing the voting options.
		final List<GqElement> encodedVotingOptions = getEncodedVotingOptionsForBallotBox(precomputeContext, electionGqGroup);

		// Build payloads to request the return code generation (choice return codes and vote cast return code) from the control components.

		// Build full-sized chunks (i.e. with `chunkSize` elements)
		final int fullChunkCount = numberOfVotingCardsToGenerate / chunkSize;
		for (int i = 0; i < fullChunkCount; i++) {
			// Generate verification data for the chunk.
			generateRequestPayload(precomputeContext, encodedVotingOptions, i, chunkSize, administrationBoardPrivateKeyPEM);
		}

		// Build an eventual last chunk with the remaining elements.
		final int lastChunkSize = numberOfVotingCardsToGenerate % chunkSize;
		if (lastChunkSize > 0) {
			generateRequestPayload(precomputeContext, encodedVotingOptions, fullChunkCount, lastChunkSize, administrationBoardPrivateKeyPEM);
		}

		LOGGER.info("Generation of {} voting cards for votingCardSetId {} successful.", numberOfVotingCardsToGenerate,
				precomputeContext.getVotingCardSetId());

		// Update the voting card set status to 'pre-computed'.
		configurationEntityStatusService.update(toStatus.name(), precomputeContext.getVotingCardSetId(), votingCardSetRepository);
	}

	/**
	 * Generates the verification data with genVerData and creates the ReturnCodeGenerationRequestPayload.
	 */
	private void generateRequestPayload(final VerificationCardSet precomputeContext, final List<GqElement> encodedVotingOptions, final int chunkId,
			final int chunkSize, final String administrationBoardPrivateKeyPEM)
			throws PayloadSignatureException, PayloadStorageException {

		// Generate verification data. Since we chunk the payloads, we are not directly working with the number of eligible voters (N_E),
		// but rather with the chunk size as input of the algorithm.
		LOGGER.debug("Generating verification data for verificationCardSet {} and chunk {}...", precomputeContext.getVerificationCardSetId(),
				chunkId);
		final GenVerDatOutput genVerDatOutput = genVerDatService.genVerDat(chunkSize, encodedVotingOptions, precomputeContext, setupPublicKey);

		// Persist casting keys and verification card key pairs.
		persistBallotCastingKeys(precomputeContext, genVerDatOutput);
		persistVerificationCardKeyPairs(precomputeContext, genVerDatOutput);

		LOGGER.debug("Generation of verification data for verificationCardSet {} and chunk {} successful. Creating payload...",
				precomputeContext.getVerificationCardSetId(),
				chunkId);

		// Create the payload.
		final ReturnCodeGenerationRequestPayload payload = createRequestPayload(precomputeContext, chunkId, genVerDatOutput,
				administrationBoardPrivateKeyPEM);

		// Store the payload.
		returnCodeGenerationRequestPayloadRepository.store(payload);
	}

	private ReturnCodeGenerationRequestPayload createRequestPayload(final VerificationCardSet precomputeContext, final int chunkId,
			final GenVerDatOutput genVerDatOutput, final String administrationBoardPrivateKeyPEM) throws PayloadSignatureException {

		// Create payload.
		final List<ReturnCodeGenerationInput> returnCodeGenerationInputList = new ArrayList<>();
		for (int j = 0; j < genVerDatOutput.size(); j++) {
			final String verificationCardId = genVerDatOutput.getVerificationCardIds().get(j);
			final ElGamalMultiRecipientCiphertext encryptedHashedConfirmationKey = genVerDatOutput.getEncryptedHashedConfirmationKeys().get(j);
			final ElGamalMultiRecipientCiphertext encryptedHashedPartialChoiceReturnCodes = genVerDatOutput.getEncryptedHashedPartialChoiceReturnCodes()
					.get(j);
			final ElGamalMultiRecipientPublicKey verificationCardPublicKey = genVerDatOutput.getVerificationCardKeyPairs().get(j).getPublicKey();

			returnCodeGenerationInputList
					.add(new ReturnCodeGenerationInput(verificationCardId, encryptedHashedConfirmationKey, encryptedHashedPartialChoiceReturnCodes,
							verificationCardPublicKey));
		}
		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(getBallot(precomputeContext));
		final ReturnCodeGenerationRequestPayload payload = new ReturnCodeGenerationRequestPayload(tenantId, precomputeContext.getElectionEventId(),
				precomputeContext.getVerificationCardSetId(), genVerDatOutput.getPartialChoiceReturnCodesAllowList(), chunkId,
				genVerDatOutput.getGroup(), returnCodeGenerationInputList, combinedCorrectnessInformation);

		// Sign the payload.
		LOGGER.debug("Signing payload for verificationCardSet {} and chunkId {}...", precomputeContext.getVerificationCardSetId(), chunkId);
		try {
			signPayload(precomputeContext, payload, administrationBoardPrivateKeyPEM);
		} catch (final CertificateManagementException | PayloadSignatureException e) {
			throw new PayloadSignatureException(e);
		}

		LOGGER.debug("Payload successfully created and signed for verificationCardSet {} and chunkId {}.",
				precomputeContext.getVerificationCardSetId(), chunkId);

		return payload;
	}

	/**
	 * Signs a return code generation request payload.
	 *
	 * @param precomputeContext the request context.
	 * @param payload           the payload to sign.
	 * @throws PayloadSignatureException      If an error occurs while getting the admin board's signing key.
	 * @throws CertificateManagementException If an error occurs while getting the admin board's certificate chain.
	 */
	private void signPayload(final VerificationCardSet precomputeContext, final ReturnCodeGenerationRequestPayload payload,
			final String administrationBoardPrivateKeyPEM)
			throws PayloadSignatureException, CertificateManagementException {
		// Get the admin board's signing key.
		final PrivateKey signingKey;
		try {
			signingKey = PemUtils.privateKeyFromPem(administrationBoardPrivateKeyPEM);
		} catch (final GeneralCryptoLibException e) {
			throw new PayloadSignatureException(e);
		}

		// Get the admin board's certificate chain.
		final X509Certificate[] certificateChain = adminBoardService.getCertificateChain(precomputeContext.getAdminBoardId());

		// Hash and sign the payload.
		payloadSignatureService.sign(payload, signingKey, certificateChain);
	}

	private void persistVerificationCardKeyPairs(final VerificationCardSet precomputeContext, final GenVerDatOutput genVerDatOutput) {

		final Path verificationCardSetKeyPairsPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR)
				.resolve(precomputeContext.getElectionEventId())
				.resolve(Constants.CONFIG_DIR_NAME_OFFLINE).resolve(Constants.CONFIG_VERIFICATION_CARDS_KEY_PAIR_DIRECTORY)
				.resolve(precomputeContext.getVerificationCardSetId());
		try {
			Files.createDirectories(verificationCardSetKeyPairsPath);
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Failed to create all directories along the path %s.", verificationCardSetKeyPairsPath), e);
		}

		for (int i = 0; i < genVerDatOutput.size(); i++) {
			final String verificationCardId = genVerDatOutput.getVerificationCardIds().get(i);
			final ElGamalMultiRecipientKeyPair keyPair = genVerDatOutput.getVerificationCardKeyPairs().get(i);
			final ElGamalMultiRecipientPublicKey publicKey = keyPair.getPublicKey();
			final ElGamalMultiRecipientPrivateKey privateKey = keyPair.getPrivateKey();
			final GqGroup gqGroup = keyPair.getGroup();

			final String verificationCardSecretKey;
			final String verificationCardPublicKey;

			// Convert keys to cryptolib.
			final ElGamalPublicKey elGamalPublicKey = CryptoAdapters.convert(publicKey);
			final ElGamalPrivateKey elGamalPrivateKey = CryptoAdapters.convert(privateKey, gqGroup);
			try {
				verificationCardSecretKey = elGamalPrivateKey.toJson();
				verificationCardPublicKey = elGamalPublicKey.toJson();
			} catch (final GeneralCryptoLibException e) {
				throw new IllegalArgumentException("Failed to convert keys to json.", e);
			}

			final Path verificationCardKeyPairFilePath = verificationCardSetKeyPairsPath.resolve(verificationCardId + Constants.KEY);

			try {
				FileUtils.writeByteArrayToFile(verificationCardKeyPairFilePath.toFile(),
						(verificationCardSecretKey + System.lineSeparator() + verificationCardPublicKey).getBytes(StandardCharsets.UTF_8));
			} catch (final IOException e) {
				throw new IllegalStateException("Error saving verification cards key pairs", e);
			}
		}
	}

	@VisibleForTesting
	void persistBallotCastingKeys(final VerificationCardSet precomputeContext, final GenVerDatOutput genVerDatOutput) {
		LOGGER.info("Persisting the ballot casting keys for for the electionEventId {} and verificationCardSetId {}.",
				precomputeContext.getElectionEventId(),
				precomputeContext.getVerificationCardSetId());

		final Path ballotCastingKeysDirPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(precomputeContext.getElectionEventId())
				.resolve(Constants.CONFIG_DIR_NAME_OFFLINE).resolve(Constants.CONFIG_BALLOT_CASTING_KEYS_DIRECTORY)
				.resolve(precomputeContext.getVerificationCardSetId());

		final File ballotCastingKeyPairsFile = ballotCastingKeysDirPath.toFile();

		if (!ballotCastingKeyPairsFile.exists() && !ballotCastingKeyPairsFile.mkdirs()) {
			throw new IllegalStateException(String.format(
					"An error occurred while creating the directory for saving the ballot casting keys for electionEventId %s and verificationCardSetId %s",
					precomputeContext.getElectionEventId(), precomputeContext.getVerificationCardSetId()));
		}

		for (int i = 0; i < genVerDatOutput.size(); i++) {
			final String verificationCardId = genVerDatOutput.getVerificationCardIds().get(i);
			final String BCK = genVerDatOutput.getBallotCastingKeys().get(i);
			try {
				Files.write(ballotCastingKeysDirPath.resolve(verificationCardId + Constants.KEY), BCK.getBytes(StandardCharsets.UTF_8));
			} catch (final IOException e) {
				throw new IllegalStateException(String.format(
						"An error occurred while saving ballot casting key for electionEventId %s, verificationCardSetId %s and verificationCardId %s.",
						precomputeContext.getElectionEventId(), precomputeContext.getVerificationCardSetId(), verificationCardId), e);
			}
		}
	}

	private Ballot getBallot(final VerificationCardSet precomputeContext) {
		final String ballotId = ballotBoxRepository.getBallotId(precomputeContext.getBallotBoxId());
		return ballotService.getBallot(precomputeContext.getElectionEventId(), ballotId);
	}

	/**
	 * Gets a ballot box's encoded voting options (prime numbers) as {@link GqElement}s.
	 *
	 * @param precomputeContext the request context.
	 * @param group             the election event group.
	 * @return a stream of encrypted representations
	 */
	private List<GqElement> getEncodedVotingOptionsForBallotBox(final VerificationCardSet precomputeContext, final GqGroup group) {
		return getBallot(precomputeContext).getEncodedVotingOptions().stream().map(value -> GqElementFactory.fromValue(value, group))
				.collect(Collectors.toList());
	}

	/**
	 * Read the setup secret key from the file system and derive the setup public key from it. To reconstruct the secret key, the encryption
	 * parameters are first read from the file system.
	 *
	 * @return the setup public key.
	 */
	private ElGamalMultiRecipientPublicKey getSetupPublicKey(final VerificationCardSet precomputeContext) {
		final GqGroup gqGroup = encryptionParametersService.load(precomputeContext.getElectionEventId());

		try {
			// Read the setup secret key from file system.
			final ElGamalMultiRecipientPrivateKey setupSecretKey = objectMapper.reader().withAttribute("group", gqGroup).readValue(pathResolver
					.resolve(Constants.CONFIG_FILES_BASE_DIR, precomputeContext.getElectionEventId(), Constants.CONFIG_DIR_NAME_OFFLINE,
							Constants.SETUP_SECRET_KEY_FILE_NAME).toFile(), ElGamalMultiRecipientPrivateKey.class);

			// Create key pair from the setup secret key and retrieve corresponding setup public key.
			return ElGamalMultiRecipientKeyPair.from(setupSecretKey, gqGroup.getGenerator()).getPublicKey();
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to deserialize setup secret key.", e);
		}
	}

}

