/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.tally.mixonline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.CryptolibPayloadSignatureService;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeys;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.controlcomponents.keymanagement.KeyServicesMock;
import ch.post.it.evoting.controlcomponents.keymanagement.KeysManager;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetState;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;

@SpringBootTest
@ActiveProfiles("test")
class MixDecryptMessageConsumerTest {

	private static final String BALLOT_BOX_ID = "f0dd956605bb47d589f1bd7b195d6f38";
	private static final String ELECTION_EVENT_ID = "0b88257ec32142bb8ee0ed1bb70f362e";
	private static final GqGroup gqGroup = new GqGroup(new BigInteger(
			"16370518994319586760319791526293535327576438646782139419846004180837103527129035954742043590609421369665944746587885814920851694546456891767644945459124422553763416586515339978014154452159687109161090635367600349264934924141746082060353483306855352192358732451955232000593777554431798981574529854314651092086488426390776811367125009551346089319315111509277347117467107914073639456805159094562593954195960531136052208019343392906816001017488051366518122404819967204601427304267380238263913892658950281593755894747339126531018026798982785331079065126375455293409065540731646939808640273393855256230820509217411510058759"),
			new BigInteger(
					"8185259497159793380159895763146767663788219323391069709923002090418551763564517977371021795304710684832972373293942907460425847273228445883822472729562211276881708293257669989007077226079843554580545317683800174632467462070873041030176741653427676096179366225977616000296888777215899490787264927157325546043244213195388405683562504775673044659657555754638673558733553957036819728402579547281296977097980265568026104009671696453408000508744025683259061202409983602300713652133690119131956946329475140796877947373669563265509013399491392665539532563187727646704532770365823469904320136696927628115410254608705755029379"),
			BigInteger.valueOf(2));

