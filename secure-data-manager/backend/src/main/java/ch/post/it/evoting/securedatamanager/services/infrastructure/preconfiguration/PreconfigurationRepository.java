/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.preconfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.orientechnologies.orient.core.storage.ORecordDuplicatedException;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.service.KeyStoreService;
import ch.post.it.evoting.securedatamanager.services.domain.model.EntityRepository;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.administrationauthority.AdministrationAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext.BallotTextRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.AdminPortalClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Implementation of operations with election event.
 */
@Repository
public class PreconfigurationRepository {

	private static final String EMPTY_SIGNATURE = "";

	private static final String INPUT_DATE_FORMAT = "dd/MM/yyyy HH:mm";

	private static final Logger LOGGER = LoggerFactory.getLogger(PreconfigurationRepository.class);

	private static final String ERROR_SAVING_DUPLICATED = "Error saving entity: {0}. It is duplicated.";

	private static final String ERROR_DOWNLOADING_DATA = "Error downloading data from administrator portal. Status code: {0}.";

	@Autowired
	private ElectionEventRepository electionEventRepository;

	@Autowired
	private BallotBoxRepository ballotBoxRepository;

	@Autowired
	private BallotRepository ballotRepository;

	@Autowired
	private BallotTextRepository ballotTextRepository;

	@Autowired
	private VotingCardSetRepository votingCardSetRepository;

	@Autowired
	private ElectoralAuthorityRepository electoralAuthorityRepository;

	@Autowired
	private AdministrationAuthorityRepository administrationAuthorityRepository;

	@Autowired
	private PathResolver pathResolver;

	@Autowired
	private KeyStoreService keystoreService;

	@Value("${tenantID}")
	private String tenantId;

	@Value("${admin.portal.url}")
	private String adminPortalBaseURL;

	@Value("${admin.portal.enabled:true}")
	private boolean isAdminPortalEnabled;

	@Autowired
	private ObjectReader jsonReader;

	/**
	 * Reads a json of configuration from a file and save the data related with each contests.
	 *
	 * @param filename the name of the file.
	 * @return The ids of the created election events.
	 * @throws IOException if there are any problem during json parsing.
	 */
	public String readFromFileAndSave(final String filename) throws IOException {
		// result
		final JsonObjectBuilder jsonBuilderResult = Json.createObjectBuilder();

		// load json from file
		try (final InputStream is = Files.newInputStream(Paths.get(filename))) {

			final JsonNode rootNode = jsonReader.readTree(is);

			// save election events
			final JsonNode enrichedElectionEvents = enrichElectionEvents(rootNode);
			saveElectionEvents(jsonBuilderResult, enrichedElectionEvents);

			// save ballots
			saveBallots(jsonBuilderResult, rootNode);

			// save ballot texts
			final JsonNode enrichedBallotTexts = enrichBallotTexts(rootNode);
			saveBallotTexts(enrichedBallotTexts);

			// save ballot boxes
			saveBallotBoxes(jsonBuilderResult, rootNode);

			// save voting card sets
			saveVotingCardSets(jsonBuilderResult, rootNode);

			// save electoral authorities
			saveElectoralAuthorities(jsonBuilderResult, rootNode);

			// save admin boards
			saveAdminBoards(jsonBuilderResult, rootNode);

			// because the relations ballot/ballotbox, votingcardset/ballot,
			// electoralauthority/ballotbox we needed to put them
			// in the json
			final JsonObject result = jsonBuilderResult.build();
			final JsonArray arrayBallots = result.getJsonArray(JsonConstants.BALLOTS);
			if (!arrayBallots.isEmpty()) {
				updateBallots(arrayBallots.getValuesAs(JsonString.class));
			}
			final JsonArray arrayVotingCardSets = result.getJsonArray(JsonConstants.VOTING_CARD_SETS);
			if (!arrayVotingCardSets.isEmpty()) {
				updateVotingCardSets(arrayVotingCardSets.getValuesAs(JsonString.class));
			}
			final JsonArray arrayElectoralAuthorities = result.getJsonArray(JsonConstants.ELECTORAL_AUTHORITIES);
			if (!arrayElectoralAuthorities.isEmpty()) {
				updateElectoralAuthorities(arrayElectoralAuthorities.getValuesAs(JsonString.class));
			}
			final JsonArray arrayBallotBoxes = result.getJsonArray(JsonConstants.BALLOT_BOXES);
			if (!arrayBallotBoxes.isEmpty()) {
				updateBallotBoxes(arrayBallotBoxes.getValuesAs(JsonString.class));
			}
			return result.toString();
		}
	}

