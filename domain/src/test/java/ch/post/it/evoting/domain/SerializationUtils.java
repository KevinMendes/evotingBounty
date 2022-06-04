/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain;

import static ch.post.it.evoting.cryptoprimitives.domain.mixnet.ConversionUtils.bigIntegerToHex;
import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;

import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.VerifiablePlaintextDecryption;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationInput;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationOutput;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.HadamardArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.MultiExponentiationArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ProductArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.ShuffleArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.SingleValueProductArgument;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.mixnet.ZeroArgument;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;

public class SerializationUtils {

	private static final GqGroup gqGroup = getGqGroup();
	private static final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
	private static ObjectMapper mapper = DomainObjectMapper.getNewInstance();

	private static final BigInteger ZERO = BigInteger.valueOf(0);
	private static final BigInteger ONE = BigInteger.valueOf(1);
	private static final BigInteger TWO = BigInteger.valueOf(2);
	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FOUR = BigInteger.valueOf(4);
	private static final BigInteger FIVE = BigInteger.valueOf(5);
	private static final BigInteger NINE = BigInteger.valueOf(9);

	private static final GqElement gOne = GqElementFactory.fromValue(ONE, gqGroup);
	private static final GqElement gThree = GqElementFactory.fromValue(THREE, gqGroup);
	private static final GqElement gFour = GqElementFactory.fromValue(FOUR, gqGroup);
	private static final GqElement gFive = GqElementFactory.fromValue(FIVE, gqGroup);
	private static final GqElement gNine = GqElementFactory.fromValue(NINE, gqGroup);

	private static final ZqElement zZero = ZqElement.create(ZERO, zqGroup);
	private static final ZqElement zOne = ZqElement.create(ONE, zqGroup);
	private static final ZqElement zTwo = ZqElement.create(TWO, zqGroup);
	private static final ZqElement zThree = ZqElement.create(THREE, zqGroup);
	private static final ZqElement zFour = ZqElement.create(FOUR, zqGroup);

	private SerializationUtils() {
		// Intentionally left blank.
	}

	public static GqGroup getGqGroup() {
		try (MockedStatic<SecurityLevelConfig> mockedSecurityLevel = Mockito.mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			return new GqGroup(BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(3));
		}
	}

	// ===============================================================================================================================================
	// Basic objects to serialize creation.
	// ===============================================================================================================================================

