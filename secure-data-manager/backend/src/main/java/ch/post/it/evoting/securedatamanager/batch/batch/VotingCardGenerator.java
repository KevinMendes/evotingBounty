/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_ERROR_DERIVING_CREDENTIAL_ID;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_SUCCESS_CREDENTIAL_ID_DERIVED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENSVPK_ERROR_DERIVING_KEYSTORE_SYMMETRIC_ENCRYPTION_KEY;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENSVPK_ERROR_GENERATING_SVK;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENSVPK_ERROR_GENERATING_VCID;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENSVPK_SUCCESS_KEYSTORE_SYMMETRIC_ENCRYPTION_KEY_DERIVED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENSVPK_SUCCESS_SVK_GENERATED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENSVPK_SUCCESS_VCIDS_GENERATED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCC_ERROR_GENERATING_LONGVOTECASTCODE;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCC_ERROR_STORING_VOTECASTCODE;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCC_SUCCESS_LONGCHOICECODES_GENERATED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCC_SUCCESS_LONGVOTECASTCODE_GENERATED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCC_SUCCESS_PRE_CHOICECODES_GENERATED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCC_SUCCESS_VOTECASTCODE_STORED;
import static java.util.Arrays.fill;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.returncode.CodesMappingTableEntry;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesService;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.cryptoadapters.CryptoAdapters;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.batch.batch.exceptions.GenerateCredentialIdException;
import ch.post.it.evoting.securedatamanager.batch.batch.exceptions.GenerateSVKVotingCardIdPassKeystoreException;
import ch.post.it.evoting.securedatamanager.batch.batch.exceptions.GenerateVerificationCardCodesException;
import ch.post.it.evoting.securedatamanager.batch.batch.exceptions.GenerateVotingcardIdException;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.domain.VcIdCombinedReturnCodesGenerationValues;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtendedAuthInformation;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;
import ch.post.it.evoting.securedatamanager.config.engine.actions.ExtendedAuthenticationService;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCodesDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators.VerificationCardCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators.VotingCardCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.StartVotingKeyService;

/**
 * Generates voting and verification cards.
 */
