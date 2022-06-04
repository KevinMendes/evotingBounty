/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.cert.X509Certificate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.MapperSetUp;
import ch.post.it.evoting.domain.SerializationUtils;

@DisplayName("An EncryptedVerifiableVotePayload")
class EncryptedVerifiableVotePayloadTest extends MapperSetUp {

	private static final String electionEventId = "1";
	private static final String verificationCardSetId = "2";
	private static final String verificationCardId = "3";
	private static final String requestId = "4";
	private static final HashService hashService = HashService.getInstance();

	private static ObjectNode rootNode;
	private static EncryptedVerifiableVotePayload encryptedVerifiableVotePayload;

	@BeforeAll
	static void setUpAll() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
		final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(ZqGroup.sameOrderAs(gqGroup));

		// Create payload.
		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);
		final ElGamalMultiRecipientCiphertext encryptedVote = elGamalGenerator.genRandomCiphertext(2);
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote = elGamalGenerator.genRandomCiphertext(2);
		final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(2);
		final ExponentiationProof exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(),
				zqGroupGenerator.genRandomZqElementMember());
		final PlaintextEqualityProof plaintextEqualityProof = new PlaintextEqualityProof(zqGroupGenerator.genRandomZqElementMember(),
				zqGroupGenerator.genRandomZqElementVector(2));

		final EncryptedVerifiableVote encryptedVerifiableVote = new EncryptedVerifiableVote(contextIds, encryptedVote, exponentiatedEncryptedVote,
				encryptedPartialChoiceReturnCodes, exponentiationProof, plaintextEqualityProof);

		final EncryptedVerifiableVotePayload payload = new EncryptedVerifiableVotePayload(gqGroup, encryptedVerifiableVote, requestId);
		final byte[] payloadHash = hashService.recursiveHash(payload);
		final X509Certificate certificate = SerializationUtils.generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(payloadHash, new X509Certificate[] { certificate });
		payload.setSignature(signature);

		encryptedVerifiableVotePayload = payload;

		// Create expected Json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ObjectNode contextIdNode = mapper.createObjectNode();
		contextIdNode.put("electionEventId", electionEventId);
		contextIdNode.put("verificationCardSetId", verificationCardSetId);
		contextIdNode.put("verificationCardId", verificationCardId);

		final ObjectNode voteNode = mapper.createObjectNode();
		final ObjectNode encryptedVoteNode = SerializationUtils.createCiphertextNode(encryptedVote);
		final ObjectNode exponentiatedEncryptedVoteNode = SerializationUtils.createCiphertextNode(exponentiatedEncryptedVote);
		final ObjectNode encryptedPartialChoiceReturnCodesNode = SerializationUtils.createCiphertextNode(encryptedPartialChoiceReturnCodes);
		final ObjectNode exponentiationProofNode = SerializationUtils.createExponentiationProofNode(exponentiationProof);
		final ObjectNode plaintextEqualityProofNode = SerializationUtils.createPlaintextEqualityProofNode(plaintextEqualityProof);
		voteNode.set("contextIds", contextIdNode);
		voteNode.set("encryptedVote", encryptedVoteNode);
		voteNode.set("exponentiatedEncryptedVote", exponentiatedEncryptedVoteNode);
		voteNode.set("encryptedPartialChoiceReturnCodes", encryptedPartialChoiceReturnCodesNode);
		voteNode.set("exponentiationProof", exponentiationProofNode);
		voteNode.set("plaintextEqualityProof", plaintextEqualityProofNode);

		rootNode.set("encryptedVerifiableVote", voteNode);

		rootNode.put("requestId", requestId);

		final JsonNode signatureNode = SerializationUtils.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(encryptedVerifiableVotePayload);

		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws JsonProcessingException {
		final EncryptedVerifiableVotePayload deserializedPayload = mapper.readValue(rootNode.toString(), EncryptedVerifiableVotePayload.class);

		assertEquals(encryptedVerifiableVotePayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws JsonProcessingException {
		final EncryptedVerifiableVotePayload deserializedPayload = mapper
				.readValue(mapper.writeValueAsString(encryptedVerifiableVotePayload), EncryptedVerifiableVotePayload.class);

		assertEquals(encryptedVerifiableVotePayload, deserializedPayload);
	}

}