	static ElGamalMultiRecipientMessage getMessage() {
		final List<GqElement> messageElements = Arrays
				.asList(GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup), GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup));

		return new ElGamalMultiRecipientMessage(messageElements);
	}

	static GroupVector<ElGamalMultiRecipientMessage, GqGroup> getMessages(final int nbr) {
		return Collections.nCopies(nbr, getMessage()).stream().collect(GroupVector.toGroupVector());
	}

	public static List<ElGamalMultiRecipientCiphertext> getCiphertexts(final int nbr) {
		final GqElement gamma = GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup);
		final List<GqElement> phis = Arrays
				.asList(GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup), GqElementFactory.fromValue(BigInteger.valueOf(9), gqGroup));
		final ElGamalMultiRecipientCiphertext ciphertext = ElGamalMultiRecipientCiphertext.create(gamma, phis);

		return Collections.nCopies(nbr, ciphertext);
	}

	public static ElGamalMultiRecipientCiphertext getSinglePhiCiphertext() {
		final GqElement gamma = GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup);
		final List<GqElement> phis = Collections.singletonList(GqElementFactory.fromValue(BigInteger.valueOf(5), gqGroup));

		return ElGamalMultiRecipientCiphertext.create(gamma, phis);
	}

	public static GroupVector<DecryptionProof, ZqGroup> getDecryptionProofs(final int nbr) {
		final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
		final ZqElement e = ZqElement.create(2, zqGroup);
		final GroupVector<ZqElement, ZqGroup> z = GroupVector.of(ZqElement.create(1, zqGroup), ZqElement.create(3, zqGroup));
		final DecryptionProof decryptionProof = new DecryptionProof(e, z);

		return Collections.nCopies(nbr, decryptionProof).stream().collect(GroupVector.toGroupVector());
	}

	public static GroupVector<GqElement, GqGroup> getLongChoiceCodes(final int nbr) {
		return Collections.nCopies(nbr, gFive).stream().collect(GroupVector.toGroupVector());
	}

	public static ElGamalMultiRecipientPublicKey getPublicKey() {
		final List<GqElement> keyElements = Arrays
				.asList(GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup), GqElementFactory.fromValue(BigInteger.valueOf(9), gqGroup));
		return new ElGamalMultiRecipientPublicKey(keyElements);
	}

	public static ElGamalMultiRecipientPrivateKey getPrivateKey() {
		final List<ZqElement> keyElements = Arrays
				.asList(ZqElement.create(BigInteger.valueOf(2), zqGroup), ZqElement.create(BigInteger.valueOf(3), zqGroup));
		return new ElGamalMultiRecipientPrivateKey(keyElements);
	}

	public static VerifiableShuffle getVerifiableShuffle(final int ciphertextsNbr) {
		final List<ElGamalMultiRecipientCiphertext> ciphertexts = getCiphertexts(ciphertextsNbr);
		final ShuffleArgument shuffleArgument = createShuffleArgument();

		return new VerifiableShuffle(GroupVector.from(ciphertexts), shuffleArgument);
	}

	public static VerifiablePlaintextDecryption getVerifiablePlaintextDecryption(final int messagesNbr) {
		final GroupVector<ElGamalMultiRecipientMessage, GqGroup> messages = getMessages(messagesNbr);
		final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = getDecryptionProofs(messagesNbr);

		return new VerifiablePlaintextDecryption(messages, decryptionProofs);
	}

	public static ExponentiationProof createExponentiationProof() {
		final ZqElement e = ZqElement.create(1, zqGroup);
		final ZqElement z = ZqElement.create(3, zqGroup);

		return new ExponentiationProof(e, z);
	}

	public static ReturnCodeGenerationRequestPayload getRequestPayload(final Ballot ballot, final String tenantId, final String electionEventId,
			final String verificationCardSetId, final int chunkId) {

		final List<String> allowList = Arrays.asList("a", "b", "c", "d");

		final List<ElGamalMultiRecipientCiphertext> ciphertexts = getCiphertexts(2);
		final ElGamalMultiRecipientPublicKey verificationCardPublicKey = getPublicKey();
		final List<ReturnCodeGenerationInput> returnCodeGenerationInputs = Arrays
				.asList(new ReturnCodeGenerationInput("1", ciphertexts.get(0), ciphertexts.get(1), verificationCardPublicKey),
						new ReturnCodeGenerationInput("2", ciphertexts.get(0), ciphertexts.get(1), verificationCardPublicKey));

		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(ballot);

		final ReturnCodeGenerationRequestPayload requestPayload = new ReturnCodeGenerationRequestPayload(tenantId, electionEventId,
				verificationCardSetId, allowList, chunkId, gqGroup, returnCodeGenerationInputs, combinedCorrectnessInformation);

		// Generate random bytes for signature content and create payload signature.
		final byte[] randomBytes = new byte[10];
		new SecureRandom().nextBytes(randomBytes);
		final X509Certificate certificate = generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });
		requestPayload.setSignature(signature);

		return requestPayload;
	}

	public static ReturnCodeGenerationResponsePayload getResponsePayload(final String tenantId, final String electionEventId,
			final String verificationCardSetId, final int chunkId) {

		final List<ReturnCodeGenerationOutput> returnCodeGenerationOutputs = Arrays
				.asList(getReturnCodeGenerationOutput("1"), getReturnCodeGenerationOutput("2"));

		final ReturnCodeGenerationResponsePayload responsePayload = new ReturnCodeGenerationResponsePayload(tenantId, electionEventId,
				verificationCardSetId, chunkId, gqGroup, returnCodeGenerationOutputs, 1);

		// Generate random bytes for signature content and create payload signature.
		final byte[] randomBytes = new byte[10];
		new SecureRandom().nextBytes(randomBytes);
		final X509Certificate certificate = generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });
		responsePayload.setSignature(signature);

		return responsePayload;
	}

	public static ReturnCodeGenerationOutput getReturnCodeGenerationOutput(final String verificationCardId) {
		final ElGamalMultiRecipientPublicKey voterChoiceReturnCodeGenerationPublicKey = SerializationUtils.getPublicKey();
		final ElGamalMultiRecipientPublicKey voterVoteCastReturnCodeGenerationPublicKey = SerializationUtils.getPublicKey();
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedPartialChoiceReturnCodes = SerializationUtils.getCiphertexts(1).get(0);
		final ExponentiationProof encryptedPartialChoiceReturnCodeExponentiationProof = SerializationUtils.createExponentiationProof();
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedConfirmationKey = SerializationUtils.getSinglePhiCiphertext();
		final ExponentiationProof encryptedConfirmationKeyExponentiationProof = SerializationUtils.createExponentiationProof();

		return new ReturnCodeGenerationOutput(verificationCardId, voterChoiceReturnCodeGenerationPublicKey,
				voterVoteCastReturnCodeGenerationPublicKey, exponentiatedEncryptedPartialChoiceReturnCodes,
				encryptedPartialChoiceReturnCodeExponentiationProof, exponentiatedEncryptedConfirmationKey,
				encryptedConfirmationKeyExponentiationProof);
	}

	// ===============================================================================================================================================
	// Nodes creation.
	// ===============================================================================================================================================

	public static JsonNode createEncryptionGroupNode(final GqGroup gqGroup) {
		final JsonNode encryptionGroupNode;
		try {
			encryptionGroupNode = mapper.readTree(mapper.writeValueAsString(gqGroup));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to create encryptionGroup node.");
		}

		return encryptionGroupNode;
	}

	static ObjectNode createMessageNode(final ElGamalMultiRecipientMessage elGamalMultiRecipientMessage) {
		final ObjectNode rootNode = mapper.createObjectNode();

		final ArrayNode messageArrayNode = rootNode.putArray("message");
		elGamalMultiRecipientMessage.stream().forEach(element -> messageArrayNode.add(bigIntegerToHex(element.getValue())));

		return rootNode;
	}

	static ArrayNode createMessagesNode(final List<ElGamalMultiRecipientMessage> messages) {
		final ArrayNode messageArrayNode = mapper.createArrayNode();
		messages.forEach(m -> messageArrayNode.add(createMessageNode(m)));

		return messageArrayNode;
	}

	public static ObjectNode createCiphertextNode(final ElGamalMultiRecipientCiphertext ciphertext) {
		final GqElement gamma = ciphertext.getGamma();
		final List<GqElement> phis = ciphertext.stream().skip(1).collect(Collectors.toList());
		final ObjectNode ciphertextNode = mapper.createObjectNode().put("gamma", bigIntegerToHex(gamma.getValue()));
		final ArrayNode phisArrayNode = ciphertextNode.putArray("phis");
		for (GqElement phi : phis) {
			phisArrayNode.add(bigIntegerToHex(phi.getValue()));
		}

		return ciphertextNode;
	}

	public static ArrayNode createCiphertextsNode(final List<ElGamalMultiRecipientCiphertext> ciphertexts) {
		final ArrayNode ciphertextsArrayNode = mapper.createArrayNode();
		for (ElGamalMultiRecipientCiphertext ciphertext : ciphertexts) {
			ciphertextsArrayNode.add(createCiphertextNode(ciphertext));
		}

		return ciphertextsArrayNode;
	}

	static ArrayNode createDecryptionProofsNode(final GroupVector<DecryptionProof, ZqGroup> decryptionProofs) {
		final ArrayNode decryptionProofsArrayNode = mapper.createArrayNode();

		final List<JsonNode> proofsNodes = decryptionProofs.stream().map(proof -> {
			try {
				return mapper.readTree(mapper.writeValueAsString(proof));
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Failed to serialize proofs.");
			}
		}).collect(Collectors.toList());

		for (JsonNode jsonNode : proofsNodes) {
			decryptionProofsArrayNode.add(jsonNode);
		}

		return decryptionProofsArrayNode;
	}

	public static ArrayNode createPublicKeyNode(final ElGamalMultiRecipientPublicKey publicKey) {
		final ArrayNode keyArrayNode = mapper.createArrayNode();
		for (int i = 0; i < publicKey.size(); i++) {
			keyArrayNode.add(bigIntegerToHex(publicKey.get(i).getValue()));
		}

		return keyArrayNode;
	}

	static ArrayNode createPrivateKeyNode(final ElGamalMultiRecipientPrivateKey privateKey) {
		final ArrayNode keyArrayNode = mapper.createArrayNode();
		for (int i = 0; i < privateKey.size(); i++) {
			keyArrayNode.add(bigIntegerToHex(privateKey.get(i).getValue()));
		}

		return keyArrayNode;
	}

	static ObjectNode createVerifiableShuffleNode(final VerifiableShuffle verifiableShuffle) {
		final ObjectNode rootNode = mapper.createObjectNode();

		final ArrayNode shuffledCiphertextsNode = SerializationUtils.createCiphertextsNode(verifiableShuffle.getShuffledCiphertexts());
		rootNode.set("shuffledCiphertexts", shuffledCiphertextsNode);

		final JsonNode shuffleArgumentNode = createShuffleArgumentNode();
		rootNode.set("shuffleArgument", shuffleArgumentNode);

		return rootNode;
	}

	static ObjectNode createVerifiablePlaintextDecryptionNode(final VerifiablePlaintextDecryption verifiablePlaintextDecryption) {
		final ObjectNode rootNode = mapper.createObjectNode();

		final ArrayNode messagesNode = createMessagesNode(verifiablePlaintextDecryption.getDecryptedVotes());
		rootNode.set("decryptedVotes", messagesNode);

		final ArrayNode decryptionProofsNode = createDecryptionProofsNode(verifiablePlaintextDecryption.getDecryptionProofs());
		rootNode.set("decryptionProofs", decryptionProofsNode);

		return rootNode;
	}

	public static ArrayNode createGqGroupVectorNode(final GroupVector<GqElement, GqGroup> groupVector) {
		final ArrayNode arrayNode = mapper.createArrayNode();
		groupVector.forEach(lcc -> arrayNode.add(bigIntegerToHex(lcc.getValue())));

		return arrayNode;
	}

	public static JsonNode createLVCCNode(final GqElement longVoteCastCode) {
		try {
			return mapper.readTree(mapper.writeValueAsString(longVoteCastCode));
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Failed to construct long vote cast code node.", e);
		}
	}

	public static ObjectNode createExponentiationProofNode(final ExponentiationProof exponentiationProof) {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("e", bigIntegerToHex(exponentiationProof.get_e().getValue()));
		rootNode.put("z", bigIntegerToHex(exponentiationProof.get_z().getValue()));

		return rootNode;
	}

	public static ArrayNode createExponentiationProofsNode(final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs) {
		final ArrayNode arrayNode = mapper.createArrayNode();
		exponentiationProofs.forEach(proof -> arrayNode.add(createExponentiationProofNode(proof)));

		return arrayNode;
	}

	public static ObjectNode createPlaintextEqualityProofNode(final PlaintextEqualityProof plaintextEqualityProof) {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("e", bigIntegerToHex(plaintextEqualityProof.get_e().getValue()));

		final ArrayNode arrayNode = mapper.createArrayNode();
		plaintextEqualityProof.get_z()
				.forEach(el -> arrayNode.add(bigIntegerToHex(el.getValue())));

		rootNode.set("z", arrayNode);

		return rootNode;
	}

	public static ObjectNode createRequestPayloadNode(final ReturnCodeGenerationRequestPayload requestPayload) throws JsonProcessingException {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("tenantId", requestPayload.getTenantId());
		rootNode.put("electionEventId", requestPayload.getElectionEventId());
		rootNode.put("verificationCardSetId", requestPayload.getVerificationCardSetId());
		rootNode.put("chunkId", requestPayload.getChunkId());

		final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode returnCodeGenerationInputsNode = mapper.readTree(mapper.writeValueAsString(requestPayload.getReturnCodeGenerationInputs()));
		rootNode.set("returnCodeGenerationInputs", returnCodeGenerationInputsNode);

		final JsonNode combinedCorrectnessInformationNode = mapper
				.readTree(mapper.writeValueAsString(requestPayload.getCombinedCorrectnessInformation()));
		rootNode.set("combinedCorrectnessInformation", combinedCorrectnessInformationNode);

		final JsonNode signatureNode = SerializationUtils.createSignatureNode(requestPayload.getSignature());
		rootNode.set("signature", signatureNode);

		return rootNode;
	}

	public static ObjectNode createResponsePayloadNode(final ReturnCodeGenerationResponsePayload responsePayload) throws JsonProcessingException {
		final ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("tenantId", responsePayload.getTenantId());
		rootNode.put("electionEventId", responsePayload.getElectionEventId());
		rootNode.put("verificationCardSetId", responsePayload.getVerificationCardSetId());
		rootNode.put("chunkId", responsePayload.getChunkId());

		final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final JsonNode returnCodeGenerationOutputsNode = mapper.readTree(mapper.writeValueAsString(responsePayload.getReturnCodeGenerationOutputs()));
		rootNode.set("returnCodeGenerationOutputs", returnCodeGenerationOutputsNode);

		rootNode.put("nodeId", 1);

		final JsonNode signatureNode = SerializationUtils.createSignatureNode(responsePayload.getSignature());
		rootNode.set("signature", signatureNode);

		return rootNode;
	}

	// ===============================================================================================================================================
	// Arguments creation.
	// ===============================================================================================================================================

	public static ShuffleArgument createShuffleArgument() {
		// This is an example for m,n,l = 2.

		// ZeroArgument.
		final ZeroArgument zeroArgument = new ZeroArgument.Builder().with_c_A_0(gNine).with_c_B_m(gFive)
				.with_c_d(GroupVector.of(gFive, gNine, gFour, gOne, gFour)).with_a_prime(GroupVector.of(zThree, zTwo))
				.with_b_prime(GroupVector.of(zFour, zThree)).with_r_prime(zOne).with_s_prime(zThree).with_t_prime(zOne).build();

		// HadamardArgument.
		GroupVector<GqElement, GqGroup> commitmentsB = GroupVector.of(gNine, gFive);
		final HadamardArgument hadamardArgument = new HadamardArgument(commitmentsB, zeroArgument);

		// SingleValueProductArgument.
		final SingleValueProductArgument singleValueProductArgument = new SingleValueProductArgument.Builder().with_c_d(gFour).with_c_delta(gFour)
				.with_c_Delta(gFive).with_a_tilde(GroupVector.of(zTwo, zOne)).with_b_tilde(GroupVector.of(zTwo, zThree)).with_r_tilde(zOne)
				.with_s_tilde(zZero).build();

		// ProductArgument.
		final ProductArgument productArgument = new ProductArgument(gFour, hadamardArgument, singleValueProductArgument);

		// MultiExponentiationArgument
		final ElGamalMultiRecipientCiphertext e0 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gFive, gOne));
		final ElGamalMultiRecipientCiphertext e1 = ElGamalMultiRecipientCiphertext.create(gFour, GroupVector.of(gOne, gFive));
		final ElGamalMultiRecipientCiphertext e2 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gFive, gFive));
		final ElGamalMultiRecipientCiphertext e3 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gThree, gNine));
		final MultiExponentiationArgument multiExponentiationArgument = new MultiExponentiationArgument.Builder().with_c_A_0(gThree)
				.with_c_B(GroupVector.of(gOne, gNine, gOne, gOne)).with_E(GroupVector.of(e0, e1, e2, e3)).with_a(GroupVector.of(zFour, zFour))
				.with_r(zZero).with_b(zFour).with_s(zFour).with_tau(zZero).build();

		// ShuffleArgument.
		return new ShuffleArgument.Builder().with_c_A(GroupVector.of(gNine, gFive)).with_c_B(GroupVector.of(gNine, gFive))
				.with_productArgument(productArgument).with_multiExponentiationArgument(multiExponentiationArgument).build();
	}

	static ShuffleArgument createSimplestShuffleArgument() {
		// This is an example for m=1,n=2,l=1.

		// SingleValueProductArgument.
		final SingleValueProductArgument singleValueProductArgument = new SingleValueProductArgument.Builder().with_c_d(gFour).with_c_delta(gFour)
				.with_c_Delta(gFive).with_a_tilde(GroupVector.of(zOne, zTwo)).with_b_tilde(GroupVector.of(zZero, zTwo)).with_r_tilde(zZero)
				.with_s_tilde(zZero).build();

		// ProductArgument.
		final ProductArgument productArgument = new ProductArgument(singleValueProductArgument);

		// MultiExponentiationArgument
		final ElGamalMultiRecipientCiphertext e0 = ElGamalMultiRecipientCiphertext.create(gFive, GroupVector.of(gThree));
		final ElGamalMultiRecipientCiphertext e1 = ElGamalMultiRecipientCiphertext.create(gFour, GroupVector.of(gNine));
		final MultiExponentiationArgument multiExponentiationArgument = new MultiExponentiationArgument.Builder().with_c_A_0(gFour)
				.with_c_B(GroupVector.of(gFive, gFive)).with_E(GroupVector.of(e0, e1)).with_a(GroupVector.of(zThree, zFour)).with_r(zTwo)
				.with_b(zFour).with_s(zFour).with_tau(zZero).build();

		// ShuffleArgument.
		return new ShuffleArgument.Builder().with_c_A(GroupVector.of(gThree)).with_c_B(GroupVector.of(gFour)).with_productArgument(productArgument)
				.with_multiExponentiationArgument(multiExponentiationArgument).build();
	}

	static JsonNode createShuffleArgumentNode() {
		final JsonNode jsonNode;
		try {
			jsonNode = mapper.readTree(getShuffleArgumentJson());
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to read ShuffleArgument json.");
		}

		return jsonNode;
	}

	static String getShuffleArgumentJson() {
		// Return corresponding json to createShuffleArgument.
		return "{\"c_A\":[\"0x9\",\"0x5\"],\"c_B\":[\"0x9\",\"0x5\"],\"productArgument\":{\"c_b\":\"0x4\",\"hadamardArgument\":{\"c_b\":[\"0x9\",\"0x5\"],\"zeroArgument\":{\"c_A_0\":\"0x9\",\"c_B_m\":\"0x5\",\"c_d\":[\"0x5\",\"0x9\",\"0x4\",\"0x1\",\"0x4\"],\"a_prime\":[\"0x3\",\"0x2\"],\"b_prime\":[\"0x4\",\"0x3\"],\"r_prime\":\"0x1\",\"s_prime\":\"0x3\",\"t_prime\":\"0x1\"}},\"singleValueProductArgument\":{\"c_d\":\"0x4\",\"c_delta\":\"0x4\",\"c_Delta\":\"0x5\",\"a_tilde\":[\"0x2\",\"0x1\"],\"b_tilde\":[\"0x2\",\"0x3\"],\"r_tilde\":\"0x1\",\"s_tilde\":\"0x0\"}},\"multiExponentiationArgument\":{\"c_A_0\":\"0x3\",\"c_B\":[\"0x1\",\"0x9\",\"0x1\",\"0x1\"],\"E\":[{\"gamma\":\"0x5\",\"phis\":[\"0x5\",\"0x1\"]},{\"gamma\":\"0x4\",\"phis\":[\"0x1\",\"0x5\"]},{\"gamma\":\"0x5\",\"phis\":[\"0x5\",\"0x5\"]},{\"gamma\":\"0x5\",\"phis\":[\"0x3\",\"0x9\"]}],\"a\":[\"0x4\",\"0x4\"],\"r\":\"0x0\",\"b\":\"0x4\",\"s\":\"0x4\",\"tau\":\"0x0\"}}";
	}

	public static JsonNode createSignatureNode(final CryptoPrimitivesPayloadSignature signature) {
		try {
			return mapper.readTree(mapper.writeValueAsString(signature));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize signature.");
		}
	}

	public static X509Certificate generateTestCertificate() {
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
