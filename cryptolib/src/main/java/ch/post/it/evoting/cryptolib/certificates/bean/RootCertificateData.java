/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.security.Principal;
import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Contains all necessary certificate data to build a root certificate.
 */
public class RootCertificateData implements CertificatePublicData {

	private PublicKey subjectPublicKey;

	private X509DistinguishedName subjectDn;

	private Principal subjectDnPrincipal;

	private ValidityDates validityDates;

	@Override
	public PublicKey getSubjectPublicKey() {

		return subjectPublicKey;
	}

	public void setSubjectPublicKey(final PublicKey subjectPublicKey) throws GeneralCryptoLibException {

		Validate.notNull(subjectPublicKey, "Subject public key");
		Validate.notNullOrEmpty(subjectPublicKey.getEncoded(), "Subject public key content");

		this.subjectPublicKey = subjectPublicKey;
	}

	@Override
	public X509DistinguishedName getSubjectDn() {

		return subjectDn;
	}

	public void setSubjectDn(final X509DistinguishedName subjectDn) throws GeneralCryptoLibException {

		Validate.notNull(subjectDn, "Subject distinguished name");

		this.subjectDn = subjectDn;
	}

	public void setSubjectDn(final Principal subjectDnPrincipal) throws GeneralCryptoLibException {

		Validate.notNull(subjectDnPrincipal, "Subject distinguished name");

		this.subjectDnPrincipal = subjectDnPrincipal;
	}

	@Override
	public Principal getSubjectDnPrincipal() {

		return subjectDnPrincipal;
	}

	/**
	 * Root Authorities are self-issued. So issuer is the same as the subject.
	 */
	@Override
	public X509DistinguishedName getIssuerDn() {

		return getSubjectDn();
	}

	@Override
	public Principal getIssuerDnPrincipal() {

		return getSubjectDnPrincipal();
	}

	@Override
	public ValidityDates getValidityDates() {

		return validityDates;
	}

	public void setValidityDates(final ValidityDates validityDates) throws GeneralCryptoLibException {

		Validate.notNull(validityDates, "Validity dates object");

		this.validityDates = validityDates;
	}
}
