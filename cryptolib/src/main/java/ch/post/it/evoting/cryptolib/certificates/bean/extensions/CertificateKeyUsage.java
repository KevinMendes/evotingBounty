/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean.extensions;

/**
 * Enum which defines the usages of the key usage certificate extension.
 */
public enum CertificateKeyUsage {
	DIGITAL_SIGNATURE,

	NON_REPUDIATION,

	KEY_ENCIPHERMENT,

	DATA_ENCIPHERMENT,

	KEY_AGREEMENT,

	KEY_CERT_SIGN,

	CRL_SIGN,

	ENCIPHER_ONLY,

	DECIPHER_ONLY
}