	private static final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);

	private static ElGamalMultiRecipientPublicKey electionPublicKey;

	private MixnetInitialPayload payload;

	@Autowired
	private KeysManager keysManager;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MixDecryptMessageConsumer mixDecryptMessageConsumer;

	@Autowired
	private CryptolibPayloadSignatureService cryptolibPayloadSignatureService;

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ElectionSigningKeysService electionSigningKeysService;

	@BeforeEach
	void setup() throws PayloadSignatureException, KeyManagementException {
		final List<ElGamalMultiRecipientCiphertext> encryptedVotes = new ArrayList<>(elGamalGenerator.genRandomCiphertextVector(2, 1));
		electionPublicKey = elGamalGenerator.genRandomPublicKey(1);

		payload = new MixnetInitialPayload(ELECTION_EVENT_ID, BALLOT_BOX_ID, gqGroup, encryptedVotes, electionPublicKey);
		final X509Certificate[] certificateChain = new X509Certificate[1];
		certificateChain[0] = keysManager.getPlatformCACertificate();
		final ElectionSigningKeys electionSigningKeys = electionSigningKeysService.getElectionSigningKeys(ELECTION_EVENT_ID);
		payload = cryptolibPayloadSignatureService.sign(payload, electionSigningKeys.privateKey(), certificateChain);
	}

	@Test
	void onMessageWithMixnetStateForWrongNodeGivesError() throws IOException, KeyManagementException, PayloadSignatureException {
		final MixnetState mixnetState = new MixnetState(4, payload, 5, null);
		final Message message = createMessage(mixnetState);

		mixDecryptMessageConsumer.onMessage(message);

		final MixnetState outputMixnetState = getOutputMixnetState();
		assertErrorMessage(mixnetState, outputMixnetState,
				"The following fields present validation errors: [Node to visit is expected to be 1, but was 4]");
	}

	@Test
	void onMessageWithNullPayload() throws IOException, KeyManagementException, PayloadSignatureException {
		final MixnetState mixnetState = new MixnetState(1, null, 5, null);
		final Message message = createMessage(mixnetState);

		mixDecryptMessageConsumer.onMessage(message);

		final MixnetState outputMixnetState = getOutputMixnetState();
		assertErrorMessage(mixnetState, outputMixnetState, "The following fields present validation errors: [No payload provided]");
	}

	@Test
	void onMessageWithValidInput() throws IOException, KeyManagementException, PayloadSignatureException {
		final MixnetState mixnetState = new MixnetState(1, payload, 5, null);
		final Message message = createMessage(mixnetState);

		mixDecryptMessageConsumer.onMessage(message);

		final MixnetState outputMixnetState = getOutputMixnetState();
		assertNull(outputMixnetState.getMixnetError());
		assertNotEquals(mixnetState, outputMixnetState);
		assertNotNull(outputMixnetState.getPayload());
		assertEquals(mixnetState.getNodeToVisit(), outputMixnetState.getNodeToVisit());
		assertEquals(mixnetState.getRetryCount(), outputMixnetState.getRetryCount());
	}

	@Test
	void onMessageWithIncompatibleArguments()
			throws IOException, KeyManagementException, PayloadSignatureException {

		final List<ElGamalMultiRecipientCiphertext> otherEncryptedVotes = new ArrayList<>(elGamalGenerator.genRandomCiphertextVector(2, 2));
		final MixnetInitialPayload otherPayload = new MixnetInitialPayload(ELECTION_EVENT_ID, BALLOT_BOX_ID, gqGroup, otherEncryptedVotes,
				electionPublicKey);
		final X509Certificate[] certificateChain = new X509Certificate[1];
		certificateChain[0] = keysManager.getPlatformCACertificate();
		final MixnetInitialPayload otherSignedPayload = cryptolibPayloadSignatureService.sign(otherPayload,
				electionSigningKeysService.getElectionSigningKeys(ELECTION_EVENT_ID).privateKey(), certificateChain);

		final MixnetState mixnetState = new MixnetState(1, otherSignedPayload, 5, null);
		final Message message = createMessage(mixnetState);

		mixDecryptMessageConsumer.onMessage(message);

		final MixnetState outputMixnetState = getOutputMixnetState();
		assertErrorMessage(mixnetState, outputMixnetState,
				"Incompatible input arguments: The ciphertexts size must be equal to the number of allowed write-ins + 1.");
	}

	/**
	 * Asserts that both MixnetStates are the same, with the output MixnetState having an error message in addition.
	 *
	 * @param mixnetState       the original MixnetState sent to the control component.
	 * @param outputMixnetState the MixnetState received from the control component after processing.
	 * @param errorMessage      the error message that the outputMixnetState is expected to contain.
	 */
	private void assertErrorMessage(final MixnetState mixnetState, final MixnetState outputMixnetState, final String errorMessage) {
		assertNotNull(outputMixnetState.getMixnetError());
		assertNotEquals(mixnetState, outputMixnetState);
		assertEquals(mixnetState.getPayload(), outputMixnetState.getPayload());
		assertEquals(mixnetState.getNodeToVisit(), outputMixnetState.getNodeToVisit());
		assertEquals(mixnetState.getRetryCount(), outputMixnetState.getRetryCount());
		assertEquals(errorMessage, outputMixnetState.getMixnetError());
	}

	// Utility functions

	/**
	 * Serializes a MixnetState and puts it into a Message.
	 *
	 * @param mixnetState the MixnetState object to be passed to the message.
	 * @return a Message object that can be passed into a queue.
	 * @throws JsonProcessingException if the MixnetState object cannot be serialized
	 */
	private Message createMessage(final MixnetState mixnetState) throws JsonProcessingException {
		final String mixnetStateJson = objectMapper.writeValueAsString(mixnetState);
		final byte[] serializedMixnetState = mixnetStateJson.getBytes(StandardCharsets.UTF_8);
		final byte[] byteContent = new byte[serializedMixnetState.length + 1];
		byteContent[0] = 0;
		System.arraycopy(serializedMixnetState, 0, byteContent, 1, serializedMixnetState.length);

		return new Message(byteContent, new MessageProperties());
	}

	/**
	 * Returns the MixnetState object that was sent to the control components output queue.
	 *
	 * @return a MixnetState object
	 * @throws IOException if the sending of the MixnetState failed or it could not be deserialized.
	 */
	private MixnetState getOutputMixnetState() throws IOException {
		final ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(rabbitTemplate).convertAndSend(any(), argumentCaptor.capture());
		final byte[] update = argumentCaptor.getValue();
		final byte[] mixnetStateBytes = new byte[update.length - 1];
		System.arraycopy(update, 1, mixnetStateBytes, 0, update.length - 1);

		return objectMapper.readValue(mixnetStateBytes, MixnetState.class);
	}

	@TestConfiguration
	public static class TestConfig {

		private static final KeyServicesMock KEY_SERVICES_MOCK = new KeyServicesMock();

		@Bean
		@Primary
		public KeysManager getMockKeyManager() throws GeneralCryptoLibException {
			return KEY_SERVICES_MOCK.keyManager();
		}

		@Bean
		@Primary
		public ElectionSigningKeysService getMockElectionSigningKeysService() throws KeyManagementException {
			return KEY_SERVICES_MOCK.electionSigningKeysService();
		}
	}
}
