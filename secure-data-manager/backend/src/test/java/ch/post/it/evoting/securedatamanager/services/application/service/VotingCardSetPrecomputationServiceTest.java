/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.VotingCardSetServiceTestBase;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.VerificationCardSet;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenVerDatOutput;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenVerDatService;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
class VotingCardSetPrecomputationServiceTest extends VotingCardSetServiceTestBase {

	private static final String RESOURCES_FOLDER_NAME = "GenerateVerificationDataTest";
	private static final String ADMIN_BOARD_ID = "ADMIN_BOARD_ID";
	private static final String BALLOT_ID = "BALLOT_ID";
	private static final String BALLOT_JSON = "ballot.json";
	private static final String ELECTION_EVENT_ID = "a3d790fd1ac543f9b0a05ca79a20c9e2";
	private static final String VERIFICATION_CARD_SET_ID = "9a0";
	private static final String VOTING_CARD_SET_ID = "74a4e530b24f4086b099d153321cf1b3";

	private static final int SIGNING_KEY_SIZE = 1024;
	private static final BigInteger P = new BigInteger(
			"16370518994319586760319791526293535327576438646782139419846004180837103527129035954742043590609421369665944746587885814920851694546456891767644945459124422553763416586515339978014154452159687109161090635367600349264934924141746082060353483306855352192358732451955232000593777554431798981574529854314651092086488426390776811367125009551346089319315111509277347117467107914073639456805159094562593954195960531136052208019343392906816001017488051366518122404819967204601427304267380238263913892658950281593755894747339126531018026798982785331079065126375455293409065540731646939808640273393855256230820509217411510058759");
	private static final BigInteger Q = new BigInteger(
			"8185259497159793380159895763146767663788219323391069709923002090418551763564517977371021795304710684832972373293942907460425847273228445883822472729562211276881708293257669989007077226079843554580545317683800174632467462070873041030176741653427676096179366225977616000296888777215899490787264927157325546043244213195388405683562504775673044659657555754638673558733553957036819728402579547281296977097980265568026104009671696453408000508744025683259061202409983602300713652133690119131956946329475140796877947373669563265509013399491392665539532563187727646704532770365823469904320136696927628115410254608705755029379");
	private static final BigInteger G = new BigInteger("2");
	private static final int CHUNK_SIZE = 3;

	private static String administrationBoardPrivateKeyPEM;
	private static GqGroup gqGroup;

	@Spy
	private final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@Mock
	private BallotService ballotService;
	@Mock
	private GenVerDatService genVerDatService;
	@Mock
	private AdminBoardService adminBoardService;
	@Mock
	private IdleStatusService idleStatusService;
	@Mock
	private BallotBoxRepository ballotBoxRepository;
	@Mock
	private EncryptionParametersService encryptionParametersService;
	@Mock
	private CryptolibPayloadSignatureService payloadSignatureService;
	@Mock
	private ConfigurationEntityStatusService configurationEntityStatusService;
	@Mock
	private ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository;

	@InjectMocks
	private VotingCardSetPrecomputationService votingCardSetPrecomputationService;

	@Mock
	private PathResolver pathResolver;
	@Mock
	private VotingCardSetRepository votingCardSetRepository;

	@Mock
	private HashService hashService;

	@BeforeAll
	static void setUp() throws NoSuchAlgorithmException, NoSuchProviderException, GeneralCryptoLibException {
		Security.addProvider(new BouncyCastleProvider());

		gqGroup = new GqGroup(P, Q, G);

		// Generate the signing key pair.
		final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
		generator.initialize(SIGNING_KEY_SIZE);

		final KeyPair signingKeyPair = generator.generateKeyPair();

		// Store the private key PEM.
		administrationBoardPrivateKeyPEM = PemUtils.privateKeyToPem(signingKeyPair.getPrivate());
	}

	@BeforeEach
	void beforeEachTest() {
		ReflectionTestUtils.setField(votingCardSetPrecomputationService, "chunkSize", CHUNK_SIZE);
		ReflectionTestUtils.setField(votingCardSetPrecomputationService, "tenantId", "100");
	}

