/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.integrationtests.keystore;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.config.Application;
import ch.post.it.evoting.config.ConfigurationCommandLine;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;

@SpringJUnitConfig(classes = Application.class)
class KeyStoreTest {

	@TempDir
	Path tempDir;

	@Autowired
	RandomService randomService;

	@Autowired
	private ConfigurationCommandLine commandLine;

	@Test
	void allParametersAreGiven_allFilesAreCreatedAndValid() throws Exception {
		// given
		final Path outputPath = tempDir.resolve("output");
		final String alias = "test";
		final String keystoreFileName = "signing_keystore_" + alias + ".jks";
		final String passwordFileName = "signing_pw_" + alias + ".txt";
		final String certificateFileName = "signing_certificate_" + alias + ".crt";

		// when
		commandLine.run(generateArgument(outputPath.toString()));

		// then
		assertThat(Files.list(outputPath)).containsExactlyInAnyOrder(outputPath.resolve(keystoreFileName),
				outputPath.resolve(passwordFileName), outputPath.resolve(certificateFileName));

		final KeyStore keyStore = getKeyStore(outputPath, keystoreFileName, passwordFileName);

		final PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, "".toCharArray());
		final PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();

		validateKeyPair(privateKey, publicKey);

		final Certificate fromKeyStore = keyStore.getCertificate(alias);
		final Certificate fromFile = getCertificate(outputPath, certificateFileName);

		validateCertificate(fromKeyStore, fromFile);
	}

	private String[] generateArgument(final String out) {
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("-keystore", "");
		parameters.put("-alias", "test");
		parameters.put("-valid_from", "29/03/2022");
		parameters.put("-valid_until", "31/03/2022");
		parameters.put("-certificate_common_name", "test");
		parameters.put("-certificate_country", "test");
		parameters.put("-certificate_state", "test");
		parameters.put("-certificate_locality", "test");
		parameters.put("-certificate_organisation", "test");
		parameters.put("-out", out);

		return parameters.entrySet().stream().flatMap(e -> Stream.of(e.getKey(), e.getValue())).filter(s -> !s.isEmpty()).toArray(String[]::new);
	}

	private void validateKeyPair(final PrivateKey privateKey, final PublicKey publicKey) {
		try {
			assertThat(privateKey).isNotNull();
			assertThat(publicKey).isNotNull();

			final byte[] challenge = randomService.randomBytes(1000);

			final Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(privateKey);
			sig.update(challenge);
			final byte[] signature = sig.sign();

			sig.initVerify(publicKey);
			sig.update(challenge);

			assertThat(sig.verify(signature)).isTrue();

		} catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
			throw new IllegalStateException("should not happen", e);
		}
	}

	private void validateCertificate(final Certificate fromKeyStore, final Certificate fromFile) {
		try {
			assertThat(fromKeyStore).isNotNull();
			assertThat(fromFile).isNotNull();

			assertThat(fromFile.getEncoded()).isEqualTo(fromKeyStore.getEncoded());

		} catch (CertificateEncodingException e) {
			throw new IllegalStateException("should not happen", e);
		}
	}

	private KeyStore getKeyStore(final Path outputPath, final String keyStoreFileName, final String passwordFileName) {
		try {
			final char[] password = new String(Files.readAllBytes(outputPath.resolve(passwordFileName)),
					StandardCharsets.UTF_8).toCharArray();
			final KeyStore ks = KeyStore.getInstance("JKS");

			try (final ByteArrayInputStream stream = new ByteArrayInputStream(
					Files.readAllBytes(outputPath.resolve(keyStoreFileName)))) {

				ks.load(stream, password);
				return ks;

			} catch (IOException e) {
				throw new IllegalArgumentException("Cannot read key store. The password may be incorrect.", e);
			}
		} catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new IllegalStateException("should not happen", e);
		}
	}

	public static Certificate getCertificate(final Path outputPath, final String certificateFileName) {
		try (final ByteArrayInputStream stream = new ByteArrayInputStream(
				Files.readAllBytes(outputPath.resolve(certificateFileName)))) {

			final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			return certificateFactory.generateCertificate(stream);

		} catch (IOException | CertificateException e) {
			throw new IllegalStateException("should not happen", e);
		}
	}
}
