/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.factory.builders;

import java.security.PublicKey;
import java.util.Date;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;

/**
 * Builds the object {@link CertificateData}
 */
public class CertificateDataBuilder {

	/**
	 * Builds the object {@link CertificateData}.
	 *
	 * @param publicKey
	 * @param certificateParameters
	 * @return
	 * @throws GeneralCryptoLibException
	 */
	public CertificateData build(final PublicKey publicKey, final CertificateParameters certificateParameters) throws GeneralCryptoLibException {

		final CertificateData certificateData = new CertificateData();

		certificateData.setSubjectPublicKey(publicKey);
		certificateData.setIssuerDn(createUserIssuerDistinguishedName(certificateParameters));
		certificateData.setSubjectDn(createUserSubjectDistinguishedName(certificateParameters));
		certificateData.setValidityDates(createUserValidityDates(certificateParameters));

		return certificateData;
	}

	/**
	 * @param certificateParameters
	 * @throws GeneralCryptoLibException
	 */
	private X509DistinguishedName createUserSubjectDistinguishedName(final CertificateParameters certificateParameters)
			throws GeneralCryptoLibException {
		return new X509DistinguishedName.Builder(certificateParameters.getUserSubjectCn(), certificateParameters.getUserSubjectCountry())
				.addOrganizationalUnit(certificateParameters.getUserSubjectOrgUnit()).addOrganization(certificateParameters.getUserSubjectOrg())
				.build();

	}

	/**
	 * @param certificateParameters
	 * @throws GeneralCryptoLibException
	 */
	private X509DistinguishedName createUserIssuerDistinguishedName(final CertificateParameters certificateParameters)
			throws GeneralCryptoLibException {
		if (certificateParameters.getType() == CertificateParameters.Type.ROOT) {
			return createUserSubjectDistinguishedName(certificateParameters);
		} else {
			return new X509DistinguishedName.Builder(certificateParameters.getUserIssuerCn(), certificateParameters.getUserIssuerCountry())
					.addOrganizationalUnit(certificateParameters.getUserIssuerOrgUnit()).addOrganization(certificateParameters.getUserIssuerOrg())
					.build();
		}

	}

	/**
	 * @throws GeneralCryptoLibException
	 */
	private ValidityDates createUserValidityDates(final CertificateParameters certificateParameters) throws GeneralCryptoLibException {

		final Date userNotBefore = Date.from(certificateParameters.getUserNotBefore().toInstant());
		final Date userNotAfter = Date.from(certificateParameters.getUserNotAfter().toInstant());

		return new ValidityDates(userNotBefore, userNotAfter);
	}
}
