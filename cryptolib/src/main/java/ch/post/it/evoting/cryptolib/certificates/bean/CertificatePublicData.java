/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.security.Principal;
import java.security.PublicKey;

/**
 * Provides public data that is used to build a certificate.
 */
public interface CertificatePublicData {

	PublicKey getSubjectPublicKey();

	X509DistinguishedName getSubjectDn();

	X509DistinguishedName getIssuerDn();

	Principal getSubjectDnPrincipal();

	Principal getIssuerDnPrincipal();

	ValidityDates getValidityDates();
}
