/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.keystore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.config.Parameters;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.signing.DigitalSignatures;
import ch.post.it.evoting.cryptoprimitives.signing.GenKeysAndCertOutput;
import ch.post.it.evoting.cryptoprimitives.signing.GenKeysAndCertService;

@Service
public class KeyStoreCommandProcessor implements Consumer<Parameters> {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreCommandProcessor.class);

	private final RandomService randomService;
	private final KeyStoreParametersAdapter keyStoreParametersAdapter;

	public KeyStoreCommandProcessor(final RandomService randomService, final KeyStoreParametersAdapter keyStoreParametersAdapter) {
		this.randomService = randomService;
		this.keyStoreParametersAdapter = keyStoreParametersAdapter;
	}

	@Override
	public void accept(final Parameters parameters) {

		LOGGER.info("Starting the generation of key store");

		final KeyStoreParametersContainer container = keyStoreParametersAdapter.adapt(parameters);
		final DigitalSignatures digitalSignatures = new GenKeysAndCertService(randomService, container.getAuthorityInformation());

		final Path keystorePath = container.getOutputPath().resolve("signing_keystore_" + container.getAlias() + ".jks");
		final Path passwordFilePath = container.getOutputPath().resolve("signing_pw_" + container.getAlias() + ".txt");
		final Path certificatePath = container.getOutputPath().resolve("signing_certificate_" + container.getAlias() + ".crt");

		LOGGER.info("Parameters collected");

		createFileStructure(container.getOutputPath(), keystorePath, passwordFilePath, certificatePath);

		LOGGER.info("File structure created");

		GenKeysAndCertOutput output = digitalSignatures.genKeysAndCert(container.getValidFrom(), container.getValidUntil());

		LOGGER.info("Key and cert generated");

		String password = randomService.genRandomBase64String(container.getPasswordLength());

		LOGGER.info("Password generated");

		KeyStore keyStore = createKeyStore(container, output, password);

		LOGGER.info("KeyStore created");

		writeOutputsToFile(keystorePath, passwordFilePath, certificatePath, output, password, keyStore);

		LOGGER.info("Data written to output dir '{}'...", container.getOutputPath());
	}

	private void writeOutputsToFile(final Path keystorePath, final Path passwordFilePath, final Path certificatePath,
			final GenKeysAndCertOutput output, final String password,
			KeyStore keyStore) {
		try (final FileOutputStream keyStoreFos = new FileOutputStream(keystorePath.toFile());
				final FileOutputStream passwordFileFos = new FileOutputStream(passwordFilePath.toFile());
				final FileOutputStream certificateFos = new FileOutputStream(certificatePath.toFile())) {
			keyStore.store(keyStoreFos, password.toCharArray());
			passwordFileFos.write(password.getBytes(StandardCharsets.UTF_8));
			certificateFos.write(output.getCertificate().getEncoded());
		} catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
			throw new KeyStoreGeneratorException("Error while writing outputs to files.", e);
		}
	}

	private void createFileStructure(final Path containingDirectory, final Path keystorePath, final Path passwordFilePath,
			final Path certificatePath) {
		try {
			Files.createDirectories(containingDirectory);
		} catch (FileAlreadyExistsException e) {
			LOGGER.info("Directory already exist.");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		Arrays.asList(keystorePath, passwordFilePath, certificatePath).forEach(file -> {
			try {
				Files.createFile(file);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	private KeyStore createKeyStore(final KeyStoreParametersContainer parameters, final GenKeysAndCertOutput output, final String password) {
		try {
			final KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, password.toCharArray());
			keyStore.setKeyEntry(parameters.getAlias(), output.getPrivateKey(), "".toCharArray(),
					new Certificate[] { output.getCertificate() });
			return keyStore;
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
			throw new KeyStoreGeneratorException("Error while creating the key store.", e);
		}
	}
}
