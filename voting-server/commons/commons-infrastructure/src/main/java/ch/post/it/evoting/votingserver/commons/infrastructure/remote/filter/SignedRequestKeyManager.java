/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.filter;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.EnumMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystoreReader;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystoreReaderFactory;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.NodeIdentifier;
import ch.post.it.evoting.votingserver.commons.infrastructure.config.InfrastructureConfig;
import ch.post.it.evoting.votingserver.commons.infrastructure.exception.OvCommonsInfrastructureException;

public class SignedRequestKeyManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(SignedRequestKeyManager.class);
	private static final EnumMap<NodeIdentifier, PublicKey> originatorPublicKeyMap = new EnumMap<>(NodeIdentifier.class);
	static SignedRequestKeyManager instance = null;
	private static String instanceUUID = "";

	private KeystoreReader keystoreReader;

	/**
	 * Singleton private constructor
	 */
	private SignedRequestKeyManager() {
	}

	/**
	 * Get an instance of this singleton class.
	 *
	 * @return
	 * @throws OvCommonsInfrastructureException
	 */
	public static SignedRequestKeyManager getInstance() throws OvCommonsInfrastructureException {
		validateInstance();
		return instance;
	}

	private static void validateInstance() {
		if (instance == null) {
			instance = new SignedRequestKeyManager();
			instance.keystoreReader = new KeystoreReaderFactory().getInstance();
			instanceUUID = UUID.randomUUID().toString();
			InfrastructureConfig.getInstance();
			instance.readKeysFromKeyStorage();
		}
	}

	public static CryptoX509Certificate getCryptoX509Certificate(final Certificate certificateToValidate) throws GeneralCryptoLibException {

		X509Certificate x509Certificate = (X509Certificate) certificateToValidate;

		CryptoX509Certificate cryptoX509Certificate = null;
		try {
			cryptoX509Certificate = new CryptoX509Certificate(x509Certificate);
		} catch (GeneralCryptoLibException e) {
			throw new GeneralCryptoLibException("An error occurred while trying to create a CryptoX509Certificate from the received certificate", e);
		}

		return cryptoX509Certificate;
	}

	private void readKeysFromKeyStorage() {
		for (NodeIdentifier nodeIdentifier : NodeIdentifier.values()) {
			// If the map does not contain the key for the originator, it tries to retrieve it.
			if (!originatorPublicKeyMap.containsKey(nodeIdentifier)) {
				PublicKey publicKey = this.getPublicKeyFromCertificate(nodeIdentifier);
				if (publicKey != null) {
					originatorPublicKeyMap.put(nodeIdentifier, publicKey);
				}
			}
		}
	}

	private PublicKey getPublicKeyFromCertificate(NodeIdentifier nodeIdentifier) {
		try {
			Certificate certificateToValidate = this.keystoreReader.readSigningCertificate(nodeIdentifier);

			if (certificateToValidate == null) {
				return null;
			} else {
				CryptoAPIX509Certificate cryptoAPIX509Certificate = getCryptoX509Certificate(certificateToValidate);

				return cryptoAPIX509Certificate.getPublicKey();
			}

		} catch (GeneralCryptoLibException e) {
			LOGGER.warn("Error trying to retrieve certificate for " + nodeIdentifier.name(), e);
		}
		return null;
	}

	public PublicKey getPublicKeyFromOriginator(NodeIdentifier nodeIdentifier) {
		return originatorPublicKeyMap.get(nodeIdentifier);
	}

	public void setPublicKeyForOriginator(NodeIdentifier nodeIdentifier, PublicKey publicKey) {
		originatorPublicKeyMap.put(nodeIdentifier, publicKey);
	}

	public String getInstanceUUID() {
		return instanceUUID;
	}
}
