/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.function.Consumer;

import org.bouncycastle.cms.CMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.config.Parameters;
import ch.post.it.evoting.config.commands.CertificateUtils;
import ch.post.it.evoting.config.commands.ChainValidator;
import ch.post.it.evoting.config.commands.KeyStoreUtils;
import ch.post.it.evoting.config.commands.PasswordReaderUtils;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.cmssigner.CMSSigner;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;

@Service
public class EncryptionParametersCommandProcessor implements Consumer<Parameters> {

	private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionParametersCommandProcessor.class);

	private final KeyStoreUtils keyStoreUtils;
	private final ConfigOutputSerializer configOutputSerializer;
	private final ConfigEncryptionParametersGenerator configEncryptionParametersGenerator;
	private final ConfigEncryptionParametersAdapter configEncryptionParametersAdapter;
	private final CertificateUtils certificateUtils;

	public EncryptionParametersCommandProcessor(KeyStoreUtils keyStoreUtils, ConfigOutputSerializer configOutputSerializer,
			ConfigEncryptionParametersGenerator configEncryptionParametersGenerator,
			ConfigEncryptionParametersAdapter configEncryptionParametersAdapter,
			CertificateUtils certificateUtils) {
		this.keyStoreUtils = keyStoreUtils;
		this.configOutputSerializer = configOutputSerializer;
		this.configEncryptionParametersGenerator = configEncryptionParametersGenerator;
		this.configEncryptionParametersAdapter = configEncryptionParametersAdapter;
		this.certificateUtils = certificateUtils;
	}

	@Override
	public void accept(final Parameters parameters) {

		try {
			LOGGER.info("Starting to generate the encryption parameters...");

			final ConfigEncryptionParametersContainer holder = configEncryptionParametersAdapter.adapt(parameters);

			char[] password = PasswordReaderUtils.readPasswordFromConsole();

			KeyStore store = keyStoreUtils.decodeKeyStore(holder.getP12Path().toString(), password);
			PrivateKey privateKey = keyStoreUtils.loadPrivateKeyFromKeyStore(store, password);

			LOGGER.info("Parameters collected");

			clean(password);

			LOGGER.info("Password deleted from memory");

			Certificate trustedCA = certificateUtils.readTrustedCA(holder.getTrustedCAPath());

			LOGGER.info("CA trusted file red");

			byte[] seedSignature = Files.readAllBytes(holder.getSeedSignaturePath());

			verifySeedSignature(holder, trustedCA, seedSignature);

			LOGGER.info("Seed's signature verified");

			final VerifiableElGamalEncryptionParameters encryptionParameters = configEncryptionParametersGenerator.generate(holder.getSeedPath());

			LOGGER.info("Encryption parameters generated");

			final Path writtenFilePath = configOutputSerializer.serialize(encryptionParameters, privateKey, holder.getOutputPath());

			LOGGER.info("Output processed");

			LOGGER.info("The pre-configuration was generated correctly. It can be found in: {}", writtenFilePath);
		} catch (IOException e) {
			throw new EncryptionParameterException(e);
		}
	}

	private void verifySeedSignature(ConfigEncryptionParametersContainer holder, Certificate trustedCA, byte[] seedSignature) throws IOException {
		try (InputStream seed = Files.newInputStream(holder.getSeedPath())) {
			Certificate[][] signers = CMSSigner.verify(seedSignature, seed);

			if (signers.length != 1 && signers[0].length < 1) {
				throw new IllegalArgumentException("Seed signature signers could not be recovered.");
			}

			Certificate[] chain = new Certificate[signers[0].length - 1];
			System.arraycopy(signers[0], 1, chain, 0, signers[0].length - 1);
			ChainValidator.validateChain(trustedCA, chain, signers[0][0], X509CertificateType.SIGN);
		} catch (CMSException | GeneralCryptoLibException e) {
			LOGGER.error("Seed signature could not be verified.");
			throw new IllegalArgumentException(e);
		}
	}

	private void clean(final char[] password) {
		Arrays.fill(password, '\u0000');
	}
}
