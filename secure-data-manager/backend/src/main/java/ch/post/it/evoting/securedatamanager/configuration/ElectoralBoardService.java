/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.CONFIG_DIR_NAME_AUTHENTICATION;
import static ch.post.it.evoting.securedatamanager.commons.Constants.CONFIG_DIR_NAME_ONLINE;
import static ch.post.it.evoting.securedatamanager.commons.Constants.CONFIG_FILES_BASE_DIR;
import static ch.post.it.evoting.securedatamanager.commons.Constants.CONFIG_FILE_NAME_AUTH_CONTEXT_DATA;
import static ch.post.it.evoting.securedatamanager.commons.Constants.CONFIG_FILE_NAME_AUTH_VOTER_DATA;
import static ch.post.it.evoting.securedatamanager.commons.Constants.CONFIG_FILE_NAME_SIGNED_AUTH_CONTEXT_DATA;
import static ch.post.it.evoting.securedatamanager.commons.Constants.CONFIG_FILE_NAME_SIGNED_AUTH_VOTER_DATA;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PrivateKey;

import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.domain.election.AuthenticationContextData;
import ch.post.it.evoting.domain.election.AuthenticationVoterData;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.SignedObject;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

@Service
public class ElectoralBoardService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectoralBoardService.class);

	private final ElectoralAuthorityRepository electoralAuthorityRepository;
	private final ConfigurationEntityStatusService statusService;
	private final PathResolver pathResolver;
	private final ConfigObjectMapper configObjectMapper;
	private final ElectoralBoardConstitutionService electoralBoardConstitutionService;

	public ElectoralBoardService(final ElectoralAuthorityRepository electoralAuthorityRepository,
			final ConfigurationEntityStatusService statusService, final PathResolver pathResolver, final ConfigObjectMapper configObjectMapper,
			final ElectoralBoardConstitutionService electoralBoardConstitutionService) {
		this.electoralAuthorityRepository = electoralAuthorityRepository;
		this.statusService = statusService;
		this.pathResolver = pathResolver;
		this.configObjectMapper = configObjectMapper;
		this.electoralBoardConstitutionService = electoralBoardConstitutionService;
	}

	/**
	 * Constitutes the electoral board and updates the electoral authority status.
	 *
	 * @param electionEventId      the election event id
	 * @param electoralAuthorityId the electoral authority id
	 * @return true if constitution and update operations were successful, false otherwise.
	 */
	public boolean constitute(final String electionEventId, final String electoralAuthorityId) {
		validateUUID(electionEventId);
		validateUUID(electoralAuthorityId);

		LOGGER.debug("Constituting electoral board... [electionEventId: {}, electoralAuthorityId: {}]", electionEventId, electoralAuthorityId);

		electoralBoardConstitutionService.constitute(electionEventId);

		if (updateElectoralAuthorityStatus(electoralAuthorityId)) {
			LOGGER.info("Constitute electoral board successful. [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
					electoralAuthorityId);
			return true;
		} else {
			LOGGER.error("Constitute electoral board unsuccessful. [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
					electoralAuthorityId);
			return false;
		}
	}

	/**
	 * Change the state of the electoral authority from constituted to {@link Status#SIGNED} for a given election event and electoral authority id.
	 *
	 * @param electionEventId      the election event id.
	 * @param electoralAuthorityId the electoral authority unique id.
	 * @param privateKeyPEM        the private key pem
	 * @return true if the status is successfully changed to signed. Otherwise, false.
	 * @throws ResourceNotFoundException if the electoral authority is not found.
	 */
	public boolean sign(final String electionEventId, final String electoralAuthorityId, final String privateKeyPEM)
			throws ResourceNotFoundException, IOException, GeneralCryptoLibException {

		final JsonObject electoralAuthorityJson = loadElectoralAuthorityJsonObject(electoralAuthorityId);

		if (electoralAuthorityJson != null && electoralAuthorityJson.containsKey(JsonConstants.STATUS)) {
			final String status = electoralAuthorityJson.getString(JsonConstants.STATUS);
			if (Status.READY.name().equals(status)) {

				final PrivateKey privateKey = PemUtils.privateKeyFromPem(privateKeyPEM);

				LOGGER.info("Signing authentication context data... [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
						electoralAuthorityId);
				signAuthenticationContextData(electionEventId, privateKey);

				LOGGER.info("Changing the status of the electoral authority... [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
						electoralAuthorityId);
				statusService.updateWithSynchronizedStatus(Status.SIGNED.name(), electoralAuthorityId, electoralAuthorityRepository,
						SynchronizeStatus.PENDING);

				LOGGER.info("The electoral authority was successfully signed. [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
						electoralAuthorityId);

				return true;
			}
		}

		return false;
	}

	/**
	 * Updates the electoral authority status from {@link Status#LOCKED} to {@link Status#READY}.
	 *
	 * @param electoralAuthorityId the electoral authority id
	 */
	private boolean updateElectoralAuthorityStatus(final String electoralAuthorityId) {
		final String status;
		try {
			status = loadElectoralAuthorityJsonObject(electoralAuthorityId).getString("status");
		} catch (ResourceNotFoundException e) {
			LOGGER.warn(String.format("Could not load the electoral authority JSON object. [electoralAuthorityId: %s]", electoralAuthorityId), e);
			return false;
		}
		if (!Status.LOCKED.name().equals(status)) {
			LOGGER.warn("Status of electoral authority is not LOCKED. [electoralAuthorityId: {}, status: {}]", electoralAuthorityId, status);
			return false;
		}

		statusService.update(Status.READY.name(), electoralAuthorityId, electoralAuthorityRepository);
		LOGGER.debug("Updated status of electoral authority from LOCKED to READY. [electoralAuthorityId: {}]", electoralAuthorityId);
		return true;
	}

	/**
	 * Signs the authentication context data.
	 *
	 * @param electionEventId the election event id
	 * @param privateKey      the private key
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void signAuthenticationContextData(final String electionEventId, final PrivateKey privateKey) throws IOException {

		final Path pathToFile = pathResolver.resolve(CONFIG_FILES_BASE_DIR, electionEventId, CONFIG_DIR_NAME_ONLINE, CONFIG_DIR_NAME_AUTHENTICATION);

		signFileDataJson(privateKey, pathToFile, CONFIG_FILE_NAME_AUTH_CONTEXT_DATA, CONFIG_FILE_NAME_SIGNED_AUTH_CONTEXT_DATA,
				AuthenticationContextData.class);

		signFileDataJson(privateKey, pathToFile, CONFIG_FILE_NAME_AUTH_VOTER_DATA, CONFIG_FILE_NAME_SIGNED_AUTH_VOTER_DATA,
				AuthenticationVoterData.class);
	}

	/**
	 * Loads the electoral authority json object.
	 *
	 * @param electoralAuthorityId the electoral authority id
	 * @return the electoral authority json object
	 * @throws ResourceNotFoundException if the electoral authority json object could not be retrieved.
	 */
	private JsonObject loadElectoralAuthorityJsonObject(final String electoralAuthorityId) throws ResourceNotFoundException {
		final String electoralAuthorityJSON = electoralAuthorityRepository.find(electoralAuthorityId);
		if (StringUtils.isEmpty(electoralAuthorityJSON) || JsonConstants.EMPTY_OBJECT.equals(electoralAuthorityJSON)) {
			throw new ResourceNotFoundException(String.format("Electoral Authority not found. [electoralAuthorityId: %s]", electoralAuthorityId));
		}
		return JsonUtils.getJsonObject(electoralAuthorityJSON);
	}

	/**
	 * Signs a json data file.
	 *
	 * @param privateKey              the private key
	 * @param pathToFile              the path to file
	 * @param fileNameToSign          the name of the file to sign
	 * @param fileNameSigned          the name of the file signed
	 * @param classJsonFileRepresents class that represents the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private <T> void signFileDataJson(final PrivateKey privateKey, final Path pathToFile, final String fileNameToSign, final String fileNameSigned,
			final Class<T> classJsonFileRepresents) throws IOException {

		final Path fileToSignPath = pathResolver.resolve(pathToFile.toString(), fileNameToSign);
		final Path fileSignedPath = pathResolver.resolve(pathToFile.toString(), fileNameSigned);

		if (!fileSignedPath.toFile().exists()) {
			final T objectToSign = configObjectMapper.fromJSONFileToJava(new File(fileToSignPath.toString()), classJsonFileRepresents);

			final String signedData = JsonSignatureService.sign(privateKey, objectToSign);
			final SignedObject signedDataObject = new SignedObject();
			signedDataObject.setSignature(signedData);
			configObjectMapper.fromJavaToJSONFile(signedDataObject, new File(fileSignedPath.toString()));
		}
	}
}
