/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.securedatamanager.EncryptionParametersFileRepository;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.readers.ConfigurationInputReader;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.commons.utils.SignatureVerifier;
import ch.post.it.evoting.securedatamanager.config.commons.utils.X509CertificateLoader;
import ch.post.it.evoting.securedatamanager.config.engine.it.utils.KeyPairValidator;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

class VotersHolderInitializerTest {

	private static final String ELECTION_EVENT_ID = "314bd34dcf6e4de4b771a92fa3849d3d";
	private static final String NUMBER_VOTING_CARDS = "10";
	private static final String BALLOT_ID = "1234";
	private static final String BALLOT_BOX_ID = "5678";
	private static final String VOTING_CARD_SET_ID = "111111";
	private static final String VERIFICATION_CARD_SET_ID = "111111";
	private static final String VOTING_CARD_SET_ALIAS = "TESTALIAS";
	private static final String ELECTORAL_AUTHORITY_ID = "222222";
	private static final String PLATFORM_ROOT_CA_PEM = "platformRootCAPem";
	private static final String BASE_PATH = "src/test/resources/votingCardSet/";
	private static final String OUTPUT_PATH = BASE_PATH + "output";
	private static final String ENRICHED_BALLOT_PATH = BASE_PATH + "input/enrichedBallot.json";
	private static final String TEST_EE_PROPS = BASE_PATH + "input/input.properties";

	private static ConfigObjectMapper configObjectMapper;
	private static EncryptionParametersService encryptionParametersService;
	private static ConfigurationInputReader configurationInputReader;
	private static ExtendedKeyStoreService extendedKeyStoreService;
	private static X509CertificateLoader x509CertificateLoader;
	private static KeyPairValidator keyPairValidator;

	@BeforeAll
	public static void setUp() throws CertificateException, NoSuchProviderException {
		configObjectMapper = new ConfigObjectMapper();
		configurationInputReader = new ConfigurationInputReader();
		final PathResolver pathResolver = new PathResolver(OUTPUT_PATH);
		final SignatureVerifier signatureVerifier = new SignatureVerifier();
		final EncryptionParametersFileRepository encryptionParametersFileRepository = new EncryptionParametersFileRepository(new ObjectMapper(),
				pathResolver,
				signatureVerifier);
		encryptionParametersService = new EncryptionParametersService(encryptionParametersFileRepository);
		extendedKeyStoreService = new ExtendedKeyStoreService();
		x509CertificateLoader = new X509CertificateLoader();
		keyPairValidator = new KeyPairValidator();
	}

	@Test
	void adaptTheParametersCorrectly() throws GeneralCryptoLibException, IOException {

		final VotersHolderInitializer votersHolderInitializer = new VotersHolderInitializer(configurationInputReader, x509CertificateLoader,
				extendedKeyStoreService, encryptionParametersService);

		final Properties props = new Properties();
		try (final BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(TEST_EE_PROPS))) {
			props.load(bufferedReader);
		}

		final String start = (String) props.get("start");
		final int validityPeriod = Integer.parseInt((String) props.get("validityPeriod"));

		final ZonedDateTime startValidityPeriod = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime electionStartDate = ZonedDateTime.ofInstant(Instant.parse(start), ZoneOffset.UTC);
		final ZonedDateTime endValidityPeriod = electionStartDate.plusYears(validityPeriod);

		VotersParametersHolder holder = new VotersParametersHolder(Integer.parseInt(NUMBER_VOTING_CARDS), BALLOT_ID, getBallot(), BALLOT_BOX_ID,
				VOTING_CARD_SET_ID, VERIFICATION_CARD_SET_ID, ELECTORAL_AUTHORITY_ID, Paths.get(OUTPUT_PATH, "sdm", "config", ELECTION_EVENT_ID),
				ELECTION_EVENT_ID, startValidityPeriod, endValidityPeriod, VOTING_CARD_SET_ALIAS, PLATFORM_ROOT_CA_PEM, null);

		holder = votersHolderInitializer.init(holder, getConfigurationFile());

		final CryptoAPIX509Certificate credentialCACert = holder.getCredentialCACert();
		final PrivateKey credentialCAPrivKey = holder.getCredentialCAPrivKey();

		keyPairValidator.validateKeyPair(credentialCACert.getPublicKey(), credentialCAPrivKey);

		assertNotNull(holder.getVotingCardCredentialInputDataPack());
		assertEquals("auth_sign", holder.getVotingCardCredentialInputDataPack().getCredentialAuthProperties().getAlias().get("privateKey"));
		assertNotNull(holder.getVotingCardSetCredentialInputDataPack());
	}

	private Ballot getBallot() {
		final File ballotFile = Paths.get(ENRICHED_BALLOT_PATH).toAbsolutePath().toFile();

		return getBallotFromFile(ENRICHED_BALLOT_PATH, ballotFile);
	}

	private Ballot getBallotFromFile(final String ballotPath, final File ballotFile) {
		try {
			return configObjectMapper.fromJSONFileToJava(ballotFile, Ballot.class);
		} catch (final IOException e) {
			throw new IllegalArgumentException("An error occurred while mapping \"" + ballotPath + "\" to a Ballot: " + e.getMessage());
		}
	}

	private File getConfigurationFile() {
		return Paths.get("src/test/resources/", Constants.KEYS_CONFIG_FILENAME).toFile();
	}
}
