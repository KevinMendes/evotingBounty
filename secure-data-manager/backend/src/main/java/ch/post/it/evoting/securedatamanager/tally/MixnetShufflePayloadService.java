/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.services.application.service.CertificateManagementException;
import ch.post.it.evoting.securedatamanager.services.application.service.PlatformRootCAService;

@Service
public class MixnetShufflePayloadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixnetShufflePayloadService.class);

	private final CryptolibPayloadSignatureService cryptolibPayloadSignatureService;
	private final MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository;
	private final PlatformRootCAService platformRootCAService;

	@Autowired
	public MixnetShufflePayloadService(final CryptolibPayloadSignatureService cryptolibPayloadSignatureService,
			final MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository,
			final PlatformRootCAService platformRootCAService) {
		this.cryptolibPayloadSignatureService = cryptolibPayloadSignatureService;
		this.mixnetShufflePayloadFileRepository = mixnetShufflePayloadFileRepository;
		this.platformRootCAService = platformRootCAService;
	}

	/**
	 * Checks if the signature of each of the online payloads related to the given election event id, ballot id and ballot box id is valid.
	 *
	 * @param electionEventId the election event id.
	 * @param ballotId        the ballot id.
	 * @param ballotBoxId     the ballot box id.
	 * @return true if each signature of the online payloads were successfully verified, false otherwise.
	 * @throws NullPointerException      if any of the inputs is null.
	 * @throws FailedValidationException if any of the inputs is not a valid UUID.
	 */
	public boolean areOnlinePayloadSignaturesValid(final String electionEventId, final String ballotId, final String ballotBoxId) {
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);

		LOGGER.info("Verifying the signatures of all online payloads... [electionEventId: {}, ballotId: {}, ballotBoxId: {}]", electionEventId,
				ballotId, ballotBoxId);

		final List<Integer> onlineControlComponentsNodeIds = Arrays.asList(1, 2, 3);

		final boolean isOnlinePayloadSignatureValid = onlineControlComponentsNodeIds.stream().allMatch(onlineControlComponentNodeId -> {

			final MixnetShufflePayload payload = mixnetShufflePayloadFileRepository
					.getPayload(electionEventId, ballotId, ballotBoxId, onlineControlComponentNodeId);

			return isPayloadSignatureValid(electionEventId, ballotId, ballotBoxId, onlineControlComponentNodeId, payload);

		});

		LOGGER.info(
				"Result of the verification of the signatures of all online payloads. [result: {}, electionEventId {}, ballotI: {}, ballotBoxId: {}]",
				isOnlinePayloadSignatureValid ? "Successful" : "Unsuccessful", electionEventId, ballotId, ballotBoxId);

		return isOnlinePayloadSignatureValid;
	}

	/**
	 * Checks if the signature of the given payload related to the given election event id, ballot id, ballot box id and control component id is
	 * valid.
	 *
	 * @param electionEventId         the election event id.
	 * @param ballotId                the ballot id.
	 * @param ballotBoxId             the ballot box id.
	 * @param controlComponentsNodeId the control component node id.
	 * @param payload                 the payload whose signature must be verified.
	 * @return true if the signature of the payload was successfully verified, false otherwise.
	 */
	private boolean isPayloadSignatureValid(final String electionEventId, final String ballotId, final String ballotBoxId,
			final Integer controlComponentsNodeId, final MixnetShufflePayload payload) {
		LOGGER.debug("Verifying the signature of payload... [electionEventId: {}, ballotId: {}, ballotBoxId: {}, nodeId {}]", electionEventId,
				ballotId, ballotBoxId, controlComponentsNodeId);

		final X509Certificate platformRootCertificate;
		try {
			platformRootCertificate = platformRootCAService.load();
		} catch (final CertificateManagementException e) {
			LOGGER.error("Failed to load the platform root certificate.", e);
			return false;
		}

		boolean isPayloadSignatureValid = false;
		try {
			isPayloadSignatureValid = cryptolibPayloadSignatureService.verify(payload, platformRootCertificate);
		} catch (final PayloadVerificationException e) {
			LOGGER.error(String.format(
					"An error occurred while verifying the signature. [electionEventId: %s, ballotId: %s, ballotBoxId: %s, nodeId %s]",
					electionEventId, ballotId, ballotBoxId, controlComponentsNodeId), e);
		}

		LOGGER.debug(
				"Result of the verification of the signature of payload. [result: {}, electionEventId {}, ballotId {}, ballotBoxId: {} and control component {}.",
				isPayloadSignatureValid ? "Successful" : "Unsuccessful", electionEventId, ballotId, ballotBoxId, controlComponentsNodeId);

		return isPayloadSignatureValid;
	}

}
