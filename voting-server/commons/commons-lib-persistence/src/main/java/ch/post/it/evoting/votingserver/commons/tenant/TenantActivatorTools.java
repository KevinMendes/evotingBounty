/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.tenant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.domain.election.model.tenant.TenantActivationData;

/**
 * Provides a set of utility methods that are used during the activation of tenants.
 */
public class TenantActivatorTools {

	private static final Logger LOGGER = LoggerFactory.getLogger(TenantActivatorTools.class);

	private static final String UNDERSCORE = "_";

	private static final String WILDCARD_CHARACTER = "*";

	private static final String FILE_NAME_SUFFIX = "tenant";

	private static final String PROPERTIES_FILE_EXTENSION = ".properties";

	private static CertificateFactory certificateFactory;

	static {
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOGGER.warn("Error trying to get a CertificateFactory instance.", e);
		}
	}

	/**
	 * Constructs a list of the tenants, based on the password files that exist in the specified directory.
	 *
	 * @param passwordsFilePath the passwords file path
	 * @return the list tenants from password files
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<TenantActivationData> getListTenantsFromPasswordFiles(final String pathOfDirectoryContainingPasswordFiles, final String serviceName)
			throws IOException {

		validateDirectoryPath(pathOfDirectoryContainingPasswordFiles);

		LOGGER.info(serviceName + " - building list of tenants from files found in: " + pathOfDirectoryContainingPasswordFiles);

		String nameFilter = FILE_NAME_SUFFIX + UNDERSCORE + serviceName + UNDERSCORE + WILDCARD_CHARACTER + PROPERTIES_FILE_EXTENSION;

		Collection<File> allTenantPasswordFiles = FileUtils
				.listFiles(new File(pathOfDirectoryContainingPasswordFiles), new WildcardFileFilter(nameFilter), null);

		List<TenantActivationData> data = new ArrayList<>();

		for (File tenantPasswordFiles : allTenantPasswordFiles) {

			LOGGER.info(serviceName + " - processing file: " + tenantPasswordFiles);

			Properties properties = loadPropertiesFromFile(tenantPasswordFiles.getAbsolutePath());

			Set<String> keys = properties.stringPropertyNames();

			for (String key : keys) {

				String value = properties.getProperty(key);
				String[] idAndContext = key.split(UNDERSCORE);
				String id = idAndContext[0];

				TenantActivationData tenantActivationData = new TenantActivationData();
				tenantActivationData.setTenantID(id);
				tenantActivationData.setSystemKeystorePassword(value);
				data.add(tenantActivationData);
			}
		}

		return data;
	}

	/**
	 * Gets a password from the specified file, based on the tenantID and service name.
	 *
	 * @param passwordsFilePath the passwords file path
	 * @param tenantID          the tenant id
	 * @param serviceName       the service name
	 * @return the password from file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public char[] getPasswordFromFile(final String passwordsFilePath, final String tenantID, final String serviceName) throws IOException {

		validateDirectoryPath(passwordsFilePath);

		String fileName = FILE_NAME_SUFFIX + UNDERSCORE + serviceName + UNDERSCORE + tenantID + PROPERTIES_FILE_EXTENSION;
		String propertiesKey = tenantID + UNDERSCORE + serviceName;

		Properties properties = loadPropertiesFromFile(Paths.get(passwordsFilePath, fileName).toAbsolutePath().toString());
		return properties.getProperty(propertiesKey).toCharArray();
	}

	/**
	 * Gets the private keys from the keystore.
	 *
	 * @param tenantKeystore the tenant keystore.
	 * @param password       the password to be used to open the keystore.
	 * @return the private key from keystore.
	 */
	public Map<String, PrivateKey> getPrivateKeys(final CryptoAPIExtendedKeyStore tenantKeystore, final char[] password) {

		List<String> aliases = tenantKeystore.getPrivateKeyAliases();
		final Map<String, PrivateKey> privateKeys = new HashMap<>();
		aliases.forEach(alias -> {
			try {
				privateKeys.put(alias, tenantKeystore.getPrivateKeyEntry(alias, password));
			} catch (GeneralCryptoLibException e) {
				String errorMsg = "Could not open the keystore using the received password";
				throw new IllegalArgumentException(errorMsg, e);
			}
		});

		return privateKeys;
	}

	/**
	 * Load properties from a file.
	 *
	 * @param passwordsFilePath the passwords file path.
	 * @return the properties.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Properties loadPropertiesFromFile(final String passwordsFilePath) throws IOException {
		validateFilePath(passwordsFilePath);

		Properties prop = new Properties();
		try (FileInputStream fis = new FileInputStream(passwordsFilePath)) {
			prop.load(fis);
		}
		return prop;
	}

	/**
	 * Attempt to delete the passwords file related to the specified tenant.
	 *
	 * @param pathDirectoryContainingPasswordFiles the directory from which files should be deleted.
	 * @return true if successful, false otherwise.
	 */
	public boolean attemptToDeletePasswordsFiles(final String pathDirectoryContainingPasswordFiles, final String serviceName, final String tenantID) {

		String tenantKeystorePasswordFileName = FILE_NAME_SUFFIX + UNDERSCORE + serviceName + UNDERSCORE + tenantID + PROPERTIES_FILE_EXTENSION;
		Path pathAndNameOfFileToDelete = Paths.get(pathDirectoryContainingPasswordFiles, tenantKeystorePasswordFileName);

		if (!Files.exists(pathAndNameOfFileToDelete)) {
			return true;
		}

		try {
			Files.delete(pathAndNameOfFileToDelete);
		} catch (IOException e) {
			LOGGER.warn(serviceName + " - non-fatal issue - failed to delete file: " + pathAndNameOfFileToDelete.toAbsolutePath().toString(), e);
			return false;
		}
		return true;
	}

	private void validateDirectoryPath(final String directoryPath) {

		if (directoryPath == null || directoryPath.isEmpty()) {
			String errorMsg = "The directory path cannot be null or empty";
			LOGGER.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	private void validateFilePath(final String filePath) {

		if (filePath == null || filePath.isEmpty()) {
			String errorMsg = "The file path cannot be null or empty";
			LOGGER.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		if (!Files.isReadable(Paths.get(filePath))) {
			String errorMsg = "The path: " + filePath + " is not readable";
			LOGGER.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	/**
	 * Extract all certificate chains from a tenant keystore.
	 *
	 * @param tenantKeystore the tenant keystore holding the certificate chains
	 * @return the extracted certificate chains
	 * @throws GeneralCryptoLibException
	 * @throws CertificateException
	 */
	public Map<String, X509Certificate[]> getCertificateChains(CryptoAPIExtendedKeyStore tenantKeystore)
			throws GeneralCryptoLibException, CertificateException {

		if (certificateFactory != null) {
			Map<String, X509Certificate[]> tenantCertificateChains = new HashMap<>();
			// Find each of the certificate chains
			for (String alias : tenantKeystore.getPrivateKeyAliases()) {
				Collection<X509Certificate> convertedCertificateChain = new ArrayList<>();
				// Convert the certificates to X509 certs.
				for (Certificate certificate : tenantKeystore.getCertificateChain(alias)) {
					ByteArrayInputStream bais = new ByteArrayInputStream(certificate.getEncoded());
					// Add the converted X509 certificate to a list.
					convertedCertificateChain.add((X509Certificate) certificateFactory.generateCertificate(bais));
				}
				// Store the list of converted certificates with their alias.
				tenantCertificateChains.put(alias, convertedCertificateChain.toArray(new X509Certificate[convertedCertificateChain.size()]));
			}

			return tenantCertificateChains;
		} else {
			throw new CertificateException("Error trying to get the certificate chains. The CertificateFactory is null.");
		}
	}
}