	// save admin boards
	private void saveAdminBoards(final JsonObjectBuilder jsonBuilderResult, final JsonNode rootNode) {
		final JsonArrayBuilder jsonArraySaved = saveFromTree(rootNode.path(JsonConstants.ADMINISTRATION_AUTHORITIES), administrationAuthorityRepository);
		jsonBuilderResult.add(JsonConstants.ADMINISTRATION_AUTHORITIES, jsonArraySaved);
	}

	// save electoral authorities
	private void saveElectoralAuthorities(final JsonObjectBuilder jsonBuilderResult, final JsonNode rootNode) {
		final JsonArrayBuilder jsonArraySaved = saveFromTree(rootNode.path(JsonConstants.ELECTORAL_AUTHORITIES), electoralAuthorityRepository);
		jsonBuilderResult.add(JsonConstants.ELECTORAL_AUTHORITIES, jsonArraySaved);
	}

	// save voting card sets
	private void saveVotingCardSets(final JsonObjectBuilder jsonBuilderResult, final JsonNode rootNode) {
		final JsonArrayBuilder jsonArraySaved = saveFromTree(rootNode.path(JsonConstants.VOTING_CARD_SETS), votingCardSetRepository);
		jsonBuilderResult.add(JsonConstants.VOTING_CARD_SETS, jsonArraySaved);
	}

	// save ballot boxes
	private void saveBallotBoxes(final JsonObjectBuilder jsonBuilderResult, final JsonNode rootNode) {
		final JsonArrayBuilder jsonArraySaved = saveFromTree(rootNode.path(JsonConstants.BALLOT_BOXES), ballotBoxRepository);
		jsonBuilderResult.add(JsonConstants.BALLOT_BOXES, jsonArraySaved);
	}

	// save ballot boxes
	private void saveBallotTexts(final JsonNode rootNode) {
		saveFromTree(rootNode.path(JsonConstants.TRANSLATIONS), ballotTextRepository);
	}

	// save ballot boxes
	private JsonNode enrichBallotTexts(final JsonNode rootNode) throws IOException {
		final JsonArrayBuilder jsonArrayProcessed = Json.createArrayBuilder();
		for (final JsonNode node : rootNode.path(JsonConstants.TRANSLATIONS)) {
			try {
				// add id attribute to the ballot text with a concatenation
				// between ballot id and locale
				final String json = node.toString();
				final JsonObject object = JsonUtils.getJsonObject(json);
				final String id = node.path(JsonConstants.BALLOT).path(JsonConstants.ID).textValue() + node.path(JsonConstants.LOCALE).textValue();
				final JsonObject jsonObjectWithUpdatedId = JsonUtils.jsonObjectToBuilder(object).add(JsonConstants.ID, id)
						.add(JsonConstants.SIGNED_OBJECT, EMPTY_SIGNATURE).build();
				jsonArrayProcessed.add(jsonObjectWithUpdatedId);
			} catch (final ORecordDuplicatedException e) {
				// duplicated error
				final String entityName = ballotTextRepository.getClass().getName();
				LOGGER.error(MessageFormat.format(ERROR_SAVING_DUPLICATED, entityName, e));
			}
		}

		final String json = Json.createObjectBuilder().add(JsonConstants.TRANSLATIONS, jsonArrayProcessed).build().toString();

		return jsonReader.readTree(json);
	}

