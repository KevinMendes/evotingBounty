/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Service used to perform cryptographic signatures for a specific kind of objects (e.g., for ballot box).
 */
public class SignatureForObjectService {

	private final AsymmetricServiceAPI asymmetricService;

	private final PrivateKeyForObjectRepository privateKeyRepository;

	/**
	 * Constructor of the object.
	 *
	 * @param asymmetricService    - the asymmetric service instance.
	 * @param privateKeyRepository - the private key repository instance.
	 */
	public SignatureForObjectService(final AsymmetricServiceAPI asymmetricService, final PrivateKeyForObjectRepository privateKeyRepository) {
		this.asymmetricService = asymmetricService;
		this.privateKeyRepository = privateKeyRepository;
	}

	/**
	 * Sign the input data using the private key identified by this tenant, election event and alias.
	 *
	 * @param tenantId the identifier of the tenant.
	 * @param eeId     the identifier of the election event.
	 * @param objectId the specific object identifier.
	 * @param alias    the alias of the private key that will be used to perform the signature
	 * @param data     the data to be signed. It will be serialized for signature using the input data formatter provided on this instance
	 *                 constructor.
	 * @return the signature over the input data
	 * @throws CryptographicOperationException if an error occurs during the signature operation or recovering the private key
	 * @throws ResourceNotFoundException       if the private key identified by this tenant, election event id and alias can not be found
	 */
	public byte[] sign(final String tenantId, final String eeId, final String objectId, final String alias, final String... data)
			throws CryptographicOperationException, ResourceNotFoundException {

		final PrivateKey privateKey = privateKeyRepository.findByTenantEEIDObjectIdAlias(tenantId, eeId, objectId, alias);

		final byte[] signatureInput = StringUtils.join(data).getBytes(StandardCharsets.UTF_8);

		try {
			return asymmetricService.sign(privateKey, signatureInput);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptographicOperationException("Error performing a signature:", e);
		}
	}

	/**
	 * Gets the public key associated to a given alias within a certificate chain.
	 *
	 * @param certChain - chain of certificates to check
	 * @param alias     - alias of the desired public key
	 * @return a Public Key
	 * @throws GeneralCryptoLibException
	 */
	public PublicKey getPublicKeyByAliasInCertificateChain(final Certificate[] certChain, final String alias) throws GeneralCryptoLibException {

		PublicKey result = null;
		for (final Certificate c : certChain) {
			final CryptoX509Certificate wrappedCertificate = new CryptoX509Certificate((X509Certificate) c);
			final X509DistinguishedName subject = wrappedCertificate.getSubjectDn();
			final String name = subject.getCommonName();
			if (name.equals(alias)) {
				result = c.getPublicKey();
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the certificate chain as String associated to a given alias.
	 *
	 * @param certChain - A chain of certificates to be transformed.
	 * @return a Map<String> representing the chain of certificates
	 * @throws GeneralCryptoLibException
	 * @throws CryptographicOperationException
	 */
	public Map<String, String> getCertificatesAsMap(final Certificate[] certChain) throws GeneralCryptoLibException, CryptographicOperationException {

		final Map<String, String> certChainMap = new HashMap<>();
		try {
			for (final Certificate certificate : certChain) {
				final String certString = String.format("-----BEGIN CERTIFICATE-----%s-----END CERTIFICATE-----",
						Base64.getEncoder().encodeToString(certificate.getEncoded()));

				final CryptoX509Certificate wrappedCertificate = new CryptoX509Certificate((X509Certificate) certificate);
				final X509DistinguishedName subject = wrappedCertificate.getSubjectDn();
				final String name = subject.getCommonName();

				certChainMap.put(name, certString);

			}

			return certChainMap;
		} catch (final CertificateException e) {
			throw new CryptographicOperationException("An error occured encoding a certificate object:", e);
		}
	}

}
