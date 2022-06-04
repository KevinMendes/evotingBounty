/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newDirectoryStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cms.CMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;
import ch.post.it.evoting.cryptolib.cmssigner.CMSSigner;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxIdImpl;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.ConsistencyCheckException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.EntityRepository;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.administrationauthority.AdministrationAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext.BallotTextRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@Service
public class ExportImportService {
	public static final int MAX_DEPTH = 1;

	public static final String SDM = "sdm_";

	public static final String MANY_CHARS_REGEX = "(.*)";

	public static final String BALLOT_BOX_ID_PLACEHOLDER = "%s";

	public static final String PAYLOAD_REGEX_FORMAT =
			new BallotBoxIdImpl(MANY_CHARS_REGEX, MANY_CHARS_REGEX, BALLOT_BOX_ID_PLACEHOLDER) + MANY_CHARS_REGEX;

	private static final Logger LOGGER = LoggerFactory.getLogger(ExportImportService.class);

	private static final CopyOption[] COPY_OPTIONS = { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

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
	@Qualifier(value = "absolutePathResolver")
	private PathResolver absolutePathResolver;

	@Autowired
	private HashService hashService;

	@Autowired
	private StoresServiceAPI storesService;

	@Autowired
	private ConsistencyCheckService consistencyCheckService;

	@Autowired
	private SignaturesVerifierService signaturesVerifierService;

	@Value("${tenantID}")
	private String tenantId;

	private static boolean isNodeContributions(final Path file) {
		final String name = file.getFileName().toString();
		return name.startsWith(Constants.CONFIG_FILE_NAME_NODE_CONTRIBUTIONS) && name.endsWith(Constants.JSON);
	}

	private static boolean isChoiceCodeGenerationPayload(final Path file) {
		final String name = file.getFileName().toString();
		return name.startsWith(Constants.CONFIG_FILE_NAME_PREFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD) && name
				.endsWith(Constants.CONFIG_FILE_NAME_SUFFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD);
	}

	private static boolean isControlComponentPublicKeysPayload(final Path file) {
		final String name = file.getFileName().toString();
		return name.startsWith(Constants.CONFIG_FILE_NAME_PREFIX_CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD) && name.endsWith(Constants.JSON);
	}

	private static boolean isElectionEventContextPayload(final Path file) {
		final String name = file.getFileName().toString();
		return name.equals(Constants.CONFIG_FILE_NAME_ELECTION_EVENT_CONTEXT_PAYLOAD);
	}

	public void dumpDatabase(final String eeid) throws ResourceNotFoundException {
		final JsonObjectBuilder dumpJsonBuilder = Json.createObjectBuilder();

		final String adminBoards = administrationAuthorityRepository.list();
		dumpJsonBuilder.add(JsonConstants.ADMINISTRATION_AUTHORITIES, JsonUtils.getJsonObject(adminBoards).getJsonArray(JsonConstants.RESULT));

		final String electionEvent = electionEventRepository.find(eeid);

		if (StringUtils.isEmpty(electionEvent) || JsonConstants.EMPTY_OBJECT.equals(electionEvent)) {
			throw new ResourceNotFoundException("Election Event not found");
		}

		final JsonArray electionEvents = Json.createArrayBuilder().add(JsonUtils.getJsonObject(electionEvent)).build();
		dumpJsonBuilder.add(JsonConstants.ELECTION_EVENTS, electionEvents);

		final String ballots = ballotRepository.listByElectionEvent(eeid);
		final JsonArray ballotsArray = JsonUtils.getJsonObject(ballots).getJsonArray(JsonConstants.RESULT);
		dumpJsonBuilder.add(JsonConstants.BALLOTS, ballotsArray);

		final JsonArrayBuilder ballotBoxTextsArrayBuilder = Json.createArrayBuilder();
		for (final JsonValue ballotValue : ballotsArray) {
			final JsonObject ballotObject = (JsonObject) ballotValue;
			final String id = ballotObject.getString(JsonConstants.ID);
			final String ballotTexts = ballotTextRepository.list(Collections.singletonMap(JsonConstants.BALLOT_ID, id));
			final JsonArray ballotTextsForBallot = JsonUtils.getJsonObject(ballotTexts).getJsonArray(JsonConstants.RESULT);
			for (final JsonValue ballotText : ballotTextsForBallot) {
				ballotBoxTextsArrayBuilder.add(ballotText);
			}
		}
		dumpJsonBuilder.add(JsonConstants.TEXTS, ballotBoxTextsArrayBuilder.build());

		final String ballotBoxes = ballotBoxRepository.listByElectionEvent(eeid);
		dumpJsonBuilder.add(JsonConstants.BALLOT_BOXES, JsonUtils.getJsonObject(ballotBoxes).getJsonArray(JsonConstants.RESULT));

		final String votingCardSets = votingCardSetRepository.listByElectionEvent(eeid);
		dumpJsonBuilder.add(JsonConstants.VOTING_CARD_SETS, JsonUtils.getJsonObject(votingCardSets).getJsonArray(JsonConstants.RESULT));

		final String electoralAuthorities = electoralAuthorityRepository.listByElectionEvent(eeid);
		dumpJsonBuilder.add(JsonConstants.ELECTORAL_AUTHORITIES, JsonUtils.getJsonObject(electoralAuthorities).getJsonArray(JsonConstants.RESULT));

		final Path dumpPath = getPathOfDumpDatabase();

		try {
			Files.write(dumpPath, dumpJsonBuilder.build().toString().getBytes(StandardCharsets.UTF_8));

			LOGGER.info("Database export to dump file has been completed successfully: {}", dumpPath);
		} catch (final IOException e) {
			LOGGER.error("An error occurred writing DB dump to: {}", dumpPath, e);
		}

	}

	public void importDatabase() throws IOException, CertificateException, ConsistencyCheckException, GeneralCryptoLibException, CMSException {

		final Path dumpPath = getPathOfDumpDatabase();
		if (!Files.exists(dumpPath)) {
			LOGGER.warn("There is no dump database to import");
			return;
		}

		final String dump;
		try {
			dump = new String(Files.readAllBytes(dumpPath), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new IOException("Error reading import file ", e);
		}

		final JsonObject dumpJson = JsonUtils.getJsonObject(dump);
		final JsonArray adminBoards = dumpJson.getJsonArray(JsonConstants.ADMINISTRATION_AUTHORITIES);
		for (final JsonValue adminBoard : adminBoards) {
			saveOrUpdate(adminBoard, administrationAuthorityRepository);
		}

		final JsonArray electionEvents = dumpJson.getJsonArray(JsonConstants.ELECTION_EVENTS);
		for (final JsonValue electionEvent : electionEvents) {
			checkSignaturesAndConsistency(electionEvent);
			saveOrUpdate(electionEvent, electionEventRepository);
		}

		final JsonArray ballots = dumpJson.getJsonArray(JsonConstants.BALLOTS);
		for (final JsonValue ballot : ballots) {
			saveOrUpdate(ballot, ballotRepository);
		}

		final JsonArray ballotTexts = dumpJson.getJsonArray(JsonConstants.TEXTS);
		for (final JsonValue ballotText : ballotTexts) {
			saveOrUpdate(ballotText, ballotTextRepository);
		}

		final JsonArray ballotBoxes = dumpJson.getJsonArray(JsonConstants.BALLOT_BOXES);
		for (final JsonValue ballotBox : ballotBoxes) {
			saveOrUpdate(ballotBox, ballotBoxRepository);
		}

		final JsonArray votingCardSets = dumpJson.getJsonArray(JsonConstants.VOTING_CARD_SETS);
		for (final JsonValue votingCardSet : votingCardSets) {
			saveOrUpdate(votingCardSet, votingCardSetRepository);
		}

		final JsonArray electoralAuthorities = dumpJson.getJsonArray(JsonConstants.ELECTORAL_AUTHORITIES);
		for (final JsonValue electoralAuthority : electoralAuthorities) {
			saveOrUpdate(electoralAuthority, electoralAuthorityRepository);
		}
	}

	private void saveOrUpdate(final JsonValue entity, final EntityRepository repository) {
		final JsonObject entityObject = (JsonObject) entity;
		final String id = entityObject.getString(JsonConstants.ID);
		final String foundEntityString = repository.find(id);
		final JsonObject foundEntityObject = JsonUtils.getJsonObject(foundEntityString);

		if (foundEntityObject.isEmpty()) {

			repository.save(entity.toString());
		} else if (!foundEntityObject.containsKey(JsonConstants.STATUS) || !entityObject.containsKey(JsonConstants.STATUS)) {

			repository.update(entity.toString());
		} else {

			try {

				final String entityStatus = entityObject.getString(JsonConstants.STATUS);
				final Status entityStatusEnumValue = Enum.valueOf(Status.class, entityStatus);
				final String foundEntityStatus = foundEntityObject.getString(JsonConstants.STATUS);
				final Status foundEntityStatusEnumValue = Enum.valueOf(Status.class, foundEntityStatus);

				if (foundEntityStatusEnumValue.isBefore(entityStatusEnumValue)) {
					repository.delete(id);
					repository.save(entity.toString());
				} else {
					LOGGER.warn("Entity {} can't be updated", id);
				}
			} catch (final IllegalArgumentException e) {
				LOGGER.error("Not supported entity status found. You might need a new version of this tool that supports such type.", e);
			}
		}
	}

	/**
	 * This method verifies the signature of the prime numbers and encryption parameters files and then check the consistency of the encryption
	 * parameters. The consistency check of the prime numbers is done a the configuration level in offline SDM where primes will be trusted.
	 *
	 * @param entity
	 * @throws CertificateException
	 * @throws GeneralCryptoLibException
	 * @throws CMSException
	 * @throws IOException
	 * @throws ConsistencyCheckException
	 */
	private void checkSignaturesAndConsistency(final JsonValue entity)
			throws CertificateException, GeneralCryptoLibException, CMSException, IOException, ConsistencyCheckException {
		final JsonObject eventObject = (JsonObject) entity;
		final String eeid = eventObject.getString(JsonConstants.ID);

		// Get encryption params
		final JsonObject encryptionParameters = eventObject.getJsonObject(JsonConstants.SETTINGS).getJsonObject(JsonConstants.ENCRYPTION_PARAMETERS);

		// Verify jwt with that trusted chain and check consistency between jwt and encryption params
		final VerifiableElGamalEncryptionParameters verifiedParams = signaturesVerifierService.verifyEncryptionParams(eeid);

		if (!consistencyCheckService.encryptionParamsConsistent(encryptionParameters, verifiedParams.getGroup())) {
			throw new ConsistencyCheckException("Encryption parameters consistency check between election event data and signed jwt failed.");
		}
	}

	/**
	 * Export Election Event Data only, without voting cards nor customer specific data
	 *
	 * @param usbDrive usb drive
	 * @param eeId     election event id
	 * @param eeAlias  election event alias
	 * @throws IOException
	 */
	public void exportElectionEventWithoutElectionInformation(final String usbDrive, final String eeId, final String eeAlias) throws IOException {

		final Path sdmFolder = pathResolver.resolve(Constants.SDM_DIR_NAME);
		final Path usbSdmFolder = absolutePathResolver.resolve(usbDrive, SDM + eeAlias);

		final Path configFolder = sdmFolder.resolve(Constants.CONFIG_DIR_NAME);
		final Path toConfigFolder = usbSdmFolder.resolve(Constants.CONFIG_DIR_NAME);

		final Set<String> files = new HashSet<>();
		files.add(Constants.SDM_CONFIG_DIR_NAME);
		files.add(Constants.CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON); // Copy
		files.add(Constants.CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON_SIGN);// elections_config.json
		// and signature

		files.add(eeId);
		files.add(Constants.CONFIG_DIR_NAME);
		files.add(Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_JSON);// Copy
		// encryptionParameters.json
		files.add(Constants.CONFIG_DIR_NAME_OFFLINE);
		files.add(Constants.CONFIG_FILE_NAME_AUTHORITIESCA_PEM);// Copy
		// authoritiesca.pem
		// file
		files.add(Constants.DBDUMP_FILE_NAME);// Copy db_dump.json file
		files.add(Constants.DBDUMP_SIGNATURE_FILE_NAME);// Copy db_dump.json file signature
		files.add(Constants.CONFIG_FILE_NAME_PLATFORM_ROOT_CA); // platformRootCA.pem file
		files.add(String.format(Constants.CONFIG_FILE_NAME_TENANT_CA_PATTERN, tenantId)); // tenantCA-<tenant
		// ID>-CA.pem
		// file
		Filter<Path> filter = file -> files.contains(file.getFileName().toString());
		copyFolder(sdmFolder, usbSdmFolder, filter, false);

		// Copy all csr folder
		filter = file -> true;
		final Path csrFolder = configFolder.resolve(Constants.CSR_FOLDER);
		final Path csrUSBFolder = toConfigFolder.resolve(Constants.CSR_FOLDER);
		copyFolder(csrFolder, csrUSBFolder, filter, false);

		// Copy ONLINE folder without voterMaterial, voteVerification ,
		// electionInformation and printing
		final Set<String> exceptions = new HashSet<>();
		exceptions.add(Constants.CONFIG_DIR_NAME_VOTERMATERIAL);
		exceptions.add(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);
		exceptions.add(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION);
		exceptions.add(Constants.CONFIG_DIR_NAME_PRINTING);
		filter = file -> !exceptions.contains(file.getFileName().toString());

		final Path onlineFolder = configFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE);
		final Path onlineUSBFolder = toConfigFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE);
		copyFolder(onlineFolder, onlineUSBFolder, filter, false);

		final Set<String> requiredFiles = new HashSet<>();
		requiredFiles.add(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION + Constants.CSV);
		requiredFiles.add(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION + Constants.CSV + Constants.SIGN);
		requiredFiles.add(Constants.CONFIG_FILE_NAME_VERIFICATIONSET_DATA);
		requiredFiles.add(Constants.CONFIG_FILE_NAME_VERIFICATIONSET_DATA + Constants.SIGN);
		filter = file -> Files.isDirectory(file) || requiredFiles.contains(file.getFileName().toString());

		// Copy voterInformation.csv
		final Path voterMaterialFolder = configFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL);
		final Path voterMaterialUsbFolder = toConfigFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL);
		copyFolder(voterMaterialFolder, voterMaterialUsbFolder, filter, false);

		// Copy verificationCardSetData.json
		final Path voteVerificationFolder = configFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);
		final Path voteVerificationUsbFolder = toConfigFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);
		copyFolder(voteVerificationFolder, voteVerificationUsbFolder, filter, false);

	}

	/**
	 * Exports the files of the election information context for a given election event
	 *
	 * @param usbDrive
	 * @param eeId
	 * @param eeAlias
	 * @throws IOException
	 */
	public void exportElectionEventElectionInformation(final String usbDrive, final String eeId, final String eeAlias) throws IOException {
		final Path sdmFolder = pathResolver.resolve(Constants.SDM_DIR_NAME);
		final Path usbSdmFolder = absolutePathResolver.resolve(usbDrive, SDM + eeAlias);

		final Path configFolder = sdmFolder.resolve(Constants.CONFIG_DIR_NAME);
		final Path toConfigFolder = usbSdmFolder.resolve(Constants.CONFIG_DIR_NAME);
		final Path electionInformationFolder = configFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION);
		final Path electionInformationUsbFolder = toConfigFolder.resolve(eeId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION);
		if (Files.exists(electionInformationFolder)) {
			copyFolder(electionInformationFolder, electionInformationUsbFolder, getElectionInformationFilter(electionInformationFolder), true);
		} else {
			LOGGER.warn("No files have been found in the election information folder");
		}
	}

	/**
	 * Export Voting Cards only
	 *
	 * @param usbDrive usb drive
	 * @param eeId     election event id
	 * @param eeAlias  election event alias
	 * @throws IOException
	 */
	public void exportVotingCards(final String usbDrive, final String eeId, final String eeAlias) throws IOException {

		final Path sdmOnlineFolder = pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, eeId, Constants.CONFIG_DIR_NAME_ONLINE);
		final Path usbOnlineFolder = absolutePathResolver
				.resolve(usbDrive, SDM + eeAlias, Constants.CONFIG_DIR_NAME, eeId, Constants.CONFIG_DIR_NAME_ONLINE);

		// Copy voterMaterial folder
		final Set<String> exceptions = new HashSet<>();
		exceptions.add(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION + Constants.CSV);
		exceptions.add(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION + Constants.CSV + Constants.SIGN);
		exceptions.add(Constants.CONFIG_FILE_NAME_VERIFICATIONSET_DATA);
		exceptions.add(Constants.CONFIG_FILE_NAME_VERIFICATIONSET_DATA + Constants.SIGN);
		final Filter<Path> filter = file -> !exceptions.contains(file.getFileName().toString());
		final Path voterMaterialFolder = sdmOnlineFolder.resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL);
		final Path usbVoterMaterialFolder = usbOnlineFolder.resolve(Constants.CONFIG_DIR_NAME_VOTERMATERIAL);
		copyFolder(voterMaterialFolder, usbVoterMaterialFolder, filter, false);

		// Copy voteVerification folder
		final Path voteVerification = sdmOnlineFolder.resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);
		final Path usbVoteVerificationFolder = usbOnlineFolder.resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);
		copyFolder(voteVerification, usbVoteVerificationFolder, filter, false);
	}

	/**
	 * Export customer specific data only
	 *
	 * @param usbDrive usb drive
	 * @param eeId     election event id
	 * @param eeAlias  election event alias
	 * @throws IOException
	 */
	public void exportCustomerSpecificData(final String usbDrive, final String eeId, final String eeAlias) throws IOException {

		final Filter<Path> filter = file -> true;
		final Path customerFolder = pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, eeId, Constants.CONFIG_DIR_NAME_CUSTOMER);
		final Path usbCustomerFolder = absolutePathResolver
				.resolve(usbDrive, SDM + eeAlias, Constants.CONFIG_DIR_NAME, eeId, Constants.CONFIG_DIR_NAME_CUSTOMER);

		copyFolder(customerFolder, usbCustomerFolder, filter, false);
	}

	/**
	 * Import all files from selected exported election event to user/sdm
	 *
	 * @param usbElectionPath path to selected exported election event
	 * @throws IOException
	 */
	public void importData(final String usbElectionPath) throws IOException {
		final Filter<Path> filter = file -> true;
		final Path usbFolder = absolutePathResolver.resolve(usbElectionPath);
		final Path sdmFolder = pathResolver.resolve(Constants.SDM_DIR_NAME);

		copyFolder(usbFolder, sdmFolder, filter, false);
	}

	private void copyFolder(final Path source, final Path dest, final Filter<Path> filter, final boolean isExportingElectionInformationFolders)
			throws IOException {
		if (!Files.exists(source)) {
			return;
		}

		if (Files.isDirectory(source)) {
			if (!Files.exists(dest)) {
				Files.createDirectories(dest);
				LOGGER.info("Directory created from {} to {}", source, dest);
			}
			Filter<Path> electionInformationFilter = filter;
			try (final DirectoryStream<Path> stream = Files.newDirectoryStream(source, electionInformationFilter)) {
				for (final Path file : stream) {
					if (isExportingElectionInformationFolders) {
						electionInformationFilter = getElectionInformationFilter(file);
					}
					copyFolder(file, dest.resolve(file.getFileName()), electionInformationFilter, isExportingElectionInformationFolders);
				}
			}
		} else {
			try {

				copy(source, dest, COPY_OPTIONS);

				LOGGER.info("File copied from {} to {}", source, dest);
			} catch (final IOException e) {
				LOGGER.error("Error copying files from {} to {}", source, dest, e);
			}
		}
	}

	private Filter<Path> getElectionInformationFilter(final Path source) throws IOException {
		/*
		 * The decompressed votes will be present only when the ballot box is decrypted. Once that stage
		 * is reached, the decrypted contents must not be exported.
		 */
		/*
		 * This includes legacy tally scenario.
		 */
		if (isFilePresent("decompressedVotes.csv", source)) {
			return file -> false;
		}
		/*
		 * The downloaded ballot box containing the raw ballot box information will be present with the
		 * rest of the audit files (previous to last control components mixing payloads)
		 * when the control component payload containing the mixed ballot box is downloaded.
		 */
		/*
		 * This includes legacy offline mixing scenarios.
		 */
		if (isFilePresent("downloadedBallotBox.csv", source)) {
			final String ballotBoxId = source.getFileName().toString();
			// tenantId-(.*)-ballotBoxId(.*)
			final String payloadRegex = String.format(PAYLOAD_REGEX_FORMAT, ballotBoxId);
			final String regex = "^ballotBox(.*)|^downloadedBallotBox(.*)|^" + payloadRegex;
			return file -> file.getFileName().toString().matches(regex);
		}

		/*
		 * The default scenario is the pre-electoral scenario, so we want to export all the
		 * configuration data.
		 */
		final String regex = "^ballotBox(.*)|^ballot.json$|^electionInformationContents.json(.*)";
		return file -> Files.isDirectory(file) || file.getFileName().toString().matches(regex);
	}

	/**
	 * @param fileName
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public boolean isFilePresent(final String fileName, final Path source) throws IOException {
		try (final Stream<Path> walk = Files.walk(source, MAX_DEPTH)) {
			return walk.anyMatch(path -> path.getFileName().toString().equals(fileName));
		}
	}

	/**
	 * Exports computed choice codes for given USB drive, election event and election event alias.
	 *
	 * @param usbDrive        the USB drive
	 * @param electionEventId the election event identifier
	 * @param eeAlias         the election event alias
	 * @throws IOException I/O error occurred.
	 */
	public void exportComputedChoiceCodes(final String usbDrive, final String electionEventId, final String eeAlias) throws IOException {
		final Path sourceFolder = pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, electionEventId,
				Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);
		final Path destinationFolder = absolutePathResolver
				.resolve(usbDrive, SDM + eeAlias, Constants.CONFIG_DIR_NAME, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
						Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);

		final String json = votingCardSetRepository.listByElectionEvent(electionEventId);
		final JsonObject object = JsonUtils.getJsonObject(json);
		final JsonArray array = object.getJsonArray(JsonConstants.RESULT);
		for (final JsonValue value : array) {
			final JsonObject votingCardSet = (JsonObject) value;
			JsonString attribute = votingCardSet.getJsonString(JsonConstants.STATUS);
			final Status status = Status.valueOf(attribute.getString());
			if (status == Status.COMPUTED || status.isBefore(Status.COMPUTED)) {
				// computation results have not been downloaded yet.
				continue;
			}
			attribute = votingCardSet.getJsonString(JsonConstants.VERIFICATION_CARD_SET_ID);
			final String verificationCardSetId = attribute.getString();
			final Path verificationCardSetFolder = sourceFolder.resolve(verificationCardSetId);
			final Filter<Path> filter = ExportImportService::isNodeContributions;
			try (final DirectoryStream<Path> files = newDirectoryStream(verificationCardSetFolder, filter)) {
				for (final Path source : files) {
					final Path path = sourceFolder.relativize(source);
					final Path destination = destinationFolder.resolve(path);
					createDirectories(destination.getParent());
					copy(source, destination, COPY_OPTIONS);
				}
			}
		}
	}

	/**
	 * Exports pre-computed choice codes for given USB drive, election event and election event alias.
	 *
	 * @param usbDrive        the USB drive
	 * @param electionEventId the election event identifier
	 * @param eeAlias         the election event alias
	 * @throws IOException I/O error occurred.
	 */
	public void exportPreComputedChoiceCodes(final String usbDrive, final String electionEventId, final String eeAlias) throws IOException {
		final Path sourceFolder = pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, electionEventId,
				Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);
		final Path destinationFolder = absolutePathResolver
				.resolve(usbDrive, SDM + eeAlias, Constants.CONFIG_DIR_NAME, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
						Constants.CONFIG_DIR_NAME_VOTEVERIFICATION);

		final String json = votingCardSetRepository.listByElectionEvent(electionEventId);
		final JsonObject object = JsonUtils.getJsonObject(json);
		final JsonArray array = object.getJsonArray(JsonConstants.RESULT);
		for (final JsonValue value : array) {
			final JsonObject votingCardSet = (JsonObject) value;
			JsonString attribute = votingCardSet.getJsonString(JsonConstants.STATUS);
			final Status status = Status.valueOf(attribute.getString());
			if (status.isBefore(Status.PRECOMPUTED)) {
				// pre-computation has not been done yet.
				continue;
			}
			attribute = votingCardSet.getJsonString(JsonConstants.VERIFICATION_CARD_SET_ID);
			final String verificationCardSetId = attribute.getString();
			final Path verificationCardSetFolder = sourceFolder.resolve(verificationCardSetId);
			final Filter<Path> filter = ExportImportService::isChoiceCodeGenerationPayload;
			try (final DirectoryStream<Path> files = newDirectoryStream(verificationCardSetFolder, filter)) {
				for (final Path source : files) {
					final Path path = sourceFolder.relativize(source);
					final Path destination = destinationFolder.resolve(path);
					createDirectories(destination.getParent());
					copy(source, destination, COPY_OPTIONS);
				}
			}
		}
	}

	/**
	 * Exports ballot boxes for given USB drive, election event and election event alias.
	 *
	 * @param usbDrive        the USB drive
	 * @param electionEventId the election event identifier
	 * @param eeAlias         the election event alias
	 * @throws IOException I/O error occurred.
	 */
	public void exportBallotBoxes(final String usbDrive, final String electionEventId, final String eeAlias) throws IOException {
		final Path sourceFolder = pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, electionEventId,
				Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION, Constants.CONFIG_DIR_NAME_BALLOTS);
		final Path destinationFolder = absolutePathResolver
				.resolve(usbDrive, SDM + eeAlias, Constants.CONFIG_DIR_NAME, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
						Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION, Constants.CONFIG_DIR_NAME_BALLOTS);
		copyFolder(sourceFolder, destinationFolder, p -> true, false);
	}

	/**
	 * Exports the election event context and the control components keys for given USB drive, election event and election event alias.
	 *
	 * @param usbDrive        the USB drive
	 * @param electionEventId the election event identifier
	 * @param eeAlias         the election event alias
	 * @throws IOException I/O error occurred.
	 */
	public void exportElectionEventContextAndControlComponentKeys(final String usbDrive, final String electionEventId, final String eeAlias)
			throws IOException {
		final Path sourceFolder = pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, electionEventId);
		final Path destinationFolder = absolutePathResolver.resolve(usbDrive, SDM + eeAlias, Constants.CONFIG_DIR_NAME, electionEventId);
		final Filter<Path> filter = file -> isControlComponentPublicKeysPayload(file) || isElectionEventContextPayload(file);

		copyFolder(sourceFolder, destinationFolder, filter, false);
	}

	/**
	 * Verifies the signature of both db dump and elections config files.
	 *
	 * @throws IOException
	 * @throws CMSException
	 * @throws CertificateException
	 * @throws GeneralCryptoLibException
	 */
	public void verifySignaturesOnImport() throws IOException, CMSException, CertificateException, GeneralCryptoLibException {
		signaturesVerifierService.verifyPkcs7(getPathOfDumpDatabase(), getPathOfDumpDatabaseSignature());
		signaturesVerifierService.verifyPkcs7(getPathOfElectionsConfig(), getPathOfElectionsConfigSignature());
	}

	/**
	 * Signs database dump and elections config files under the CMS PKCS7 standard.
	 *
	 * @param password
	 * @throws IOException  I/O error occurred
	 * @throws CMSException Signing file in P7 format error
	 */
	public void signDumpDatabaseAndElectionsConfig(final char[] password)
			throws IOException, CMSException, GeneralCryptoLibException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		final KeyStore keyStore = getOnlineKeyStore(password);
		final Enumeration<String> keyAliases = keyStore.aliases();
		final String keyAlias = keyAliases.nextElement();
		if (keyAliases.hasMoreElements()) {
			throw new IllegalArgumentException("There should be exactly one private key in the keystore");
		}
		final PrivateKey signingKey = (PrivateKey) keyStore.getKey(keyAlias, password);
		final List<Certificate> chainAsList = new ArrayList<>(Arrays.asList(keyStore.getCertificateChain(keyAlias)));
		final Certificate signerCert = chainAsList.remove(0);

		// Sign Database dump
		final Path dumpDatabasePath = getPathOfDumpDatabase();
		signFile(dumpDatabasePath, signingKey, chainAsList, signerCert);

		// Sign elections_config.json file
		final Path electionsConfig = getPathOfElectionsConfig();
		signFile(electionsConfig, signingKey, chainAsList, signerCert);
	}

	private KeyStore getOnlineKeyStore(final char[] password) throws IOException, GeneralCryptoLibException {
		final Path keyStoreOnlinePath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.INTEGRATION_KEYSTORE_ONLINE_FILE);
		try (final InputStream in = new FileInputStream(keyStoreOnlinePath.toFile())) {
			return storesService.loadKeyStore(KeyStoreType.PKCS12, in, password);
		}
	}

	private void signFile(final Path filePath, final PrivateKey signingKey, final List<Certificate> chain, final Certificate signerCert)
			throws IOException, CMSException {

		final Path signedFilePath = filePath.resolveSibling(filePath.getFileName() + CMSSigner.SIGNATURE_FILE_EXTENSION);

		CMSSigner.sign(filePath.toFile(), signedFilePath.toFile(), signerCert, chain, signingKey);
	}

	private Path getPathOfDumpDatabase() {
		return pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.DBDUMP_FILE_NAME);
	}

	private Path getPathOfDumpDatabaseSignature() {
		return pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.DBDUMP_SIGNATURE_FILE_NAME);
	}

	private Path getPathOfElectionsConfig() {
		return pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.SDM_CONFIG_DIR_NAME, Constants.CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON);
	}

	private Path getPathOfElectionsConfigSignature() {
		return pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.SDM_CONFIG_DIR_NAME, Constants.CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON_SIGN);
	}
}
