/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricServiceFactoryHelper;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.CertificateDataBuilder;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesServiceFactoryHelper;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesServiceFactoryHelper;

/**
 * Mix of cryptographic operations useful for tests
 */
public class CryptoUtils {

	private static final CertificateDataBuilder certificateDataBuilder = new CertificateDataBuilder();

	private static final String COUNTRY = "CH";

	private static final String ORGANISATION = "Swiss Post";

	private static final AsymmetricServiceAPI asymmetricService;

	private static final CertificatesServiceAPI certificatesService;

	private static final PrimitivesServiceAPI primitivesService;

	private static final X509CertificateGenerator certificateGenerator;

	static {
		GenericObjectPoolConfig<?> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
		genericObjectPoolConfig.setMaxTotal(50);
		genericObjectPoolConfig.setMaxIdle(50);
		ServiceFactory<CertificatesServiceAPI> certificatesServiceAPIServiceFactory = CertificatesServiceFactoryHelper
				.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
		ServiceFactory<AsymmetricServiceAPI> asymmetricServiceAPIServiceFactory = AsymmetricServiceFactoryHelper
				.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
		ServiceFactory<PrimitivesServiceAPI> primitivesServiceAPIServiceFactory = PrimitivesServiceFactoryHelper
				.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
		try {
			certificatesService = certificatesServiceAPIServiceFactory.create();
		} catch (GeneralCryptoLibException e) {
			throw new CryptoLibException("Exception while trying to create CertificatesService", e);
		}
		try {
			asymmetricService = asymmetricServiceAPIServiceFactory.create();
		} catch (GeneralCryptoLibException e) {
			throw new CryptoLibException("Exception while trying to create AsymmetricService", e);
		}
		try {
			primitivesService = primitivesServiceAPIServiceFactory.create();
		} catch (GeneralCryptoLibException e) {
			throw new CryptoLibException("Exception while trying to create PrimitivesService", e);
		}

		certificateGenerator = new X509CertificateGenerator(certificatesService, certificateDataBuilder);
	}

	private CryptoUtils() {
	}

	/**
	 * Obtains a key pair for signing purposes
	 *
	 * @return The keypair for signing purposes
	 */
	public static KeyPair getKeyPairForSigning() {
		return asymmetricService.getKeyPairForSigning();
	}

	/**
	 * Obtains a key pair for encryption purposes
	 *
	 * @return The keypair for encryption purposes
	 */
	public static KeyPair getKeyPairForEncryption() {
		return asymmetricService.getKeyPairForEncryption();
	}

	/**
	 * Creates an X509 certificate with provided params
	 *
	 * @param commonName
	 * @param certificateType
	 * @param keyPair
	 * @return The generated certificate
	 * @throws GeneralCryptoLibException
	 */
	public static CryptoAPIX509Certificate createCryptoAPIx509Certificate(final String commonName, CertificateParameters.Type certificateType,
			KeyPair keyPair) throws GeneralCryptoLibException {
		final CertificateParameters certificateParameters = createCertificateParameters(commonName, certificateType);
		return certificateGenerator.generate(certificateParameters, keyPair.getPublic(), keyPair.getPrivate());
	}

	/**
	 * Generates a root CA certificate and keypair and issues a certificate. All the content is stored in a CryptoMaterialContainer instance.
	 *
	 * @param commonName
	 * @return The cryptoMaterialContainer instance with the generated certificates and key pairs
	 * @throws IOException
	 * @throws GeneralCryptoLibException
	 */
	public static CryptoMaterialContainer createRootAndChildCertificates(final String commonName) throws GeneralCryptoLibException {
		final KeyPair rootCAKeyPair = getKeyPairForSigning();
		final KeyPair subjectKeyPair = getKeyPairForSigning();
		final CertificateParameters certificateParametersCA = createCertificateParameters(commonName, CertificateParameters.Type.ROOT);
		final CertificateParameters certificateParameters = createCertificateParameters(commonName, CertificateParameters.Type.SIGN);
		final CryptoAPIX509Certificate rootCACertificate = certificateGenerator
				.generate(certificateParametersCA, rootCAKeyPair.getPublic(), rootCAKeyPair.getPrivate());
		final CryptoAPIX509Certificate subjectCertificate = certificateGenerator
				.generate(certificateParameters, subjectKeyPair.getPublic(), rootCAKeyPair.getPrivate());
		final String rootCACertificatePem = new String(rootCACertificate.getPemEncoded(), StandardCharsets.UTF_8);
		final String subjectCertificatePem = new String(subjectCertificate.getPemEncoded(), StandardCharsets.UTF_8);
		return new CryptoMaterialContainer(rootCAKeyPair, subjectKeyPair, rootCACertificatePem, subjectCertificatePem);
	}

	/**
	 * Concatenates and signs the data with provided private key
	 *
	 * @param privateKey
	 * @param data
	 * @return The signed data
	 * @throws CryptographicOperationException
	 */
	public static byte[] sign(PrivateKey privateKey, String... data) throws CryptographicOperationException {
		byte[] signatureInput = StringUtils.join(data).getBytes(StandardCharsets.UTF_8);
		try {
			return asymmetricService.sign(privateKey, signatureInput);
		} catch (GeneralCryptoLibException e) {
			throw new CryptographicOperationException("Error performing a signature:", e);
		}
	}

	public static CryptoAPIKDFDeriver getKDFDeriver() {
		return primitivesService.getKDFDeriver();
	}

	public static AsymmetricServiceAPI getAsymmetricService() {
		return asymmetricService;
	}

	private static CertificateParameters createCertificateParameters(String commonName, CertificateParameters.Type certificateType) {
		ZonedDateTime now = ZonedDateTime.now();
		CertificateParameters params = new CertificateParameters();
		params.setType(certificateType);
		params.setUserIssuerCn(commonName);
		params.setUserIssuerCountry(COUNTRY);
		params.setUserIssuerOrg(ORGANISATION);
		params.setUserIssuerOrgUnit(ORGANISATION);
		params.setUserNotAfter(now.plusYears(1));
		params.setUserNotBefore(now);
		params.setUserSubjectCn(commonName);
		params.setUserSubjectCountry(COUNTRY);
		params.setUserSubjectOrg(ORGANISATION);
		params.setUserSubjectOrgUnit(ORGANISATION);
		return params;
	}

	/**
	 * Class for storing generated certificates
	 */
	public static class CryptoMaterialContainer {

		private final KeyPair rootCaKeyPair;
		private final KeyPair subjectKeyPair;
		private final String rootCACertificate;
		private final String subjectCertificate;

		CryptoMaterialContainer(KeyPair rootCaKeyPair, KeyPair subjectKeyPair, String rootCACertificate, String subjectCertificate) {
			this.rootCaKeyPair = rootCaKeyPair;
			this.subjectKeyPair = subjectKeyPair;
			this.rootCACertificate = rootCACertificate;
			this.subjectCertificate = subjectCertificate;
		}

		public KeyPair getRootCaKeyPair() {
			return rootCaKeyPair;
		}

		public KeyPair getSubjectKeyPair() {
			return subjectKeyPair;
		}

		public String getRootCACertificate() {
			return rootCACertificate;
		}

		public String getSubjectCertificate() {
			return subjectCertificate;
		}
	}
}
