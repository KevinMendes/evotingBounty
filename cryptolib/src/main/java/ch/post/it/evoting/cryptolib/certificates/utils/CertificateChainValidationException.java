/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

/**
 * A certificate chain could not be validated.
 */
public class CertificateChainValidationException extends Exception {

	private static final long serialVersionUID = -3382992499835661968L;

	public CertificateChainValidationException(Throwable cause) {
		super(cause);
	}

	public CertificateChainValidationException(String msg) {
		super(msg);
	}
}
