/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ElectionEventContext;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.domain.configuration.VerificationCardSetContext;
import ch.post.it.evoting.securedatamanager.configuration.setuptally.SetupTallyEBService;
import ch.post.it.evoting.securedatamanager.configuration.setuptally.SetupTallyEBService.SetupTallyEBInput;
import ch.post.it.evoting.securedatamanager.configuration.setuptally.SetupTallyEBService.SetupTallyEBOutput;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenerateVerificationCardSetKeysService;
import ch.post.it.evoting.securedatamanager.services.application.service.ElectionEventService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@Service
public class ElectoralBoardConstitutionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectoralBoardConstitutionService.class);
	private static final int MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS = 0;

	private final ControlComponentPublicKeysService controlComponentPublicKeysService;
	private final GenerateVerificationCardSetKeysService generateVerificationCardSetKeysService;
	private final SetupTallyEBService setupTallyEBService;
	private final VotingCardSetRepository votingCardSetRepository;
	private final BallotBoxRepository ballotBoxRepository;
	private final ElectionEventContextPayloadService electionEventContextPayloadService;
	private final ElectionEventService electionEventService;

	public ElectoralBoardConstitutionService(final ControlComponentPublicKeysService controlComponentPublicKeysService,
			final GenerateVerificationCardSetKeysService generateVerificationCardSetKeysService, final SetupTallyEBService setupTallyEBService,
			final VotingCardSetRepository votingCardSetRepository, final BallotBoxRepository ballotBoxRepository,
			final ElectionEventContextPayloadService electionEventContextPayloadService, final ElectionEventService electionEventService) {
		this.controlComponentPublicKeysService = controlComponentPublicKeysService;
		this.generateVerificationCardSetKeysService = generateVerificationCardSetKeysService;
		this.setupTallyEBService = setupTallyEBService;
		this.votingCardSetRepository = votingCardSetRepository;
		this.ballotBoxRepository = ballotBoxRepository;
		this.electionEventContextPayloadService = electionEventContextPayloadService;
		this.electionEventService = electionEventService;
	}

	/**
	 * Constitutes the electoral board.
	 *
	 * @param electionEventId the election event id.
	 */
	public void constitute(final String electionEventId) {
		validateUUID(electionEventId);

		final List<ControlComponentPublicKeys> controlComponentPublicKeys = controlComponentPublicKeysService.load(electionEventId);

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrjChoiceReturnCodesEncryptionPublicKeys = controlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::getCcrChoiceReturnCodesEncryptionPublicKey)
				.collect(GroupVector.toGroupVector());

		final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey =
				generateVerificationCardSetKeysService.genVerCardSetKeys(ccrjChoiceReturnCodesEncryptionPublicKeys);

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccmElectionPublicKeys = controlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::getCcmElectionPublicKey)
				.collect(GroupVector.toGroupVector());

		final SetupTallyEBOutput setupTallyEBOutput =
				setupTallyEBService.setupTallyEB(new SetupTallyEBInput(ccmElectionPublicKeys, MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS));
		final ElGamalMultiRecipientPublicKey electionPublicKey = setupTallyEBOutput.getElectionPublicKey();
		final ElGamalMultiRecipientKeyPair electoralBoardKeyPair = setupTallyEBOutput.getElectoralBoardKeyPair();

		final List<VerificationCardSetContext> verificationCardSetContexts = buildVerificationCardSetContexts(electionEventId);

		final LocalDateTime startTime = electionEventService.getDateFrom(electionEventId);
		final LocalDateTime finishTime = electionEventService.getDateTo(electionEventId);

		final ElectionEventContext electionEventContext = new ElectionEventContext(electionEventId, verificationCardSetContexts,
				controlComponentPublicKeys, electoralBoardKeyPair.getPublicKey(), electionPublicKey, choiceReturnCodesEncryptionPublicKey, startTime,
				finishTime);

		// Sign election event context payload.
		// Currently, we do not have a signing key in the secure data manager.
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(new byte[] {}, new X509Certificate[] {});

		electionEventContextPayloadService.save(new ElectionEventContextPayload(electionPublicKey.getGroup(), electionEventContext, signature));

		LOGGER.info("Successfully saved constituted election event context payload. [electionEventId: {}]", electionEventId);
	}

	private List<VerificationCardSetContext> buildVerificationCardSetContexts(final String electionEventId) {
		final List<String> votingCardSetIds = votingCardSetRepository.findAllVotingCardSetIds(electionEventId);

		return votingCardSetIds.stream().map(votingCardSetId -> {
			final String verificationCardSetId = votingCardSetRepository.getVerificationCardSetId(votingCardSetId);
			final String ballotBoxId = votingCardSetRepository.getBallotBoxId(votingCardSetId);
			final boolean testBallotBox = ballotBoxRepository.isTestBallotBox(ballotBoxId);
			final int numberOfWriteInFields = MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS;
			return new VerificationCardSetContext(verificationCardSetId, ballotBoxId, testBallotBox, numberOfWriteInFields);
		}).collect(Collectors.toList());
	}

}
