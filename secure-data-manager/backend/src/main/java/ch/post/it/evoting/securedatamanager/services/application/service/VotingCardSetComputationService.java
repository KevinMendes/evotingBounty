/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * This is an application service that deals with the computation of voting card data.
 */
@Service
public class VotingCardSetComputationService extends BaseVotingCardSetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetComputationService.class);

	@Autowired
	IdleStatusService idleStatusService;

	@Autowired
	private ConfigurationEntityStatusService configurationEntityStatusService;

	@Autowired
	private VotingCardSetChoiceCodesService votingCardSetChoiceCodesService;

	@Autowired
	private ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository;

	@Autowired
	private PlatformRootCAService platformRootCAService;

	@Autowired
	private CryptolibPayloadSignatureService payloadSignatureService;

	/**
	 * Compute a voting card set.
	 *
	 * @param votingCardSetId the identifier of the voting card set
	 * @param electionEventId the identifier of the election event
	 * @throws ResourceNotFoundException
	 * @throws InvalidStatusTransitionException if the original status does not allow computing
	 * @throws JsonProcessingException
	 * @throws PayloadStorageException          if the payload could not be store
	 * @throws PayloadVerificationException     if the payload signature could not be verified
	 */
	public void compute(final String votingCardSetId, final String electionEventId)
			throws ResourceNotFoundException, InvalidStatusTransitionException, IOException, PayloadStorageException, PayloadVerificationException {

		if (!idleStatusService.getIdLock(votingCardSetId)) {
			return;
		}

		LOGGER.info("Starting computation of voting card set {}...", votingCardSetId);

		try {

			validateUUID(votingCardSetId);
			validateUUID(electionEventId);

			final Status fromStatus = Status.PRECOMPUTED;
			final Status toStatus = Status.COMPUTING;
			checkVotingCardSetStatusTransition(electionEventId, votingCardSetId, fromStatus, toStatus);

			final String verificationCardSetId = votingCardSetRepository.getVerificationCardSetId(votingCardSetId);

			final X509Certificate platformRootCACertificate;
			try {
				platformRootCACertificate = platformRootCAService.load();
			} catch (final CertificateManagementException e) {
				// The payload cannot be verified because the certificate could not be loaded.
				throw new PayloadVerificationException(e);
			}

			final int chunkCount = returnCodeGenerationRequestPayloadRepository.getCount(electionEventId, verificationCardSetId);
			for (int i = 0; i < chunkCount; i++) {
				// Retrieve the payload.
				final ReturnCodeGenerationRequestPayload payload = returnCodeGenerationRequestPayloadRepository
						.retrieve(electionEventId, verificationCardSetId, i);

				// Validate the signature.
				final boolean isSignatureValid = payloadSignatureService.verify(payload, platformRootCACertificate);

				if (isSignatureValid) {
					// The signature is valid, send chunk for processing.
					votingCardSetChoiceCodesService.sendToCompute(payload);
					LOGGER.info("Chunk {}/{} from voting card set {} was sent", i + 1, chunkCount, votingCardSetId);
				} else {
					// The signature is not valid: do not send.
					final String cause = String
							.format("Chunk %s/%s from voting card set %s was NOT sent: signature is not valid", i + 1, chunkCount, votingCardSetId);
					throw new PayloadVerificationException(cause);
				}
			}

			// All chunks have been sent, update status.
			configurationEntityStatusService.update(toStatus.name(), votingCardSetId, votingCardSetRepository);
			LOGGER.info("Computation of voting card set {} started", votingCardSetId);

		} finally {
			idleStatusService.freeIdLock(votingCardSetId);
		}
	}
}
