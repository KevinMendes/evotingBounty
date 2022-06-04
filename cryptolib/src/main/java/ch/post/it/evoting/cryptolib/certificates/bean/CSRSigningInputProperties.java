/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.time.ZonedDateTime;

public final class CSRSigningInputProperties {

	private final ZonedDateTime notBefore;

	private final ZonedDateTime notAfter;

	private final CertificateParameters.Type type;

	public CSRSigningInputProperties(final ZonedDateTime notBefore, final ZonedDateTime notAfter, final CertificateParameters.Type type) {
		this.notBefore = notBefore;
		this.notAfter = notAfter;
		this.type = type;
	}

	public ZonedDateTime getNotBefore() {
		return notBefore;
	}

	public ZonedDateTime getNotAfter() {
		return notAfter;
	}

	public CertificateParameters.Type getType() {
		return type;
	}
}
