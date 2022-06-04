/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetFinalPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.securedatamanager.services.application.exception.CheckedIllegalStateException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotBoxService;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotService;
import ch.post.it.evoting.securedatamanager.services.domain.model.mixing.PrimeFactors;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.tally.MixnetShufflePayloadFileRepository;
import ch.post.it.evoting.securedatamanager.tally.MixnetShufflePayloadService;

/**
 * Handles the offline mixing steps.
 */
@Service
public class MixOfflineFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixOfflineFacade.class);

	private final BallotBoxService ballotBoxService;
	private final BallotBoxRepository ballotBoxRepository;
	private final MixnetShufflePayloadFileRepository shufflePayloadFileRepository;
	private final MixnetFinalPayloadFileRepository finalPayloadFileRepository;
	private final ConfigurationEntityStatusService configurationEntityStatusService;
	private final FactorizeService factorizeService;
	private final VotePrimeFactorsFileRepository votePrimeFactorsFileRepository;
	private final BallotService ballotService;
	private final MixDecryptService mixDecryptService;
	private final MixnetShufflePayloadService mixnetShufflePayloadService;

	@Autowired
	MixOfflineFacade(final BallotBoxService ballotBoxService, final BallotBoxRepository ballotBoxRepository,
			final MixnetShufflePayloadFileRepository shufflePayloadFileRepository, final MixnetFinalPayloadFileRepository finalPayloadFileRepository,
			final ConfigurationEntityStatusService configurationEntityStatusService, final FactorizeService factorizeService,
			final VotePrimeFactorsFileRepository votePrimeFactorsFileRepository, final BallotService ballotService,
			final MixDecryptService mixDecryptService, final MixnetShufflePayloadService mixnetShufflePayloadService) {
		this.ballotBoxService = ballotBoxService;
		this.ballotBoxRepository = ballotBoxRepository;
		this.shufflePayloadFileRepository = shufflePayloadFileRepository;
		this.finalPayloadFileRepository = finalPayloadFileRepository;
		this.configurationEntityStatusService = configurationEntityStatusService;
		this.factorizeService = factorizeService;
		this.votePrimeFactorsFileRepository = votePrimeFactorsFileRepository;
		this.ballotService = ballotService;
		this.mixDecryptService = mixDecryptService;
		this.mixnetShufflePayloadService = mixnetShufflePayloadService;
	}

	/**
	 * Coordinates the offline mixing: mixing, decryption, factorisation and persistence
	 *
	 * @param electionEventId the id of the election event for which we want to mix a ballot box. Not null
	 * @param ballotBoxId     the id of the ballot box to mix. Not null
	 * @throws ResourceNotFoundException    if no ballot can be found for this ballot box id
	 * @throws CheckedIllegalStateException if the ballot box has not been downloaded prior
	 */
	public void mixOffline(final String electionEventId, final String ballotBoxId) throws ResourceNotFoundException, CheckedIllegalStateException {
		validateUUID(electionEventId);
		validateUUID(ballotBoxId);

		final String ballotId = ballotBoxService.getBallotId(ballotBoxId);

		if (!ballotBoxService.isDownloaded(ballotBoxId)) {
			throw new CheckedIllegalStateException(String.format(
					"Ballot box has not been downloaded, hence it cannot be mixed. [electionEventId: %s, ballotId: %s, ballotBoxId: %S]",
					electionEventId, ballotId, ballotBoxId));
		}

		if (!ballotBoxService.isDownloadedBallotBoxEmpty(electionEventId, ballotId, ballotBoxId) && ballotBoxService
				.hasDownloadedBallotBoxConfirmedVotes(electionEventId, ballotId, ballotBoxId)) {

			LOGGER.info("Mixing and decrypting. [electionEventId: {}, ballotId: {}, ballotBoxId: {}]", electionEventId, ballotId, ballotBoxId);

			checkArgument(mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(electionEventId, ballotId, ballotBoxId),
					String.format("The signatures verifications failed. [electionEventId: %s, ballotId: %s, ballotBoxId: %s]", electionEventId,
							ballotId, ballotBoxId));

			final MixnetFinalPayload payload = mixDecrypt(electionEventId, ballotId, ballotBoxId);

			LOGGER.info("Persisting final payload. [electionEventId: {}, ballotId: {}, ballotBoxId: {}]", electionEventId, ballotId, ballotBoxId);

			finalPayloadFileRepository.savePayload(electionEventId, ballotId, ballotBoxId, payload);

			final GroupVector<ElGamalMultiRecipientMessage, GqGroup> decryptedVotes = payload.getVerifiablePlaintextDecryption().getDecryptedVotes();
			final GroupVector<GqElement, GqGroup> encodedVoterSelections = extractEncodedVoterSelection(decryptedVotes);
			final Ballot ballot = ballotService.getBallot(electionEventId, ballotId);
			final List<GqElement> encodedVotingOptions = getVoteOptions(payload.getEncryptionGroup(), ballot);
			final int numberOfSelections = getNumberOfSelections(ballot);

			LOGGER.info("Factorizing voter selections. [electionEventId: {}, ballotId: {}, ballotBoxId: {}]", electionEventId, ballotId, ballotBoxId);
			final List<PrimeFactors> voterSelections = factorizeService.factorize(encodedVoterSelections, encodedVotingOptions, numberOfSelections);

			LOGGER.info("Persisting decompressed votes. [electionEventId: {}, ballotId: {}, ballotBoxId: {}]", electionEventId, ballotId,
					ballotBoxId);
			votePrimeFactorsFileRepository.saveDecompressedVotes(voterSelections, electionEventId, ballotId, ballotBoxId);

		} else {
			LOGGER.info(
					"Persisting an empty decompressed votes. There are no votes in the ballot box. [electionEventId: {}, ballotId: {}, ballotBoxId: {}]",
					electionEventId, ballotId, ballotBoxId);
			votePrimeFactorsFileRepository.saveDecompressedVotes(new ArrayList<>(), electionEventId, ballotId, ballotBoxId);
		}

		configurationEntityStatusService.update(Status.DECRYPTED.name(), ballotBoxId, ballotBoxRepository);
	}

	private GroupVector<GqElement, GqGroup> extractEncodedVoterSelection(final GroupVector<ElGamalMultiRecipientMessage, GqGroup> decryptedVotes) {
		return decryptedVotes.stream().map(message -> message.get(0)).collect(toGroupVector());
	}

	/**
	 * Computes the allowed number of selections
	 */
	private int getNumberOfSelections(final Ballot ballot) {
		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);
		return combinedCorrectnessInformation.getTotalNumberOfSelections();
	}

	private ImmutableList<GqElement> getVoteOptions(final GqGroup encryptionGroup, final Ballot ballot) {
		final List<BigInteger> encodedVotingOptions = ballot.getEncodedVotingOptions();
		return encodedVotingOptions.stream().map(bigInteger -> GqElementFactory.fromValue(bigInteger, encryptionGroup)).collect(toImmutableList());
	}

	/**
	 * Mixes and decrypts the votes in the specified ballot box.
	 *
	 * @param electionEventId identifier of the election event.
	 * @param ballotBoxId     identifier of the ballot box to mix and decrypt.
	 * @return the result of the mixing and decryption as a MixnetPayload object.
	 */
	private MixnetFinalPayload mixDecrypt(final String electionEventId, final String ballotId, final String ballotBoxId) {

		final MixnetShufflePayload lastPayload = shufflePayloadFileRepository.getPayload(electionEventId, ballotId, ballotBoxId, 3);

		final GqGroup encryptionGroup = lastPayload.getEncryptionGroup();
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts = GroupVector.from(lastPayload.getEncryptedVotes());
		final ElGamalMultiRecipientPublicKey remainingElectionPublicKey = lastPayload.getRemainingElectionPublicKey();

		final ZqElement zeroZqElement = ZqElement.create(BigInteger.ZERO, ZqGroup.sameOrderAs(encryptionGroup));
		final ElGamalMultiRecipientPrivateKey electoralBoardPrivateKey =
				new ElGamalMultiRecipientPrivateKey(Collections.singletonList(zeroZqElement));

		final ElGamalMultiRecipientKeyPair electoralBoardKeyPair =
				ElGamalMultiRecipientKeyPair.from(electoralBoardPrivateKey, encryptionGroup.getGenerator());

		final Instant start = Instant.now();
		final MixDecryptService.Result mixingResult = mixDecryptService
				.mixDecryptOffline(ciphertexts, remainingElectionPublicKey, electoralBoardKeyPair, electionEventId, ballotBoxId);
		LOGGER.info("Mixing duration was {}", Duration.between(start, Instant.now()));

		return new MixnetFinalPayload(encryptionGroup, mixingResult.getVerifiableShuffle().orElse(null),
				mixingResult.getVerifiablePlaintextDecryption(), remainingElectionPublicKey);
	}
}