@Service
@JobScope
public class VotingCardGenerator implements ItemProcessor<VcIdCombinedReturnCodesGenerationValues, GeneratedVotingCardOutput> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardGenerator.class);
	private static final String KEY_ALREADY_EXISTS = "Key %s already exists in codes mapping table";

	private final CryptoPrimitives cryptoPrimitives = CryptoPrimitivesService.get();
	private final ElGamalService cryptoPrimitivesElGamalService = new ElGamalService();
	private final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	private final VotingCardGenerationJobExecutionContext jobExecutionContext;
	private final String verificationCardSetId;
	private final String votingCardSetId;
	private final int numberOfVotingCards;
	private final CryptoAPIPBKDFDeriver pbkdfDeriver;
	private final VotersParametersHolder holder;

	private final JobExecutionObjectContext objectContext;
	private final VoterCodesService voterCodesService;
	private final ExtendedAuthenticationService extendedAuthenticationService;
	private final StartVotingKeyService startVotingKeyService;
	private final VotingCardCredentialDataPackGenerator votingCardCredentialDataPackGenerator;
	private final VerificationCardCredentialDataPackGenerator verificationCardCredentialDataPackGeneratorWithJobScope;
	private final PathResolver pathResolver;

	public VotingCardGenerator(final VoterCodesService voterCodesService,
			@Qualifier("extendedAuthenticationServiceWithJobScope")
			final ExtendedAuthenticationService extendedAuthenticationService,
			final StartVotingKeyService startVotingKeyService,
			final VotingCardCredentialDataPackGenerator votingCardCredentialDataPackGenerator,
			@Qualifier("verificationCardCredentialDataPackGeneratorWithJobScope")
			final VerificationCardCredentialDataPackGenerator verificationCardCredentialDataPackGeneratorWithJobScope,
			final PathResolver pathResolver,
			final PrimitivesServiceAPI primitivesService,
			final JobExecutionObjectContext objectContext,
			@Value("#{jobExecution}")
			final JobExecution jobExecution) {
		this.objectContext = objectContext;
		this.voterCodesService = voterCodesService;
		this.extendedAuthenticationService = extendedAuthenticationService;
		this.startVotingKeyService = startVotingKeyService;
		this.votingCardCredentialDataPackGenerator = votingCardCredentialDataPackGenerator;
		this.verificationCardCredentialDataPackGeneratorWithJobScope = verificationCardCredentialDataPackGeneratorWithJobScope;
		this.pathResolver = pathResolver;
		this.pbkdfDeriver = primitivesService.getPBKDFDeriver();
		this.jobExecutionContext = new VotingCardGenerationJobExecutionContext(jobExecution.getExecutionContext());
		this.verificationCardSetId = jobExecutionContext.getVerificationCardSetId();
		this.votingCardSetId = jobExecutionContext.getVotingCardSetId();
		this.numberOfVotingCards = jobExecutionContext.getNumberOfVotingCards();
		this.holder = this.objectContext.get(jobExecutionContext.getJobInstanceId(), VotersParametersHolder.class);

	}

	@VisibleForTesting
	static String retrieveBallotCastingKey(final String verificationCardSetId, final String verificationCardId, final Path basePath) {

		LOGGER.info("Retrieving the ballot casting key for the base path {}, verificationCardSetId {} and verificationCardId {}.", basePath,
				verificationCardSetId, verificationCardId);

		final Path ballotCastingKeyPath = basePath.resolve(Constants.CONFIG_DIR_NAME_OFFLINE).resolve(Constants.CONFIG_BALLOT_CASTING_KEYS_DIRECTORY)
				.resolve(verificationCardSetId).resolve(verificationCardId + Constants.KEY);

		try {
			return new String(Files.readAllBytes(ballotCastingKeyPath), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new IllegalStateException(
					String.format("Error retrieving the ballot casting key for the base path %s, verificationCardSetId %s and verificationCardId %s.",
							basePath, verificationCardSetId, verificationCardId), e);
		}

	}

	@Override
	public GeneratedVotingCardOutput process(final VcIdCombinedReturnCodesGenerationValues vcIdCombinedReturnCodesGenerationValues) throws Exception {

		final String verificationCardId = vcIdCombinedReturnCodesGenerationValues.getVerificationCardId();
		final String electionEventId = jobExecutionContext.getElectionEventId();
		final byte[] saltCredentialId = Base64.getDecoder().decode(jobExecutionContext.getSaltCredentialId());
		final byte[] saltKeystoreSymmetricEncryptionKey = Base64.getDecoder().decode(jobExecutionContext.getSaltKeystoreSymmetricEncryptionKey());

		LOGGER.debug("Generating voting and verification cards for computed values of electionEventId {} and verificationCardId {}.", electionEventId,
				verificationCardId);

		char[] keystoreSymmetricEncryptionKey = null;
		try {

			final String startVotingKey = generateStartVotingKey(electionEventId);
			final String credentialId = generateCredentialId(pbkdfDeriver, electionEventId, saltCredentialId, startVotingKey);

			keystoreSymmetricEncryptionKey = generateKeystoreSymmetricEncryptionKey(pbkdfDeriver, electionEventId, saltKeystoreSymmetricEncryptionKey,
					startVotingKey);

			final VotingCardCredentialDataPack voterCredentialDataPack = generateVotersCredentialDataPack(keystoreSymmetricEncryptionKey,
					credentialId);

			final VerificationCardCredentialDataPack verificationCardCredentialDataPack = verificationCardCredentialDataPackGeneratorWithJobScope
					.generate(holder.getVerificationCardInputDataPack(), electionEventId, verificationCardId, verificationCardSetId,
							keystoreSymmetricEncryptionKey, holder.getAbsoluteBasePath());

			final GqGroup gqGroup = vcIdCombinedReturnCodesGenerationValues.getEncryptedPreChoiceReturnCodes().getGroup();
			final ElGamalMultiRecipientPrivateKey setupPrivateKey = getSetupPrivateKey(gqGroup);

			final List<ZpGroupElement> preChoiceReturnCodes = decryptPreChoiceReturnCodes(
					vcIdCombinedReturnCodesGenerationValues.getEncryptedPreChoiceReturnCodes(), setupPrivateKey);

			final List<BigInteger> encodedVotingOptions = holder.getBallot().getEncodedVotingOptions();

			if (encodedVotingOptions.size() != preChoiceReturnCodes.size()) {
				throw new IllegalStateException(String.format("The encodedVotingOptions size (%s) does not match the preChoiceReturnCodes size (%s)",
						encodedVotingOptions.size(), preChoiceReturnCodes.size()));
			}

			final Map<BigInteger, ZpGroupElement> encodedVotingOptionsToPreChoiceReturnCodes = IntStream.range(0, encodedVotingOptions.size()).boxed()
					.collect(Collectors.toMap(encodedVotingOptions::get, preChoiceReturnCodes::get));

			final String ballotCastingKey = retrieveBallotCastingKey(verificationCardSetId, verificationCardId, holder.getAbsoluteBasePath());

			final ZpGroupElement preVoteCastReturnCode = decryptPreVoteCastReturnCode(
					vcIdCombinedReturnCodesGenerationValues.getEncryptedPreVoteCastReturnCode(), setupPrivateKey);

			final VerificationCardCodesDataPack verificationCardCodesDataPack = createVerificationCardCode(holder.getBallot(), verificationCardId,
					encodedVotingOptionsToPreChoiceReturnCodes, ballotCastingKey, preVoteCastReturnCode);

			final ExtendedAuthInformation extendedAuthInformation = getExtendedAuthInformation(electionEventId, startVotingKey);
			final String ballotId = jobExecutionContext.getBallotId();
			final String ballotBoxId = jobExecutionContext.getBallotBoxId();
			final String votingCardId = generateVotingCardId(electionEventId);

			return GeneratedVotingCardOutput
					.success(votingCardId, votingCardSetId, ballotId, ballotBoxId, credentialId, electionEventId, verificationCardId,
							verificationCardSetId, startVotingKey, voterCredentialDataPack, verificationCardCredentialDataPack,
							verificationCardCodesDataPack, extendedAuthInformation);

		} finally {
			if (keystoreSymmetricEncryptionKey != null) {
				fill(keystoreSymmetricEncryptionKey, ' ');
			}
		}
	}

	private ElGamalMultiRecipientPrivateKey getSetupPrivateKey(final GqGroup gqGroup) {
		final String electionEventId = jobExecutionContext.getElectionEventId();
		try {
			// Read secret key from file system.
			return mapper.reader().withAttribute("group", gqGroup).readValue(pathResolver
					.resolve(Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_OFFLINE,
							Constants.SETUP_SECRET_KEY_FILE_NAME).toFile(), ElGamalMultiRecipientPrivateKey.class);
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to deserialize set up secret key.", e);
		}
	}

	private List<ZpGroupElement> decryptPreChoiceReturnCodes(final ElGamalMultiRecipientCiphertext encryptedPreChoiceReturnCodes,
			final ElGamalMultiRecipientPrivateKey compressedSetupSecretKey) {

		final ElGamalMultiRecipientMessage pC_id = cryptoPrimitivesElGamalService.getMessage(encryptedPreChoiceReturnCodes, compressedSetupSecretKey);

		return pC_id.stream().map(CryptoAdapters::convert).collect(Collectors.toList());
	}

	private ZpGroupElement decryptPreVoteCastReturnCode(final ElGamalMultiRecipientCiphertext encryptedPreVoteCastReturnCode,
			final ElGamalMultiRecipientPrivateKey compressedSetupSecretKey) {

		final ElGamalMultiRecipientMessage pVCC_id = cryptoPrimitivesElGamalService
				.getMessage(encryptedPreVoteCastReturnCode, compressedSetupSecretKey);

		return CryptoAdapters.convert(pVCC_id.get(0));
	}

	private ExtendedAuthInformation getExtendedAuthInformation(final String electionEventId, final String startVotingKey) {
		// generates the authentication key and derives the ID
		return extendedAuthenticationService.create(StartVotingKey.ofValue(startVotingKey), electionEventId);
	}

	private VerificationCardCodesDataPack createVerificationCardCode(final Ballot enrichedBallot, final String verificationCardId,
			final Map<BigInteger, ZpGroupElement> ballotVotingOptionToPreChoiceReturnCodes, final String ballotCastingKey,
			final ZpGroupElement preVoteCastReturnCode) {

		// Create the Vote Cast Return Code
		final String voteCastReturnCode = voterCodesService.generateShortVoteCastReturnCode();

		// Extract the list of encoded voting options and create the combinedCorrectnessInformation
		final List<BigInteger> encodedVotingOptions = enrichedBallot.getEncodedVotingOptions();
		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(enrichedBallot);

		final Map<String, BigInteger> choiceCodes2BallotVotingOptionOrderedBasedOnInsertion = new LinkedHashMap<>();

		// The TreeMap shuffles the Codes Mapping Table since it sorts the Map by the hash of the long Choice Return Code or the hash of the long
		// Vote Cast Return Code.
		final Map<String, String> shuffledCodesMappingTable = new TreeMap<>();

		final int n = encodedVotingOptions.size();
		for (int k = 0; k < n; k++) {
			final String shortChoiceCode = generateShortChoiceReturnCodeForVO(choiceCodes2BallotVotingOptionOrderedBasedOnInsertion);
			final BigInteger encodedVotingOption = encodedVotingOptions.get(k);

			// add to the map [short Choice Return Codes - encoded voting options]
			choiceCodes2BallotVotingOptionOrderedBasedOnInsertion.put(shortChoiceCode, encodedVotingOption);

			final byte[] shortChoiceCodeAsBytes = shortChoiceCode.getBytes(StandardCharsets.UTF_8);
			final ZpGroupElement preChoiceReturnCode = ballotVotingOptionToPreChoiceReturnCodes.get(encodedVotingOption);

			// create a long Choice Return Code
			final List<String> correctnessId = Collections.singletonList(combinedCorrectnessInformation.getCorrectnessIdForVotingOptionIndex(k));
			final byte[] longChoiceCode = generateLongChoiceReturnCodeForVO(holder.getEeid(), verificationCardId, preChoiceReturnCode, correctnessId);

			// encrypt the Short Choice Return Code with the long Choice Return Code and add the generated entry to the map
			final CodesMappingTableEntry codesMappingTableEntry;
			try {
				codesMappingTableEntry = voterCodesService.generateCodesMappingTableEntry(shortChoiceCodeAsBytes, longChoiceCode);
			} catch (final GeneralCryptoLibException e) {
				throw new CreateVotingCardSetException("An error occurred while generating the mapping table codes.", e);
			}

			// convert key and data to Base64
			final String keyAsBase64 = Base64.getEncoder().encodeToString(codesMappingTableEntry.getKey());
			final String dataAsBase64 = Base64.getEncoder().encodeToString(codesMappingTableEntry.getData());

			putKeyValuePairInMap(enrichedBallot, verificationCardId, shuffledCodesMappingTable, keyAsBase64, dataAsBase64);
		}

		// success - pre-Choice Return Codes successfully generated
		LOGGER.debug("{}. [electionEventId: {}, verificationCardId: {}, encodedVotingOptions size: {}]",
				GENVCC_SUCCESS_PRE_CHOICECODES_GENERATED.getInfo(), enrichedBallot.getElectionEvent().getId(), verificationCardId, n);

		// success - long Choice Return Code successfully generated
		LOGGER.debug("{}. [electionEventId: {}, verificationCardId: {}, encodedVotingOptions size: {}]",
				GENVCC_SUCCESS_LONGCHOICECODES_GENERATED.getInfo(), enrichedBallot.getElectionEvent().getId(), verificationCardId, n);

		final byte[] longVoteCastReturnCode = generateLongVoteCastReturnCode(enrichedBallot, holder.getEeid(), verificationCardId,
				preVoteCastReturnCode, Collections.emptyList());

		final String keyAsBase64;
		final String dataAsBase64;
		try {
			final CodesMappingTableEntry voterCastingCodeMappingEntry = voterCodesService
					.generateCodesMappingTableEntry(voteCastReturnCode.getBytes(StandardCharsets.UTF_8), longVoteCastReturnCode);
			keyAsBase64 = Base64.getEncoder().encodeToString(voterCastingCodeMappingEntry.getKey());
			dataAsBase64 = Base64.getEncoder().encodeToString(voterCastingCodeMappingEntry.getData());

		} catch (final GeneralCryptoLibException e) {
			// error storing the Vote Cast Return Code in the Codes Mapping Table
			LOGGER.error("{}. [electionEventId: {}, verificationCardId: {}]", GENVCC_ERROR_STORING_VOTECASTCODE.getInfo(),
					enrichedBallot.getElectionEvent().getId(), verificationCardId);
			throw new GenerateVerificationCardCodesException(GENVCC_ERROR_STORING_VOTECASTCODE.getInfo(), e);
		}

		putKeyValuePairInMap(enrichedBallot, verificationCardId, shuffledCodesMappingTable, keyAsBase64, dataAsBase64);

		// Vote Cast Return Code correctly stored
		LOGGER.debug("{}. [electionEventId: {}, verificationCardId: {}]", GENVCC_SUCCESS_VOTECASTCODE_STORED.getInfo(),
				enrichedBallot.getElectionEvent().getId(), verificationCardId);

		return new VerificationCardCodesDataPack(shuffledCodesMappingTable, ballotCastingKey, voteCastReturnCode,
				choiceCodes2BallotVotingOptionOrderedBasedOnInsertion);
	}

	private void putKeyValuePairInMap(final Ballot enrichedBallot, final String verificationCardId,
			final Map<String, String> shuffledCodesMappingTable, final String keyAsBase64, final String dataAsBase64) {

		if (shuffledCodesMappingTable.containsKey(keyAsBase64)) {
			LOGGER.error("{}. {}. [electionEventId: {}, verificationCardId: {}]", GENVCC_ERROR_STORING_VOTECASTCODE.getInfo(),
					String.format(KEY_ALREADY_EXISTS, keyAsBase64), enrichedBallot.getElectionEvent().getId(), verificationCardId);
			throw new GenerateVerificationCardCodesException(GENVCC_ERROR_STORING_VOTECASTCODE.getInfo());
		}

		shuffledCodesMappingTable.put(keyAsBase64, dataAsBase64);
	}

	private byte[] generateLongVoteCastReturnCode(final Ballot enrichedBallot, final String electionEventId, final String verificationCardId,
			final ZpGroupElement preVoteCastReturnCode, final List<String> attributesWithCorrectness) {

		final byte[] longVoteCastReturnCode;
		try {
			longVoteCastReturnCode = voterCodesService
					.generateLongReturnCode(electionEventId, verificationCardId, preVoteCastReturnCode, attributesWithCorrectness);
		} catch (final Exception e) {
			// error generating the long Vote Cast Return Code
			LOGGER.error("{}. [electionEventId: {}, verificationCardId: {}]", GENVCC_ERROR_GENERATING_LONGVOTECASTCODE.getInfo(),
					enrichedBallot.getElectionEvent().getId(), verificationCardId);
			throw new GenerateVerificationCardCodesException(GENVCC_ERROR_GENERATING_LONGVOTECASTCODE.getInfo(), e);
		}

		// success - long Vote Cast Return Code successfully generated
		LOGGER.debug("{}. [electionEventId: {}, verificationCardId: {}]", GENVCC_SUCCESS_LONGVOTECASTCODE_GENERATED.getInfo(),
				enrichedBallot.getElectionEvent().getId(), verificationCardId);

		return longVoteCastReturnCode;
	}

	private byte[] generateLongChoiceReturnCodeForVO(final String electionEventId, final String verificationCardId,
			final ZpGroupElement preChoiceReturnCode, final List<String> attributesWithCorrectness) {
		return voterCodesService.generateLongReturnCode(electionEventId, verificationCardId, preChoiceReturnCode, attributesWithCorrectness);
	}

	private String generateShortChoiceReturnCodeForVO(final Map<String, BigInteger> choiceCodes2BallotVotingOption) {
		String shortChoiceReturnCode;
		do {
			// create a short Choice Return Code that differs from the previous ones.
			shortChoiceReturnCode = voterCodesService.generateShortChoiceReturnCode();
		} while (choiceCodes2BallotVotingOption.containsKey(shortChoiceReturnCode));

		return shortChoiceReturnCode;
	}

	private VotingCardCredentialDataPack generateVotersCredentialDataPack(final char[] keystoreSymmetricEncryptionKey, final String credentialId) {
		final VotingCardCredentialDataPack voterCredentialDataPack;

		// create replacementHolder with eeid and credential ID
		final ReplacementsHolder replacementsHolder = new ReplacementsHolder(holder.getVotingCardCredentialInputDataPack().getEeid(), credentialId);

		try {
			voterCredentialDataPack = votingCardCredentialDataPackGenerator
					.generate(holder.getVotingCardCredentialInputDataPack(), replacementsHolder, keystoreSymmetricEncryptionKey, credentialId,
							holder.getVotingCardSetID(),
							holder.getCreateVotingCardSetCertificateProperties().getCredentialAuthCertificateProperties(),
							holder.getCredentialCACert(), holder.getElectionCACert());

		} catch (final GeneralCryptoLibException e) {
			throw new CreateVotingCardSetException("An error occurred while generating the voters credential data pack: " + e.getMessage(), e);
		}
		return voterCredentialDataPack;
	}

	private String generateVotingCardId(final String electionEventId) {
		final String votingCardId;

		try {
			votingCardId = cryptoPrimitives.genRandomBase16String(Constants.BASE16_ID_LENGTH).toLowerCase();
			LOGGER.debug("{}. [electionEventId: {}, votingCardSetId: {}, votingCardId: {}]", GENSVPK_SUCCESS_VCIDS_GENERATED.getInfo(),
					electionEventId, votingCardSetId, votingCardId);
		} catch (final Exception e) {
			LOGGER.error("{}. [electionEventId: {}, votingCardSetId: {}]", GENSVPK_ERROR_GENERATING_VCID.getInfo(), electionEventId, votingCardSetId);
			throw new GenerateVotingcardIdException(GENSVPK_ERROR_GENERATING_VCID.getInfo(), e);
		}

		return votingCardId;
	}

	private String generateCredentialId(final CryptoAPIPBKDFDeriver pbkdfDeriver, final String electionEventId, final byte[] salt,
			final String startVotingKey) {
		final String credentialId;
		try {
			credentialId = String.valueOf(getDerivedBytesInHEX(pbkdfDeriver, salt, startVotingKey));
		} catch (final Exception e) {
			LOGGER.error("{}. [electionEventId: {}, votingCardSetId: {}, voting card size: {}]", GENCREDAT_ERROR_DERIVING_CREDENTIAL_ID.getInfo(),
					electionEventId, votingCardSetId, numberOfVotingCards);
			throw new GenerateCredentialIdException(GENCREDAT_ERROR_DERIVING_CREDENTIAL_ID.getInfo(), e);
		}
		LOGGER.debug("{}. [electionEventId: {}, votingCardSetId: {}, credentialId: {}]", GENCREDAT_SUCCESS_CREDENTIAL_ID_DERIVED.getInfo(),
				electionEventId, votingCardSetId, credentialId);

		return credentialId;
	}

	private char[] generateKeystoreSymmetricEncryptionKey(final CryptoAPIPBKDFDeriver pbkdfDeriver, final String electionEventId, final byte[] salt,
			final String startVotingKey) {
		final char[] keystoreSymmetricEncryptionKey;

		try {
			keystoreSymmetricEncryptionKey = getDerivedBytesInHEX(pbkdfDeriver, salt, startVotingKey);
		} catch (final Exception e) {
			// error deriving the keystore symmetric encryption key KSKey
			LOGGER.error("{}. [electionEventId: {}, votingCardSetId: {}, voting card size: {}]",
					GENSVPK_ERROR_DERIVING_KEYSTORE_SYMMETRIC_ENCRYPTION_KEY.getInfo(), electionEventId, votingCardSetId, numberOfVotingCards);
			throw new GenerateSVKVotingCardIdPassKeystoreException(GENSVPK_ERROR_DERIVING_KEYSTORE_SYMMETRIC_ENCRYPTION_KEY.getInfo(), e);
		}

		// success - Keystore symmetric encryption key KSKey successfully derived
		LOGGER.debug("{}. [electionEventId: {}, votingCardSetId: {}, voting card size: {}]",
				GENSVPK_SUCCESS_KEYSTORE_SYMMETRIC_ENCRYPTION_KEY_DERIVED.getInfo(), electionEventId, votingCardSetId, numberOfVotingCards);

		return keystoreSymmetricEncryptionKey;
	}

	private String generateStartVotingKey(final String electionEventId) {
		final String startVotingKey;

		try {
			startVotingKey = startVotingKeyService.generateStartVotingKey();
		} catch (final Exception e) {
			// error when generating the Start Voting Key
			LOGGER.error("{}. [electionEventId: {}, votingCardSetId: {}, voting card size: {}]", GENSVPK_ERROR_GENERATING_SVK.getInfo(),
					electionEventId, holder.getVotingCardSetID(), numberOfVotingCards);

			throw new CreateVotingCardSetException(GENSVPK_ERROR_GENERATING_SVK.getInfo() + ":" + e.getMessage(), e);
		}

		// success - Start Voting Key successfully generated
		LOGGER.info("{}. [electionEventId: {}, votingCardSetId: {}, voting card size: {}]", GENSVPK_SUCCESS_SVK_GENERATED.getInfo(), electionEventId,
				votingCardSetId, numberOfVotingCards);

		return startVotingKey;
	}

	private char[] getDerivedBytesInHEX(final CryptoAPIPBKDFDeriver derived, final byte[] salt, final String inputString)
			throws GeneralCryptoLibException {
		final CryptoAPIDerivedKey cryptoAPIDerivedKey = derived.deriveKey(inputString.toCharArray(), salt);
		final byte[] encoded = cryptoAPIDerivedKey.getEncoded();
		return Hex.encodeHex(encoded);
	}

}
