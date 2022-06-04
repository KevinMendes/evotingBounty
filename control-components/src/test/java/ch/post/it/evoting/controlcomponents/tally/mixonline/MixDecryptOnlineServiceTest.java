/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.tally.mixonline;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@SpringBootTest
@ActiveProfiles("test")
class MixDecryptOnlineServiceTest {

	private static final int NUM_KEY_ELEMENTS = 5;
	private static final int CIPHERTEXT_SIZE = 1; // Currently, we do not support write-ins.
	private static final String BALLOT_BOX_ID = "f0dd956605bb47d589f1bd7b19F5d6f38";
	private static final String ELECTION_EVENT_ID = "0b88257ec32142bb8ee0ed1bb70f362e";

	private final GqGroup gqGroup = new GqGroup(new BigInteger(
			"22588801568735561413035633152679913053449200833478689904902877673687016391844561133376032309307885537704777240609087377993341380751697605235541131273868440070920362148431866829787784445019147999379498503693247429579480289226602748397335327890884464685051682703709742724121783217827040722415360103179289160056581759372475845985438977307323570530753362027145384124771826114651710264766437273044759690955051982839684910462609395741692689616014805965573558015387956017183286848440036954926101719205598449898400180082053755864070690174202432196678045052744337832802051787273056312757384654145455745603262082348042780103679"),
			new BigInteger(
					"11294400784367780706517816576339956526724600416739344952451438836843508195922280566688016154653942768852388620304543688996670690375848802617770565636934220035460181074215933414893892222509573999689749251846623714789740144613301374198667663945442232342525841351854871362060891608913520361207680051589644580028290879686237922992719488653661785265376681013572692062385913057325855132383218636522379845477525991419842455231304697870846344808007402982786779007693978008591643424220018477463050859602799224949200090041026877932035345087101216098339022526372168916401025893636528156378692327072727872801631041174021390051839"),
			BigInteger.valueOf(2));

	private final GqGroup otherGqGroup = new GqGroup(new BigInteger(
			"B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF324E7738926CFBE5F4BF8D8D8C31D763DA06C80ABB1185EB4F7C7B5757F5958490CFD47D7C19BB42158D9554F7B46BCED55C4D79FD5F24D6613C31C3839A2DDF8A9A276BCFBFA1C877C56284DAB79CD4C2B3293D20E9E5EAF02AC60ACC93ED874422A52ECB238FEEE5AB6ADD835FD1A0753D0A8F78E537D2B95BB79D8DCAEC642C1E9F23B829B5C2780BF38737DF8BB300D01334A0D0BD8645CBFA73A6160FFE393C48CBBBCA060F0FF8EC6D31BEB5CCEED7F2F0BB088017163BC60DF45A0ECB1BCD289B06CBBFEA21AD08E1847F3F7378D56CED94640D6EF0D3D37BE69D0063",
			16), new BigInteger(
			"5BF0A8B1457695355FB8AC404E7A79E3B1738B079C5A6D2B53C26C8228C867F799273B9C49367DF2FA5FC6C6C618EBB1ED0364055D88C2F5A7BE3DABABFACAC24867EA3EBE0CDDA10AC6CAAA7BDA35E76AAE26BCFEAF926B309E18E1C1CD16EFC54D13B5E7DFD0E43BE2B1426D5BCE6A6159949E9074F2F5781563056649F6C3A21152976591C7F772D5B56EC1AFE8D03A9E8547BC729BE95CADDBCEC6E57632160F4F91DC14DAE13C05F9C39BEFC5D98068099A50685EC322E5FD39D30B07FF1C9E2465DDE5030787FC763698DF5AE6776BF9785D84400B8B1DE306FA2D07658DE6944D8365DFF510D68470C23F9FB9BC6AB676CA3206B77869E9BDF34E8031",
			16), new BigInteger(
			"1CB7C6D53960F1ABA5254DD328022F899DA8A86C809CA0CFC474A4BF183D9A79F75289DA2ACC9FF38CB57BD80EC3F24B647033B6524684FF4062732ED1F79467CA02B7A35F615388CCF9DD638A0916D7B90E83F8C3562B8A6DEC66A98847FCD8159682539A9FB8C1ACA7F07209645681123B2AC89DBACA18D1B4D245D44E31E68AF03226DAC36472DAF1E170CFFA0095A06A8427B428FDB03EBB40D241B5AEA9F491CB0AAB1B175464351F22D5D5004747AA483E97770C495B05F227CE46F28317495DFD0D7C789ECCB597BB5B2F357811303697D4B8475F1100C173E50A009811F07F4B0E16C4876D871EEB2C588874C4C422F7DDC79EDD3B276F3BF5E36D9",
			16));

