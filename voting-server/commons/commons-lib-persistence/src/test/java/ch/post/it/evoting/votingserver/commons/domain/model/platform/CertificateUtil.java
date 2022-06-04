/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.platform;

import java.security.KeyPair;
import java.util.Date;

import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;

public class CertificateUtil {

	public static CryptoAPIX509Certificate createRootSelfSignedCertificate(String commonName, String orgName, String orgUnit, String country,
			Date validFrom, Date validTo, KeyPair certKeyPair) throws GeneralCryptoLibException {
		CertificatesServiceAPI certificateGenerator = new CertificatesService();
		RootCertificateData rootCertificateData = new RootCertificateData();
		X509DistinguishedName self = new X509DistinguishedName.Builder(commonName, country).addOrganization(orgName).addOrganizationalUnit(orgUnit)
				.build();
		rootCertificateData.setSubjectDn(self);
		rootCertificateData.setSubjectPublicKey(certKeyPair.getPublic());
		rootCertificateData.setValidityDates(new ValidityDates(validFrom, validTo));
		return certificateGenerator.createRootAuthorityX509Certificate(rootCertificateData, certKeyPair.getPrivate());
	}

	public static CryptoAPIX509Certificate createIntermediateCertificate(String commonName, String orgName, String orgUnit, String country,
			Date validFrom, Date validTo, KeyPair certKeyPair, KeyPair issuerKeyPair, X509DistinguishedName x509DistinguishedName)
			throws GeneralCryptoLibException {
		CertificatesServiceAPI certificateGenerator = new CertificatesService();
		CertificateData certificateData = new CertificateData();
		X509DistinguishedName self = new X509DistinguishedName.Builder(commonName, country).addOrganization(orgName).addOrganizationalUnit(orgUnit)
				.build();
		certificateData.setSubjectDn(self);
		certificateData.setIssuerDn(x509DistinguishedName);
		certificateData.setSubjectPublicKey(certKeyPair.getPublic());
		certificateData.setValidityDates(new ValidityDates(validFrom, validTo));
		return certificateGenerator.createIntermediateAuthorityX509Certificate(certificateData, issuerKeyPair.getPrivate());
	}

}
