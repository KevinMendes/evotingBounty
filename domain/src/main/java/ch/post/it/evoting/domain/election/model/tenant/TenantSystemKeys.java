/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.tenant;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container class that maintains a map that links tenant IDs to private keys and their related certificate chains. These mappings are stored in
 * memory.
 * <p>
 * For each tenant ID, zero or more private keys and/or certificate chains can be stored.
 */
public class TenantSystemKeys {

	private static final String ENCRYPTION_KEY_ALIAS = "encryptionkey";

	private static final String SIGNING_KEY_ALIAS = "signingkey";

	/**
	 * Map of private keys indexed by type (encryption, signing) and then indexed by tenant.
	 */
	private final Map<String, Map<String, PrivateKey>> privateKeys = new HashMap<>();

	/**
	 * Map of certificate chains indexed by type (encryption, signing) and then indexed by tenant.
	 */
	private final Map<String, Map<String, X509Certificate[]>> certificateChains = new HashMap<>();

	/**
	 * Adds an entry (a tenant ID and a list of private keys) into the map.
	 *
	 * @param tenantId          the tenant ID
	 * @param tenantPrivateKeys the private key(s) associated with the tenant ID.
	 */
	public void setInitialized(final String tenantId, final Map<String, PrivateKey> tenantPrivateKeys) {
		privateKeys.put(tenantId, tenantPrivateKeys);
	}

	/**
	 * Adds an entry (a tenant ID and a list of private keys) into the map.
	 *
	 * @param tenantId                the tenant ID
	 * @param tenantCertificateChains the certificate chain(s) of the public key(s) associated with the tenant ID.
	 */
	public void addCertificateChains(final String tenantId, final Map<String, X509Certificate[]> tenantCertificateChains) {
		certificateChains.put(tenantId, tenantCertificateChains);
	}

	/**
	 * Invalidates the key(s) associated with the specified tenant ID. This results in the mapping associated with this tenant ID being deleted from
	 * the map.
	 *
	 * @param tenantId the tendant ID for which to invalidate the key(s).
	 */
	public void invalidate(final String tenantId) {
		privateKeys.remove(tenantId);
	}

	/**
	 * Determines whether or not there is an entry for the specified tenant ID in the mapping.
	 */
	public boolean getInitialized(final String tenantId) {
		return privateKeys.containsKey(tenantId);
	}

	/**
	 * Gets a tenant's private key from the encryption key pair (or, more precisely, the decryption key).
	 *
	 * @param tenantId the tenant identifier
	 * @return the encryption key for the current service and specified tenant.
	 */
	public PrivateKey getEncryptionPrivateKey(final String tenantId) {
		validateTenant(privateKeys.keySet(), tenantId);

		return privateKeys.get(tenantId).get(ENCRYPTION_KEY_ALIAS);
	}

	/**
	 * Gets a tenant's private key from the signing key pair.
	 *
	 * @param tenantId the tenant identifier
	 * @return the requested private key for the current service and specified tenant.
	 */
	public PrivateKey getSigningPrivateKey(final String tenantId) {
		validateTenant(privateKeys.keySet(), tenantId);

		return privateKeys.get(tenantId).get(SIGNING_KEY_ALIAS);
	}

	public X509Certificate[] getSigningCertificateChain(final String tenantId) {
		validateTenant(certificateChains.keySet(), tenantId);

		return certificateChains.get(tenantId).get(SIGNING_KEY_ALIAS);
	}

	/**
	 * Ensure that a set of keys contains the tenant ID.
	 *
	 * @param keys     the keys to check, typically passed as <@code mapToCheck.keySet()>
	 * @param tenantId the tenant to look for
	 */
	private void validateTenant(final Set<String> keys, final String tenantId) {
		if (!keys.contains(tenantId)) {
			throw new IllegalArgumentException("No keys found for tenant " + tenantId);
		}
	}
}