	private final RandomService randomService = new RandomService();

	private List<ElGamalMultiRecipientCiphertext> ciphertexts;
	private ElGamalMultiRecipientPublicKey remainingElectionPublicKey;
	private ElGamalMultiRecipientKeyPair ccmKeyPair;

	@Autowired
	private MixDecryptOnlineService mixDecryptOnlineService;

	@BeforeEach
	void setup() {
		ciphertexts = new ElGamalGenerator(gqGroup).genRandomCiphertextVector(10, CIPHERTEXT_SIZE);
		remainingElectionPublicKey = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_KEY_ELEMENTS, randomService).getPublicKey();
		ccmKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_KEY_ELEMENTS, randomService);
	}

	@Test
	@DisplayName("MixDecOnline with null arguments throws a NullPointerException")
	void mixDecOnlineWithNullArguments() {
		assertThrows(NullPointerException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, null, remainingElectionPublicKey, ccmKeyPair));
		assertThrows(NullPointerException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, null, ccmKeyPair));
		assertThrows(NullPointerException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, remainingElectionPublicKey, null));
	}

	@Test
	@DisplayName("MixDecOnline with valid input does not throw")
	void mixDecOnlineWithValidArguments() {
		assertDoesNotThrow(
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, remainingElectionPublicKey, ccmKeyPair));
	}

	@Test
	@DisplayName("MixDecOnline with ciphertexts that contain too many elements throws an IllegalArgumentException")
	void mixDecOnlineWithCiphertextsTooManyElements() {
		ciphertexts = new ElGamalGenerator(gqGroup).genRandomCiphertextVector(10, CIPHERTEXT_SIZE + 1);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, remainingElectionPublicKey, ccmKeyPair));
		assertEquals("The ciphertexts size must be equal to the number of allowed write-ins + 1.", exception.getMessage());
	}

	@Test
	@DisplayName("MixDecOnline with a too long remaining election public key throws an IllegalArgumentException")
	void mixDecOnlineWithCiphertextsTooLongRemainingElectionPublicKey() {
		remainingElectionPublicKey = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, NUM_KEY_ELEMENTS + 1, randomService).getPublicKey();
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, remainingElectionPublicKey, ccmKeyPair));
		assertEquals("The remaining election public key must not be longer than the control component public key.", exception.getMessage());
	}

	@Test
	@DisplayName("MixDecOnline with ciphertext vector from other group throws an IllegalArgumentException")
	void mixDecOnlineWithCiphertextsFromOtherGroup() {
		ciphertexts = new ElGamalGenerator(otherGqGroup).genRandomCiphertextVector(10, CIPHERTEXT_SIZE);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, remainingElectionPublicKey, ccmKeyPair));
		assertEquals("The ciphertexts to be decrypted must have the same group as the remaining election public key.", exception.getMessage());
	}

	@Test
	@DisplayName("MixDecOnline with remaining election public key from other group throws an IllegalArgumentException")
	void mixDecOnlineWithRemainingElectionPublicKeyFromOtherGroup() {
		remainingElectionPublicKey = ElGamalMultiRecipientKeyPair.genKeyPair(otherGqGroup, NUM_KEY_ELEMENTS, randomService).getPublicKey();
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, remainingElectionPublicKey, ccmKeyPair));
		assertEquals("The ciphertexts to be decrypted must have the same group as the remaining election public key.", exception.getMessage());
	}

	@Test
	@DisplayName("MixDecOnline with ccm election key pair from other group throws an IllegalArgumentException")
	void mixDecOnlineWithCCMElectionKeyPairFromOtherGroup() {
		ccmKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(otherGqGroup, NUM_KEY_ELEMENTS, randomService);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> mixDecryptOnlineService.mixDecOnline(ELECTION_EVENT_ID, BALLOT_BOX_ID, ciphertexts, remainingElectionPublicKey, ccmKeyPair));
		assertEquals("The remaining election public key and the ccm election key must have the same group.", exception.getMessage());
	}
}