	// save election events
	private void saveElectionEvents(final JsonObjectBuilder jsonBuilderResult, final JsonNode rootNode) {
		final JsonArrayBuilder jsonArraySaved = saveFromTree(rootNode.path(JsonConstants.ELECTION_EVENTS), electionEventRepository);
		jsonBuilderResult.add(JsonConstants.ELECTION_EVENTS, jsonArraySaved);
	}

	// save election events
	private JsonNode enrichElectionEvents(final JsonNode rootNode) throws IOException {
		final JsonArrayBuilder jsonArrayProcessed = Json.createArrayBuilder();
		for (final JsonNode electionEventNode : rootNode.path(JsonConstants.ELECTION_EVENTS)) {
			final String electionEventId = electionEventNode.get(JsonConstants.ID).textValue();

			for (final JsonNode settingNode : rootNode.path(JsonConstants.SETTINGS)) {
				// add settings and status
				if (electionEventId.equals(settingNode.get(JsonConstants.ELECTION_EVENT).get(JsonConstants.ID).asText())) {
					final JsonObject electionEvent = JsonUtils.getJsonObject(electionEventNode.toString());
					final JsonObject setting = JsonUtils.getJsonObject(settingNode.toString());
					final JsonObject electionEventWithSettings = JsonUtils.jsonObjectToBuilder(electionEvent).add(JsonConstants.SETTINGS, setting).build();
					jsonArrayProcessed.add(electionEventWithSettings);
				}
			}
		}

		final String json = Json.createObjectBuilder().add(JsonConstants.ELECTION_EVENTS, jsonArrayProcessed).build().toString();

		return jsonReader.readTree(json);
	}

	// save ballot
	private void saveBallots(final JsonObjectBuilder jsonBuilderResult, final JsonNode rootNode) {
		final JsonArrayBuilder jsonArraySaved = saveFromTree(rootNode.path(JsonConstants.BALLOTS), ballotRepository);
		jsonBuilderResult.add(JsonConstants.BALLOTS, jsonArraySaved);
	}

	// convert date to iso instance format
	private String convertDate(final String dateInString) {
		final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(INPUT_DATE_FORMAT);
		final ZonedDateTime ldt = ZonedDateTime.parse(dateInString, inputFormatter.withZone(ZoneOffset.UTC));
		return ldt.toInstant().toString();
	}

