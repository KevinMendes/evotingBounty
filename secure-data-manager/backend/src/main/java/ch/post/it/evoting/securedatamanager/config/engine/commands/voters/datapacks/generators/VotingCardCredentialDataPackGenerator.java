/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators;

import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_ERROR_GENERATING_CREDENTIAL_ID_AUTHENTICATION_CERTIFICATE;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_ERROR_GENERATING_CREDENTIAL_ID_AUTHENTICATION_KEYPAIR;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_SUCCESS_CREDENTIAL_ID_AUTHENTICATION_CERTIFICATE_GENERATED;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_SUCCESS_CREDENTIAL_ID_AUTHENTICATION_KEYPAIR_GENERATED;

import java.security.KeyPair;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.specific.GenerateCredentialDataException;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.generators.CredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.logging.ExecutionTimeLogger;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialInputDataPack;

@Component
public class VotingCardCredentialDataPackGenerator extends CredentialDataPackGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardCredentialDataPackGenerator.class);

	public VotingCardCredentialDataPackGenerator(
			@Qualifier("asymmetricServiceAPI")
			final AsymmetricServiceAPI asymmetricService, final X509CertificateGenerator certificateGenerator, final KeyStoreService storesService,
			final CryptoAPIRandomString cryptoRandomString) {
		super(asymmetricService, cryptoRandomString, certificateGenerator, storesService);
	}

	public VotingCardCredentialDataPack generate(final VotingCardCredentialInputDataPack inputDataPack, final ReplacementsHolder replacementsHolder,
			final char[] keystoreSymmetricEncryptionKey, final String credentialID, final String votingCardSetID,
			final Properties credentialAuthCertificateProperties,
			final CryptoAPIX509Certificate... parentCerts) throws GeneralCryptoLibException {

		final VotingCardCredentialDataPack dataPack = new VotingCardCredentialDataPack();

		final ExecutionTimeLogger timer = new ExecutionTimeLogger("generateVotingCardCredentialDataPack-Detailed");

		final KeyPair keyPairAuth;
		try {
			keyPairAuth = asymmetricService.getKeyPairForSigning();
			LOGGER.debug("{}. [electionEventId: {}, votingCardSetId: {}, credentialId: {}]",
					GENCREDAT_SUCCESS_CREDENTIAL_ID_AUTHENTICATION_KEYPAIR_GENERATED.getInfo(), inputDataPack.getEeid(), votingCardSetID,
					credentialID);

			dataPack.setKeyPairAuth(keyPairAuth);

		} catch (final Exception e) {
			LOGGER.error("{}. [electionEventId: {}, votingCardSetId: {}, credentialId: {}]",
					GENCREDAT_ERROR_GENERATING_CREDENTIAL_ID_AUTHENTICATION_KEYPAIR.getInfo(), inputDataPack.getEeid(), votingCardSetID,
					credentialID);

			throw new GenerateCredentialDataException(e);
		}

		timer.log("generateKeyPairForAuthentication");

		final CryptoAPIX509Certificate certificateAuth;

		final CertificateParameters certificateParametersAuth = getCertificateParameters(inputDataPack.getCredentialAuthProperties(),
				inputDataPack.getStartDate(), inputDataPack.getEndDate(), replacementsHolder, credentialAuthCertificateProperties);

		try {

			certificateAuth = createX509Certificate(inputDataPack, certificateParametersAuth, keyPairAuth);

			LOGGER.debug("{}, [electionEventId: {}, votingCardSetId: {}, credentialId: {}, certificate CN: {}, certificate SN: {}]",
					GENCREDAT_SUCCESS_CREDENTIAL_ID_AUTHENTICATION_CERTIFICATE_GENERATED.getInfo(), inputDataPack.getEeid(), votingCardSetID,
					credentialID, certificateAuth.getSubjectDn().getCommonName(), certificateAuth.getSerialNumber().toString());

			dataPack.setCertificateAuth(certificateAuth);

		} catch (final Exception e) {

			LOGGER.error("{}, [electionEventId: {}, votingCardSetId: {}, credentialId: {}, certificate CN: {}]",
					GENCREDAT_ERROR_GENERATING_CREDENTIAL_ID_AUTHENTICATION_CERTIFICATE.getInfo(), inputDataPack.getEeid(), votingCardSetID,
					credentialID, certificateParametersAuth.getUserSubjectDn().getCommonName());

			throw new GenerateCredentialDataException(e);
		}

		timer.log("certificateAuth");

		final CryptoAPIExtendedKeyStore keyStore = storesService.createKeyStore();

		final CryptoAPIX509Certificate[] certs;

		certs = new CryptoAPIX509Certificate[parentCerts.length + 1];

		certs[0] = certificateAuth;
		System.arraycopy(parentCerts, 0, certs, 1, parentCerts.length);

		setPrivateKeyToKeystore(keyStore, inputDataPack.getCredentialAuthProperties().obtainPrivateKeyAlias(), keyPairAuth.getPrivate(),
				keystoreSymmetricEncryptionKey, certs);

		dataPack.setKeystoreToBeSerialized(keyStore, keystoreSymmetricEncryptionKey);

		timer.log("createKeyStores");
		return dataPack;
	}
}
