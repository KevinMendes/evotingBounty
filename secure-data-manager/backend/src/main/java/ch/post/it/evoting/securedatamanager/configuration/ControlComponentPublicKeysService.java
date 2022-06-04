/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.NODE_IDS;
import static com.google.common.base.Preconditions.checkNotNull;

import java.security.cert.X509Certificate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;

/**
 * Allows saving, retrieving and finding existing control component public keys. The control components list is defined by {@link
 * Constants#NODE_IDS}.
 */
@Service
public class ControlComponentPublicKeysService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlComponentPublicKeysService.class);

	private final PlatformRootCertificateService platformRootCertificateService;
	private final CryptolibPayloadSignatureService cryptolibPayloadSignatureService;
	private final ControlComponentPublicKeysPayloadFileRepository controlComponentPublicKeysPayloadFileRepository;

	public ControlComponentPublicKeysService(
			final PlatformRootCertificateService platformRootCertificateService,
			final CryptolibPayloadSignatureService cryptolibPayloadSignatureService,
			final ControlComponentPublicKeysPayloadFileRepository controlComponentPublicKeysPayloadFileRepository) {
		this.platformRootCertificateService = platformRootCertificateService;
		this.cryptolibPayloadSignatureService = cryptolibPayloadSignatureService;
		this.controlComponentPublicKeysPayloadFileRepository = controlComponentPublicKeysPayloadFileRepository;
	}

	/**
	 * Saves a control component public keys payloads in the corresponding election event folder.
	 *
	 * @param controlComponentPublicKeysPayload the payload to save.
	 * @throws NullPointerException if {@code controlComponentPublicKeysPayload} is null.
	 */
	public void save(final ControlComponentPublicKeysPayload controlComponentPublicKeysPayload) {
		checkNotNull(controlComponentPublicKeysPayload);

		final String electionEventId = controlComponentPublicKeysPayload.getElectionEventId();
		final int nodeId = controlComponentPublicKeysPayload.getControlComponentPublicKeys().getNodeId();

		controlComponentPublicKeysPayloadFileRepository.save(controlComponentPublicKeysPayload);
		LOGGER.info("Saved control component public keys payload. [electionEventId: {}, nodeId: {}]", electionEventId, nodeId);
	}

	/**
	 * Checks if all control component public keys payloads are present for the given election event id.
	 *
	 * @param electionEventId the election event id to check.
	 * @return {@code true} if all payloads are present, {@code false} otherwise.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 */
	public boolean exist(final String electionEventId) {
		validateUUID(electionEventId);

		return NODE_IDS.stream()
				.allMatch(nodeId -> controlComponentPublicKeysPayloadFileRepository.existsById(electionEventId, nodeId));
	}

	/**
	 * Loads all {@link ControlComponentPublicKeys} for the given {@code electionEventId}. Upon retrieving the public keys, the signatures of their
	 * respective payloads are first verified. The result of this method is cached.
	 *
	 * @param electionEventId the election event id for which to get the public keys.
	 * @return the control component public keys for this {@code electionEventId}.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws IllegalStateException     if
	 *                                   <ul>
	 *                                       <li>There is not the correct number of payloads.</li>
	 *                                       <li>If any verification of signature fails.</li>
	 *                                       <li>If any signature is invalid.</li>
	 *                                   </ul>
	 */
	@Cacheable("controlComponentPublicKeys")
	public List<ControlComponentPublicKeys> load(final String electionEventId) {
		validateUUID(electionEventId);

		final List<ControlComponentPublicKeysPayload> controlComponentPublicKeysPayloads =
				controlComponentPublicKeysPayloadFileRepository.findAllOrderByNodeId(electionEventId);

		// Ensure we received all payloads corresponds to the node ids.
		final List<Integer> payloadsNodeIds = controlComponentPublicKeysPayloads.stream()
				.map(ControlComponentPublicKeysPayload::getControlComponentPublicKeys)
				.map(ControlComponentPublicKeys::getNodeId)
				.collect(Collectors.toList());
		if (NODE_IDS.size() != payloadsNodeIds.size() || !payloadsNodeIds.containsAll(NODE_IDS)) {
			throw new IllegalStateException(
					String.format("Wrong number of control component public keys payloads. [required node ids: %s, found: %s]", NODE_IDS,
							payloadsNodeIds));
		}

		// Check signatures of payloads.
		final X509Certificate platformRootCA = platformRootCertificateService.load();

		controlComponentPublicKeysPayloads.forEach(payload -> {
			final int nodeId = payload.getControlComponentPublicKeys().getNodeId();
			final boolean isValid;
			try {
				isValid = cryptolibPayloadSignatureService.verify(payload, platformRootCA);
			} catch (final PayloadVerificationException e) {
				throw new IllegalStateException(
						String.format("Failed to verify control component public keys payload signature. [electionEventId: %s, nodeId: %s]",
								electionEventId, nodeId), e);
			}

			if (!isValid) {
				throw new IllegalStateException(
						String.format("The signature of the control component public keys payload is invalid. [electionEventId: %s, nodeId: %s]",
								electionEventId, nodeId));
			}
		});
		LOGGER.info("Signature of all control component public keys payloads are valid. [electionEventId: {}]", electionEventId);

		return controlComponentPublicKeysPayloads.stream()
				.map(ControlComponentPublicKeysPayload::getControlComponentPublicKeys)
				.sorted(Comparator.comparingInt(ControlComponentPublicKeys::getNodeId))
				.collect(Collectors.toList());
	}

}
