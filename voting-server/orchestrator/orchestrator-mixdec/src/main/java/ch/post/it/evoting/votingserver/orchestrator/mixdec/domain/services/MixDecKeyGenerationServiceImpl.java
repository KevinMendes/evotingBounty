/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalEncryptionParameters;
import ch.post.it.evoting.domain.election.model.messaging.SafeStreamDeserializationException;
import ch.post.it.evoting.domain.returncodes.CCPublicKey;
import ch.post.it.evoting.domain.returncodes.KeyCreationDTO;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectReader;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectReaderImpl;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.Queue;
import ch.post.it.evoting.votingserver.orchestrator.commons.config.QueuesConfig;
import ch.post.it.evoting.votingserver.orchestrator.commons.config.TopicsConfig;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.PollingService;

public class MixDecKeyGenerationServiceImpl implements MixDecKeyGenerationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecKeyGenerationServiceImpl.class);

	private static final int INDEX_OF_KEY_WITHIN_GENERATED_LIST = 0;

	private final StreamSerializableObjectReader<KeyCreationDTO> reader = new StreamSerializableObjectReaderImpl<>();

	@Inject
	private MessagingService messagingService;

	@Inject
	@MixDecKeyGeneration
	@Contributions
	private MessageListener contributionsConsumer;

	@Inject
	@MixDecKeyGeneration
	@ResultsReady
	private MessageListener resultsReadyConsumer;

	@Inject
	@MixDecKeyGeneration
	private PollingService<List<byte[]>> pollingService;

	@Override
	public Map<String, List<String>> requestMixDecKeyGenerationSync(String trackingId, String tenantId, String electionEventId,
			final List<String> electoralAuthorityIds, ZonedDateTime keysDateFrom, ZonedDateTime keysDateTo,
			ElGamalEncryptionParameters elGamalEncryptionParameters) throws ResourceNotFoundException, GeneralCryptoLibException {

		LOGGER.info("OR - Requesting MixDec keys");

		Map<String, List<String>> electoralAuthorityKeys = new HashMap<>();

		for (String electoralAuthorityId : electoralAuthorityIds) {

			KeyCreationDTO keyCreationDTO = new KeyCreationDTO();
			keyCreationDTO.setCorrelationId(UUID.randomUUID());
			keyCreationDTO.setElectionEventId(electionEventId);
			keyCreationDTO.setResourceId(electoralAuthorityId);
			keyCreationDTO.setFrom(keysDateFrom);
			keyCreationDTO.setTo(keysDateTo);
			keyCreationDTO.setEncryptionParameters(elGamalEncryptionParameters.toJson());
			keyCreationDTO.setRequestId(trackingId);

			publishToReqQ(keyCreationDTO);

			List<KeyCreationDTO> collectedMessages = collectKeyCreationDTOMessages(keyCreationDTO);

			List<String> collectedPublicKeys = collectPublicKeys(collectedMessages);

			electoralAuthorityKeys.put(electoralAuthorityId, collectedPublicKeys);
		}

		return electoralAuthorityKeys;
	}

	@Override
	public void startup() throws MessagingException {
		consumeResultsReady();
		LOGGER.info("OR - Consuming the results ready notifications mixing key generation");
		consumeKeyGenerationContributions();
		LOGGER.info("OR - Consuming the mixing key generation contributions");
	}

	private void consumeKeyGenerationContributions() throws MessagingException {
		Queue[] queues = QueuesConfig.MIX_DEC_KEY_GENERATION_RES_QUEUES;

		if (queues == null || queues.length == 0) {
			throw new IllegalStateException("No MixDec key generation response queues provided to listen to.");
		}

		for (Queue queue : queues) {
			messagingService.createReceiver(queue, contributionsConsumer);
		}
	}

	private void consumeResultsReady() throws MessagingException {
		messagingService.createReceiver(TopicsConfig.HA_TOPIC, resultsReadyConsumer);
	}

	@Override
	public void shutdown() throws MessagingException {
		stopConsumingResultsReady();
		LOGGER.info("OR - Consuming the results ready notifications mixing key generation is stopped");
		stopConsumingKeyGenerationContributions();
		LOGGER.info("OR - Consuming the mixing key generation contributions is stopped");
	}

	private void stopConsumingKeyGenerationContributions() throws MessagingException {
		Queue[] queues = QueuesConfig.MIX_DEC_KEY_GENERATION_RES_QUEUES;

		if (queues == null || queues.length == 0) {
			throw new IllegalStateException("No MixDec key generation response queues provided to listen to.");
		}

		for (Queue queue : queues) {
			messagingService.destroyReceiver(queue, contributionsConsumer);
		}
	}

	private void stopConsumingResultsReady() throws MessagingException {
		messagingService.destroyReceiver(TopicsConfig.HA_TOPIC, resultsReadyConsumer);
	}

	private void publishToReqQ(KeyCreationDTO keyCreationDTO) throws ResourceNotFoundException {

		Queue[] queues = QueuesConfig.MIX_DEC_KEY_GENERATION_REQ_QUEUES;

		if (queues == null || queues.length == 0) {
			throw new IllegalStateException("No MixDec key generation request queues provided to publish to.");
		}

		for (Queue queue : queues) {

			try {
				messagingService.send(queue, keyCreationDTO);
			} catch (MessagingException e) {
				throw new ResourceNotFoundException("Error publishing the MixDec key generation request to the nodes queues. ", e);
			}
		}
	}

	private List<KeyCreationDTO> collectKeyCreationDTOMessages(KeyCreationDTO keyCreationDTO) throws ResourceNotFoundException {

		UUID correlationId = keyCreationDTO.getCorrelationId();

		List<byte[]> collectedKeys;

		try {

			LOGGER.info("OR - Waiting for the MixDec key generation from the nodes to be returned.");
			collectedKeys = pollingService.getResults(correlationId);

		} catch (TimeoutException e) {
			throw new ResourceNotFoundException("Error collecting the MixDec key generation from the nodes. ", e);
		}

		return collectedKeys.stream().map(this::deserializeKeyCreationDTO).collect(Collectors.toList());
	}

	private KeyCreationDTO deserializeKeyCreationDTO(byte[] bytes) {
		try {
			return reader.read(bytes);
		} catch (SafeStreamDeserializationException e) {
			throw new IllegalStateException("Failed to deserialize the key creation DTO.", e);
		}
	}

	private List<String> collectPublicKeys(List<KeyCreationDTO> collectedKeys) throws GeneralCryptoLibException {

		List<String> jsons = new ArrayList<>(collectedKeys.size());

		for (KeyCreationDTO dto : collectedKeys) {
			CCPublicKey key = dto.getPublicKeys().get(INDEX_OF_KEY_WITHIN_GENERATED_LIST);
			/*
			 * For each key the following JSON is created { publicKey: "<ElGamalPublicKey as JSON>",
			 * signature: "<Base64 encoded signature>" signerCertificate:
			 * "<Signer certificate in PEM format>", nodeCACertificate:
			 * "<CCN CA certificate in PEM format>" }
			 */
			JsonObjectBuilder json = Json.createObjectBuilder();
			json.add("publicKey", key.getPublicKey().toJson());
			json.add("signature", Base64.getEncoder().encodeToString(key.getKeySignature()));
			json.add("signerCertificate", PemUtils.certificateToPem(key.getSignerCertificate()));
			json.add("nodeCACertificate", PemUtils.certificateToPem(key.getNodeCACertificate()));
			jsons.add(json.build().toString());
		}

		return jsons;
	}
}
