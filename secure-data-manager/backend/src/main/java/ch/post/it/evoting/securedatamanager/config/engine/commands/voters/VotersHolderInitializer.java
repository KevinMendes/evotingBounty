/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.ConfigurationInput;
import ch.post.it.evoting.securedatamanager.config.commons.readers.ConfigurationInputReader;
import ch.post.it.evoting.securedatamanager.config.commons.utils.X509CertificateLoader;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardSetCredentialInputDataPack;

@Component
public class VotersHolderInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotersHolderInitializer.class);

	private final ConfigurationInputReader configurationInputReader;

	private final X509CertificateLoader x509CertificateLoader;

	private final KeyStoreService storesService;

	private final EncryptionParametersService encryptionParametersService;

	public VotersHolderInitializer(final ConfigurationInputReader configurationInputReader, final X509CertificateLoader x509CertificateLoader,
			final KeyStoreService storesService, final EncryptionParametersService encryptionParametersService) {
		this.configurationInputReader = configurationInputReader;
		this.x509CertificateLoader = x509CertificateLoader;
		this.storesService = storesService;
		this.encryptionParametersService = encryptionParametersService;

	}

	public VotersParametersHolder init(final VotersParametersHolder holder, final File configurationInputFile)
			throws IOException, GeneralCryptoLibException {

		try (final InputStream inputStream = Files.newInputStream(configurationInputFile.toPath())) {
			return init(holder, inputStream);
		}
	}

	public VotersParametersHolder init(final VotersParametersHolder holder, final InputStream configurationInputStream)
			throws GeneralCryptoLibException {

		final VotingCardCredentialInputDataPack votingCardCredentialInputDataPack;
		final VotingCardSetCredentialInputDataPack votingCardSetCredentialInputDataPack;
		final VerificationCardCredentialInputDataPack verificationCardCredentialInputDataPack;
		final PrivateKey credentialCAPrivKey;
		final PrivateKey servicesCAPrivKey;
		final CryptoAPIX509Certificate credentialsCACert;
		final CryptoAPIX509Certificate electionCACert;
		final CryptoAPIX509Certificate servicesCACert;

		final Path absoluteBasePath = holder.getAbsoluteBasePath();
		final String eeID = holder.getEeid();
		final ZonedDateTime startValidityPeriod = holder.getCertificatesStartValidityPeriod();
		final ZonedDateTime endValidityPeriod = holder.getCertificatesEndValidityPeriod();

		final ConfigurationInput configurationInput = loadConfigurationInput(configurationInputStream);

		final CredentialProperties credentialPropertiesCredentialsCA = configurationInput.getConfigProperties()
				.get(Constants.CONFIGURATION_CREDENTIALS_CA_JSON_TAG);

		final CredentialProperties credentialPropertiesElectionCA = configurationInput.getConfigProperties()
				.get(Constants.CONFIGURATION_ELECTION_CA_JSON_TAG);

		final CredentialProperties credentialPropertiesServicesCA = configurationInput.getConfigProperties()
				.get(Constants.CONFIGURATION_SERVICES_CA_JSON_TAG);

		final String aliasCredentialsCAPrivateKey = credentialPropertiesCredentialsCA.getAlias()
				.get(Constants.CONFIGURATION_CREDENTIALS_CA_PRIVATE_KEY_JSON_TAG);

		final String aliasServicesCAPrivateKey = credentialPropertiesServicesCA.getAlias()
				.get(Constants.CONFIGURATION_SERVICES_CA_PRIVATE_KEY_JSON_TAG);

		final String nameCredentialsCA = credentialPropertiesCredentialsCA.getName();

		final String nameElectionCA = credentialPropertiesElectionCA.getName();

		final String nameServicesCA = credentialPropertiesServicesCA.getName();

		final String pemFileCredentialsCA = nameCredentialsCA + Constants.PEM;

		final String pemFileElectionCA = nameElectionCA + Constants.PEM;

		final String pemFileServicesCA = nameServicesCA + Constants.PEM;

		try {
			LOGGER.info("Obtaining the CredentialsCA certificate {} ...", pemFileCredentialsCA);
			credentialsCACert = loadCertificate(absoluteBasePath, pemFileCredentialsCA);
			LOGGER.info("Obtaining the ElectionCA certificate {} ...", pemFileElectionCA);
			electionCACert = loadCertificate(absoluteBasePath, pemFileElectionCA);
			LOGGER.info("Obtaining the ServicesCA certificate {} ...", pemFileServicesCA);
			servicesCACert = loadCertificate(absoluteBasePath, pemFileServicesCA);
		} catch (final GeneralSecurityException | GeneralCryptoLibException e) {
			throw new CreateVotingCardSetException("An error occurred while loading the CA certificates: " + e.getMessage(), e);
		}

		try {
			LOGGER.info("Obtaining the CredentialsCA private key from its keystore {} with alias \"{}\" ...", nameCredentialsCA,
					aliasCredentialsCAPrivateKey);
			credentialCAPrivKey = getCredentialsCAPrivateKey(absoluteBasePath, nameCredentialsCA, aliasCredentialsCAPrivateKey);
		} catch (final IOException | GeneralCryptoLibException e) {
			throw new CreateVotingCardSetException(
					String.format("An error occurred while retrieving the private key of %s with the alias \"%s\": %s", nameCredentialsCA,
							aliasCredentialsCAPrivateKey, e.getMessage()), e);
		}

		try {
			LOGGER.info("Obtaining the ServicesCA private key from its keystore {} with the alias \"{}\" ...", nameServicesCA,
					aliasServicesCAPrivateKey);
			servicesCAPrivKey = getServicesCAPrivateKey(absoluteBasePath, nameServicesCA, aliasServicesCAPrivateKey);
		} catch (final IOException | GeneralCryptoLibException e) {
			throw new CreateVotingCardSetException(
					"An error occurred while retrieving the private key of " + nameServicesCA + " with the alias \"" + aliasServicesCAPrivateKey
							+ "\": " + e.getMessage(), e);
		}

		LOGGER.info("Loading the datapacks properties...");
		votingCardCredentialInputDataPack = initializeVotingCardCredentialInputDataPack(eeID, credentialCAPrivKey, credentialsCACert,
				configurationInput);

		votingCardSetCredentialInputDataPack = initializeVotingCardSetCredentialInputDataPack(eeID, servicesCAPrivKey, servicesCACert,
				configurationInput);

		verificationCardCredentialInputDataPack = initializeVerificationCardCredentialInputDataPack(eeID, configurationInput);

		votingCardCredentialInputDataPack.setStartDate(startValidityPeriod);
		votingCardCredentialInputDataPack.setEndDate(endValidityPeriod);

		votingCardSetCredentialInputDataPack.setStartDate(startValidityPeriod);
		votingCardSetCredentialInputDataPack.setEndDate(endValidityPeriod);

		final GqGroup encryptionGroup = encryptionParametersService.load(holder.getEeid());
		final EncryptionParameters encryptionParameters = new EncryptionParameters(encryptionGroup.getP().toString(),
				encryptionGroup.getQ().toString(), encryptionGroup.getGenerator().getValue().toString());

		holder.setVotingCardCredentialInputDataPack(votingCardCredentialInputDataPack);
		holder.setVotingCardSetCredentialInputDataPack(votingCardSetCredentialInputDataPack);
		holder.setVerificationCardCredentialInputDataPack(verificationCardCredentialInputDataPack);
		holder.setCredentialCAPrivKey(credentialCAPrivKey);
		holder.setServicesCAPrivKey(servicesCAPrivKey);
		holder.setCredentialsCACert(credentialsCACert);
		holder.setElectionCACert(electionCACert);
		holder.setEncryptionParameters(encryptionParameters);

		return holder;
	}

	private CryptoAPIX509Certificate loadCertificate(final Path absoluteBasePath, final String pemFile)
			throws GeneralSecurityException, GeneralCryptoLibException {
		try {
			return x509CertificateLoader.load(Paths.get(absoluteBasePath.toString(), Constants.CONFIG_DIR_NAME_OFFLINE, pemFile).toString());
		} catch (final IOException e) {
			throw new CreateVotingCardSetException("An error occurred while loading certificate: " + e.getMessage(), e);
		}
	}

	private ConfigurationInput loadConfigurationInput(final InputStream configurationInputStream) {
		final ConfigurationInput configurationInput;
		try {
			configurationInput = configurationInputReader.fromStreamToJava(configurationInputStream);
		} catch (final IOException e) {
			throw new CreateVotingCardSetException("An error occurred while fetching the configuration json of the api: " + e.getMessage(), e);
		}
		return configurationInput;
	}

	private PrivateKey getCredentialsCAPrivateKey(final Path absolutePath, final String passwordTag, final String alias)
			throws IOException, GeneralCryptoLibException {

		final Path credentialsKeyStorePath = Paths
				.get(absolutePath.toString(), Constants.CONFIG_DIR_NAME_OFFLINE, Constants.CREDENTIAL_SIGNER_SKS_FILENAME);
		final CryptoAPIExtendedKeyStore ks;
		try (final InputStream in = new FileInputStream(credentialsKeyStorePath.toFile())) {
			final char[] password = getPassword(Paths.get(absolutePath.toString(), Constants.CONFIG_DIR_NAME_OFFLINE, Constants.PW_TXT), passwordTag);
			ks = storesService.loadKeyStore(in, new KeyStore.PasswordProtection(password));
			return ks.getPrivateKeyEntry(alias, password);
		}
	}

	private PrivateKey getServicesCAPrivateKey(final Path absolutePath, final String passwordTag, final String alias)
			throws IOException, GeneralCryptoLibException {

		final Path servicesKeyStorePath = Paths
				.get(absolutePath.toString(), Constants.CONFIG_DIR_NAME_OFFLINE, Constants.SERVICES_SIGNER_SKS_FILENAME);
		final CryptoAPIExtendedKeyStore ks;
		try (final InputStream in = new FileInputStream(servicesKeyStorePath.toFile())) {
			final char[] password = getPassword(Paths.get(absolutePath.toString(), Constants.CONFIG_DIR_NAME_OFFLINE, Constants.PW_TXT), passwordTag);
			ks = storesService.loadKeyStore(in, new KeyStore.PasswordProtection(password));
			return ks.getPrivateKeyEntry(alias, password);
		}
	}

	private char[] getPassword(final Path path, final String name) throws IOException {

		final List<String> lines = Files.readAllLines(path);
		String password = null;

		for (final String line : lines) {
			final String[] splittedLine = line.split(",");

			if (splittedLine[0].equals(name)) {
				password = splittedLine[1];
			}
		}

		if (password == null) {
			throw new CreateVotingCardSetException("The passwords file does not contain a password for " + name);
		}

		return password.toCharArray();
	}

	private VerificationCardCredentialInputDataPack initializeVerificationCardCredentialInputDataPack(final String eeID,
			final ConfigurationInput configurationInput) {

		final CredentialProperties verificationCardProperties = configurationInput.getVerificationCard();

		final VerificationCardCredentialInputDataPack inputDataPack;

		inputDataPack = new VerificationCardCredentialInputDataPack(verificationCardProperties);

		inputDataPack.setEeid(eeID);

		final ReplacementsHolder replacementsHolder = new ReplacementsHolder(eeID);
		inputDataPack.setReplacementsHolder(replacementsHolder);

		return inputDataPack;
	}

	private VotingCardCredentialInputDataPack initializeVotingCardCredentialInputDataPack(final String eeID, final PrivateKey credentialCAPrivKey,
			final CryptoAPIX509Certificate credentialsCACert, final ConfigurationInput configurationInput) {
		final CredentialProperties credentialAuthProperties;

		final VotingCardCredentialInputDataPack inputDataPack;

		credentialAuthProperties = configurationInput.getCredentialAuth();

		inputDataPack = new VotingCardCredentialInputDataPack(credentialAuthProperties);
		inputDataPack.setParentKeyPair(new KeyPair(credentialsCACert.getPublicKey(), credentialCAPrivKey));

		inputDataPack.setEeid(eeID);

		return inputDataPack;
	}

	private VotingCardSetCredentialInputDataPack initializeVotingCardSetCredentialInputDataPack(final String eeID, final PrivateKey servicesCAPrivKey,
			final CryptoAPIX509Certificate servicesCACert, final ConfigurationInput configurationInput) {
		final CredentialProperties credentialProperties;

		final VotingCardSetCredentialInputDataPack inputDataPack;
		credentialProperties = configurationInput.getVotingCardSet();

		inputDataPack = new VotingCardSetCredentialInputDataPack(credentialProperties);
		inputDataPack.setParentKeyPair(new KeyPair(servicesCACert.getPublicKey(), servicesCAPrivKey));

		inputDataPack.setEeid(eeID);

		final ReplacementsHolder replacementsHolder = new ReplacementsHolder(eeID);
		inputDataPack.setReplacementsHolder(replacementsHolder);

		return inputDataPack;
	}
}