	// Save from a set of entities taking into account status
	private JsonArrayBuilder saveFromTree(final JsonNode path, final EntityRepository repository) {
		final JsonArrayBuilder jsonArraySaved = Json.createArrayBuilder();
		for (final JsonNode node : path) {
			try {
				// update dates to correct format in iso instant
				updateDatesToIsoFormat(node);

				// add status attribute to the object with the current status as
				// value
				final String json = node.toString();
				final JsonObject object = JsonUtils.getJsonObject(json);
				final JsonObject jsonObjectWithStatus = JsonUtils.jsonObjectToBuilder(object)
						.add(JsonConstants.DETAILS, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss")))
						.add(JsonConstants.SYNCHRONIZED, Boolean.TRUE.toString()).build();
				final Optional<String> optionalId = saveOrUpdate(jsonObjectWithStatus, repository);
				optionalId.ifPresent(jsonArraySaved::add);
			} catch (final ORecordDuplicatedException e) {
				// duplicated error
				final String entityName = repository.getClass().getName();
				LOGGER.error(MessageFormat.format(ERROR_SAVING_DUPLICATED, entityName, e));
			}
		}
		return jsonArraySaved;
	}

	private Optional<String> saveOrUpdate(final JsonObject entityObject, final EntityRepository repository) {
		final String id = entityObject.getString(JsonConstants.ID);
		final String foundEntityString = repository.find(id);
		final JsonObject foundEntityObject = JsonUtils.getJsonObject(foundEntityString);
		Optional<String> resultId = Optional.empty();
		if (foundEntityObject.isEmpty()) {
			repository.save(entityObject.toString());
			resultId = Optional.of(id);
		} else if (!foundEntityObject.containsKey(JsonConstants.STATUS) || !entityObject.containsKey(JsonConstants.STATUS)) {
			repository.update(entityObject.toString());
			resultId = Optional.of(id);
		} else {

			try {

				final String entityStatus = entityObject.getString(JsonConstants.STATUS);
				final Status entityStatusEnumValue = Status.valueOf(entityStatus);
				final String foundEntityStatus = foundEntityObject.getString(JsonConstants.STATUS);
				final Status foundEntityStatusEnumValue = Status.valueOf(foundEntityStatus);

				if (entityStatusEnumValue.ordinal() > foundEntityStatusEnumValue.ordinal()) {
					repository.delete(id);
					repository.save(entityObject.toString());
					resultId = Optional.of(id);
				} else {
					// Entity status is before or equal to current status.
					LOGGER.debug(
							"Entity status is before or equal to current status, must not be updated. Entity {}, Entity status {}, Current status {}",
							id, entityStatusEnumValue, foundEntityStatusEnumValue);
				}
			} catch (final IllegalArgumentException e) {
				LOGGER.error("Not supported entity status found. You might need a new version of this tool that supports such type.", e);
			}
		}
		return resultId;
	}

	private void updateDatesToIsoFormat(final JsonNode node) {
		// convert dates to expected format
		if (node.has(JsonConstants.DATE_FROM)) {
			LOGGER.debug("Converting date for 'date from'");

			final String originalDate = node.path(JsonConstants.DATE_FROM).textValue();
			LOGGER.debug("Original value: {}", originalDate);

			final String convertedDate = convertDate(originalDate);
			LOGGER.debug("Converted to: {}", convertedDate);

			((ObjectNode) node).put(JsonConstants.DATE_FROM, convertedDate);
		}

		if (node.has(JsonConstants.DATE_TO)) {
			LOGGER.debug("Converting date for 'date to'");

			final String originalDate = node.path(JsonConstants.DATE_TO).textValue();
			LOGGER.debug("Original value: {}", originalDate);

			final String convertedDate = convertDate(originalDate);
			LOGGER.debug("Converted to: {}", convertedDate);

			((ObjectNode) node).put(JsonConstants.DATE_TO, convertedDate);
		}
	}

	private void updateBallots(final List<JsonString> ids) {
		final List<String> list = ids.stream().map(JsonString::getString).collect(Collectors.toList());
		ballotRepository.updateRelatedBallotBox(list);
	}

	private void updateVotingCardSets(final List<JsonString> ids) {
		final List<String> list = ids.stream().map(JsonString::getString).collect(Collectors.toList());
		votingCardSetRepository.updateRelatedBallot(list);
	}

	private void updateBallotBoxes(final List<JsonString> ids) {
		final List<String> list = ids.stream().map(JsonString::getString).collect(Collectors.toList());
		ballotBoxRepository.updateRelatedBallotAlias(list);
	}

	private void updateElectoralAuthorities(final List<JsonString> ids) {
		final List<String> list = ids.stream().map(JsonString::getString).collect(Collectors.toList());
		electoralAuthorityRepository.updateRelatedBallotBox(list);
	}

	/**
	 * Download configuration data from administration portal and save it on a json file.
	 *
	 * @param filename the name of the file where the data is stored.
	 * @return True if the preconfigurations are successfully downloaded. Otherwise, false.
	 * @throws IOException if there are any problem writing the configuration file.
	 */
	public boolean download(final String filename) throws IOException {
		LOGGER.info("Trying to download from {}", adminPortalBaseURL);
		final AdminPortalClient client = getAdminPortalClient(adminPortalBaseURL);

		final Response<ResponseBody> response;
		try {
			response = client.export(tenantId).execute();
		} catch (final IOException e) {
			LOGGER.error("Failed to communicate with admin portal.", e);
			throw new IOException("Failed to communicate with admin portal.");
		}

		if (!response.isSuccessful()) {
			final String errMsg = MessageFormat.format(ERROR_DOWNLOADING_DATA, response.code());
			LOGGER.error(errMsg);
			throw new IOException(errMsg);
		}

		final int status = response.code();
		final boolean result = status == javax.ws.rs.core.Response.Status.OK.getStatusCode();
		LOGGER.info("Connected to the Admin Portal: {}", result);

		// Extract the Admin portal configuration
		final String originalConfigurationContestsJson = new String(response.body().bytes(), StandardCharsets.UTF_8);

		// Filter valid election events and store related files.
		LOGGER.info("Filter election events and store related files");
		final String filteredConfigurationContestsJson = filterElectionEventsAndStoreFiles(originalConfigurationContestsJson);

		// Save the configuration to a file
		LOGGER.info("Store configuration to file {}", filename);
		saveJson(filename, filteredConfigurationContestsJson);

		LOGGER.info("Admin Portal configuration successfully downloaded and saved into {}", filename);
		return result;
	}

	/**
	 * Filters the valid election events and stores the files related to each election event.
	 * <p>
	 * Only the election events for which the corresponding local configuration exists in the Secure Data Manager integration output directory are
	 * valid.
	 * <br>The files related to each election event are:
	 * <ul>
	 *     <li>the input/output integration files</li>
	 *     <li>the voter options representation files (contained in the json file)</li>
	 *     <li>the encryption parameters file (contained in the json file)</li>
	 * </ul>
	 *
	 * @return The filtered configuration contest json
	 */
	private String filterElectionEventsAndStoreFiles(final String configurationContestsJson) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode rootNode = (ObjectNode) mapper.readTree(configurationContestsJson);
		final JsonNode electionEvents = rootNode.path(JsonConstants.ELECTION_EVENTS);
		if (!electionEvents.isMissingNode()) {
			final ArrayNode filteredElectionEvents = mapper.createArrayNode();
			for (final JsonNode electionEvent : electionEvents) {

				final String electionEventAlias = electionEvent.get(JsonConstants.ALIAS).asText();
				final Path sourceIntegrationOutputPath = pathResolver.resolveIntegrationOutputPath(electionEventAlias);

				// Filter valid election event on its integration output directory
				if (Files.exists(sourceIntegrationOutputPath)) {

					// Add the valid election event to the filtered array
					filteredElectionEvents.add(electionEvent);

					final String electionEventId = electionEvent.get(JsonConstants.ID).asText();

					// Output directory creation
					final Path electionEventOutputFolder = pathResolver.resolveOutputPath(electionEventId);
					if (!Files.exists(electionEventOutputFolder)) {
						Files.createDirectories(electionEventOutputFolder);
						LOGGER.info("Election event output directory created: {}", electionEventOutputFolder);
					}

					// Representation and encryption file store
					saveRepresentationsAndEncryptionParametersFiles(electionEvent, electionEventOutputFolder);
					LOGGER.info("Representations and encryptionParameters files saved from election event: {}", electionEventId);

					// Integration files copy
					copyIntegrationElectionEventOutputFiles(sourceIntegrationOutputPath, electionEventOutputFolder);
					LOGGER.info("Integration output files copied for election event: {}", electionEventId);

					// Input directory creation
					final Path electionEventInputFolder = pathResolver.resolveInputPath(electionEventId);
					if (!Files.exists(electionEventInputFolder)) {
						Files.createDirectories(electionEventInputFolder);
						LOGGER.info("Election event input directory created: {}", electionEventInputFolder);
					}
					copyConfigurationAnonymizedFiles(electionEventAlias, electionEventInputFolder);
					LOGGER.info("Configuration anonymized files copied");
				}
			}
			// Replace election events array with the filtered one
			rootNode.replace(JsonConstants.ELECTION_EVENTS, filteredElectionEvents);
		}

		// Return the json string of the configuration contest
		return mapper.writeValueAsString(rootNode);
	}

