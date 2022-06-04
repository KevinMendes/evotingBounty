/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.micrometer.core.instrument.util.StringUtils.isNotEmpty;

import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupMatrix;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.SetupKeyPairService;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

/**
 * Allows to generate and persist {@link ReturnCodesMappingTablePayload}.
 */
@Service
public class ReturnCodesMappingTablePayloadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodesMappingTablePayloadService.class);

	private final GenCMTableService genCMTableService;
	private final BallotBoxRepository ballotBoxRepository;
	private final SetupKeyPairService setupKeyPairService;
	private final VotingCardSetRepository votingCardSetRepository;
	private final EncryptionParametersService encryptionParametersService;
	private final CombineEncLongCodeSharesService combineEncLongCodeSharesService;
	private final EncryptedNodeLongReturnCodeSharesService encryptedNodeLongReturnCodeSharesService;
	private final ReturnCodesMappingTablePayloadFileRepository returnCodesMappingTablePayloadFileRepository;

	public ReturnCodesMappingTablePayloadService(
			final GenCMTableService genCMTableService,
			final BallotBoxRepository ballotBoxRepository,
			final SetupKeyPairService setupKeyPairService,
			final VotingCardSetRepository votingCardSetRepository,
			final EncryptionParametersService encryptionParametersService,
			final CombineEncLongCodeSharesService combineEncLongCodeSharesService,
			final EncryptedNodeLongReturnCodeSharesService encryptedNodeLongReturnCodeSharesService,
			final ReturnCodesMappingTablePayloadFileRepository returnCodesMappingTablePayloadFileRepository) {
		this.genCMTableService = genCMTableService;
		this.ballotBoxRepository = ballotBoxRepository;
		this.setupKeyPairService = setupKeyPairService;
		this.votingCardSetRepository = votingCardSetRepository;
		this.encryptionParametersService = encryptionParametersService;
		this.combineEncLongCodeSharesService = combineEncLongCodeSharesService;
		this.encryptedNodeLongReturnCodeSharesService = encryptedNodeLongReturnCodeSharesService;
		this.returnCodesMappingTablePayloadFileRepository = returnCodesMappingTablePayloadFileRepository;
	}

	/**
	 * Calls the CombineEncLongCodeShares and GenCMTable algorithms in order to produce a {@link ReturnCodesMappingTablePayload}.
	 *
	 * @param electionEventId the election event id. Must be non-null and a valid UUID.
	 * @param votingCardSetId the voting card set id. Must be non-null and a valid UUID.
	 * @return a {@link ReturnCodesMappingTablePayload} containing the CMtable
	 * @throws NullPointerException      if {@code electionEventId} or {@code votingCardSetId} is null.
	 * @throws FailedValidationException if {@code electionEventId} or {@code votingCardSetId} is invalid.
	 */
	public ReturnCodesMappingTablePayload generate(final String electionEventId, final String votingCardSetId) {
		validateUUID(electionEventId);
		validateUUID(votingCardSetId);

		LOGGER.info("Generating the return codes mapping table. [electionEventId: {}, votingCardSetId: {}]", electionEventId, votingCardSetId);

		final String verificationCardSetId = getVerificationCardSetId(electionEventId, votingCardSetId);

		// Load the node contributions
		final EncryptedNodeLongReturnCodeShares encryptedNodeLongReturnCodeShares = encryptedNodeLongReturnCodeSharesService.load(electionEventId,
				verificationCardSetId);

		// Load Setup secret key
		final ElGamalMultiRecipientPrivateKey setupSecretKey = setupKeyPairService.load(electionEventId).getPrivateKey();

		// Prepare and call CombineEncLongCodeShares algorithm
		final CombineEncLongCodeSharesContext combineEncLongCodeSharesContext = new CombineEncLongCodeSharesContext.Builder()
				.setElectionEventId(electionEventId)
				.setVerificationCardSetId(verificationCardSetId)
				.setSetupSecretKey(setupSecretKey)
				.build();
		final CombineEncLongCodeSharesInput combineEncLongCodeSharesInput = prepareCombineEncLongCodeSharesInput(
				encryptedNodeLongReturnCodeShares);
		final CombineEncLongCodeSharesOutput combineEncLongCodeSharesOutput = combineEncLongCodeSharesService.combineEncLongCodeShares(
				combineEncLongCodeSharesContext, combineEncLongCodeSharesInput);
		LOGGER.info("Encrypted long return code shares successfully combined. [electionEventId: {}, votingCardSetId: {}]", electionEventId,
				votingCardSetId);

		final GqGroup encryptionGroup = encryptionParametersService.load(electionEventId);

		// Prepare and call genCMTable algorithm
		final GenCMTableContext genCMTableContext = new GenCMTableContext.Builder()
				.setEncryptionGroup(encryptionGroup)
				.setElectionEventId(electionEventId)
				.setBallotId(getBallotId(votingCardSetId))
				.setVerificationCardSetId(verificationCardSetId)
				.setSetupSecretKey(setupSecretKey)
				.build();
		final GenCMTableInput genCMTableInput = new GenCMTableInput.Builder()
				.setVerificationCardIds(encryptedNodeLongReturnCodeShares.getVerificationCardIds())
				.setEncryptedPreChoiceReturnCodes(combineEncLongCodeSharesOutput.getEncryptedPreChoiceReturnCodesVector())
				.setPreVoteCastReturnCodes(GroupVector.from(combineEncLongCodeSharesOutput.getPreVoteCastReturnCodesVector()))
				.build();
		final GenCMTableOutput genCMTableOutput = genCMTableService.genCMTable(genCMTableContext, genCMTableInput);
		LOGGER.info("Return codes mapping table successfully generated. [electionEventId: {}, votingCardSetId: {}, verificationCardSetId: {}]",
				electionEventId, votingCardSetId, verificationCardSetId);

		// Prepare payload
		return new ReturnCodesMappingTablePayload.Builder()
				.setElectionEventId(electionEventId)
				.setVerificationCardSetId(verificationCardSetId)
				.setReturnCodesMappingTable(genCMTableOutput.getReturnCodesMappingTable())
				.build();
	}

	/**
	 * Persists a {@link ReturnCodesMappingTablePayload}.
	 *
	 * @param payload, the payload to be saved. Must be non-null.
	 * @throws NullPointerException if the payload is null.
	 */
	public void save(final ReturnCodesMappingTablePayload payload) {
		checkNotNull(payload);
		returnCodesMappingTablePayloadFileRepository.save(payload);
		LOGGER.info("Return codes mapping table successfully saved. [electionEventId: {}, verificationCardSetId: {}]", payload.getElectionEventId(),
				payload.getVerificationCardSetId());
	}

	private CombineEncLongCodeSharesInput prepareCombineEncLongCodeSharesInput(
			final EncryptedNodeLongReturnCodeShares encryptedNodeLongReturnCodeShares) {

		// Prepare the input matrix from the node return codes values
		final List<EncryptedSingleNodeLongReturnCodeShares> nodeReturnCodesValues = encryptedNodeLongReturnCodeShares.getNodeReturnCodesValues();

		final List<List<ElGamalMultiRecipientCiphertext>> partialChoiceReturnCodesColumns = nodeReturnCodesValues.stream()
				.map(EncryptedSingleNodeLongReturnCodeShares::getExponentiatedEncryptedPartialChoiceReturnCodes).collect(Collectors.toList());
		final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> partialChoiceReturnCodesMatrix = GroupMatrix.fromColumns(
				partialChoiceReturnCodesColumns);

		final List<List<ElGamalMultiRecipientCiphertext>> confirmationKeysColumns = nodeReturnCodesValues.stream()
				.map(EncryptedSingleNodeLongReturnCodeShares::getExponentiatedEncryptedConfirmationKeys).collect(Collectors.toList());
		final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> confirmationKeysMatrix = GroupMatrix.fromColumns(confirmationKeysColumns);

		return new CombineEncLongCodeSharesInput.Builder()
				.setExponentiatedEncryptedChoiceReturnCodesMatrix(partialChoiceReturnCodesMatrix)
				.setExponentiatedEncryptedConfirmationKeysMatrix(confirmationKeysMatrix)
				.setVerificationCardIds(encryptedNodeLongReturnCodeShares.getVerificationCardIds())
				.build();
	}

	private String getVerificationCardSetId(final String electionEventId, final String votingCardSetId) {
		final String votingCardSetAsJson = votingCardSetRepository.find(votingCardSetId);
		if (isNotEmpty(votingCardSetAsJson) && JsonConstants.EMPTY_OBJECT.equals(votingCardSetAsJson)) {
			throw new IllegalStateException(
					String.format("No voting card set. [electionEventId: %s, votingCardSetId: %s]", electionEventId, votingCardSetId));
		}
		final JsonObject votingCardSet = JsonUtils.getJsonObject(votingCardSetAsJson);
		return votingCardSet.getString(JsonConstants.VERIFICATION_CARD_SET_ID);
	}

	private String getBallotId(final String votingCardSetId) {
		// get ballot box from voting card set repository
		final String ballotBoxId = votingCardSetRepository.getBallotBoxId(votingCardSetId);

		// get ballot from ballot box repository
		return ballotBoxRepository.getBallotId(ballotBoxId);
	}

}
