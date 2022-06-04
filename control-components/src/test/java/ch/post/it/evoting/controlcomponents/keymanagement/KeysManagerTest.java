/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;

class KeysManagerTest {

	private static final PasswordProtection PASSWORD_PROTECTION = new PasswordProtection("password".toCharArray());
	private static final String ALIAS = "alias";

	private NodeKeys nodeKeys;
	private KeysManager manager;

	@BeforeEach
	public void setUp()
			throws GeneralCryptoLibException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, KeyManagementException,
			CertificateException, IOException {

		PrivateKey caPrivateKey = mock(PrivateKey.class);
		when(caPrivateKey.getAlgorithm()).thenReturn("RSA");
		PublicKey caPublicKey = mock(PublicKey.class);
		when(caPublicKey.getAlgorithm()).thenReturn("RSA");
		when(caPublicKey.getEncoded()).thenReturn(new byte[0]);
		X509Certificate caCertificate = mock(X509Certificate.class);
		when(caCertificate.getPublicKey()).thenReturn(caPublicKey);
		X509Certificate[] caCertificateChain = new X509Certificate[] { caCertificate };

		PrivateKey encryptionPrivateKey = mock(PrivateKey.class);
		PublicKey encryptionPublicKey = mock(PublicKey.class);
		X509Certificate encryptionCertificate = mock(X509Certificate.class);
		when(encryptionCertificate.getPublicKey()).thenReturn(encryptionPublicKey);
		X509Certificate[] encryptionCertificateChain = { encryptionCertificate, caCertificate };

		PrivateKey logSigningPrivateKey = mock(PrivateKey.class);
		PublicKey logSigningPublicKey = mock(PublicKey.class);
		X509Certificate logSigningCertificate = mock(X509Certificate.class);
		when(logSigningCertificate.getPublicKey()).thenReturn(logSigningPublicKey);
		X509Certificate[] logSigningCertificateChain = { logSigningCertificate, caCertificate };

		PrivateKey logEncryptionPrivateKey = mock(PrivateKey.class);
		PublicKey logEncryptionPublicKey = mock(PublicKey.class);
		X509Certificate logEncryptionCertificate = mock(X509Certificate.class);
		when(logEncryptionCertificate.getPublicKey()).thenReturn(logEncryptionPublicKey);
		X509Certificate[] logEncryptionCertificateChain = { logEncryptionCertificate, caCertificate };

		nodeKeys = new NodeKeys.Builder().setCAKeys(caPrivateKey, caCertificateChain)
				.setEncryptionKeys(encryptionPrivateKey, encryptionCertificateChain)
				.setLogSigningKeys(logSigningPrivateKey, logSigningCertificateChain)
				.setLogEncryptionKeys(logEncryptionPrivateKey, logEncryptionCertificateChain).build();

		AsymmetricServiceAPI asymmetricService = mock(AsymmetricServiceAPI.class);
		when(asymmetricService.sign(eq(caPrivateKey), any(byte[].class))).thenReturn(new byte[0]);
		when(asymmetricService.verifySignature(any(byte[].class), eq(caPublicKey), any(byte[].class))).thenReturn(true);

		KeyStoreSpi keyStoreSpi = mock(KeyStoreSpi.class);
		doReturn(new PrivateKeyEntry(caPrivateKey, caCertificateChain)).when(keyStoreSpi).engineGetEntry(ALIAS, PASSWORD_PROTECTION);
		doReturn(true).when(keyStoreSpi).engineEntryInstanceOf(ALIAS, PrivateKeyEntry.class);

		KeyStoreDouble store = new KeyStoreDouble(keyStoreSpi);
		store.load(null, null);

		StoresServiceAPI storesService = mock(StoresServiceAPI.class);
		when(storesService.loadKeyStore(eq(KeyStoreType.PKCS12), any(InputStream.class), eq(PASSWORD_PROTECTION.getPassword()))).thenReturn(store);

		KeysAndCertificateGenerator keysAndCertificateGenerator = mock(KeysAndCertificateGenerator.class);
		when(keysAndCertificateGenerator.generateNodeKeys(caPrivateKey, caCertificateChain)).thenReturn(nodeKeys);

		KeysRepository keysRepository = mock(KeysRepository.class);
		when(keysRepository.loadNodeKeys(PASSWORD_PROTECTION)).thenReturn(nodeKeys);

		manager = new KeysManager(asymmetricService, storesService, keysRepository);
	}

	@Test
	void testGetPlatformCACertificate() {
		manager.activateNodeKeys(nodeKeys);
		X509Certificate platformCACertificate = nodeKeys.caCertificateChain()[nodeKeys.caCertificateChain().length - 1];
		assertEquals(platformCACertificate, manager.getPlatformCACertificate());
	}

	@Test
	void testNodeCACertificate() {
		manager.activateNodeKeys(nodeKeys);
		assertEquals(nodeKeys.caCertificate(), manager.nodeCACertificate());
	}

	@Test
	void testNodeLogEncryptionPublicKey() {
		manager.activateNodeKeys(nodeKeys);
		assertEquals(nodeKeys.logEncryptionPublicKey(), manager.nodeLogEncryptionPublicKey());
	}

	@Test
	void testNodeLogSigningPrivateKey() {
		manager.activateNodeKeys(nodeKeys);
		assertEquals(nodeKeys.logSigningPrivateKey(), manager.nodeLogSigningPrivateKey());
	}

	private static class KeyStoreDouble extends KeyStore {
		public KeyStoreDouble(KeyStoreSpi keyStoreSpi) {
			super(keyStoreSpi, null, "PkCSS12");
		}
	}
}