	/**
	 * Saves the voter options representations files and the encryption parameters file under election event output directory {@link
	 * PathResolver#resolveOutputPath}
	 *
	 * @param electionEvent           JsonNode that contains the files to save
	 * @param electionEventOutputPath the path where to save the files
	 * @throws IOException if an I/O error occurs when saving a binary file
	 */
	private void saveRepresentationsAndEncryptionParametersFiles(final JsonNode electionEvent, final Path electionEventOutputPath)
			throws IOException {
		final Path representationsFilePath = electionEventOutputPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV);
		if (!Files.exists(representationsFilePath)) {
			final String representationsFileB64 = electionEvent.get(JsonConstants.REPRESENTATIONS_FILE).asText();
			final byte[] representationsFile = Base64.getDecoder().decode(representationsFileB64);
			saveFile(representationsFilePath, representationsFile);
		}

		final Path representationsSignatureFilePath = electionEventOutputPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN);
		if (!Files.exists(representationsSignatureFilePath)) {
			final String representationsSignatureFileB64 = electionEvent.get(JsonConstants.REPRESENTATIONS_SIGNATURE_FILE).asText();
			final byte[] representationsSignatureFile = Base64.getDecoder().decode(representationsSignatureFileB64);
			saveFile(representationsSignatureFilePath, representationsSignatureFile);
		}

		final Path encryptionParametersFilePath = electionEventOutputPath.resolve(Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON);
		if (!Files.exists(encryptionParametersFilePath)) {
			final String encryptionParametersFileB64 = electionEvent.get(JsonConstants.ENCRYPTION_PARAMETERS_FILE).asText();
			final byte[] encryptionParametersFile = Base64.getDecoder().decode(encryptionParametersFileB64);
			saveFile(encryptionParametersFilePath, encryptionParametersFile);
		}
	}

	private void copyIntegrationElectionEventOutputFiles(final Path sourcePath, final Path destinationPath) throws IOException {
		FileUtils.copyDirectory(sourcePath.toFile(), destinationPath.toFile());
	}

	private void copyConfigurationAnonymizedFiles(final String electionEventAlias, final Path destinationPath) throws IOException {
		final Path sourcePath = pathResolver.resolveIntegrationInputPath(electionEventAlias);
		final Path configurationFile = destinationPath.resolve(Constants.CONFIG_FILE_NAME_CONFIGURATION_ANONYMIZED);
		if (!Files.exists(configurationFile)) {
			Files.copy(sourcePath.resolve(Constants.CONFIG_FILE_NAME_CONFIGURATION_ANONYMIZED), configurationFile);
		}
		final Path configurationFileSignature = destinationPath.resolve(Constants.CONFIG_FILE_NAME_CONFIGURATION_ANONYMIZED_SIGN);
		if (!Files.exists(configurationFileSignature)) {
			Files.copy(sourcePath.resolve(Constants.CONFIG_FILE_NAME_CONFIGURATION_ANONYMIZED_SIGN), configurationFileSignature);
		}
	}

	/**
	 * Saves json in to a file.
	 *
	 * @param filename name of the file to store data.
	 * @param json     the json to be stored.
	 * @throws IOException if something fails when json is saving.
	 */
	private void saveJson(final String filename, final String json) throws IOException {
		try (final FileWriterWithEncoding fw = new FileWriterWithEncoding(filename, StandardCharsets.UTF_8)) {
			fw.write(json);
		}
	}

	/**
	 * Save binary data in to a file
	 *
	 * @throws IOException if something fails when file is written.
	 */
	private void saveFile(final Path path, final byte[] data) throws IOException {
		try (final OutputStream outputStream = Files.newOutputStream(path)) {
			outputStream.write(data);
		}
	}

	/**
	 * Get admin portal client.
	 *
	 * @param uri - the URI of the web resource.
	 * @return a client to admin portal.
	 */
	@VisibleForTesting
	AdminPortalClient getAdminPortalClient(final String uri) {
		final PrivateKey privateKey = keystoreService.getPrivateKey();
		return RestClientService.getInstance().getRestClientWithInterceptorAndJacksonConverter(uri, privateKey, "SECURE_DATA_MANAGER")
				.create(AdminPortalClient.class);
	}

}
