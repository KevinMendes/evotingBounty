/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.generators;

import java.security.KeyPair;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.generators.CredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.utils.PasswordEncrypter;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.CreateElectionEventException;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.GenerateAdminBoardKeysException;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.GenerateElectionEventCAException;

@Service
public class ElectionCredentialDataPackGenerator extends CredentialDataPackGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionCredentialDataPackGenerator.class);

	private final PasswordEncrypter passwordEncrypter;

	public ElectionCredentialDataPackGenerator(final AsymmetricServiceAPI asymmetricService, final X509CertificateGenerator certificateGenerator,
			final KeyStoreService storesService, final CryptoAPIRandomString cryptoRandomString) {
		super(asymmetricService, cryptoRandomString, certificateGenerator, storesService);

		passwordEncrypter = new PasswordEncrypter(asymmetricService);
	}

	public ElectionCredentialDataPack generate(final ElectionInputDataPack electionInputDataPack, final ReplacementsHolder replacementsHolder,
			final String name, final String keyForProtectingKeystorePassword, final Properties certificateProperties,
			final CryptoAPIX509Certificate... parentCerts) throws GeneralCryptoLibException {

		final ElectionCredentialDataPack dataPack = new ElectionCredentialDataPack();

		final KeyPair keyPair;
		try {

			keyPair = asymmetricService.getKeyPairForSigning();
			dataPack.setKeyPair(keyPair);

			switch (name) {
			case Constants.CONFIGURATION_ELECTION_CA_JSON_TAG:
				// log success - Key Pairs successfully generated and stored
				LOGGER.info(ConfigGeneratorLogEvents.GENEECA_SUCCESS_KEYPAIR_GENERATED_STORED.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID);
				break;
			case Constants.CONFIGURATION_ADMINBOARD_CA_JSON_TAG:
				// log success - key pair successfully generated and stored
				LOGGER.info(ConfigGeneratorLogEvents.GENABK_SUCCESS_KEYPAIR_GENERATED_STORED.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID);
				break;
			case Constants.CONFIGURATION_SERVICES_CA_JSON_TAG:
			case Constants.CONFIGURATION_CREDENTIALS_CA_JSON_TAG:
			case Constants.CONFIGURATION_AUTHORITIES_CA_JSON_TAG:
			case Constants.CONFIGURATION_AUTH_TOKEN_SIGNER_JSON_TAG:
				final String successMessage = "success - key pair successfully generated and stored. [{}, {}]";
				LOGGER.info(successMessage, electionInputDataPack.getEeid(), Constants.ADMIN_ID);
				break;
			default:
				LOGGER.warn("Unknown Json tag when generating key pair: {}.", name);
				break;
			}
		} catch (final Exception e) {

			switch (name) {
			case Constants.CONFIGURATION_ELECTION_CA_JSON_TAG:
				// log error - Error generating the key pair for the CA Certificate
				LOGGER.error(ConfigGeneratorLogEvents.GENEECA_ERROR_GENERATING_KEYPAIR_CA_CERTIFICATE.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID, Constants.ERR_DESC, e.getMessage());
				throw new GenerateElectionEventCAException(e);
			case Constants.CONFIGURATION_ADMINBOARD_CA_JSON_TAG:
				// log error - Error generating the key pair of the AB
				LOGGER.error(ConfigGeneratorLogEvents.GENABK_ERROR_GENERATING_KEYPAIR_AB.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID, Constants.ERR_DESC, e.getMessage());
				throw new GenerateAdminBoardKeysException(e);
			default:
				throw new CreateElectionEventException(e);
			}
		}

		final CryptoAPIX509Certificate certificate;
		try {

			final CertificateParameters certificateParameters = getCertificateParameters(electionInputDataPack.getCredentialProperties(),
					electionInputDataPack.getStartDate(), electionInputDataPack.getEndDate(), replacementsHolder, certificateProperties);

			certificate = createX509Certificate(electionInputDataPack, certificateParameters, keyPair);
			dataPack.setCertificate(certificate);

			switch (name) {
			case Constants.CONFIGURATION_ADMINBOARD_CA_JSON_TAG:
				// log success - AB Certificate correctly generated
				LOGGER.info(ConfigGeneratorLogEvents.GENABK_SUCCESS_AB_CERTIFICATE_GENERATED.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID, Constants.CERT_CN, dataPack.getCertificate().getSubjectDn().getCommonName(), Constants.CERT_SN,
						dataPack.getCertificate().getSerialNumber());
				break;
			case Constants.CONFIGURATION_BALLOTBOX_JSON_TAG:
				LOGGER.info(ConfigGeneratorLogEvents.GENBB_SUCCESS_CERTIFICATE_GENERATED.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID, Constants.CERT_CN, dataPack.getCertificate().getSubjectDn().getCommonName(), Constants.CERT_SN,
						dataPack.getCertificate().getSerialNumber());
				break;
			case Constants.CONFIGURATION_ELECTION_CA_JSON_TAG:
			case Constants.CONFIGURATION_SERVICES_CA_JSON_TAG:
			case Constants.CONFIGURATION_CREDENTIALS_CA_JSON_TAG:
			case Constants.CONFIGURATION_AUTHORITIES_CA_JSON_TAG:
			case Constants.CONFIGURATION_AUTH_TOKEN_SIGNER_JSON_TAG:
				final String successMessage = "success - certificate correctly generated. [{}, {}, {}, {}, {}, {}]";
				LOGGER.info(successMessage, electionInputDataPack.getEeid(), Constants.ADMIN_ID, Constants.CERT_CN,
						dataPack.getCertificate().getSubjectDn().getCommonName(), Constants.CERT_SN, dataPack.getCertificate().getSerialNumber());
				break;
			default:
				LOGGER.warn("Unknown Json tag when generating certificate: {}.", name);
				break;
			}
		} catch (final Exception e) {

			switch (name) {
			case Constants.CONFIGURATION_ADMINBOARD_CA_JSON_TAG:
				// log error - Error generating the AB certificate
				LOGGER.info(ConfigGeneratorLogEvents.GENABK_ERROR_GENERATING_AB_CERTIFICATE.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID, Constants.CERT_CN, dataPack.getCertificate().getSubjectDn().getCommonName(), Constants.CERT_SN,
						dataPack.getCertificate().getSerialNumber().toString());
				throw new GenerateAdminBoardKeysException(e);
			case Constants.CONFIGURATION_BALLOTBOX_JSON_TAG:
				LOGGER.info(ConfigGeneratorLogEvents.GENBB_ERROR_GENERATING_CERTIFICATE.getInfo(), electionInputDataPack.getEeid(),
						Constants.ADMIN_ID, Constants.CERT_CN, dataPack.getCertificate().getSubjectDn().getCommonName(), Constants.CERT_SN,
						dataPack.getCertificate().getSerialNumber().toString());
				throw new GenerateAdminBoardKeysException(e);
			default:
				throw new CreateElectionEventException(e);
			}
		}

		final char[] keyStorePassword = cryptoRandomString.nextRandom(Constants.KEYSTORE_PW_LENGTH).toCharArray();
		dataPack.setPassword(keyStorePassword);

		final CryptoAPIExtendedKeyStore keyStore = storesService.createKeyStore();
		dataPack.setKeyStore(keyStore);

		if (parentCerts != null) {

			final CryptoAPIX509Certificate[] certs = new CryptoAPIX509Certificate[parentCerts.length + 1];

			certs[0] = certificate;
			System.arraycopy(parentCerts, 0, certs, 1, parentCerts.length);

			setPrivateKeyToKeystore(keyStore, electionInputDataPack.getCredentialProperties().obtainPrivateKeyAlias(), keyPair.getPrivate(),
					keyStorePassword, certs);

		} else {

			setPrivateKeyToKeystore(keyStore, electionInputDataPack.getCredentialProperties().obtainPrivateKeyAlias(), keyPair.getPrivate(),
					keyStorePassword, certificate);

		}

		final String encryptedKeyStorePassword = passwordEncrypter
				.encryptPasswordIfEncryptionKeyAvailable(keyStorePassword, keyForProtectingKeystorePassword);
		dataPack.setEncryptedPassword(encryptedKeyStorePassword);

		// Adding the start and end dates
		dataPack.setStartDate(electionInputDataPack.getElectionStartDate());
		dataPack.setEndDate(electionInputDataPack.getElectionEndDate());

		return dataPack;
	}
}
