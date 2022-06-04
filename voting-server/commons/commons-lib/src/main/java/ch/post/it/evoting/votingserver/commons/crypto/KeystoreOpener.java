/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Generates cryptographic keystore instances
 */
public class KeystoreOpener {

	private final KeyStoreService storesService;

	private final KeystoreRepository keystoreRepository;

	private final PasswordRepository passwordRepository;

	/**
	 * @param storesService      a cryptographic keystore service. Thread safety of the new keystore opener will depend on whether this keystore
	 *                           service is thread safe or not.
	 * @param keystoreRepository a repository used to locate the keystore contents
	 * @param passwordRepository a repository used to locate the keystore password
	 */
	public KeystoreOpener(KeyStoreService storesService, KeystoreRepository keystoreRepository, PasswordRepository passwordRepository) {
		this.storesService = storesService;
		this.keystoreRepository = keystoreRepository;
		this.passwordRepository = passwordRepository;
	}

	/**
	 * Search, open and create an instance for the keystore identified by this tenant id, election event id and alias.
	 *
	 * @param tenant the tenant identifier.
	 * @param eeid   the election event identifier.
	 * @param alias  the keystore alias.
	 * @return a keystore instance.
	 * @throws CryptographicOperationException if an error occurs during the keystore loading process.
	 * @throws ResourceNotFoundException       if the keystore for this tenant id, election event id and alias can not be found.
	 */
	public CryptoAPIExtendedKeyStore openForTenantEEIDAlias(String tenant, String eeid, String alias)
			throws CryptographicOperationException, ResourceNotFoundException {

		String keystoreJson = keystoreRepository.getJsonByTenantEEID(tenant, eeid);
		String password = passwordRepository.getByTenantEEIDAlias(tenant, eeid, alias);

		try (InputStream inputStream = new ByteArrayInputStream(keystoreJson.getBytes(StandardCharsets.UTF_8))) {

			char[] passwordAsCharArray = password.toCharArray();
			KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(passwordAsCharArray);

			return storesService.loadKeyStoreFromJSON(inputStream, passwordProtection);

		} catch (IOException | GeneralCryptoLibException e) {
			throw new CryptographicOperationException("Error opening a keystore: ", e);
		}
	}
}
