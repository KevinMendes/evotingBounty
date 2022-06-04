/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.commons.progress.JobProgressDetails;
import ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.generators.ElectionCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.progress.ProgressManager;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.CreateElectionEventException;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.GenerateElectionEventCAException;

/**
 * The class that generates certificates, keystores, and other auxiliary data on the create election event command and returns it using a
 * CreateElectionEventOutput class.
 */
@Service
public class CreateElectionEventGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateElectionEventGenerator.class);
	private static final String DATA_PACK_SUCCESSFULLY_CREATED = "Data pack successfully created for";

	private final ElectionCredentialDataPackGenerator electionCredentialDataPackGenerator;
	private final ProgressManager votersProgressManager;

	public CreateElectionEventGenerator(final ElectionCredentialDataPackGenerator electionCredentialDataPackGenerator,
			final ProgressManager votersProgressManager) {
		this.electionCredentialDataPackGenerator = electionCredentialDataPackGenerator;
		this.votersProgressManager = votersProgressManager;
	}

	/**
	 * Generates all the datapacks (certs, keystores) defined on the ElectionInputDataPack (inside CreateElectionEventParametersHolder) and pass them
	 * using CreateElectionEventOutput class.
	 */
	public CreateElectionEventOutput generate(final CreateElectionEventParametersHolder sharedData) throws GeneralCryptoLibException {

		final CreateElectionEventOutput createElectionEventOutput = new CreateElectionEventOutput();

		final ElectionInputDataPack electionInputDataPack = sharedData.getInputDataPack();

		final Map<String, CredentialProperties> configProperties = sharedData.getConfigurationInput().getConfigProperties();

		final List<CredentialProperties> sortedCredentialProperties = new ArrayList<>(configProperties.values());
		sortedCredentialProperties.sort(Comparator.comparing(CredentialProperties::getCredentialType));

		final UUID jobId = UUID.randomUUID();
		final JobProgressDetails details = new JobProgressDetails(jobId, sortedCredentialProperties.size());

		votersProgressManager.registerJob(jobId, details);

		for (final CredentialProperties current : sortedCredentialProperties) {

			try {
				LOGGER.info("Creating data pack for {}", current.getName());

				electionInputDataPack.setCredentialProperties(current);
				if (StringUtils.isNotEmpty(current.getParentName())) {
					electionInputDataPack.setParentKeyPair(createElectionEventOutput.getDataPackMap().get(current.getParentName()).getKeyPair());
				}
				final CryptoAPIX509Certificate[] parentCertificates = getParentCertificatesChain(current.getParentName(),
						createElectionEventOutput.getDataPackMap(), configProperties);

				final Properties certificateProperties = sharedData.getCertificatePropertiesInput().getNameToCertificateProperties().get(current.getName());

				final ElectionCredentialDataPack dataPack = electionCredentialDataPackGenerator
						.generate(electionInputDataPack, electionInputDataPack.getReplacementsHolder(), current.getName(), Constants.EMPTY,
								certificateProperties, parentCertificates);
				createElectionEventOutput.getDataPackMap().put(current.getName(), dataPack);

				LOGGER.info(DATA_PACK_SUCCESSFULLY_CREATED + " {}", current.getName());

				if (current.getName().equals(Constants.CONFIGURATION_ELECTION_CA_JSON_TAG)) {
					// log success - CA certificate successfully generated and stored
					LOGGER.info(ConfigGeneratorLogEvents.GENEECA_SUCCESS_CA_CERTIFICATE_GENERATED.getInfo(), sharedData.getInputDataPack().getEeid(),
							"adminID", "cert_cn", dataPack.getCertificate().getSubjectDn().getCommonName(), "cert_sn",
							dataPack.getCertificate().getSerialNumber().toString());
				}

			} catch (final Exception e) {

				if (current.getName().equals(Constants.CONFIGURATION_ELECTION_CA_JSON_TAG)) {
					// log error - Error generating the CA certificate
					String commonName = "";
					final ElectionCredentialDataPack dataPack = createElectionEventOutput.getDataPackMap().get(current.getName());
					if (dataPack != null) {
						commonName = dataPack.getCertificate().getSubjectDn().getCommonName();
					}

					LOGGER.error(ConfigGeneratorLogEvents.GENEECA_ERROR_GENERATING_CA_CERTIFICATE.getInfo(), sharedData.getInputDataPack().getEeid(),
							"adminID", "cert_cn", commonName, "err_desc", e.getMessage());
					throw new GenerateElectionEventCAException(e);
				}

				throw new CreateElectionEventException(e);
			} finally {
				votersProgressManager.updateProgress(jobId, 1);
			}
		}

		final CredentialProperties authTokenSigner = sharedData.getConfigurationInput().getAuthTokenSigner();

		LOGGER.info("Creating data pack for the {}", authTokenSigner.getName());

		electionInputDataPack.setCredentialProperties(authTokenSigner);

		electionInputDataPack.setParentKeyPair(createElectionEventOutput.getDataPackMap().get(authTokenSigner.getParentName()).getKeyPair());

		final CryptoAPIX509Certificate[] parentCertificates = getParentCertificatesChain(authTokenSigner.getParentName(),
				createElectionEventOutput.getDataPackMap(), configProperties);

		final String keyForProtectingKeystorePassword = sharedData.getKeyForProtectingKeystorePassword();

		final Properties certificateProperties = sharedData.getCertificatePropertiesInput().getAuthTokenSignerCertificateProperties();

		createElectionEventOutput.setAuthTokenSigner(electionCredentialDataPackGenerator
				.generate(electionInputDataPack, electionInputDataPack.getReplacementsHolder(), Constants.CONFIGURATION_AUTH_TOKEN_SIGNER_JSON_TAG,
						keyForProtectingKeystorePassword, certificateProperties, parentCertificates));

		LOGGER.info(DATA_PACK_SUCCESSFULLY_CREATED + "{}", authTokenSigner.getName());

		return createElectionEventOutput;

	}

	private CryptoAPIX509Certificate[] getParentCertificatesChain(final String parentName, final Map<String, ElectionCredentialDataPack> dataPackMap,
			final Map<String, CredentialProperties> credentialPropertiesMap) {

		final List<CryptoAPIX509Certificate> parentCertificates = new ArrayList<>();
		if (parentName != null) {
			String currentParent = parentName;
			while (currentParent != null) {
				parentCertificates.add(dataPackMap.get(currentParent).getCertificate());
				currentParent = credentialPropertiesMap.get(currentParent).getParentName();
			}
		}
		return parentCertificates.toArray(new CryptoAPIX509Certificate[0]);
	}

}
