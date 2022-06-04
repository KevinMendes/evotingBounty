/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.InvalidPayloadSignatureException;
import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.returncodes.ComputeResults;
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.domain.voting.sendvote.CombinedPartiallyDecryptedEncryptedPCCPayload;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVote;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVotePayload;
import ch.post.it.evoting.domain.voting.sendvote.LongChoiceReturnCodesShare;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCCPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContent;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContentRepository;
import ch.post.it.evoting.votingserver.voteverification.domain.model.platform.VvPlatformCARepository;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.VerificationRepository;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.MessageBrokerOrchestratorClient;

/**
 * Generate the short choice return codes based on the encrypted partial choice return codes - in interaction with the control components.
 */
@Stateless
public class ChoiceReturnCodesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceReturnCodesService.class);
	private static final String GROUP_ATTRIBUTE = "group";

	private final HashService hashService;
	private final ObjectMapper objectMapper;
	private final ExtractCRCService extractCRCService;
	private final PlatformCARepository platformCARepository;
	private final VerificationRepository verificationRepository;
	private final CryptolibPayloadSignatureService payloadSignatureService;
	private final VerificationContentRepository verificationContentRepository;
	private final MessageBrokerOrchestratorClient messageBrokerOrchestratorClient;

	@Inject
	ChoiceReturnCodesService(
			final HashService hashService,
			final ObjectMapper objectMapper,
			final ExtractCRCService extractCRCService,
			@VvPlatformCARepository
			final PlatformCARepository platformCARepository,
			final VerificationRepository verificationRepository,
			final CryptolibPayloadSignatureService payloadSignatureService,
			final VerificationContentRepository verificationContentRepository,
			final MessageBrokerOrchestratorClient messageBrokerOrchestratorClient) {

		this.hashService = hashService;
		this.objectMapper = objectMapper;
		this.extractCRCService = extractCRCService;
		this.platformCARepository = platformCARepository;
		this.verificationRepository = verificationRepository;
		this.payloadSignatureService = payloadSignatureService;
		this.verificationContentRepository = verificationContentRepository;
		this.messageBrokerOrchestratorClient = messageBrokerOrchestratorClient;
	}

	/**
	 * Retrieves the Short Choice Return Codes from the vote with the help of the control components.
	 *
	 * @param tenantId              the tenant id.
	 * @param electionEventId       the election event id.
	 * @param verificationCardId    the verification card id.
	 * @param voteAndComputeResults the object containing the vote and optionally previous computation results.
	 * @return the Short Choice Return Codes.
	 * @throws IOException                     if any deserialization error occurs.
	 * @throws CryptographicOperationException if there is an error computing the Short Choice Return Codes.
	 * @throws ResourceNotFoundException       if an error occurs while accessing the database or calling the orchestrator.
	 */
	public ShortChoiceReturnCodeAndComputeResults retrieveShortChoiceReturnCodes(final String tenantId, final String electionEventId,
			final String verificationCardId, final VoteAndComputeResults voteAndComputeResults)
			throws IOException, CryptographicOperationException, ResourceNotFoundException {

		final Vote vote = voteAndComputeResults.getVote();
		final Verification verification = verificationRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId,
				verificationCardId);
		final String verificationCardSetId = verification.getVerificationCardSetId();

		checkArgument(vote.getElectionEventId().equals(electionEventId));
		checkArgument(vote.getVerificationCardSetId().equals(verificationCardSetId));
		checkArgument(vote.getVerificationCardId().equals(verificationCardId));

		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);

		final List<String> shortChoiceReturnCodes;
		final List<LongReturnCodesSharePayload> longChoiceReturnCodesSharePayloads;

		final ComputeResults computeResults = voteAndComputeResults.getComputeResults();
		if (computeResults != null && computeResults.getComputationResults() != null && !computeResults.getComputationResults().isEmpty()) {
			LOGGER.info("Long Choice Return Codes previously computed, extracting short codes. [contextIds: {}]", contextIds);

			// The long Choice Return Codes have been previously computed. This can happen during a re-login event.
			final String computationResultsString = computeResults.getComputationResults();
			longChoiceReturnCodesSharePayloads = Arrays.asList(objectMapper.readValue(computationResultsString, LongReturnCodesSharePayload[].class));
		} else {
			LOGGER.info("Long Choice Return Codes not previously computed, requesting control components. [contextIds: {}]", contextIds);

			// Transform Vote to EncryptedVerifiableVote.
			final EncryptedVerifiableVote encryptedVerifiableVote = voteToVerifiableVote(tenantId, electionEventId, verificationCardSetId,
					verificationCardId, vote);

			// Create and sign EncryptedVerifiableVotePayload with secret signing key.
			final GqGroup gqGroup = encryptedVerifiableVote.getEncryptedVote().getGroup();
			final String requestId = CryptoPrimitivesService.get().genRandomBase32String(32);
			final EncryptedVerifiableVotePayload encryptedVerifiableVotePayload = new EncryptedVerifiableVotePayload(gqGroup, encryptedVerifiableVote,
					requestId);
			// Currently, we do not have a signing key in the vote-verification.
			final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(new byte[] {}, new X509Certificate[] {});
			encryptedVerifiableVotePayload.setSignature(signature);

			// Ask the control components to partially decrypt the pCC.
			final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads = decryptEncryptedPartialChoiceReturnCodes(
					contextIds, encryptedVerifiableVotePayload);

			// Combine response payloads.
			final CombinedPartiallyDecryptedEncryptedPCCPayload combinedPCCPayloads = combinePCCPayloads(partiallyDecryptedEncryptedPCCPayloads);

			// Ask the control components to compute the Long Choice Return Codes shares. The DecryptPCC_j will be done at same time by the CCs.
			longChoiceReturnCodesSharePayloads = retrieveLongChoiceReturnCodesShares(contextIds, combinedPCCPayloads);
		}

		// Retrieve short Choice Return Codes by combining CCR shares and looking up the CMTable.
		final List<GroupVector<GqElement, GqGroup>> lCCShares = getLCCShares(longChoiceReturnCodesSharePayloads);
		shortChoiceReturnCodes = extractCRCService.extractCRC(lCCShares, verificationCardId, vote);

		LOGGER.info("Short Choice Return Codes successfully retrieved. [electionEventId: {}, verificationCardSetId: {}, verificationCardId: {}]",
				electionEventId, verificationCardSetId, verificationCardId);

		return createResponse(shortChoiceReturnCodes, longChoiceReturnCodesSharePayloads);
	}

	/**
	 * Converts the {@link Vote} to a {@link EncryptedVerifiableVote}.
	 */
	private EncryptedVerifiableVote voteToVerifiableVote(final String tenantId, final String electionEventId, final String verificationCardSetId,
			final String verificationCardId, final Vote vote) throws IOException, ResourceNotFoundException {

		// Retrieve group.
		final VerificationContent verificationContent = verificationContentRepository.findByTenantIdElectionEventIdVerificationCardSetId(tenantId,
				electionEventId, verificationCardSetId);
		final VoteVerificationContextData voteVerificationContextData = objectMapper.readValue(verificationContent.getJson(),
				VoteVerificationContextData.class);

		final EncryptionParameters encryptionParameters = voteVerificationContextData.getEncryptionParameters();
		final BigInteger p = new BigInteger(encryptionParameters.getP());
		final BigInteger q = new BigInteger(encryptionParameters.getQ());
		final BigInteger g = new BigInteger(encryptionParameters.getG());
		final GqGroup gqGroup = new GqGroup(p, q, g);

		// Read ids, encrypted vote and proofs.
		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);

		final ElGamalMultiRecipientCiphertext encryptedVote = objectMapper.reader()
				.withAttribute(GROUP_ATTRIBUTE, gqGroup)
				.readValue(vote.getEncryptedOptions(), ElGamalMultiRecipientCiphertext.class);

		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote = objectMapper.reader()
				.withAttribute(GROUP_ATTRIBUTE, gqGroup)
				.readValue(vote.getCipherTextExponentiations(), ElGamalMultiRecipientCiphertext.class);

		final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes = objectMapper.reader()
				.withAttribute(GROUP_ATTRIBUTE, gqGroup)
				.readValue(vote.getEncryptedPartialChoiceCodes(), ElGamalMultiRecipientCiphertext.class);

		final ExponentiationProof exponentiationProof = objectMapper.reader()
				.withAttribute(GROUP_ATTRIBUTE, gqGroup)
				.readValue(vote.getExponentiationProof(), ExponentiationProof.class);

		final PlaintextEqualityProof plaintextEqualityProof = objectMapper.reader()
				.withAttribute(GROUP_ATTRIBUTE, gqGroup)
				.readValue(vote.getPlaintextEqualityProof(), PlaintextEqualityProof.class);

		return new EncryptedVerifiableVote(contextIds, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
				exponentiationProof, plaintextEqualityProof);
	}

	/**
	 * Calls the orchestrator which asks the control components to execute PartialDecryptPCC_j algorithm and collects their contributions.
	 */
	private List<PartiallyDecryptedEncryptedPCCPayload> decryptEncryptedPartialChoiceReturnCodes(final ContextIds contextIds,
			final EncryptedVerifiableVotePayload encryptedVerifiableVotePayload) throws RetrofitException {

		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		// Call orchestrator.
		final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads = RetrofitConsumer.processResponse(
				messageBrokerOrchestratorClient.getChoiceReturnCodesPartialDecryptContributions(electionEventId, verificationCardSetId,
						verificationCardId, encryptedVerifiableVotePayload));
		LOGGER.info("Partial decryptions received from the control-components. contextIds: {}]", contextIds);

		// Verify response payloads signatures and consistency.
		verifyPCCPayloads(contextIds, partiallyDecryptedEncryptedPCCPayloads);

		return partiallyDecryptedEncryptedPCCPayloads;
	}

	/**
	 * Verifies the signatures and consistency of the received {@link PartiallyDecryptedEncryptedPCCPayload}s.
	 */
	private void verifyPCCPayloads(final ContextIds contextIds, final List<PartiallyDecryptedEncryptedPCCPayload> pCCPayloads) {

		final X509Certificate rootCertificate;
		try {
			rootCertificate = (X509Certificate) PemUtils.certificateFromPem(platformCARepository.getRootCACertificate().getCertificateContent());
		} catch (final GeneralCryptoLibException | ResourceNotFoundException e) {
			throw new IllegalArgumentException("Failed to retrieve root certificate to verify payload signature.", e);
		}

		for (final PartiallyDecryptedEncryptedPCCPayload payload : pCCPayloads) {
			final byte[] payloadHash = hashService.recursiveHash(payload);

			final boolean signatureValid;
			try {
				signatureValid = payloadSignatureService.verify(payload.getSignature(), rootCertificate, payloadHash);
			} catch (final PayloadVerificationException e) {
				throw new IllegalArgumentException("Failed to verify payload signature.", e);
			}

			if (!signatureValid) {
				throw new InvalidPayloadSignatureException(PartiallyDecryptedEncryptedPCCPayload.class,
						String.format("[contextIds: %s]", contextIds));
			}
		}

	}

	/**
	 * Combines the control components contributions into a {@link CombinedPartiallyDecryptedEncryptedPCCPayload}.
	 */
	private CombinedPartiallyDecryptedEncryptedPCCPayload combinePCCPayloads(final List<PartiallyDecryptedEncryptedPCCPayload> pCCPayloads) {
		final CombinedPartiallyDecryptedEncryptedPCCPayload combinedPayload = new CombinedPartiallyDecryptedEncryptedPCCPayload(pCCPayloads);

		// Sign combined payload.
		// Currently, we do not have a signing key in the vote-verification.
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(new byte[] {}, new X509Certificate[] {});
		combinedPayload.setSignature(signature);

		return combinedPayload;
	}

	/**
	 * Extracts the lCC shares from the contributions.
	 */
	private List<GroupVector<GqElement, GqGroup>> getLCCShares(final List<LongReturnCodesSharePayload> longReturnCodesSharePayloads) {
		return longReturnCodesSharePayloads.stream()
				.map(LongReturnCodesSharePayload::getLongReturnCodesShare)
				.map(LongChoiceReturnCodesShare.class::cast)
				.map(LongChoiceReturnCodesShare::getLongChoiceReturnCodeShare)
				.collect(Collectors.toList());
	}

	/**
	 * Asks the control components to compute the Long Choice Return Codes.
	 */
	private List<LongReturnCodesSharePayload> retrieveLongChoiceReturnCodesShares(final ContextIds contextIds,
			final CombinedPartiallyDecryptedEncryptedPCCPayload combinedPartiallyDecryptedEncryptedPCCPayload)
			throws ResourceNotFoundException {

		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		final List<LongReturnCodesSharePayload> longReturnCodesSharePayloads = RetrofitConsumer.processResponse(
				messageBrokerOrchestratorClient.getLongChoiceReturnCodesContributions(electionEventId, verificationCardSetId, verificationCardId,
						combinedPartiallyDecryptedEncryptedPCCPayload));

		// Choice Return Codes computation response correctly received.
		LOGGER.info("Retrieved the long Choice Return Code shares payloads. [contextIds: {}]", contextIds);
		verifySharesPayloadSignatures(contextIds, longReturnCodesSharePayloads);

		return longReturnCodesSharePayloads;
	}

	/**
	 * Verifies the signatures of the received {@link LongReturnCodesSharePayload}s.
	 */
	private void verifySharesPayloadSignatures(final ContextIds contextIds, final List<LongReturnCodesSharePayload> longReturnCodesSharePayloads) {

		final X509Certificate rootCertificate;
		try {
			rootCertificate = (X509Certificate) PemUtils.certificateFromPem(platformCARepository.getRootCACertificate().getCertificateContent());
		} catch (final GeneralCryptoLibException | ResourceNotFoundException e) {
			throw new IllegalArgumentException("Failed to retrieve root certificate to verify payload signature.", e);
		}

		for (final LongReturnCodesSharePayload payload : longReturnCodesSharePayloads) {
			final byte[] payloadHash = hashService.recursiveHash(payload);

			final boolean signatureValid;
			try {
				signatureValid = payloadSignatureService.verify(payload.getSignature(), rootCertificate, payloadHash);
			} catch (final PayloadVerificationException e) {
				throw new IllegalArgumentException("Failed to verify payload signature.", e);
			}

			if (!signatureValid) {
				throw new InvalidPayloadSignatureException(LongReturnCodesSharePayload.class, String.format("[contextIds: %s]", contextIds));
			}
		}
	}

	/**
	 * Creates response containing the computed Short Choice Return Codes.
	 */
	private ShortChoiceReturnCodeAndComputeResults createResponse(final List<String> shortChoiceReturnCodes,
			final List<LongReturnCodesSharePayload> longReturnCodesSharePayloads) throws JsonProcessingException {

		final ShortChoiceReturnCodeAndComputeResults choiceCodes = new ShortChoiceReturnCodeAndComputeResults();
		choiceCodes.setComputationResults(objectMapper.writeValueAsString(longReturnCodesSharePayloads));

		final String serializedShortChoiceReturnCodes = StringUtils.join(shortChoiceReturnCodes, ';');
		choiceCodes.setShortChoiceReturnCodes(serializedShortChoiceReturnCodes);

		return choiceCodes;
	}

}
