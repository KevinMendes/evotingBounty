/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.ElectionEventEntity;
import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.controlcomponents.TestDatabaseCleanUpService;
import ch.post.it.evoting.controlcomponents.VerificationCard;
import ch.post.it.evoting.controlcomponents.VerificationCardService;
import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeys;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.controlcomponents.voting.VotingIntegrationTestBase;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.distributedprocessing.commands.Command;
import ch.post.it.evoting.distributedprocessing.commands.CommandId;
import ch.post.it.evoting.distributedprocessing.commands.CommandService;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCCPayload;

@DisplayName("PartialDecryptProcessor consuming")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PartialDecryptProcessorIT extends VotingIntegrationTestBase {

	private static byte[] encryptedVotePayloadBytes;
	private static PartiallyDecryptedEncryptedPCCPayload partiallyDecryptedEncryptedPCCPayload;
	private static String firstRequestUUID;

	@Value("${nodeID}")
	private int nodeId;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RabbitAdmin rabbitAdmin;

	@SpyBean
	private PartialDecryptProcessor partialDecryptProcessor;

	@SpyBean
	private CommandService commandService;

	@MockBean
	private ElectionSigningKeysService electionSigningKeysService;

	@MockBean
	private PartialDecryptService partialDecryptService;

	@BeforeAll
	static void setUpAll(
			@Autowired
			final ObjectMapper objectMapper,
			@Autowired
			final ElectionEventService electionEventService,
			@Autowired
			final VerificationCardSetService verificationCardSetService,
			@Autowired
			final VerificationCardService verificationCardService) throws IOException, URISyntaxException {

		// Must match the group in the json.
		final GqGroup encryptionGroup = new GqGroup(BigInteger.valueOf(59), BigInteger.valueOf(29), BigInteger.valueOf(3));

		// Save election event.
		final ElectionEventEntity electionEventEntity = electionEventService.save(electionEventId, encryptionGroup);

		// Save verification card set.
		final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity(verificationCardSetId, electionEventEntity);
		verificationCardSetEntity.setAllowList(Collections.emptyList());
		verificationCardSetEntity.setCombinedCorrectnessInformation(new CombinedCorrectnessInformation(Collections.emptyList()));
		verificationCardSetService.save(verificationCardSetEntity);

		// Save verification card.
		final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(encryptionGroup);
		final ElGamalMultiRecipientPublicKey publicKey = elGamalGenerator.genRandomPublicKey(1);
		final String verificationCardId = "dd4063884c144446a6dfb63c42eb9e86";

		verificationCardService.save(new VerificationCard(verificationCardId, verificationCardSetId, publicKey));

		// Request payload.
		final Path encryptedVotePayloadPath = Paths.get(
				PartialDecryptProcessorIT.class.getResource("/voting/sendvote/encrypted-verifiable-vote-payload.json").toURI());

		encryptedVotePayloadBytes = Files.readAllBytes(encryptedVotePayloadPath);

		// Response from PartialDecryptService.
		final URL partiallyDecryptedPCCPayloadUrl = PartialDecryptProcessorIT.class.getResource(
				"/voting/sendvote/partially-decrypted-encrypted-pcc-payload.json");
		partiallyDecryptedEncryptedPCCPayload = objectMapper.readValue(partiallyDecryptedPCCPayloadUrl, PartiallyDecryptedEncryptedPCCPayload.class);

		// UUID of the first request received.
		firstRequestUUID = UUID.randomUUID().toString();
	}

	@AfterAll
	static void cleanUp(
			@Autowired
			final TestDatabaseCleanUpService testDatabaseCleanUpService) {

		testDatabaseCleanUpService.cleanUp();
	}

	@Test
	@Order(0)
	@DisplayName("a request for the first time perform calculation")
	void firstTimeCommand() throws KeyManagementException, NoSuchAlgorithmException {
		when(partialDecryptService.performPartialDecrypt(any())).thenReturn(
				partiallyDecryptedEncryptedPCCPayload.getPartiallyDecryptedEncryptedPCC());

		final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		final X509Certificate[] x509CertificateChain = new X509Certificate[] { generateTestCertificate() };
		final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(keyPair.getPrivate(), x509CertificateChain);
		doReturn(electionSigningKeys).when(electionSigningKeysService).getElectionSigningKeys(electionEventId);

		// Send to request queue the EncryptedVerifiableVotePayload.
		final MessageProperties messageProperties = new MessageProperties();
		final String correlationId = firstRequestUUID;
		messageProperties.setCorrelationId(correlationId);

		final Message message = new Message(encryptedVotePayloadBytes, messageProperties);
		rabbitTemplate.send(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE, message);

		// Verifications.
		final Message responseMessage = rabbitTemplate.receive(PARTIAL_DECRYPT_RESPONSE_QUEUE, 5000);
		assertNotNull(responseMessage);
		assertEquals(correlationId, responseMessage.getMessageProperties().getCorrelationId());

		verify(commandService, times(1)).saveRequest(any(), any());
		verify(commandService, times(1)).saveResponse(any(), any());
		verify(partialDecryptService, times(1)).performPartialDecrypt(any());

		final String contextId = String.join("-", Arrays.asList(electionEventId, verificationCardSetId, verificationCardId));
		final CommandId commandId =
				new CommandId.Builder()
						.contextId(contextId)
						.context(Context.VOTING_RETURN_CODES_PARTIAL_DECRYPT_PCC.toString())
						.correlationId(correlationId)
						.nodeId(nodeId)
						.build();
		final Optional<Command> command = commandService.findIdenticalCommand(commandId);
		assertTrue(command.isPresent());
		assertNotNull(command.get().getResponsePayload());
	}

	@Test
	@Order(1)
	@DisplayName("an identical command does not save request/response and sends previous response")
	void sendPreviousResponseWithIdenticalCommand() {
		// Send to request queue the EncryptedVerifiableVotePayload.
		final MessageProperties messageProperties = new MessageProperties();
		messageProperties.setCorrelationId(firstRequestUUID);

		final Message message = new Message(encryptedVotePayloadBytes, messageProperties);
		rabbitTemplate.send(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE, message);

		// Verifications.
		final Message responseMessage = rabbitTemplate.receive(PARTIAL_DECRYPT_RESPONSE_QUEUE, 5000);
		assertNotNull(responseMessage);
		assertEquals(firstRequestUUID, responseMessage.getMessageProperties().getCorrelationId());

		verify(commandService, times(0)).saveRequest(any(), any());
		verify(commandService, times(0)).saveResponse(any(), any());
	}

	@Test
	@Order(2)
	@DisplayName("an identical command but different payload is rejected")
	void rejectIdenticalCommandDifferentPayload() throws IOException, URISyntaxException {
		//		doReturn(encryptionParameters).when(electionEventService).getEncryptionGroup(electionEventId);

		// Payload with same ids but a different encrypted vote.
		final Path encryptedVotePayloadPath = Paths.get(
				PartialDecryptProcessorIT.class.getResource("/voting/sendvote/encrypted-verifiable-vote-2.json").toURI());
		final byte[] differentPayloadBytes = Files.readAllBytes(encryptedVotePayloadPath);

		// Send to request queue the EncryptedVerifiableVotePayload.
		final MessageProperties messageProperties = new MessageProperties();
		messageProperties.setCorrelationId(firstRequestUUID);

		final Message message = new Message(differentPayloadBytes, messageProperties);
		rabbitTemplate.send(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE, message);

		//This will wait that the queue empties once
		await().until(() -> queueIsEmpty(PARTIAL_DECRYPT_REQUEST_QUEUE));

		verify(commandService, times(0)).saveRequest(any(), any());
		verify(commandService, times(0)).saveResponse(any(), any());
		verify(partialDecryptProcessor, after(5000).times(1)).onMessage(any());
	}

	private boolean queueIsEmpty(String queue) {
		return Objects.requireNonNull(rabbitAdmin.getQueueInfo(queue)).getMessageCount() == 0;
	}

	private static X509Certificate generateTestCertificate() {
		try {
			final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			final KeyPair keyPair = keyPairGenerator.generateKeyPair();

			final X500Name x500Name = new X500Name("CN=test.com, OU=test, O=test., L=test, ST=test, C=CA");
			final Date start = new Date();
			final Date until = Date.from(LocalDate.now().plus(365, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.UTC));
			final SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

			final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(x500Name, new BigInteger(10, new SecureRandom()), start,
					until, x500Name, subjectPublicKeyInfo);

			final JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SHA256withRSA");
			final ContentSigner signer = contentSignerBuilder.build(keyPair.getPrivate());
			final byte[] certificateBytes = certificateBuilder.build(signer).getEncoded();

			final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

			return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate X509Certificate.", e);
		}
	}

}
