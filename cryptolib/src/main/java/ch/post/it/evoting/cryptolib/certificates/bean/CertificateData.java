/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.security.Principal;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class which contains the basic certificate data.
 */
public class CertificateData extends RootCertificateData implements CertificatePublicData {

	private X509DistinguishedName issuerDn;

	private Principal issuerDnPrincipal;

	@Override
	public X509DistinguishedName getIssuerDn() {
		return issuerDn;
	}

	/**
	 * Assigns the issue to the certificate.
	 *
	 * @param issuerDn the issuer {@link X509DistinguishedName} to be assigned to the certificate.
	 * @throws GeneralCryptoLibException
	 */
	public void setIssuerDn(final X509DistinguishedName issuerDn) throws GeneralCryptoLibException {

		Validate.notNull(issuerDn, "Issuer distinguished name");

		this.issuerDn = issuerDn;
	}

	/**
	 * Assigns the issue to the certificate.
	 *
	 * @param issuerDnPrincipal the issuer {@link Principal} to be assigned to the certificate.
	 * @throws GeneralCryptoLibException
	 */
	public void setIssuerDn(final Principal issuerDnPrincipal) throws GeneralCryptoLibException {

		Validate.notNull(issuerDnPrincipal, "Issuer Principal");

		this.issuerDnPrincipal = issuerDnPrincipal;
	}

	@Override
	public Principal getIssuerDnPrincipal() {
		return issuerDnPrincipal;
	}
}
