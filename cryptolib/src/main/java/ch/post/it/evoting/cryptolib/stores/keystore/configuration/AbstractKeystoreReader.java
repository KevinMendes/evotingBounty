/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.stores.service.StoresService;

public abstract class AbstractKeystoreReader implements KeystoreReader {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractKeystoreReader.class);
	private static final String ERROR_TRYING_TO_READ_CERTIFICATE = "Error while trying to read certificate {}: {}";
	private final KeystoreResources resources;

	private final KeystoreProperties properties;

	private final StoresService storesService;

	protected AbstractKeystoreReader(final KeystoreResources resources) {

		this.resources = resources;
		this.properties = resources.getKeystoreProperties();
		storesService = new StoresService();
	}

	@Override
	public PrivateKey readSigningPrivateKey(final NodeIdentifier nodeIdentifier, final String keystorePassword) {
		return this.readSigningPrivateKey(nodeIdentifier, keystorePassword, keystorePassword);
	}

	@Override
	public PrivateKey readSigningPrivateKey(final NodeIdentifier nodeIdentifier, final String keystorePassword,
			final String signingPrivateKeyPassword) {
		final KeystoreProperties.NodeProperties nodeProperties = this.properties.getNodeProperties(nodeIdentifier);
		return this.readPrivateKey(nodeProperties.getPrivateKeystoreFilename(), nodeProperties.getPrivateKeystoreType(), keystorePassword,
				nodeProperties.getSigningPrivateKeyAlias(), signingPrivateKeyPassword);
	}

	@Override
	public PrivateKey readSigningPrivateKey(final NodeIdentifier nodeIdentifier, final KeystorePasswords keystorePasswords) {
		final AbstractKeystorePasswords readableKeystorePasswords = (AbstractKeystorePasswords) keystorePasswords;
		return this.readSigningPrivateKey(nodeIdentifier, readableKeystorePasswords.getPrivateKeystorePassword(),
				readableKeystorePasswords.getSigningPrivateKeyPassword());

	}

	@Override
	public PrivateKey readEncryptionPrivateKey(final NodeIdentifier nodeIdentifier, final String keystorePassword) {
		return this.readEncryptionPrivateKey(nodeIdentifier, keystorePassword, keystorePassword);
	}

	@Override
	public PrivateKey readEncryptionPrivateKey(final NodeIdentifier nodeIdentifier, final String keystorePassword,
			final String encryptionPrivateKeyPassword) {
		final KeystoreProperties.NodeProperties nodeProperties = this.properties.getNodeProperties(nodeIdentifier);
		return this.readPrivateKey(nodeProperties.getPrivateKeystoreFilename(), nodeProperties.getPrivateKeystoreType(), keystorePassword,
				nodeProperties.getEncryptionPrivateKeyAlias(), encryptionPrivateKeyPassword);
	}

	@Override
	public PrivateKey readEncryptionPrivateKey(final NodeIdentifier nodeIdentifier, final KeystorePasswords keystorePasswords) {
		final AbstractKeystorePasswords readableKeystorePasswords = (AbstractKeystorePasswords) keystorePasswords;
		return this.readEncryptionPrivateKey(nodeIdentifier, readableKeystorePasswords.getPrivateKeystorePassword(),
				readableKeystorePasswords.getEncryptionPrivateKeyPassword());
	}

	private PrivateKey readPrivateKey(final String privateKeystoreFilename, final String privateKeystoreType, final String privateKeystorePassword,
			final String privateKeyAlias, final String privateKeyPassword) {
		try (final InputStream inputStream = this.resources.getResourceAsStream(privateKeystoreFilename)) {
			if (inputStream == null) {
				return null;
			} else {
				try {
					final KeyStore privateKeystore = this.storesService
							.loadKeyStore(KeyStoreType.valueOf(privateKeystoreType), inputStream, privateKeystorePassword.toCharArray());
					return (PrivateKey) privateKeystore.getKey(privateKeyAlias, privateKeyPassword.toCharArray());

				} catch (final GeneralCryptoLibException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
					LOGGER.error("Error while trying to read private key {} in keystore {}: {}", privateKeyAlias, privateKeystoreFilename,
							e.getMessage(), e);
					return null;
				}
			}
		} catch (final IOException ioE) {
			LOGGER.error("Could not find private keystore {}.", privateKeystoreFilename, ioE);
			return null;
		}
	}

	@Override
	public Certificate readRootCertificate() {
		return this.readCertificate(this.properties.getRootCertificateFilename());
	}

	@Override
	public Certificate readSigningCertificate(final NodeIdentifier nodeIdentifier) {
		final KeystoreProperties.NodeProperties nodeProperties = this.properties.getNodeProperties(nodeIdentifier);
		return this.readCertificate(nodeProperties.getSigningCertificateFilename());
	}

	@Override
	public Certificate readEncryptionCertificate(final NodeIdentifier nodeIdentifier) {
		final KeystoreProperties.NodeProperties nodeProperties = this.properties.getNodeProperties(nodeIdentifier);
		return this.readCertificate(nodeProperties.getEncryptionCertificateFilename());
	}

	private Certificate readCertificate(final String certificateFilename) {
		try (final InputStream inputStream = this.resources.getResourceAsStream(certificateFilename)) {
			if (inputStream == null) {
				return null;
			} else {
				final String certificateString;
				try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
					certificateString = buffer.lines().collect(Collectors.joining("\n"));
				} catch (final IOException e) {
					LOGGER.error(ERROR_TRYING_TO_READ_CERTIFICATE, certificateFilename, e.getMessage(), e);
					return null;
				}
				try {
					return PemUtils.certificateFromPem(certificateString);
				} catch (final GeneralCryptoLibException e) {
					LOGGER.error(ERROR_TRYING_TO_READ_CERTIFICATE, certificateFilename, e.getMessage(), e);
					return null;
				}
			}
		} catch (final IOException e) {
			LOGGER.error(ERROR_TRYING_TO_READ_CERTIFICATE, certificateFilename, e.getMessage(), e);
			return null;
		}
	}

}
