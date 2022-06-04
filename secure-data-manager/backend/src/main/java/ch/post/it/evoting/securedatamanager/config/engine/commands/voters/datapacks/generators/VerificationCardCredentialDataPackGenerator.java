/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalKeyPair;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;
import ch.post.it.evoting.cryptolib.extendedkeystore.factory.CryptoExtendedKeyStoreWithPBKDF;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.specific.GenerateVerificationCardDataException;
import ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialInputDataPack;

public class VerificationCardCredentialDataPackGenerator extends ElGamalCredentialDataPackGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationCardCredentialDataPackGenerator.class);

	public VerificationCardCredentialDataPackGenerator(final AsymmetricServiceAPI asymmetricService, final CryptoAPIRandomString cryptoRandomString,
			final X509CertificateGenerator certificateGenerator, final KeyStoreService storesService) {

		super(asymmetricService, cryptoRandomString, certificateGenerator, storesService);
	}

	public VerificationCardCredentialDataPack generate(final VerificationCardCredentialInputDataPack inputDataPack, final String eeid,
			final String verificationCardID, final String verificationCardSetID,
			final char[] keystoreSymmetricEncryptionKey, final Path absoluteBasePath) throws GeneralCryptoLibException {

		final VerificationCardCredentialDataPack dataPack = new VerificationCardCredentialDataPack();

		final ElGamalKeyPair verificationCardKeypair = retrieveVerificationCardKeyPair(eeid, verificationCardID, verificationCardSetID, dataPack,
				absoluteBasePath);

		final CryptoExtendedKeyStoreWithPBKDF keyStore = createCryptoKeyStoreWithPBKDF();
		saveToKeyStore(inputDataPack, keyStore, keystoreSymmetricEncryptionKey, verificationCardKeypair);
		dataPack.setKeystoreToBeSerialized(keyStore, keystoreSymmetricEncryptionKey);
		return dataPack;
	}

	private void saveToKeyStore(final VerificationCardCredentialInputDataPack inputDataPack, final CryptoExtendedKeyStoreWithPBKDF keyStore,
			final char[] keystoreSymmetricEncryptionKey, final ElGamalKeyPair verificationCardKeypair) throws GeneralCryptoLibException {

		putKeyInKeystore(keyStore, verificationCardKeypair.getPrivateKeys(), keystoreSymmetricEncryptionKey,
				inputDataPack.getVerificationCardProperties().getAlias().get(Constants.CONFIGURATION_VERIFICATION_CARD_PRIVATE_KEY_JSON_TAG));
	}

	private ElGamalKeyPair retrieveVerificationCardKeyPair(final String eeid, final String verificationCardID, final String verificationCardSetID,
			final VerificationCardCredentialDataPack dataPack, final Path absoluteBasePath) {

		try {
			final Path verificationCardsKeyPairsFilePath = absoluteBasePath.resolve(Constants.CONFIG_DIR_NAME_OFFLINE)
					.resolve(Constants.CONFIG_VERIFICATION_CARDS_KEY_PAIR_DIRECTORY).resolve(verificationCardSetID)
					.resolve(verificationCardID + Constants.KEY);

			final String[] verificationCardKeyPair = new String(Files.readAllBytes(verificationCardsKeyPairsFilePath), StandardCharsets.UTF_8)
					.split(System.lineSeparator());

			dataPack.setVerificationCardKeyPair(new ElGamalKeyPair(ElGamalPrivateKey.fromJson(verificationCardKeyPair[0]),
					ElGamalPublicKey.fromJson(verificationCardKeyPair[1])));

			LOGGER.debug(ConfigGeneratorLogEvents.GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_KEYPAIR.getInfo(), eeid, Constants.ADMIN_ID,
					Constants.VERIFCS_ID, verificationCardSetID, Constants.VERIFC_ID, verificationCardID);

		} catch (final GeneralCryptoLibException | IOException e) {

			LOGGER.error(ConfigGeneratorLogEvents.GENVCD_ERROR_GENERATING_VERIFICATION_CARD_KEYPAIR.getInfo(), eeid, Constants.ADMIN_ID,
					Constants.VERIFCS_ID, verificationCardSetID, Constants.VERIFC_ID, verificationCardID, Constants.ERR_DESC, e.getMessage());

			throw new GenerateVerificationCardDataException(
					"An error occurred while trying to create the Verification Card ElGamal Keypair: " + e.getMessage(), e);
		}

		return dataPack.getVerificationCardKeyPair();
	}
}
