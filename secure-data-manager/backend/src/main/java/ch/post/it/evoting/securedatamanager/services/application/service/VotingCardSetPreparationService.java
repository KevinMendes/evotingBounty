/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.securedatamanager.services.application.sign.FileSignature;
import ch.post.it.evoting.securedatamanager.services.application.sign.FileSignerService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@Service
public class VotingCardSetPreparationService {

	@VisibleForTesting
	static final String CHALLENGE_PROFILE = "challenge";

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetPreparationService.class);

	private static final String SIGNATURE_COMPONENT = "SDM Preparation Service";

	// CSV Writer config
	private static final char SEMICOLON_SEPARATOR = ';';
	private static final CsvMapper CSV_MAPPER = new CsvMapper();
	private static final CsvSchema CSV_SCHEMA_WITHOUT_QUOTE_CHAR = CSV_MAPPER.schemaFor(List.class).withColumnSeparator(SEMICOLON_SEPARATOR)
			.withoutQuoteChar();
	private static final ObjectWriter CSV_OBJECT_WRITER_WITHOUT_QUOTE_CHAR = CSV_MAPPER.writer(CSV_SCHEMA_WITHOUT_QUOTE_CHAR);
	private final VotingCardSetRepository votingCardSetRepository;
	private final ElectionEventService electionEventService;
	private final PathResolver pathResolver;
	private final FileSignerService fileSignerService;
	private final ObjectMapper objectMapper;
	private final String activeProfiles;

	@Value("${preparation.aliases.file:aliases.csv}")
	private String aliasesFilename;

	@Value("${preparation.aliases.signature.file:aliases.metadata}")
	private String aliasesSignatureFilename;

	@Value("${preparation.votingcardalias.prefix:voting_card_alias_}")
	private String votingCardAliasPrefix;

	@Value("${preparation.votingcardalias.suffix:.csv}")
	private String votingCardAliasSuffix;

	@Autowired
	public VotingCardSetPreparationService(final VotingCardSetRepository votingCardSetRepository, final ElectionEventService electionEventService,
			final PathResolver pathResolver, final FileSignerService fileSignerService, final ObjectMapper objectMapper,
			@Value("${spring.profiles.active:}")
			final String activeProfiles) {
		this.votingCardSetRepository = votingCardSetRepository;
		this.electionEventService = electionEventService;
		this.pathResolver = pathResolver;
		this.fileSignerService = fileSignerService;
		this.objectMapper = objectMapper;
		this.activeProfiles = activeProfiles;
	}

	/**
	 * The "prepare" step creates the required directories and files to store the printing data related to the given election event and voting card
	 * set.
	 * <p>
	 * The aliases file and its signature file are persisted in the corresponding voting card set directory located in the printing directory {@link
	 * PathResolver#resolvePrintingPath}.
	 * <p>
	 * The content of aliases file is printed separated with a semicolon (";").
	 * <p>
	 * This step is only executed when the challenge profile is activated.
	 *
	 * @param electionEventId the election event to prepare. Must be non-null and a valid UUID.
	 * @param votingCardSetId the voting card set. Must be non-null and a valid UUID.
	 * @param privateKeyPEM   the signing key. Must be non-null.
	 */
	public void prepare(final String electionEventId, final String votingCardSetId, final String privateKeyPEM) {
		checkNotNull(electionEventId);
		validateUUID(electionEventId);

		checkNotNull(votingCardSetId);
		validateUUID(votingCardSetId);

		checkNotNull(privateKeyPEM);

		LOGGER.info("Voting Card Set - Prepare");

		if (isChallengeProfileActive()) {
			LOGGER.info("The challenge profile is activated, preparation step should be executed");

			// Create printing directory
			final Path printingPath = pathResolver.resolvePrintingPath(electionEventId);
			createDirectory(printingPath);

			// Create voting card set directory
			final Path votingCardSetPath = printingPath.resolve(votingCardSetId);
			createDirectory(votingCardSetPath);

			// Election event and voting card set aliases
			final String electionEventAlias = electionEventService.getElectionEventAlias(electionEventId);
			final String votingCardSetAlias = votingCardSetRepository.getVotingCardSetAlias(votingCardSetId);

			// Extract VC Alias and EA Code form csv
			final List<List<String>> vcAliasesAndEaCodes = extractVcAliasesAndEaCodes(electionEventId, electionEventAlias, votingCardSetAlias);
			if (vcAliasesAndEaCodes.isEmpty()) {
				throw new IllegalArgumentException("The list of voting card alias and extended authentication code must not be empty.");
			}

			// Create the aliases CSV file
			final Path aliasesPath = votingCardSetPath.resolve(aliasesFilename);
			saveContent(aliasesPath, vcAliasesAndEaCodes);

			// Generate signature metadata
			final FileSignature fileSignature = sign(aliasesPath, privateKeyPEM, electionEventId, votingCardSetId);

			// Create and save signature metadata file
			final Path aliasesMetadataPath = votingCardSetPath.resolve(aliasesSignatureFilename);
			saveSignature(aliasesMetadataPath, fileSignature);
		}
	}

	/**
	 * Defines if the challenge profile is activated or not.
	 */
	private boolean isChallengeProfileActive() {
		final String[] profiles = activeProfiles.split(",");
		final String activatedProfiles = String.format("Activated profiles: %s.", Arrays.toString(profiles));
		LOGGER.debug(activatedProfiles);
		return Arrays.asList(profiles).contains(CHALLENGE_PROFILE);
	}

	/**
	 * Creates the directory for the given path if it does not already exists.
	 */
	private void createDirectory(final Path directoryPath) {
		if (!Files.exists(directoryPath)) {
			LOGGER.debug("Directory path to create: {}", directoryPath);
			try {
				Files.createDirectories(directoryPath);
				LOGGER.debug("Directory created: {}", directoryPath);
			} catch (final IOException e) {
				throw new UncheckedIOException(String.format("Unable to create directory: %s", directoryPath), e);
			}
		} else {
			LOGGER.debug("Directory already existing: {}", directoryPath);
		}
	}

	/**
	 * Extracts a list of voting card alias and the extended authentication code related to the given election and voting card set.
	 */
	private List<List<String>> extractVcAliasesAndEaCodes(final String electionEventId, final String electionEventAlias,
			final String votingCardSetAlias) {
		// Voting Card Aliases file
		final Path outputPath = pathResolver.resolveOutputPath(electionEventId);
		final Path votingCardAliasPath = outputPath.resolve(votingCardAliasPrefix + electionEventAlias + votingCardAliasSuffix);

		// Csv file creation
		final List<List<String>> vcAliasesAndEaCodes = new ArrayList<>();
		try (final Reader reader = Files.newBufferedReader(votingCardAliasPath);
				final MappingIterator<List<String>> iterator = CSV_MAPPER.readerForListOf(String.class).with(CsvParser.Feature.WRAP_AS_ARRAY)
						.readValues(reader)) {

			while (iterator.hasNextValue()) {
				final List<String> row = iterator.nextValue();
				// 0: electionEventAlias, 1: votingCardSetAlias, 2: votingCardAlias, 3: extendedAuthenticationCode
				if (votingCardSetAlias.equals(row.get(1))) {
					final List<String> columns = new ArrayList<>();
					columns.add(row.get(2));
					columns.add(row.get(3));
					vcAliasesAndEaCodes.add(columns);
				}
			}

		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Unable to parse Voting Card Aliases file %s.", votingCardAliasPath), e);
		}

		return vcAliasesAndEaCodes;
	}

	/**
	 * Persists the given {@code vcAliasesAndEaCodes} in the file corresponding to the given {@code vcAliasesEaCodesPath}.
	 *
	 * @param vcAliasesEaCodesPath the path to the file where to persist the content. Must be non-null.
	 * @param vcAliasesAndEaCodes  the list of Voting card set alias and Extended authentication code to persist. Must be non-null and non-empty.
	 */
	private void saveContent(final Path vcAliasesEaCodesPath, final List<List<String>> vcAliasesAndEaCodes) {
		checkNotNull(vcAliasesEaCodesPath);
		checkNotNull(vcAliasesAndEaCodes);
		checkArgument(!vcAliasesAndEaCodes.isEmpty());

		try {
			CSV_OBJECT_WRITER_WITHOUT_QUOTE_CHAR.writeValue(vcAliasesEaCodesPath.toFile(), vcAliasesAndEaCodes);
		} catch (final IOException e) {
			throw new UncheckedIOException("Could not persist the aliases file.", e);
		}
	}

	/**
	 * Persists the given {@code signatureMetadata} in the file corresponding to the given {@code aliasesMetadataPath}.
	 *
	 * @param aliasesMetadataPath the path to the file where to persist the signature. Must be non-null.
	 * @param fileSignature       the signature to persist. Must be non-null.
	 */
	private void saveSignature(final Path aliasesMetadataPath, final FileSignature fileSignature) {
		checkNotNull(aliasesMetadataPath);
		checkNotNull(fileSignature);
		try {
			final Path filePath = Files.createFile(aliasesMetadataPath);
			objectMapper.writeValue(filePath.toFile(), fileSignature);
		} catch (final IOException e) {
			throw new UncheckedIOException("Could not persist the aliases signature file.", e);
		}
	}

	private FileSignature sign(final Path path, final String privateKeyPEM, final String electionEventId, final String votingCardSetId) {
		final PrivateKey signingKey;
		try {
			signingKey = PemUtils.privateKeyFromPem(privateKeyPEM);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Cannot decode a private key from the provided string.", e);
		}

		final Map<String, String> fieldsToSign = createSignedFields(electionEventId, votingCardSetId);

		try {
			return fileSignerService.createSignature(signingKey, path, fieldsToSign);
		} catch (final IOException e) {
			throw new UncheckedIOException("Couldn't read the input data.", e);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException("Couldn't create the signature.", e);
		}
	}

	private Map<String, String> createSignedFields(final String electionEventId, final String votingCardSetId) {

		final Map<String, String> signedFields = new LinkedHashMap<>();
		signedFields.put(AliasSignatureConstants.ELECTION_EVENT_ID, electionEventId);
		signedFields.put(AliasSignatureConstants.VOTING_CARD_SET_ID, votingCardSetId);
		signedFields.put(AliasSignatureConstants.TIMESTAMP, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
		signedFields.put(AliasSignatureConstants.COMPONENT, SIGNATURE_COMPONENT);

		return signedFields;
	}

}
