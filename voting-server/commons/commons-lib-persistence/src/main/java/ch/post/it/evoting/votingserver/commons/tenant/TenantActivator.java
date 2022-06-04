/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.tenant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.domain.election.model.tenant.TenantActivationData;
import ch.post.it.evoting.domain.election.model.tenant.TenantSystemKeys;
import ch.post.it.evoting.votingserver.commons.application.TenantActivatorKeystoreException;
import ch.post.it.evoting.votingserver.commons.application.TenantActivatorPasswordException;
import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreEntity;
import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreRepository;

/**
 * Class that can be used for activating a tenant in a service.
 */
public final class TenantActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TenantActivator.class);

	private static final String NAME_OF_PROPERTY_THAT_SPECIFIES_TENANT_PW_DIRECTORY = "tenantpasswordsdirectory";

	private static final String KEYTYPE = X509CertificateType.ENCRYPT.name();

	private final TenantKeystoreRepository tenantKeystoreRepository;

	private final TenantSystemKeys tenantSystemKeys;

	private final String serviceName;

	private final TenantActivatorTools tenantActivatorTools = new TenantActivatorTools();

	/**
	 * Instantiates a new TenantActivator.
	 *
	 * @param tenantKeystoreRepo the repository that contains tenant keystores.
	 * @param tenantSysKeys      the tenant system keys.
	 * @param serviceName        the service name.
	 */
	public TenantActivator(final TenantKeystoreRepository tenantKeystoreRepo, final TenantSystemKeys tenantSysKeys, final String serviceName) {

		tenantKeystoreRepository = tenantKeystoreRepo;
		tenantSystemKeys = tenantSysKeys;
		this.serviceName = serviceName;
	}

	/**
	 * Activate a tenant using received keystore.
	 * <p>
	 * This method could be called when a tenant system keystore for the tenant with the specified tenantID has been received. This method will
	 * attempt to find the corresponding keystore password in a file that should exist in the directory specified by the property
	 * "tenantpasswordsdirectory".
	 * <p>
	 * This method will delete tenant system keystore password files that exist in the directory defined by the property "tenantpasswordsdirectory".
	 *
	 * @param tenantID the tenant id.
	 * @param keystore the keystore.
	 * @return true if successful, false otherwise.
	 */
	public boolean activateUsingReceivedKeystore(final String tenantID, final String keystore) {

		LOGGER.info("{} - activating tenantID: {}", serviceName, tenantID);

		char[] password;
		try {
			password = tenantActivatorTools.getPasswordFromFile(getTenantPasswordsDirectoryProperty(), tenantID, serviceName);
		} catch (IOException e) {
			String errorMsg = serviceName + " - failed to get tenant password from file for tenant: " + tenantID;
			LOGGER.error(errorMsg, e);
			return false;
		}

		LOGGER.info("{} - obtained tenant keystore password for tenant: {}", serviceName, tenantID);

		boolean result = populateCacheFromKeystore(tenantID, keystore, password);

		deleteKeystorePasswordFileRelatedToThisTenant(tenantID);

		return result;
	}

	/**
	 * Activate tenants from DB and files.
	 * <p>
	 * This method could be called at the moment that the application has been started up. It searches for tenant keystore passwords in files and the
	 * actual keystores in the DB. It will attempt to activate all of the tenants for which it can find passwords and keystores.
	 * <p>
	 * This method will delete tenant system keystore password files that exist in the directory defined by the property "tenantpasswordsdirectory".
	 *
	 * @return a list of tenants that have been successfully activated.
	 */
	public List<String> activateTenantsFromDbAndFiles() {

		LOGGER.info("{} - attempting to activate tenants from DB and files", serviceName);

		List<String> listSuccessfullyActivatedTenants = new ArrayList<>();

		List<TenantActivationData> tenantData;
		try {
			tenantData = tenantActivatorTools.getListTenantsFromPasswordFiles(getTenantPasswordsDirectoryProperty(), serviceName);
		} catch (IOException e) {
			String errorMsg = serviceName + " - failed to construct list of tenants from tenant keystore password files";
			LOGGER.error(errorMsg, e);
			return listSuccessfullyActivatedTenants;
		}

		int numTenants = tenantData.size();
		LOGGER.info("{} - number of tenants found in keystore password files: {}", serviceName, numTenants);
		if (numTenants < 1) {
			return listSuccessfullyActivatedTenants;
		}

		for (TenantActivationData tenantActivationData : tenantData) {

			String tenantID = tenantActivationData.getTenantID();

			LOGGER.info("{} - processing tenant: {}", serviceName, tenantID);

			if (tenantKeystoreRepository.checkIfKeystoreExists(tenantID, KEYTYPE)) {

				LOGGER.info("{} - keystore for this tenant exists in DB, will attempt to activate from DB", serviceName);

				if (activateFromDB(tenantActivationData)) {
					listSuccessfullyActivatedTenants.add(tenantID);
				} else {
					LOGGER.error("{} - failed to activate tenant: {}", serviceName, tenantID);
				}
			} else {

				LOGGER.warn("{} - keystore for this tenant not found in DB", serviceName);
			}

			deleteKeystorePasswordFileRelatedToThisTenant(tenantActivationData.getTenantID());
		}

		LOGGER.info("{} - number of tenants activated: {}", serviceName, listSuccessfullyActivatedTenants.size());

		return listSuccessfullyActivatedTenants;
	}

	/**
	 * Activate a particular tenant using keystores from the DB, and using the passwords contained within the received tenant data.
	 * <p>
	 * This method can be called when a particular tenant should be activated without restarting the system.
	 *
	 * @param tenantActivationData the tenant activation data.
	 * @return true if successful, false otherwise.
	 */
	public boolean activateFromDB(final TenantActivationData tenantActivationData) {

		String tenantID = tenantActivationData.getTenantID();

		LOGGER.info("{} - activating tenantID: {}", serviceName, tenantID);

		TenantKeystoreEntity tenantKeystoreEntity = getTenantKeystoreEntity(tenantID);

		LOGGER.info("{} - obtained tenant keystore entity from DB", serviceName);

		return populateCacheFromKeystore(tenantID, tenantKeystoreEntity.getKeystoreContent(),
				tenantActivationData.getSystemKeystorePassword().toCharArray());
	}

	private void deleteKeystorePasswordFileRelatedToThisTenant(final String tenantID) {

		if (tenantActivatorTools.attemptToDeletePasswordsFiles(getTenantPasswordsDirectoryProperty(), serviceName, tenantID)) {
			LOGGER.info("{} - deleted tenant keystore password file related to tenant: {}", serviceName, tenantID);
		} else {
			LOGGER.warn("{} - non-fatal issue - failed to delete tenant keystore password file related to tenant: {}", serviceName, tenantID);
		}
	}

	/**
	 * Get the relevant contents of a keystore and place them in memory for convenient access.
	 *
	 * @param tenantID         the identifier of the tenant the keystore belongs to
	 * @param keystoreContents the Base64-encoded contents of the keystore
	 * @param password         the password to open the keystore
	 * @return
	 */
	private boolean populateCacheFromKeystore(final String tenantID, final String keystoreContents, final char[] password) {

		boolean result;
		try {
			// Open the keystore.
			CryptoAPIExtendedKeyStore tenantKeystore = new ExtendedKeyStoreService()
					.loadKeyStore(new ByteArrayInputStream(Base64.getDecoder().decode(keystoreContents)), password);

			LOGGER.info("{} - obtained tenant keystore", serviceName);

			// Cache the tenant's private keys.
			tenantSystemKeys.setInitialized(tenantID, tenantActivatorTools.getPrivateKeys(tenantKeystore, password));

			Arrays.fill(password, '\u0000');
			LOGGER.info("{} - obtained tenant private key", serviceName);

			// Cache the tenant's certificate chains.
			tenantSystemKeys.addCertificateChains(tenantID, tenantActivatorTools.getCertificateChains(tenantKeystore));

			LOGGER.info("{} - tenant: {} activated successfully", serviceName, tenantID);
			result = true;
		} catch (GeneralCryptoLibException | CertificateException e) {
			LOGGER.error("The tenant key store could not be loaded into the cache", e);
			result = false;
		}

		return result;
	}

	private String getTenantPasswordsDirectoryProperty() {

		String passwordsFilePath = System.getenv(NAME_OF_PROPERTY_THAT_SPECIFIES_TENANT_PW_DIRECTORY);

		if (passwordsFilePath == null) {
			String errorMsg = serviceName + " - failed to obtain passwords files directory property";
			LOGGER.error(errorMsg);
			throw new TenantActivatorPasswordException(errorMsg);
		}

		return passwordsFilePath;
	}

	private TenantKeystoreEntity getTenantKeystoreEntity(final String tenantID) {

		LOGGER.info("{} - searching in DB for: {}, {}", serviceName, tenantID, KEYTYPE);
		TenantKeystoreEntity entity;
		try {
			entity = tenantKeystoreRepository.getByTenantAndType(tenantID, KEYTYPE);
		} catch (Exception e) {
			throw new TenantActivatorKeystoreException(serviceName + " - failed to get privatekey from keystore", e);
		}

		return entity;
	}
}
