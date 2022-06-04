/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.service;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import ch.post.it.evoting.cryptolib.CryptolibService;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.csr.CSRGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGeneratorFactory;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class which implements {@link CertificatesServiceAPI}.
 *
 * <p>Instances of this class are immutable.
 */
public class CertificatesService extends CryptolibService implements CertificatesServiceAPI {

	private final CryptoX509CertificateGenerator x509CertificateGenerator;

	/**
	 * Default constructor which initializes all properties to default values. These default values are obtained from the path indicated by {@link
	 * ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper#CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH}.
	 */
	public CertificatesService() {

		final X509CertificateGeneratorFactory x509CertificateGeneratorFactory = new X509CertificateGeneratorFactory();

		x509CertificateGenerator = x509CertificateGeneratorFactory.create();
	}

	private static void validate(final RootCertificateData certificateData, final PrivateKey privateKey) throws GeneralCryptoLibException {

		Validate.notNull(certificateData, "Certificate data");
		if (certificateData.getIssuerDnPrincipal() == null) {
			Validate.notNull(certificateData.getIssuerDn(), "Issuer distinguished name");
		}
		Validate.notNull(certificateData.getSubjectDn(), "Subject distinguished name");
		Validate.notNull(certificateData.getSubjectPublicKey(), "Subject public key");
		Validate.notNull(certificateData.getValidityDates(), "Validity dates object");
		Validate.notNull(privateKey, "Issuer private key");
		Validate.notNullOrEmpty(privateKey.getEncoded(), "Issuer private key content");
	}

	@Override
	public CryptoAPIX509Certificate createRootAuthorityX509Certificate(final RootCertificateData rootCertificateData, final PrivateKey rootPrivateKey)
			throws GeneralCryptoLibException {

		validate(rootCertificateData, rootPrivateKey);

		return x509CertificateGenerator.generate(rootCertificateData, X509CertificateType.CERTIFICATE_AUTHORITY.getExtensions(), rootPrivateKey);
	}

	@Override
	public CryptoAPIX509Certificate createIntermediateAuthorityX509Certificate(final CertificateData certificateData,
			final PrivateKey issuerPrivateKey) throws GeneralCryptoLibException {

		validate(certificateData, issuerPrivateKey);

		return x509CertificateGenerator.generate(certificateData, X509CertificateType.CERTIFICATE_AUTHORITY.getExtensions(), issuerPrivateKey);
	}

	@Override
	public CryptoAPIX509Certificate createSignX509Certificate(final CertificateData certificateData, final PrivateKey issuerPrivateKey)
			throws GeneralCryptoLibException {

		validate(certificateData, issuerPrivateKey);

		return x509CertificateGenerator.generate(certificateData, X509CertificateType.SIGN.getExtensions(), issuerPrivateKey);
	}

	@Override
	public CryptoAPIX509Certificate createEncryptionX509Certificate(final CertificateData certificateData, final PrivateKey issuerPrivateKey)
			throws GeneralCryptoLibException {

		validate(certificateData, issuerPrivateKey);

		return x509CertificateGenerator.generate(certificateData, X509CertificateType.ENCRYPT.getExtensions(), issuerPrivateKey);
	}

	@Override
	public PKCS10CertificationRequest generate(final PublicKey publickey, final PrivateKey privatekey, final X500Principal subject) throws GeneralCryptoLibException {
		try {
			return new CSRGenerator().generate(publickey, privatekey, subject);
		} catch (final OperatorCreationException e) {
			throw new GeneralCryptoLibException(e);
		}
	}

}
