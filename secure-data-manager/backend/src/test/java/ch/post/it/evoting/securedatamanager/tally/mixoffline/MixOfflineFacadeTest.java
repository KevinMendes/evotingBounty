/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.generators.MixnetShufflePayloadGenerator;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.services.application.exception.CheckedIllegalStateException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotBoxService;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.tally.MixnetShufflePayloadFileRepository;
import ch.post.it.evoting.securedatamanager.tally.MixnetShufflePayloadService;

class MixOfflineFacadeTest {

	private static final String RESOURCES_FOLDER_NAME = "MixOfflineFacadeTest";

	private static final int UUID_LENGTH = 32;
	private static final RandomService randomService = new RandomService();
	private static String electionEventId;
	private static String ballotBoxId = "ballotBoxId";
	private static Ballot ballot;
	private static String ballotId;
	private final Random random = new SecureRandom();
	private MixnetShufflePayload lastMixnetShufflePayload;
	private BallotBoxService ballotBoxService;
	private MixnetShufflePayloadFileRepository shufflepayloadFileRepository;
	private FactorizeService factorizeService;
	private VotePrimeFactorsFileRepository votePrimeFactorsFileRepository;
	private BallotService ballotService;
	private MixOfflineFacade mixFacade;
	private MixDecryptService mixDecryptService;
	private MixDecryptService.Result result;
	private CryptolibPayloadSignatureService signatureService;
	private MixnetShufflePayloadService mixnetShufflePayloadService;

	@BeforeAll
	static void setUpSuite() throws IOException {
		electionEventId = genValidUUID();
		ballotId = genValidUUID();
		ballotBoxId = genValidUUID();
		ballot = getBallotFromResourceFolder();
	}

	private static Ballot getBallotFromResourceFolder() throws IOException {
		return new ObjectMapper().readValue(
				MixOfflineFacadeTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME + File.separator + "ballotForMixing.json"),
				Ballot.class);
	}

	@BeforeEach
	void setUp() {
		ballotBoxService = mock(BallotBoxService.class);
		final BallotBoxRepository ballotBoxRepository = mock(BallotBoxRepository.class);
		shufflepayloadFileRepository = mock(MixnetShufflePayloadFileRepository.class);
		final MixnetFinalPayloadFileRepository finalPayloadFileRepository = mock(MixnetFinalPayloadFileRepository.class);
		final ConfigurationEntityStatusService configurationEntityStatusService = mock(ConfigurationEntityStatusService.class);
		factorizeService = mock(FactorizeService.class);
		votePrimeFactorsFileRepository = mock(VotePrimeFactorsFileRepository.class);
		ballotService = mock(BallotService.class);
		mixDecryptService = mock(MixDecryptService.class);
		mixnetShufflePayloadService = mock(MixnetShufflePayloadService.class);

		//Have to use real group because of cryptolib properties
		final GqGroup group = GroupTestData.getLargeGqGroup();
		final int numVotes = random.nextInt(10) + 2;
		final int voteSize = random.nextInt(10) + 1;
		final int nodeId = random.nextInt(3) + 1;
		lastMixnetShufflePayload = new MixnetShufflePayloadGenerator(group).genPayload(numVotes, voteSize, nodeId);
		result = new MixDecryptResultGenerator(group).genMixDecryptResult(numVotes, voteSize);

		signatureService = mock(CryptolibPayloadSignatureService.class);

		mixFacade = new MixOfflineFacade(ballotBoxService, ballotBoxRepository, shufflepayloadFileRepository, finalPayloadFileRepository,
				configurationEntityStatusService, factorizeService, votePrimeFactorsFileRepository, ballotService, mixDecryptService,
				mixnetShufflePayloadService);
	}

	@Test
	void mixDecryptMixesFactorizesAndPersists() throws ResourceNotFoundException, PayloadSignatureException {
		when(ballotBoxService.getBallotId(ballotBoxId)).thenReturn(ballotId);
		when(ballotBoxService.isDownloaded(ballotBoxId)).thenReturn(true);
		when(ballotBoxService.hasDownloadedBallotBoxConfirmedVotes(electionEventId, ballotId, ballotBoxId)).thenReturn(true);
		when(shufflepayloadFileRepository.getPayload(eq(electionEventId), eq(ballotId), eq(ballotBoxId), anyInt()))
				.thenReturn(lastMixnetShufflePayload);
		when(mixDecryptService.mixDecryptOffline(any(), any(), any(), any(), any())).thenReturn(result);
		when(ballotService.getBallot(electionEventId, ballotId)).thenReturn(ballot);
		when(mixnetShufflePayloadService.areOnlinePayloadSignaturesValid(any(), any(), any())).thenReturn(true);
		final CryptoPrimitivesPayloadSignature signature = mock(CryptoPrimitivesPayloadSignature.class);
		when(signatureService.sign(any(), any(), any())).thenReturn(signature);

		assertDoesNotThrow(() -> mixFacade.mixOffline(electionEventId, ballotBoxId));

		verify(mixDecryptService).mixDecryptOffline(any(), any(), any(), any(), any());
		verify(factorizeService).factorize((GroupVector<GqElement, GqGroup>) any(), anyList(), anyInt());
		verify(votePrimeFactorsFileRepository).saveDecompressedVotes(any(), anyString(), anyString(), anyString());
	}

	@Test
	void mixFacadeThrowsForInvalidUUID() {
		assertThrows(FailedValidationException.class, () -> mixFacade.mixOffline("", ballotBoxId));
		assertThrows(FailedValidationException.class, () -> mixFacade.mixOffline(electionEventId, ""));
	}

	@Test
	void mixFacadeThrowsWhenBallotBoxIsNotDownloaded() throws ResourceNotFoundException {
		when(ballotBoxService.isDownloaded(ballotBoxId)).thenReturn(false);
		assertThrows(CheckedIllegalStateException.class, () -> mixFacade.mixOffline(electionEventId, ballotBoxId));
	}

	private static String genValidUUID() {
		return randomService.genRandomBase16String(UUID_LENGTH).toLowerCase(Locale.ROOT);
	}
}
