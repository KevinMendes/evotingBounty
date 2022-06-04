/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.InvalidPayloadSignatureException;
import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.domain.election.model.confirmation.TraceableConfirmationMessage;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.voting.confirmvote.ConfirmationKey;
import ch.post.it.evoting.domain.voting.confirmvote.ConfirmationKeyPayload;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.domain.voting.sendvote.LongVoteCastReturnCodesShare;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContent;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContentRepository;
import ch.post.it.evoting.votingserver.voteverification.domain.model.platform.VvPlatformCARepository;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.VerificationRepository;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.MessageBrokerOrchestratorClient;

/**
 * Generate the short vote cast return code based on the confirmation message - in interaction with the control components.
 */
@Stateless
public class VoteCastReturnCodeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteCastReturnCodeService.class);

	private static final int REQUEST_ID_LENGTH = 32;

	private final HashService hashService;
	private final ObjectMapper objectMapper;
	private final ExtractVCCService extractVCCService;
	private final PlatformCARepository platformCARepository;
	private final VerificationRepository verificationRepository;
	private final CryptolibPayloadSignatureService payloadSignatureService;
	private final VerificationContentRepository verificationContentRepository;
	private final MessageBrokerOrchestratorClient messageBrokerOrchestratorClient;

	@Inject
	public VoteCastReturnCodeService(
			final HashService hashService,
			final ObjectMapper objectMapper,
			final ExtractVCCService extractVCCService,
			@VvPlatformCARepository
			final PlatformCARepository platformCARepository,
			final VerificationRepository verificationRepository,
			final CryptolibPayloadSignatureService payloadSignatureService,
			final VerificationContentRepository verificationContentRepository,
			final MessageBrokerOrchestratorClient messageBrokerOrchestratorClient) {
		this.hashService = hashService;
		this.objectMapper = objectMapper;
		this.extractVCCService = extractVCCService;
		this.platformCARepository = platformCARepository;
		this.verificationRepository = verificationRepository;
		this.payloadSignatureService = payloadSignatureService;
		this.verificationContentRepository = verificationContentRepository;
		this.messageBrokerOrchestratorClient = messageBrokerOrchestratorClient;
	}

	/**
	 * Calculates in interaction with the control components the short vote cast return code based on the confirmation message received by the voting
	 * client.
	 *
	 * @param tenantId            tenant identifier.
	 * @param electionEventId     election event identifier.
	 * @param verificationCardId  verification card id
	 * @param confirmationMessage The confirmation message received by the voting client
	 * @return An object cast code message that contains the short vote cast return code
	 */
	public ShortVoteCastReturnCodeAndComputeResults retrieveShortVoteCastCode(final String tenantId, final String electionEventId,
			final String verificationCardId, final TraceableConfirmationMessage confirmationMessage)
			throws ResourceNotFoundException, CryptographicOperationException, IOException {

		final Verification verification = verificationRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId,
				verificationCardId);
		final String verificationCardSetId = verification.getVerificationCardSetId();

		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);

		LOGGER.info("Generating the vote cast code... [contextIds: {}]", contextIds);

		// Create ConfirmationKeyPayload to send.
		final ConfirmationKeyPayload confirmationKeyPayload = createConfirmationKeyPayload(tenantId, confirmationMessage, contextIds);

		// Ask the control components to compute the long vote cast return code shares lCC_j_id.
		final List<LongVoteCastReturnCodesShare> longVoteCastReturnCodesShares = collectLongVoteCastReturnCodeShares(contextIds,
				confirmationKeyPayload);

		// Retrieve short codes by combining CCR shares and looking up the CMTable.
		final List<GqElement> lVCCShares = longVoteCastReturnCodesShares.stream()
				.map(LongVoteCastReturnCodesShare::getLongVoteCastReturnCodeShare)
				.collect(Collectors.toList());
		final String voteCastReturnCode = extractVCCService.extractVCC(lVCCShares, verificationCardId, electionEventId, tenantId);

		// Prepare response.
		final ShortVoteCastReturnCodeAndComputeResults castCodeMessage = new ShortVoteCastReturnCodeAndComputeResults();
		castCodeMessage.setShortVoteCastReturnCode(voteCastReturnCode);
		castCodeMessage.setComputationResults(objectMapper.writeValueAsString(longVoteCastReturnCodesShares));

		return castCodeMessage;
	}

	private ConfirmationKeyPayload createConfirmationKeyPayload(final String tenantId, final TraceableConfirmationMessage confirmationMessage,
			final ContextIds contextIds)
			throws ResourceNotFoundException, JsonProcessingException {

		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();

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

		// Create confirmation key.
		final String confirmationCodeString = new String(Base64.getDecoder().decode(confirmationMessage.getConfirmationKey()),
				StandardCharsets.UTF_8);
		final BigInteger confirmationCodeValue = new BigInteger(confirmationCodeString);
		final GqElement element = GqElementFactory.fromValue(confirmationCodeValue, gqGroup);

		final ConfirmationKey confirmationKey = new ConfirmationKey(contextIds, element);
		final String requestId = CryptoPrimitivesService.get().genRandomBase32String(REQUEST_ID_LENGTH);

		// Create and sign payload.
		// Currently, we do not have a signing key in the vote-verification.
		final ConfirmationKeyPayload confirmationKeyPayload = new ConfirmationKeyPayload(gqGroup, confirmationKey, requestId);
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(new byte[] {}, new X509Certificate[] {});
		confirmationKeyPayload.setSignature(signature);

		return confirmationKeyPayload;
	}

	private List<LongVoteCastReturnCodesShare> collectLongVoteCastReturnCodeShares(final ContextIds contextIds,
			final ConfirmationKeyPayload confirmationKeyPayload)
			throws ResourceNotFoundException {

		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		final List<LongReturnCodesSharePayload> longReturnCodesSharePayloads = RetrofitConsumer.processResponse(
				messageBrokerOrchestratorClient.getLongVoteCastReturnCodeContributions(electionEventId, verificationCardSetId, verificationCardId,
						confirmationKeyPayload));

		// Vote Cast Return Code computation response correctly received.
		LOGGER.info("Successfully retrieved the Long Vote Cast Return Code shares. [contextIds: {}", contextIds);

		verifyPayloadSignatures(contextIds, longReturnCodesSharePayloads);

		return longReturnCodesSharePayloads.stream()
				.map(LongReturnCodesSharePayload::getLongReturnCodesShare)
				.map(LongVoteCastReturnCodesShare.class::cast)
				.collect(Collectors.toList());

	}

	private void verifyPayloadSignatures(final ContextIds contextIds, final List<LongReturnCodesSharePayload> choiceCodesVerificationResults) {

		final X509Certificate rootCertificate;
		try {
			rootCertificate = (X509Certificate) PemUtils
					.certificateFromPem(platformCARepository.getRootCACertificate().getCertificateContent());
		} catch (GeneralCryptoLibException | ResourceNotFoundException e) {
			throw new IllegalArgumentException("Failed to retrieve root certificate to verify payload signature.", e);
		}

		for (final LongReturnCodesSharePayload payload : choiceCodesVerificationResults) {
			final byte[] payloadHash = hashService.recursiveHash(payload);

			final boolean signatureValid;
			try {
				signatureValid = payloadSignatureService.verify(payload.getSignature(), rootCertificate, payloadHash);
			} catch (PayloadVerificationException e) {
				throw new IllegalArgumentException("Failed to verify payload signature.", e);
			}

			if (!signatureValid) {
				throw new InvalidPayloadSignatureException(LongReturnCodesSharePayload.class, String.format("[contextIds: %s", contextIds));
			}
		}
	}

}
