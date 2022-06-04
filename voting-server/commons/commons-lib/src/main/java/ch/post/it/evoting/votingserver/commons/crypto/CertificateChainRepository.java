/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

import java.security.cert.Certificate;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Repository for certificate chain instances
 */
public class CertificateChainRepository {

	private static final String EMPTY_ID = "";
	private final KeystoreForObjectOpener keystoreOpener;

	/**
	 * Create a keystore based certificate chain repository.
	 *
	 * @param keystoreOpener the keystore opener that will be used to explore keystores for the required certificate chains
	 */
	public CertificateChainRepository(KeystoreForObjectOpener keystoreOpener) {
		this.keystoreOpener = keystoreOpener;
	}

	/**
	 * Find a certificate chain identified by the provided tenant id, election event id and alias.
	 *
	 * @param tenant the tenant identifier.
	 * @param eeid   the election event identifier.
	 * @param alias  the certificate chain alias.
	 * @return a certificate array containing the certificate chain with the leaf certificate as first element.
	 * @throws CryptographicOperationException if an error occurs during the certificate chain location or retrieval.
	 * @throws ResourceNotFoundException       if the certificate chain source can not be found.
	 */
	public Certificate[] findByTenantEEIDAlias(String tenant, String eeid, String alias)
			throws CryptographicOperationException, ResourceNotFoundException {
		return findByTenantEEIDAlias(tenant, eeid, EMPTY_ID, alias);
	}

	/**
	 * Find a certificate chain identified by the provided tenant id, election event id and alias.
	 *
	 * @param tenant   the tenant identifier.
	 * @param eeId     the election event identifier.
	 * @param objectId the object id identified
	 * @param alias    the certificate chain alias.
	 * @return a certificate array containing the certificate chain with the leaf certificate as first element.
	 * @throws CryptographicOperationException if an error occurs during the certificate chain location or retrieval.
	 * @throws ResourceNotFoundException       if the certificate chain source can not be found.
	 */
	public Certificate[] findByTenantEEIDAlias(String tenant, String eeId, String objectId, String alias)
			throws CryptographicOperationException, ResourceNotFoundException {
		CryptoAPIExtendedKeyStore keystore = keystoreOpener.openForTenantEEIDObjectId(tenant, eeId, objectId);

		Certificate[] certificates;
		try {
			certificates = keystore.getCertificateChain(alias);
		} catch (GeneralCryptoLibException e) {
			throw new CryptographicOperationException("Can not recover certificate chain from keystore:", e);
		}
		return certificates;
	}
}
