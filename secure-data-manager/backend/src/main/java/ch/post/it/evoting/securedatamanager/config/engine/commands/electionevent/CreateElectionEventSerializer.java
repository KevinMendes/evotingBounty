/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.keystore.KeyStoreReader;
import ch.post.it.evoting.domain.election.AuthenticationContextData;
import ch.post.it.evoting.domain.election.AuthenticationVoterData;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.GenerateElectionEventCAException;

/**
 * The class that writes to file all the info as a output of the create election event command.
 */
@Service
public class CreateElectionEventSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateElectionEventSerializer.class);
	private final ConfigObjectMapper configObjectMapper = new ConfigObjectMapper();

	/**
	 * Write the info inside CreateElectionEventOutput class on the corresponding output folders.
	 */
	public void serialize(final CreateElectionEventParametersHolder holder, final CreateElectionEventOutput createElectionEventOutput)
			throws IOException, GeneralCryptoLibException {

		// 1st. Create folder structure
		createFolderStructure(holder);

		saveOnOfflineFolder(holder, createElectionEventOutput);

		saveOnOnlineFolder(holder, createElectionEventOutput);
	}

	private void saveOnOnlineFolder(final CreateElectionEventParametersHolder holder, final CreateElectionEventOutput createElectionEventOutput)
			throws IOException {

		saveOnAuthenticationFolder(holder, createElectionEventOutput);
	}

	private void saveOnOfflineFolder(final CreateElectionEventParametersHolder holder, final CreateElectionEventOutput createElectionEventOutput)
			throws IOException, GeneralCryptoLibException {

		final Map<String, ElectionCredentialDataPack> dataPacksMap = createElectionEventOutput.getDataPackMap();

		// 2nd. save certs and sks on offline
		for (final Entry<String, ElectionCredentialDataPack> entry : dataPacksMap.entrySet()) {

			final String name = entry.getKey();
			final ElectionCredentialDataPack electionCredentialDataPack = entry.getValue();

			saveCertificate(electionCredentialDataPack.getCertificate(), holder.getOfflineFolder().resolve(name + Constants.PEM));

			saveKeyStore(electionCredentialDataPack.getKeyStore(), holder.getOfflineFolder().resolve(name + Constants.SKS),
					electionCredentialDataPack.getPassword(), holder.getInputDataPack().getEeid());
		}

		// Write passwords.txt file
		writeKeyStorePasswordsToFile(dataPacksMap, holder.getOfflineFolder().resolve(Constants.PW_TXT));
	}

	private void saveOnAuthenticationFolder(final CreateElectionEventParametersHolder holder,
			final CreateElectionEventOutput createElectionEventOutput) throws IOException {

		final Map<String, ElectionCredentialDataPack> dataPacksMap = createElectionEventOutput.getDataPackMap();

		final AuthenticationVoterData authenticationVoterData = populateAuthenticationVoterData(holder, createElectionEventOutput, dataPacksMap);

		final File authenticationVoterDataFile = new File(holder.getOnlineAuthenticationFolder().toFile(),
				Constants.CONFIG_FILE_NAME_AUTH_VOTER_DATA);
		configObjectMapper.fromJavaToJSONFile(authenticationVoterData, authenticationVoterDataFile);

		final AuthenticationContextData authenticationContextData = populateAuthenticationContextData(holder, createElectionEventOutput);

		final File authenticationContextDataFile = new File(holder.getOnlineAuthenticationFolder().toFile(),
				Constants.CONFIG_FILE_NAME_AUTH_CONTEXT_DATA);
		configObjectMapper.fromJavaToJSONFile(authenticationContextData, authenticationContextDataFile);

	}

	private AuthenticationVoterData populateAuthenticationVoterData(final CreateElectionEventParametersHolder holder,
			final CreateElectionEventOutput createElectionEventOutput, final Map<String, ElectionCredentialDataPack> dataPacksMap) {

		final AuthenticationVoterData authenticationVoterData = new AuthenticationVoterData();

		authenticationVoterData.setElectionEventId(holder.getInputDataPack().getEeid());

		final String electionRootCA = serializeCertificate(getCertFromDataPack(dataPacksMap, Constants.CONFIGURATION_ELECTION_CA_JSON_TAG));
		authenticationVoterData.setElectionRootCA(electionRootCA);

		final String servicesCA = serializeCertificate(getCertFromDataPack(dataPacksMap, Constants.CONFIGURATION_SERVICES_CA_JSON_TAG));
		authenticationVoterData.setServicesCA(servicesCA);

		final String authoritiesCA = serializeCertificate(getCertFromDataPack(dataPacksMap, Constants.CONFIGURATION_AUTHORITIES_CA_JSON_TAG));
		authenticationVoterData.setAuthoritiesCA(authoritiesCA);

		final String credentialsCA = serializeCertificate(getCertFromDataPack(dataPacksMap, Constants.CONFIGURATION_CREDENTIALS_CA_JSON_TAG));
		authenticationVoterData.setCredentialsCA(credentialsCA);

		final String authTokenSignerCert = serializeCertificate(createElectionEventOutput.getAuthTokenSigner().getCertificate());
		authenticationVoterData.setAuthenticationTokenSignerCert(authTokenSignerCert);

		return authenticationVoterData;
	}

	private AuthenticationContextData populateAuthenticationContextData(final CreateElectionEventParametersHolder holder,
			final CreateElectionEventOutput createElectionEventOutput) {

		final AuthenticationContextData authenticationContextData = new AuthenticationContextData();

		authenticationContextData.setElectionEventId(holder.getInputDataPack().getEeid());

		final CryptoAPIExtendedKeyStore keystore = createElectionEventOutput.getAuthTokenSigner().getKeyStore();
		final String authTokenSignerSKSJSONBase64 = KeyStoreReader.toString(keystore, createElectionEventOutput.getAuthTokenSigner().getPassword());

		authenticationContextData.setAuthenticationTokenSignerKeystore(authTokenSignerSKSJSONBase64);

		authenticationContextData.setAuthenticationTokenSignerPassword(createElectionEventOutput.getAuthTokenSigner().getEncryptedPassword());

		authenticationContextData.setAuthenticationParams(holder.getAuthenticationParams());

		return authenticationContextData;
	}

	private CryptoAPIX509Certificate getCertFromDataPack(final Map<String, ElectionCredentialDataPack> dataPacksMap, final String name) {
		return dataPacksMap.get(name).getCertificate();
	}

	private void createFolderStructure(final CreateElectionEventParametersHolder holder) throws IOException {
		Files.createDirectories(holder.getOfflineFolder());
		Files.createDirectories(holder.getOnlineAuthenticationFolder());
		Files.createDirectories(holder.getOnlineElectionInformationFolder());
	}

	private void saveCertificate(final CryptoAPIX509Certificate certificate, final Path path) throws IOException {

		final PrintWriter writer = new PrintWriter(path.toFile());
		writer.write(new String(certificate.getPemEncoded(), StandardCharsets.UTF_8));
		writer.close();
	}

	private void saveKeyStore(final CryptoAPIExtendedKeyStore systemCAKeyStore, final Path path, final char[] keyStorePassword, final String eeid)
			throws IOException, GeneralCryptoLibException {

		try (final OutputStream out = Files.newOutputStream(path)) {
			systemCAKeyStore.store(out, keyStorePassword);
		}
		confirmKeystoreWrittenSuccessfully(path, eeid);
	}

	private void confirmKeystoreWrittenSuccessfully(final Path path, final String eeid) {

		try {

			final byte[] bytesReadFromFile = Files.readAllBytes(path);

			if (bytesReadFromFile.length < 1) {
				throw new GeneralCryptoLibException("The keystore that was written appears to be empty or inaccessible");
			}

			LOGGER.info(ConfigGeneratorLogEvents.GENEECA_SUCCESS_STORING_KEYSTORE.getInfo(), eeid, "adminID");

		} catch (final Exception e) {

			LOGGER.info(ConfigGeneratorLogEvents.GENEECA_ERROR_STORING_KEYSTORE.getInfo(), eeid, "adminID", "err_desc", e.getMessage());
			throw new GenerateElectionEventCAException(e);
		}
	}

	private void writeKeyStorePasswordsToFile(final Map<String, ElectionCredentialDataPack> dataPacksMap, final Path path)
			throws FileNotFoundException {

		try (final PrintStream out = new PrintStream(path.toFile())) {
			for (final Entry<String, ElectionCredentialDataPack> entry : dataPacksMap.entrySet()) {
				final String name = entry.getKey();
				final ElectionCredentialDataPack electionCredentialDataPack = entry.getValue();
				out.print(name);
				out.print(Constants.COMMA);
				out.println(electionCredentialDataPack.getPassword());
			}
		}
	}

	private String serializeCertificate(final CryptoAPIX509Certificate certificate) {

		final byte[] certPEMArray = certificate.getPemEncoded();
		return new String(certPEMArray, StandardCharsets.UTF_8);
	}
}
