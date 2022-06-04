/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.domain.election.model.tenant.TenantActivationData;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CryptoException;

class TenantActivatorToolsTest {

	private final TenantActivatorTools tenantActivatorTools = new TenantActivatorTools();

	@Test
	void testGetListTenantsFromPasswordFiles() throws IOException, URISyntaxException {
		final String path = getClass().getClassLoader().getResource("tenantDirTwoValidTenants").toURI().getPath();
		System.out.println(path);

		final List<TenantActivationData> listTenants = tenantActivatorTools.getListTenantsFromPasswordFiles(path, "AU");

		final String errorMsg = "Failed to find the expected number of tenants";
		assertEquals(2, listTenants.size(), errorMsg);
	}

	@Test
	void testGetListTenantsFromEmptyDirectory() throws IOException, URISyntaxException {
		final String path = getClass().getClassLoader().getResource("properties").toURI().getPath();
		System.out.println(path);

		final List<TenantActivationData> listTenants = tenantActivatorTools.getListTenantsFromPasswordFiles(path, "AU");

		final String errorMsg = "Expected there to be zero tenants found";
		assertEquals(0, listTenants.size(), errorMsg);
	}

	@Test
	void testGetPasswordFromFile() throws URISyntaxException, IOException {
		final Path path = Paths.get(getClass().getClassLoader().getResource("tenantDirTwoValidTenants").toURI());
		final String tenantID = "100";
		final String serviceName = "AU";
		final int expectedNumberOfCharacters = 26;

		final char[] password = tenantActivatorTools.getPasswordFromFile(path.toAbsolutePath().toString(), tenantID, serviceName);

		final String errorMsg = "Obtained password does not contain the expected number of characters";
		assertEquals(expectedNumberOfCharacters, password.length, errorMsg);
	}

	@Test
	void testGetPrivateKeyFromKeystore() throws CryptoException, GeneralCryptoLibException {
		final String pathSystemTanantKeystore = "/keystore/100_AU.sks";
		final String systemTenantKeystorePassword = "RBW6WD4IPTA73VT4KASLILIIFJ";

		final CryptoAPIExtendedKeyStore tenantKeystore = new ExtendedKeyStoreService().loadKeyStore(readKeystoreAsStream(pathSystemTanantKeystore),
				systemTenantKeystorePassword.toCharArray());

		final PrivateKey privateKey = tenantActivatorTools.getPrivateKeys(tenantKeystore, systemTenantKeystorePassword.toCharArray())
				.get("encryptionkey");

		final String errorMsg = "Failed to extract private key from keystore";
		assertEquals("RSA", privateKey.getAlgorithm(), errorMsg);
	}

	@Test
	void testLoadPropertiesFromFile() throws URISyntaxException, IOException {
		final Path path = Paths.get(getClass().getClassLoader().getResource("properties/test.properties").toURI());

		final Properties props = tenantActivatorTools.loadPropertiesFromFile(path.toAbsolutePath().toString());

		assertEquals(3, props.keySet().size());
		assertEquals("value1", props.getProperty("key1"));
		assertEquals("value2", props.getProperty("key2"));
		assertEquals("value3", props.getProperty("key3"));
	}

	@Test
	void testAttemptToDeletePasswordsFiles() throws URISyntaxException, IOException {
		final String testDirectoryName = "testDir";
		final Path testDirectoryAsPath = Paths.get(testDirectoryName);
		final File testDirectoryAsFile = testDirectoryAsPath.toFile();

		Files.createDirectories(testDirectoryAsPath);

		final File testFileAsFile = new File(
				getClass().getClassLoader().getResource("tenantDirTwoValidTenants/tenant_AU_100.properties").toURI().getPath());
		final File destinationFileAsFile = Paths.get(testDirectoryAsPath.toAbsolutePath().toString(), "tenant_AU_100.properties").toFile();

		FileUtils.copyFile(testFileAsFile, destinationFileAsFile);

		final Collection<File> filesBeforeClean = FileUtils.listFiles(testDirectoryAsFile, null, false);
		String errorMsg = "Expected that there would be 1 file";
		assertEquals(1, filesBeforeClean.size(), errorMsg);

		tenantActivatorTools.attemptToDeletePasswordsFiles(testDirectoryAsPath.toAbsolutePath().toString(), "AU", "100");

		final Collection<File> filesAfterClean = FileUtils.listFiles(testDirectoryAsFile, null, false);
		errorMsg = "Expected that there would be 0 files";
		assertEquals(0, filesAfterClean.size(), errorMsg);
	}

	@Test
	void testGetCertificateChainFromKeystore() throws CryptoException, GeneralCryptoLibException, CertificateException {

		final String pathSystemTanantKeystore = "/keystore/100_AU.sks";
		final String systemTenantKeystorePassword = "RBW6WD4IPTA73VT4KASLILIIFJ";

		final CryptoAPIExtendedKeyStore tenantKeystore = new ExtendedKeyStoreService().loadKeyStore(readKeystoreAsStream(pathSystemTanantKeystore),
				systemTenantKeystorePassword.toCharArray());

		final X509Certificate[] certificateChain = tenantActivatorTools.getCertificateChains(tenantKeystore).get("encryptionkey");

		assertEquals(2, certificateChain.length);
	}

	private InputStream readKeystoreAsStream(final String path) {
		return getClass().getResourceAsStream(path);
	}
}
