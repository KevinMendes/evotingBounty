/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.securedatamanager.commons.Constants.SDM_DIR_NAME;
import static ch.post.it.evoting.securedatamanager.commons.Constants.SDM_LANGS_DIR_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectReader;

import ch.post.it.evoting.securedatamanager.services.domain.model.sdmconfig.I18nConfiguration;
import ch.post.it.evoting.securedatamanager.services.domain.model.sdmconfig.Language;

@Service
public class SdmConfigService {

	public static final String DEFAULT_LOCALE_FILE_NAME = "default";
	private static final Logger LOGGER = LoggerFactory.getLogger(SdmConfigService.class);
	private static final String GENERATE_PRE_VOTING_OUTPUTS_ENABLED_PROPERTY = "generatePreVotingOutputsEnabled";
	private static final String GENERATE_POST_VOTING_OUTPUTS_ENABLED_PROPERTY = "generatePostVotingOutputsEnabled";
	private static final String VC_PRECOMPUTATION_ENABLED_PROPERTY = "vcPrecomputationEnabled";
	private static final String VC_COMPUTATION_ENABLED_PROPERTY = "vcComputationEnabled";
	private static final String VC_DOWNLOAD_ENABLED_PROPERTY = "vcDownloadEnabled";
	private static final String IMPORT_EXPORT_ENABLED_PROPERTY = "importExportEnabled";
	private static final String I18N_PROPERTY = "i18n";
	private final Map<String, Object> config = new HashMap<>();
	@Autowired
	ObjectReader jsonReader;
	@Value("${generatePreVotingOutputs.enabled}")
	private boolean isGeneratePreVotingOutputsEnabled;
	@Value("${generatePostVotingOutputs.enabled}")
	private boolean isGeneratePostVotingOutputsEnabled;
	@Value("${vcPrecomputation.enabled}")
	private boolean isVcPrecomputationEnabled;
	@Value("${vcComputation.enabled}")
	private boolean isVcComputationEnabled;
	@Value("${vcDownload.enabled}")
	private boolean isVcDownloadEnabled;
	@Value("${importExport.enabled}")
	private boolean isImportExportEnabled;
	@Value("${user.home}")
	private String workspace;

	public Map<String, Object> getConfig() throws IOException {
		config.put(GENERATE_PRE_VOTING_OUTPUTS_ENABLED_PROPERTY, isGeneratePreVotingOutputsEnabled);
		config.put(GENERATE_POST_VOTING_OUTPUTS_ENABLED_PROPERTY, isGeneratePostVotingOutputsEnabled);
		config.put(VC_PRECOMPUTATION_ENABLED_PROPERTY, isVcPrecomputationEnabled);
		config.put(VC_COMPUTATION_ENABLED_PROPERTY, isVcComputationEnabled);
		config.put(VC_DOWNLOAD_ENABLED_PROPERTY, isVcDownloadEnabled);
		config.put(IMPORT_EXPORT_ENABLED_PROPERTY, isImportExportEnabled);
		addLanguagesConfiguration(config);

		return config;
	}

	public FileSystemResource loadLanguage(final String languageCode) {

		String languageFileName = languageCode;
		if (isDefaultLanguageCode(languageCode)) {
			languageFileName = DEFAULT_LOCALE_FILE_NAME;
		}

		final Path languageFilePath = Paths.get(workspace, SDM_DIR_NAME, SDM_LANGS_DIR_NAME, languageFileName + ".json");
		return new FileSystemResource(languageFilePath.toFile());
	}

	private boolean isDefaultLanguageCode(final String languageCode) {
		final I18nConfiguration i18nConfiguration = (I18nConfiguration) config.get(I18N_PROPERTY);
		return languageCode.equals(i18nConfiguration.getDefaultLanguage());
	}

	private void addLanguagesConfiguration(final Map<String, Object> config) throws IOException {

		final I18nConfiguration i18nConfiguration = new I18nConfiguration();
		final List<String> languageCodes = discoverAvailableLanguages();
		for (final String languageCode : languageCodes) {
			final String localeName = extractLocaleNameFromFile(languageCode);
			final Language lang = processLocaleName(localeName);
			final boolean isDefault = languageCode.equalsIgnoreCase(DEFAULT_LOCALE_FILE_NAME);
			i18nConfiguration.addLanguage(lang, isDefault);
		}
		config.put(I18N_PROPERTY, i18nConfiguration);
	}

	private Language processLocaleName(final String localeName) {
		final String languageTag = localeName.replace('_', '-');
		final Locale locale = Locale.forLanguageTag(languageTag);
		return new Language(locale.toString(), locale.getDisplayLanguage(locale));
	}

	private String extractLocaleNameFromFile(final String localeFileName) throws IOException {
		final Path localeFilePath = Paths.get(workspace, SDM_DIR_NAME, SDM_LANGS_DIR_NAME, localeFileName + ".json");
		try (final InputStream is = Files.newInputStream(localeFilePath)) {
			// get the first element 'name' from the json file. MUST be the name of a locale
			return jsonReader.readTree(is).fieldNames().next();
		}
	}

	private List<String> discoverAvailableLanguages() {
		final Path languagesPath = Paths.get(workspace, SDM_DIR_NAME, SDM_LANGS_DIR_NAME);
		try (final Stream<Path> streamPath = Files.list(languagesPath)) {
			return streamPath.map(p -> p.toFile().getName()).filter(this::isValidNameOrDefault).map(FilenameUtils::getBaseName)
					.collect(Collectors.toList());
		} catch (final IOException e) {
			LOGGER.error("Error trying to discover available languages.", e);
		}
		return Collections.emptyList();
	}

	private boolean isValidNameOrDefault(final String fileName) {
		// is the lang filename called "default" or is named after a 'valid' locale
		return "json".equalsIgnoreCase(FilenameUtils.getExtension(fileName)) && (
				DEFAULT_LOCALE_FILE_NAME.equalsIgnoreCase(FilenameUtils.getBaseName(fileName))
						|| Locale.forLanguageTag(FilenameUtils.getBaseName(fileName).replace('_', '-')) != null);
	}
}