	@Test
	void precompute() throws ResourceNotFoundException, IOException, URISyntaxException, PayloadSignatureException {

		setUpService();

		when(idleStatusService.getIdLock(anyString())).thenReturn(true);
		// Put strings of size 44 in the allowList to make the GenVerDatOutput checks pass.
		final byte[] decodedPcc = Base64.getDecoder().decode("+ivmzla8ALXHkq4ssfQU9wlE8GvwUOHFDik3MYW5D4I=");
		//		when(hashService.recursiveHash(any())).thenReturn(decodedPcc);
		//		when(hashService.hashAndSquare(any(), any())).thenReturn(GqElement.GqElementFactory.fromSquareRoot(new BigInteger(1, decodedPcc), gqGroup));
		when(payloadSignatureService.sign(any(), any(), any())).thenReturn(
				new CryptoPrimitivesPayloadSignature(new byte[] {}, new X509Certificate[] {}));

		when(encryptionParametersService.load(any())).thenReturn(gqGroup);

		final Path setupKeyPath = getPathOfFileInResources(
				Paths.get(RESOURCES_FOLDER_NAME, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_OFFLINE, Constants.SETUP_SECRET_KEY_FILE_NAME));
		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_OFFLINE, Constants.SETUP_SECRET_KEY_FILE_NAME))
				.thenReturn(setupKeyPath);

		final GenVerDatOutput genVerDatOutput = getGenVerDatOutput();
		when(genVerDatService.genVerDat(anyInt(), anyList(), any(), any())).thenReturn(genVerDatOutput);
		assertDoesNotThrow(
				() -> votingCardSetPrecomputationService.precompute(VOTING_CARD_SET_ID, ELECTION_EVENT_ID, administrationBoardPrivateKeyPEM,
						ADMIN_BOARD_ID));

		final Path path = ReturnCodeGenerationRequestPayloadFileSystemRepository.getStoragePath(pathResolver, ELECTION_EVENT_ID,
				VERIFICATION_CARD_SET_ID, 0);

		final ReturnCodeGenerationRequestPayload payload = objectMapper.readValue(path.toFile(), ReturnCodeGenerationRequestPayload.class);

		assertAll(() -> assertEquals(ELECTION_EVENT_ID, payload.getElectionEventId()),
				() -> assertEquals(VERIFICATION_CARD_SET_ID, payload.getVerificationCardSetId()),
				() -> verify(returnCodeGenerationRequestPayloadRepository, times(4)).store(any()));
	}

	@Test
	void precomputeInvalidParams() {
		assertThrows(IllegalArgumentException.class, () -> votingCardSetPrecomputationService.precompute("", "", "", ""));
	}

	@Test
	void precomputeInvalidStatus() throws ResourceNotFoundException {
		setStatusForVotingCardSetFromRepository(Status.SIGNED.name(), votingCardSetRepository);

		when(idleStatusService.getIdLock(anyString())).thenReturn(true);

		assertThrows(InvalidStatusTransitionException.class,
				() -> votingCardSetPrecomputationService.precompute(VOTING_CARD_SET_ID, ELECTION_EVENT_ID, administrationBoardPrivateKeyPEM,
						ADMIN_BOARD_ID));
	}

	@Test
	void precomputeInvalidSigningParameters()
			throws ResourceNotFoundException, URISyntaxException, IOException {
		setUpService();

		when(idleStatusService.getIdLock(anyString())).thenReturn(true);
		// Put strings of size 44 in the allowList to make the GenVerDatOutput checks pass.
		final byte[] decodedPcc = Base64.getDecoder().decode("+ivmzla8ALXHkq4ssfQU9wlE8GvwUOHFDik3MYW5D4I=");
		//		when(hashService.recursiveHash(any())).thenReturn(decodedPcc);
		//		when(hashService.hashAndSquare(any(), any())).thenReturn(GqElement.GqElementFactory.fromSquareRoot(new BigInteger(1, decodedPcc), gqGroup));

		when(encryptionParametersService.load(any())).thenReturn(gqGroup);

		final Path setupKeyPath = getPathOfFileInResources(
				Paths.get(RESOURCES_FOLDER_NAME, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_OFFLINE, Constants.SETUP_SECRET_KEY_FILE_NAME));
		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_OFFLINE, Constants.SETUP_SECRET_KEY_FILE_NAME))
				.thenReturn(setupKeyPath);

		final GenVerDatOutput genVerDatOutput = getGenVerDatOutput();
		when(genVerDatService.genVerDat(anyInt(), any(), any(), any())).thenReturn(genVerDatOutput);

		assertThrows(PayloadSignatureException.class,
				() -> votingCardSetPrecomputationService.precompute(VOTING_CARD_SET_ID, ELECTION_EVENT_ID, "", ""));
	}

	@Test
	void persistBallotCastingKeysTest(
			@TempDir
			final Path tempDir) throws IOException {

		when(pathResolver.resolve(any())).thenReturn(tempDir);

		final Map<String, String> ballotCastingKeyPairs = new HashMap<String, String>() {{
			put("verificationCardId1", "ballotCastingKey1");
			put("verificationCardId2", "ballotCastingKey2");
			put("verificationCardId3", "ballotCastingKey3");
		}};

		final GenVerDatOutput genVerDatOutput = getGenVerDatOutput();

		final VerificationCardSet precomputeContext = new VerificationCardSet(ELECTION_EVENT_ID, "", VOTING_CARD_SET_ID, "verificationCardSetId",
				ADMIN_BOARD_ID);
		assertDoesNotThrow(() -> votingCardSetPrecomputationService.persistBallotCastingKeys(precomputeContext, genVerDatOutput));

		final Stream<Path> streamedFiles = Files.list(tempDir.resolve(ELECTION_EVENT_ID).resolve(Constants.CONFIG_DIR_NAME_OFFLINE)
				.resolve(Constants.CONFIG_BALLOT_CASTING_KEYS_DIRECTORY).resolve("verificationCardSetId"));
		final List<Path> filesList = streamedFiles.collect(Collectors.toList());
		streamedFiles.close();

		for (final Path file : filesList) {
			final String BCK = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
			assertEquals(ballotCastingKeyPairs.get(file.getFileName().toString().split("\\.")[0]), BCK);
		}

	}

	private void setUpService() throws URISyntaxException, ResourceNotFoundException, IOException {
		setStatusForVotingCardSetFromRepository(Status.LOCKED.name(), votingCardSetRepository);

		when(pathResolver.resolve(any())).thenReturn(getPathOfFileInResources(Paths.get(RESOURCES_FOLDER_NAME)));
		when(ballotBoxRepository.getBallotId(any())).thenReturn(BALLOT_ID);
		when(votingCardSetRepository.getVerificationCardSetId(VOTING_CARD_SET_ID)).thenReturn(VERIFICATION_CARD_SET_ID);
		when(votingCardSetRepository.getNumberOfVotingCards(ELECTION_EVENT_ID, VOTING_CARD_SET_ID)).thenReturn(10);
		when(ballotService.getBallot(ELECTION_EVENT_ID, BALLOT_ID)).thenReturn(getBallotFromResourceFolder());
	}

	private static Ballot getBallotFromResourceFolder() throws IOException {
		return new ObjectMapper().readValue(
				VotingCardSetPrecomputationServiceTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME + File.separator + BALLOT_JSON),
				Ballot.class);
	}

	private GenVerDatOutput getGenVerDatOutput() {
		final List<String> verificationCardIds = asList("verificationCardId1", "verificationCardId2", "verificationCardId3");
		final List<String> ballotCastingKeys = asList("ballotCastingKey1", "ballotCastingKey2", "ballotCastingKey3");
		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, 3, new RandomService());
		final List<ElGamalMultiRecipientKeyPair> keyPairs = Collections.nCopies(3, keyPair);
		final List<String> allowList = asList(
				"+ivmzla8ALXHkq4ssfQU9wlE8GvwUOHFDik3MYW5D4I=",
				"+oICJQGqd+n1qyxRmfgfZZkw4+HpR7wxMXlzeXu5yXY=",
				"/G9WM/QYtDypeTX145qZBSvu3d6n9xE6nqFzh1hCq80=",
				"/HJU7k/zhGihPP6izDvl3Xtax0Uhh9vNwH8JbCg9gWU=",
				"/TJeRo+zUgxTYDkRuTAzUQ43OYS92ze/aCHfst8vmiA=",
				"/dzRBCJL3nmsjMtRmjlVRm0IS+bUSJEV3gJGOziZvtw=",
				"/gjPZIPxnPFpZh/UrfK3mLs6RyMNq5WL9jVYCaRnW/M=",
				"/jLd+Zs5hJw7HXe3r8qCSVW/UTA1cSYr2krm+Bua1dU=",
				"/mFIunEisguqvYDgzFkVsbFhX2/jooRmWv/C/4d+vDw=");

		final ElGamalMultiRecipientCiphertext ciphertext = ElGamalMultiRecipientCiphertext.neutralElement(3, gqGroup);
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> returnCodes = GroupVector.of(ciphertext, ciphertext, ciphertext);
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> confirmationKey = GroupVector.of(ciphertext, ciphertext, ciphertext);

		return new GenVerDatOutput.Builder()
				.setVerificationCardIds(verificationCardIds)
				.setVerificationCardKeyPairs(keyPairs)
				.setPartialChoiceReturnCodesAllowList(allowList)
				.setBallotCastingKeys(ballotCastingKeys)
				.setEncryptedHashedPartialChoiceReturnCodes(returnCodes)
				.setEncryptedHashedConfirmationKeys(confirmationKey)
				.build();
	}

}
