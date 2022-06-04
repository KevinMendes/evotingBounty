/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.CertificateDataBuilder;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.domain.election.AuthenticationParams;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateElectionEventCertificatePropertiesContainer;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.ConfigurationInput;
import ch.post.it.evoting.securedatamanager.config.commons.readers.ConfigurationInputReader;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.generators.ElectionCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.progress.ProgressManager;

class CreateElectionEventSerializerTest {

	private static final ProgressManager votersProgressManager = new ProgressManager();
	private static final String ELECTION_EVENT_ID = "1ac7f3a8951540c1a0f1ea4a5bc7c26f";
	private static ConfigurationInputReader configurationInputReader;
	private static CreateElectionEventSerializer createElectionEventSerializer;
	private static CreateElectionEventGenerator createElectionEventGenerator;

	@BeforeAll
	static void setUp() {
		configurationInputReader = new ConfigurationInputReader();
		createElectionEventSerializer = new CreateElectionEventSerializer();

		final KeyStoreService storesService = new ExtendedKeyStoreService();
		final PrimitivesServiceAPI primitivesService = new PrimitivesService();
		final X509CertificateGenerator x509CertificateGenerator = new X509CertificateGenerator(new CertificatesService(),
				new CertificateDataBuilder());

		final ElectionCredentialDataPackGenerator electionCredentialDataPackGenerator = new ElectionCredentialDataPackGenerator(
				new AsymmetricService(), x509CertificateGenerator, storesService, primitivesService.get32CharAlphabetCryptoRandomString());

		createElectionEventGenerator = new CreateElectionEventGenerator(electionCredentialDataPackGenerator, votersProgressManager);
	}

	@AfterAll
	static void cleanup() throws IOException {
		FileUtils.deleteDirectory(new File("./target/output"));
	}

	@Test
	void serializeTest() throws IOException, GeneralCryptoLibException, URISyntaxException {

		final Properties inputProperties = new Properties();
		inputProperties.load(this.getClass().getClassLoader().getResourceAsStream("CreateElectionEventSerializerTest/config.properties"));

		final ElectionInputDataPack electionInputDataPack = new ElectionInputDataPack();
		electionInputDataPack.setEeid(ELECTION_EVENT_ID);

		final ReplacementsHolder replacementsHolder = new ReplacementsHolder(ELECTION_EVENT_ID);
		electionInputDataPack.setReplacementsHolder(replacementsHolder);

		setInputDataProperties(inputProperties, electionInputDataPack);

		final String challengeResExpTime = (String) inputProperties.get("challengeResExpTime");
		final String authTokenExpTime = (String) inputProperties.get("authTokenExpTime");
		final String challengeLength = (String) inputProperties.get("challengeLength");

		final AuthenticationParams authenticationParams = new AuthenticationParams(challengeResExpTime, authTokenExpTime, challengeLength);

		final String outputPath = "./target/output";
		final Path electionPath = Paths.get(outputPath, ELECTION_EVENT_ID);
		final Path offlinePath = electionPath.resolve(Constants.CONFIG_DIR_NAME_OFFLINE);
		final Path onlinePath = electionPath.resolve(Constants.CONFIG_DIR_NAME_ONLINE);
		final Path autenticationPath = onlinePath.resolve(Constants.CONFIG_DIR_NAME_AUTHENTICATION);
		final Path electionInformationPath = onlinePath.resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION);

		final CreateElectionEventCertificatePropertiesContainer createElectionEventCertificateProperties = getCertificateProperties();

		final CreateElectionEventParametersHolder holder = new CreateElectionEventParametersHolder(electionInputDataPack, Paths.get(outputPath),
				electionPath, offlinePath, autenticationPath, electionInformationPath, authenticationParams, Constants.EMPTY,
				createElectionEventCertificateProperties);

		final Path outputDirectory = electionPath.resolve(Constants.CONFIG_DIR_NAME_CUSTOMER).resolve(Constants.CONFIG_DIR_NAME_OUTPUT);
		createDirectories(outputDirectory);

		final byte[] jwtBytes = Files
				.readAllBytes(Paths.get(this.getClass().getResource("/" + Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON).toURI()));
		write(outputDirectory.resolve(Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON), jwtBytes);

		final byte[] trustedChainBytes = Files
				.readAllBytes(Paths.get(this.getClass().getResource("/" + Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM).toURI()));
		write(electionPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM), trustedChainBytes);

		// Read from configuration
		final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("engine_keys_config.json");
		final ConfigurationInput configurationInput = configurationInputReader.fromStreamToJava(inputStream);
		holder.setConfigurationInput(configurationInput);

		final CreateElectionEventOutput createElectionEventOutput = createElectionEventGenerator.generate(holder);

		createElectionEventSerializer.serialize(holder, createElectionEventOutput);

		final File outputFolder = new File("./target", "output");
		final File eeidFolder = new File(outputFolder, ELECTION_EVENT_ID);
		final File offLineFolder = new File(eeidFolder, Constants.CONFIG_DIR_NAME_OFFLINE);
		final File onLineFolder = new File(eeidFolder, Constants.CONFIG_DIR_NAME_ONLINE);
		final File authenticationFolder = new File(onLineFolder, Constants.CONFIG_DIR_NAME_AUTHENTICATION);
		final File electionInformationFolder = new File(onLineFolder, Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION);

		assertAll(
				// check folders structure
				() -> assertTrue(outputFolder.exists()),
				() -> assertTrue(eeidFolder.exists()),
				() -> assertTrue(offLineFolder.exists()),
				() -> assertTrue(onLineFolder.exists()),
				() -> assertTrue(authenticationFolder.exists()),
				() -> assertTrue(electionInformationFolder.exists()),

				// check number of files generated
				() -> assertEquals(9, offLineFolder.list().length),
				() -> assertEquals(2, authenticationFolder.list().length),
				() -> assertEquals(0, electionInformationFolder.list().length));
	}

	private void setInputDataProperties(final Properties inputProperties, final ElectionInputDataPack electionInputDataPack) {

		// ISO_INSTANT format => 2011-12-03T10:15:30Z
		electionInputDataPack.setStartDate(ZonedDateTime.ofInstant(Instant.parse((String) inputProperties.get("start")), ZoneOffset.UTC));
		electionInputDataPack.setEndDate(ZonedDateTime.ofInstant(Instant.parse((String) inputProperties.get("end")), ZoneOffset.UTC));

	}

	private CreateElectionEventCertificatePropertiesContainer getCertificateProperties() throws IOException {

		final String servicesCaCertificateProperties = "certificateProperties/servicesCAX509Certificate.properties";
		final String electionCaCertificateProperties = "certificateProperties/electionCAX509Certificate.properties";
		final String credentialsCaCertificateProperties = "certificateProperties/credentialsCAX509Certificate.properties";
		final String authoritiesCaCertificateProperties = "certificateProperties/authoritiesCAX509Certificate.properties";
		final String authTokenSignerCertificateProperties = "certificateProperties/authTokenSignerX509Certificate.properties";

		final CreateElectionEventCertificatePropertiesContainer createElectionEventCertificatePropertiesContainer = new CreateElectionEventCertificatePropertiesContainer();

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

		createElectionEventCertificatePropertiesContainer.setAuthTokenSignerCertificateProperties(loadedAuthTokenSignerCertificatePropertiesAsString);
		createElectionEventCertificatePropertiesContainer.setNameToCertificateProperties(configProperties);

		return createElectionEventCertificatePropertiesContainer;
	}

	private Properties getCertificateParameters(final String path) throws IOException {

		final Properties props = new Properties();

		try (final InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
			props.load(input);
		}

		return props;
	}
}
