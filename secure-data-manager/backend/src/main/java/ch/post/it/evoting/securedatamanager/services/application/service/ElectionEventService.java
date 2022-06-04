/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.configuration.ControlComponentKeyGenerationRequestPayload;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.SetupKeyPairService;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.configuration.ControlComponentPublicKeysService;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenSetupEncryptionKeysService;
import ch.post.it.evoting.securedatamanager.services.application.exception.CCKeysAlreadyExistException;
import ch.post.it.evoting.securedatamanager.services.application.exception.CCKeysNotExistException;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.service.ElectionEventDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.MessageBrokerOrchestratorClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This is a service for handling election event entities.
 */
@Service
public class ElectionEventService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventService.class);

	private final ObjectMapper mapper;
	private final PathResolver pathResolver;
	private final KeyStoreService keystoreService;
	private final SetupKeyPairService setupKeyPairService;
	private final ElectionEventRepository electionEventRepository;
	private final EncryptionParametersService encryptionParametersService;
	private final GenSetupEncryptionKeysService genSetupEncryptionKeysService;
	private final ConfigurationEntityStatusService configurationEntityStatusService;
	private final ElectionEventDataGeneratorService electionEventDataGeneratorService;
	private final ControlComponentPublicKeysService controlComponentPublicKeysService;

	@Value("${MO_URL}")
	private String orchestratorUrl;

	public ElectionEventService(
			final ObjectMapper mapper,
			final PathResolver pathResolver,
			final KeyStoreService keystoreService,
			final SetupKeyPairService setupKeyPairService,
			final ElectionEventRepository electionEventRepository,
			final EncryptionParametersService encryptionParametersService,
			final GenSetupEncryptionKeysService genSetupEncryptionKeysService,
			final ConfigurationEntityStatusService configurationEntityStatusService,
			final ElectionEventDataGeneratorService electionEventDataGeneratorService,
			final ControlComponentPublicKeysService controlComponentPublicKeysService) {
		this.mapper = mapper;
		this.pathResolver = pathResolver;
		this.keystoreService = keystoreService;
		this.setupKeyPairService = setupKeyPairService;
		this.electionEventRepository = electionEventRepository;
		this.encryptionParametersService = encryptionParametersService;
		this.genSetupEncryptionKeysService = genSetupEncryptionKeysService;
		this.configurationEntityStatusService = configurationEntityStatusService;
		this.electionEventDataGeneratorService = electionEventDataGeneratorService;
		this.controlComponentPublicKeysService = controlComponentPublicKeysService;
	}

	/**
	 * Creates an election event based on the given id and if everything ok, it sets its status to ready.
	 *
	 * @param electionEventId identifies the election event to be created.
	 * @return an object containing the result of the creation.
	 */
	public DataGeneratorResponse create(final String electionEventId) throws IOException, CCKeysNotExistException {

		// Check if Control Components keys exists
		if (!controlComponentPublicKeysService.exist(electionEventId)) {
			throw new CCKeysNotExistException(
					String.format("The Control Components keys do not exist for this election event. [electionEventId: %s]", electionEventId));
		}

		// Load the election encryption parameters.
		final GqGroup group = encryptionParametersService.load(electionEventId);

		// Generate the setup key pair
		final ElGamalMultiRecipientKeyPair setupKeyPair = genSetupEncryptionKeysService.genSetupEncryptionKeys(group);

		final Path offlinePath = pathResolver.resolveOfflinePath(electionEventId);
		Files.createDirectories(offlinePath);

		// Persist the setup key pair.
		setupKeyPairService.save(electionEventId, setupKeyPair);

		// Persist the setup secret key.
		mapper.writeValue(offlinePath.resolve(Constants.SETUP_SECRET_KEY_FILE_NAME).toFile(), setupKeyPair.getPrivateKey());

		// Create election event data.
		final DataGeneratorResponse result = electionEventDataGeneratorService.generate(electionEventId);
		if (result.isSuccessful()) {
			configurationEntityStatusService.update(Status.READY.name(), electionEventId, electionEventRepository);
		}

		return result;
	}

	/**
	 * Requests the Control Components keys for an election event based on the given id.
	 *
	 * @param electionEventId identifies the election event for which Control Components keys are requested.
	 * @throws CCKeysAlreadyExistException if Control Components keys already exist.
	 */
	public void requestCCKeys(final String electionEventId) throws CCKeysAlreadyExistException {

		if (controlComponentPublicKeysService.exist(electionEventId)) {
			throw new CCKeysAlreadyExistException(
					String.format("The Control Components keys already exist for this election event. [electionEventId: %s]", electionEventId));
		}

		final GqGroup gqGroup = encryptionParametersService.load(electionEventId);
		final ControlComponentKeyGenerationRequestPayload requestPayload = new ControlComponentKeyGenerationRequestPayload(electionEventId, gqGroup);

		final Response<List<ControlComponentPublicKeysPayload>> response;
		try {
			response = getMessageBrokerOrchestratorClient().generateCCKeys(electionEventId, requestPayload).execute();
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to communicate with message broker orchestrator.", e);
		}

		if (!response.isSuccessful()) {
			throw new IllegalStateException(String.format("Request failed. [electionEventId: %s]", electionEventId));
		}
		LOGGER.info("Successfully retrieved control component public keys payloads. [electionEventId: {}]", electionEventId);

		response.body().forEach(controlComponentPublicKeysService::save);
		LOGGER.info("Successfully saved control component public keys payloads. [electionEventId: {}]", electionEventId);
	}

	/**
	 * This method returns election event alias based on the given id
	 */
	public String getElectionEventAlias(final String electionEventId) {

		return electionEventRepository.getElectionEventAlias(electionEventId);
	}

	/**
	 * Get all available election events
	 *
	 * @return the election events list
	 */
	public List<String> getAllElectionEventIds() {

		return electionEventRepository.listIds();
	}

	public LocalDateTime getDateFrom(final String electionEventId) {
		validateUUID(electionEventId);

		return LocalDateTime.parse(electionEventRepository.getDateFrom(electionEventId), DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
	}

	public LocalDateTime getDateTo(final String electionEventId) {
		validateUUID(electionEventId);

		return LocalDateTime.parse(electionEventRepository.getDateTo(electionEventId), DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
	}

	/**
	 * Gets the message broker orchestrator Retrofit client
	 */
	private MessageBrokerOrchestratorClient getMessageBrokerOrchestratorClient() {
		final PrivateKey privateKey = keystoreService.getPrivateKey();
		final Retrofit restAdapter = RestClientService.getInstance()
				.getRestClientWithInterceptorAndJacksonConverter(orchestratorUrl, privateKey, "SECURE_DATA_MANAGER");
		return restAdapter.create(MessageBrokerOrchestratorClient.class);
	}

}
