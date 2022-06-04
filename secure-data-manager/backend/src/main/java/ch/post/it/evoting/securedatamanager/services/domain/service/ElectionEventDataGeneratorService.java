/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters.Type;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateElectionEventCertificatePropertiesContainer;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateElectionEventInput;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.output.ElectionEventServiceOutput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventHolderInitializer;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventOutput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.CreateElectionEventSerializer;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.CreateElectionEventException;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.CreateElectionEventParametersHolderAdapter;
import ch.post.it.evoting.securedatamanager.services.domain.service.utils.SystemTenantPublicKeyLoader;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;

/**
 * This implementation generates the election event data.
 */
@Service
public class ElectionEventDataGeneratorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventDataGeneratorService.class);
	private static final String JSON_PARAM_NAME_MAXIMUM_NUMBER_OF_ATTEMPTS = "maximumNumberOfAttempts";
	private static final String JSON_PARAM_NAME_NUMBER_VOTES_PER_AUTH_TOKEN = "numberVotesPerAuthToken";
	private static final String JSON_PARAM_NAME_NUMBER_VOTES_PER_VOTING_CARD = "numberVotesPerVotingCard";
	private static final String JSON_PARAM_NAME_AUTH_TOKEN_EXPIRATION_TIME = "authTokenExpirationTime";
	private static final String JSON_PARAM_NAME_CHALLENGE_LENGTH = "challengeLength";
	private static final String JSON_PARAM_NAME_CHALLENGE_RESPONSE_EXPIRATION_TIME = "challengeResponseExpirationTime";
	private static final String JSON_PARAM_NAME_CERTIFICATES_VALIDITY_PERIOD = "certificatesValidityPeriod";
	private static final String JSON_PARAM_NAME_DATE_TO = "dateTo";
	private static final String JSON_PARAM_NAME_DATE_FROM = "dateFrom";
	private static final String TAB_LOG = "\t {}";

	private final ElectionEventRepository electionEventRepository;
	private final PathResolver pathResolver;
	private final SystemTenantPublicKeyLoader systemTenantPublicKeyLoader;
	private final CreateElectionEventSerializer createElectionEventSerializer;
	private final CreateElectionEventHolderInitializer electionEventHolderInitializer;
	private final CreateElectionEventParametersHolderAdapter createElectionEventParametersHolderAdapter;
	private final CreateElectionEventGenerator createElectionEventGenerator;
	private final String tenantId;
	private final String servicesCaCertificateProperties;
	private final String electionCaCertificateProperties;
	private final String credentialsCaCertificateProperties;
	private final String authoritiesCaCertificateProperties;
	private final String authTokenSignerCertificateProperties;

	public ElectionEventDataGeneratorService(final ElectionEventRepository electionEventRepository, final PathResolver pathResolver,
			final SystemTenantPublicKeyLoader systemTenantPublicKeyLoader, final CreateElectionEventSerializer createElectionEventSerializer,
			final CreateElectionEventHolderInitializer electionEventHolderInitializer,
			final CreateElectionEventParametersHolderAdapter createElectionEventParametersHolderAdapter,
			final CreateElectionEventGenerator createElectionEventGenerator,
			@Value("${tenantID}")
			final String tenantId,
			@Value("${services.ca.certificate.properties}")
			final String servicesCaCertificateProperties,
			@Value("${election.ca.certificate.properties}")
			final String electionCaCertificateProperties,
			@Value("${credentials.ca.certificate.properties}")
			final String credentialsCaCertificateProperties,
			@Value("${authorities.ca.certificate.properties}")
			final String authoritiesCaCertificateProperties,
			@Value("${auth.token.signer.certificate.properties}")
			final String authTokenSignerCertificateProperties) {
		this.electionEventRepository = electionEventRepository;
		this.pathResolver = pathResolver;
		this.systemTenantPublicKeyLoader = systemTenantPublicKeyLoader;
		this.createElectionEventSerializer = createElectionEventSerializer;
		this.electionEventHolderInitializer = electionEventHolderInitializer;
		this.createElectionEventParametersHolderAdapter = createElectionEventParametersHolderAdapter;
		this.createElectionEventGenerator = createElectionEventGenerator;
		this.tenantId = tenantId;
		this.servicesCaCertificateProperties = servicesCaCertificateProperties;
		this.electionCaCertificateProperties = electionCaCertificateProperties;
		this.credentialsCaCertificateProperties = credentialsCaCertificateProperties;
		this.authoritiesCaCertificateProperties = authoritiesCaCertificateProperties;
		this.authTokenSignerCertificateProperties = authTokenSignerCertificateProperties;
	}

	/**
	 * This method creates the input necessary for the configuration to work and calls it to generate the election event data for the one identified
	 * by the given id. It simulates the use from command line by calling directly the code. One of the inputs being a properties file, this is
	 * created and saved on disk.
	 *
	 * @param electionEventId The identifier of the election event for whom to generate the data.
	 * @return a bean containing information about the result of the generation.
	 */
	public DataGeneratorResponse generate(final String electionEventId) throws IOException {
		final DataGeneratorResponse result = new DataGeneratorResponse();

		// basic validation of the input
		if (StringUtils.isBlank(electionEventId)) {
			result.setSuccessful(false);

			return result;
		}

		// just in case the directory is not created
		final Path configPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR);
		try {
			makePath(configPath);
		} catch (final IOException e2) {
			LOGGER.error("", e2);

			result.setSuccessful(false);
			return result;
		}

		final String electionEvent = electionEventRepository.find(electionEventId);
		if (JsonConstants.EMPTY_OBJECT.equals(electionEvent)) {

			result.setSuccessful(false);
			return result;
		}

		final JsonObject electionEventJson = JsonUtils.getJsonObject(electionEvent);
		final JsonObject settings = electionEventJson.getJsonObject(JsonConstants.SETTINGS);

		final CreateElectionEventInput input = new CreateElectionEventInput();
		input.setAuthTokenExpTime(Integer.toString(settings.getInt(JSON_PARAM_NAME_AUTH_TOKEN_EXPIRATION_TIME)));
		input.setChallengeLength(Integer.toString(settings.getInt(JSON_PARAM_NAME_CHALLENGE_LENGTH)));
		input.setChallengeResExpTime(Integer.toString(settings.getInt(JSON_PARAM_NAME_CHALLENGE_RESPONSE_EXPIRATION_TIME)));
		input.setEeid(electionEventId);
		input.setEnd(electionEventJson.getString(JSON_PARAM_NAME_DATE_TO));
		input.setMaxNumberOfAttempts(Integer.toString(settings.getInt(JSON_PARAM_NAME_MAXIMUM_NUMBER_OF_ATTEMPTS)));
		input.setNumVotesPerAuthToken(Integer.toString(settings.getInt(JSON_PARAM_NAME_NUMBER_VOTES_PER_AUTH_TOKEN)));
		input.setNumVotesPerVotingCard(Integer.toString(settings.getInt(JSON_PARAM_NAME_NUMBER_VOTES_PER_VOTING_CARD)));
		input.setOutputPath(configPath.toString());
		input.setStart(electionEventJson.getString(JSON_PARAM_NAME_DATE_FROM));
		input.setValidityPeriod(settings.getInt(JSON_PARAM_NAME_CERTIFICATES_VALIDITY_PERIOD));
		input.setKeyForProtectingKeystorePassword(getPublicKeyForProtectingKeystorePassword());
		input.setCertificatePropertiesInput(getCertificateProperties());

		try {
			final CreateElectionEventParametersHolder holder = createElectionEventParametersHolderAdapter.adapt(input);
			createElectionEvent(holder);
		} catch (final CreateElectionEventException e) {
			LOGGER.error(String.format("Error creating election event. [electionEventId=%s", electionEventId), e);
			result.setSuccessful(false);
		}

		return result;
	}

	@VisibleForTesting
	public ElectionEventServiceOutput createElectionEvent(final CreateElectionEventParametersHolder holder) {

		try {
			LOGGER.info("Loading internal configuration...");

			try (final InputStream is = getKeysConfiguration()) {
				electionEventHolderInitializer.init(holder, is);
			}

			LOGGER.info("Generating Election Event...");

			final CreateElectionEventOutput createElectionEventOutput = createElectionEventGenerator.generate(holder);
			try {
				LOGGER.info("Processing the output...");

				createElectionEventSerializer.serialize(holder, createElectionEventOutput);
			} finally {
				createElectionEventOutput.clearPasswords();
			}

			LOGGER.info("The generation of Election Event finished correctly. It can be found in:");
			LOGGER.info(TAB_LOG, holder.getOfflineFolder().toAbsolutePath());
			LOGGER.info(TAB_LOG, holder.getOnlineAuthenticationFolder().toAbsolutePath());
			LOGGER.info(TAB_LOG, holder.getOnlineElectionInformationFolder().toAbsolutePath());

			final ElectionEventServiceOutput electionEventOutput = new ElectionEventServiceOutput();
			electionEventOutput.setOfflineFolder(holder.getOfflineFolder().toAbsolutePath().toString());
			electionEventOutput.setOnlineAuthenticationFolder(holder.getOnlineAuthenticationFolder().toAbsolutePath().toString());
			electionEventOutput.setOnlineElectionInformationFolder(holder.getOnlineElectionInformationFolder().toAbsolutePath().toString());

			return electionEventOutput;

		} catch (final Exception e) {
			throw new CreateElectionEventException(e);
		}
	}

	private InputStream getKeysConfiguration() throws FileNotFoundException {
		// get inputstream to keys_config.json from classpath
		final String resourceName = "/" + Constants.KEYS_CONFIG_FILENAME;
		final InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
		if (resourceAsStream == null) {
			throw new FileNotFoundException(String.format("Resource file '%s' was not found on the classpath", resourceName));
		}
		return resourceAsStream;
	}

	private CreateElectionEventCertificatePropertiesContainer getCertificateProperties() throws IOException {

		LOGGER.info("Obtaining certificate properties from the following paths:");
		LOGGER.info(" {}", servicesCaCertificateProperties);
		LOGGER.info(" {}", electionCaCertificateProperties);
		LOGGER.info(" {}", credentialsCaCertificateProperties);
		LOGGER.info(" {}", authoritiesCaCertificateProperties);
		LOGGER.info(" {}", authTokenSignerCertificateProperties);

		final CreateElectionEventCertificatePropertiesContainer createElectionEventCertificateProperties = new CreateElectionEventCertificatePropertiesContainer();

		final Properties loadedServicesCaCertificatePropertiesAsString = getCertificateParameters(servicesCaCertificateProperties);
		final Properties loadedElectionCaCertificatePropertiesAsString = getCertificateParameters(electionCaCertificateProperties);
		final Properties loadedCredentialsCaCertificatePropertiesAsString = getCertificateParameters(credentialsCaCertificateProperties);
		final Properties loadedAuthoritiesCaCertificatePropertiesAsString = getCertificateParameters(authoritiesCaCertificateProperties);

		final Properties loadedAuthTokenSignerCertificatePropertiesAsString = getCertificateParameters(authTokenSignerCertificateProperties);

		final Map<String, Properties> configProperties = new HashMap<>();
		configProperties.put("electioneventca", loadedElectionCaCertificatePropertiesAsString);
		configProperties.put("authoritiesca", loadedAuthoritiesCaCertificatePropertiesAsString);
		configProperties.put("servicesca", loadedServicesCaCertificatePropertiesAsString);
		configProperties.put("credentialsca", loadedCredentialsCaCertificatePropertiesAsString);

		createElectionEventCertificateProperties.setAuthTokenSignerCertificateProperties(loadedAuthTokenSignerCertificatePropertiesAsString);
		createElectionEventCertificateProperties.setNameToCertificateProperties(configProperties);

		LOGGER.info("Obtained certificate properties");

		return createElectionEventCertificateProperties;
	}

	private Properties getCertificateParameters(final String path) throws IOException {
		final Properties props = new Properties();
		try (final FileInputStream fis = new FileInputStream(path)) {
			props.load(fis);
		}
		return props;
	}

	private String getPublicKeyForProtectingKeystorePassword() throws IOException {

		return systemTenantPublicKeyLoader.load(tenantId, "AU", Type.ENCRYPTION);
	}

	/**
	 * Creates all the directories from the path if they don't exist yet. This is wrapper over a static method in order to make the class testable.
	 *
	 * @param path The path to be created.
	 * @return a Path representing the directory created.
	 * @throws IOException in case there is a I/O problem.
	 */
	Path makePath(final Path path) throws IOException {
		return Files.createDirectories(path);
	}

}
