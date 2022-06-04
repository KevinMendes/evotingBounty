/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class KeystoreTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeystoreTest.class);

	@Test
	void test() {
		try {
			final KeystoreReader keystoreReader = TestKeystoreReader.getInstance();

			final Certificate rootCertificate = keystoreReader.readRootCertificate();
			assertNotNull(rootCertificate);

			LOGGER.debug("\n-Root certificate:\n" + SecurityUtils.certificateToString(rootCertificate));

			final Set<NodeIdentifier> signingNodes = EnumSet.allOf(NodeIdentifier.class);

			for (final NodeIdentifier nodeIdentifier : signingNodes) {
				LOGGER.debug("\nSigning node: " + nodeIdentifier.getName() + " (" + nodeIdentifier.getAlias() + ")\n");

				// Passwords

				final KeystorePasswordsReader keystorePasswordsReader = TestKeystorePasswordsReader.getInstance(nodeIdentifier);
				final KeystorePasswords keystorePasswords = keystorePasswordsReader.read();
				assertNotNull(keystorePasswords);

				final AbstractKeystorePasswords readableKeystorePasswords = (AbstractKeystorePasswords) keystorePasswords;

				assertNotNull(readableKeystorePasswords.getPrivateKeystorePassword());
				LOGGER.debug("-Private keystore password--->" + readableKeystorePasswords.getPrivateKeystorePassword() + "<---\n");

				assertNotNull(readableKeystorePasswords.getSigningPrivateKeyPassword());
				LOGGER.debug("-Signing private key password--->" + readableKeystorePasswords.getSigningPrivateKeyPassword() + "<---\n");

				assertNotNull(readableKeystorePasswords.getEncryptionPrivateKeyPassword());
				LOGGER.debug("-Encryption private key password--->" + readableKeystorePasswords.getEncryptionPrivateKeyPassword() + "<---\n");

				// Private keys

				final PrivateKey signingPrivateKey = keystoreReader.readSigningPrivateKey(nodeIdentifier, keystorePasswords);
				assertNotNull(signingPrivateKey);
				LOGGER.debug("-Signing private key:\n" + SecurityUtils.privateKeyToString(signingPrivateKey));

				final PrivateKey encryptionPrivateKey = keystoreReader.readEncryptionPrivateKey(nodeIdentifier, keystorePasswords);
				assertNull(encryptionPrivateKey);
				LOGGER.debug("-Encryption private key: <null>\n");

				// Destroy passwords

				LOGGER.debug("-Before passwords destroy:\n" + readableKeystorePasswords.getString() + "\n");
				keystorePasswords.destroy();
				assertTrue(keystorePasswords.isDestroyed());
				LOGGER.debug("-After passwords destroy:\n" + readableKeystorePasswords.getString() + "\n");

				keystorePasswordsReader.destroy();
				assertTrue(keystorePasswordsReader.isDestroyed());

				// Certificates

				final Certificate signingCertificate = keystoreReader.readSigningCertificate(nodeIdentifier);
				assertNotNull(signingCertificate);
				LOGGER.debug("-Signing certificate:\n" + SecurityUtils.certificateToString(signingCertificate));

				final Certificate encryptionCertificate = keystoreReader.readEncryptionCertificate(nodeIdentifier);
				assertNull(encryptionCertificate);
				LOGGER.debug("-Encryption certificate: <null>\n");
			}

			// Currently this part of the test is ignored (no non-signing modules are available that
			// need logging keys), since we do not have any module that encrypts and signs logs based on
			// the
			// generated certificates, so the resulting set will be empty.
			final Set<NodeIdentifier> nonSigningNodes = EnumSet.allOf(NodeIdentifier.class);
			nonSigningNodes.removeAll(signingNodes);
			for (final NodeIdentifier nodeIdentifier : nonSigningNodes) {
				LOGGER.debug("\nNon-signing node: " + nodeIdentifier.getName() + " (" + nodeIdentifier.getAlias() + ")\n");

				// Private keys

				final PrivateKey signingPrivateKey = keystoreReader.readSigningPrivateKey(nodeIdentifier, "");
				assertNull(signingPrivateKey);
				LOGGER.debug("-Signing private key: <null>\n");

				final PrivateKey encryptionPrivateKey = keystoreReader.readEncryptionPrivateKey(nodeIdentifier, "");
				assertNull(encryptionPrivateKey);
				LOGGER.debug("-Encryption private key: <null>\n");

				// Certificates

				final Certificate signingCertificate = keystoreReader.readSigningCertificate(nodeIdentifier);
				assertNull(signingCertificate);
				LOGGER.debug("-Signing certificate: <null>\n");

				final Certificate encryptionCertificate = keystoreReader.readEncryptionCertificate(nodeIdentifier);
				assertNull(encryptionCertificate);
				LOGGER.debug("-Encryption certificate: <null>\n");
			}
			LOGGER.debug("\n\n");
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

}
