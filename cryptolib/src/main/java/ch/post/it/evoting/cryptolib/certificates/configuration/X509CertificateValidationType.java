/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.configuration;

/**
 * Enum which defines the X509 certificate validation types.
 */
public enum X509CertificateValidationType {
	DATE,

	SUBJECT,

	ISSUER,

	KEY_TYPE,

	SIGNATURE
}
